package com.example.firebase

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.model.ExpenseEntity
import com.example.data.model.ExpenseItemEntity
import com.example.data.model.ExpenseWithItems
import com.example.data.model.ShoppingListItemEntity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG = "FirebaseSync"

class FirebaseSyncManager(private val context: Context) {

    private val auth: FirebaseAuth = Firebase.auth

    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private val credentialManager = CredentialManager.create(context)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onError("Google Sign-In configuration missing: default_web_client_id")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(authCredential).await()
                    _currentUser.value = auth.currentUser
                    onSuccess()
                    // Trigger sync automatically upon successful sign in
                    syncWithCloud(scope)
                } else {
                    onError("Unexpected credential format")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In cancelled: ${e.message}", e)
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                onError(e.localizedMessage ?: "Sign in failed")
            }
        }
    }

    fun attemptSilentSignIn(scope: CoroutineScope) {
        if (auth.currentUser != null) return

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(authCredential).await()
                    _currentUser.value = auth.currentUser
                }
            } catch (e: Exception) {
                // Expected when user hasn't authorized account previously
                Log.d(TAG, "Silent sign in skipped: ${e.message}")
            }
        }
    }

    fun signOut(scope: CoroutineScope, onComplete: () -> Unit = {}) {
        scope.launch {
            withContext(NonCancellable) {
                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to clear credential state: ${e.message}")
                } finally {
                    auth.signOut()
                    _currentUser.value = null
                    onComplete()
                }
            }
        }
    }

    fun syncWithCloud(scope: CoroutineScope) {
        val user = auth.currentUser ?: return
        if (_isSyncing.value) return

        scope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            _syncMessage.value = "ক্লাউডে সিঙ্ক হচ্ছে... / Syncing..."
            try {
                val appDb = AppDatabase.getDatabase(context)
                val expenseDao = appDb.expenseDao()
                val shoppingDao = appDb.shoppingListDao()

                val localExpenses = expenseDao.getAllExpensesWithItems().first()
                val uid = user.uid

                // Backup local expenses to Firestore
                for (item in localExpenses) {
                    val expDoc = db.collection("users").document(uid)
                        .collection("expenses").document(item.expense.id.toString())

                    val expData = hashMapOf(
                        "userId" to uid,
                        "title" to item.expense.title,
                        "totalAmount" to item.expense.totalAmount,
                        "currency" to item.expense.currency,
                        "date" to item.expense.date,
                        "merchant" to item.expense.merchant,
                        "categoryId" to item.expense.categoryId,
                        "paymentMethod" to item.expense.paymentMethod,
                        "source" to item.expense.source,
                        "note" to item.expense.note,
                        "imageUri" to item.expense.imageUri,
                        "createdAt" to Timestamp(item.expense.date / 1000, 0),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    expDoc.set(expData, SetOptions.merge()).await()

                    for (subItem in item.items) {
                        val subDoc = expDoc.collection("items").document(subItem.id.toString())
                        val subData = hashMapOf(
                            "expenseId" to item.expense.id.toString(),
                            "userId" to uid,
                            "name" to subItem.name,
                            "quantity" to subItem.quantity,
                            "unit" to subItem.unit,
                            "unitPrice" to subItem.unitPrice,
                            "amount" to subItem.amount,
                            "categoryId" to subItem.categoryId,
                            "confidence" to subItem.confidence.toDouble(),
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                        subDoc.set(subData, SetOptions.merge()).await()
                    }
                }

                // Backup shopping list items
                val localShopping = shoppingDao.getAllItems().first()
                for (shopItem in localShopping) {
                    val shopDoc = db.collection("users").document(uid)
                        .collection("shopping_items").document(shopItem.id.toString())
                    val shopData = hashMapOf(
                        "userId" to uid,
                        "name" to shopItem.name,
                        "quantity" to shopItem.quantity,
                        "estimatedPrice" to shopItem.estimatedPrice,
                        "isPurchased" to shopItem.isPurchased,
                        "createdAt" to Timestamp(shopItem.createdAt / 1000, 0)
                    )
                    shopDoc.set(shopData, SetOptions.merge()).await()
                }

                _syncMessage.value = "ক্লাউড সিঙ্ক সফল হয়েছে! / Synced with cloud!"
                Log.d(TAG, "Successfully synced ${localExpenses.size} expenses to Firestore")
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing with cloud", e)
                _syncMessage.value = "সিঙ্ক ত্রুটি: ${e.localizedMessage}"
            } finally {
                _isSyncing.value = false
            }
        }
    }
}
