package com.example.ai

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase

/**
 * Initialises Firebase and App Check before any Firebase SDK is used.
 * Call once from `MainActivity.onCreate`, before the ViewModel runs.
 *
 * Never crashes the app: when `google-services.json` is missing (the
 * expected state until the founder completes `app/FIREBASE_SETUP.md`),
 * Firebase simply stays uninitialised and AI calls fail later with a clear
 * "not configured" error while the rest of the app keeps working offline.
 * The App Check provider itself is per build type — see
 * `src/debug/.../AppCheckInitializer.kt` (debug provider) and
 * `src/release/.../AppCheckInitializer.kt` (Play Integrity) — because the
 * debug provider artifact is `debugImplementation`-only and must not be
 * referenced from main source code.
 */
object FirebaseSetup {

    private const val TAG = "FirebaseSetup"

    fun initialize(context: Context) {
        try {
            Firebase.initialize(context)
            AppCheckInitializer.install()
            Log.d(TAG, "Firebase initialised; App Check provider installed")
        } catch (e: Exception) {
            Log.w(
                TAG,
                "Firebase not initialised (AI unavailable until app/FIREBASE_SETUP.md " +
                    "is done): ${e.message}",
            )
        }
    }
}
