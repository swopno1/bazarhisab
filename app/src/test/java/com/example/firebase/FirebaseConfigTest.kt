package com.example.firebase

import android.app.Activity
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FirebaseConfigTest {

    @Test
    fun `web client id resolves null without google-services json`() {
        // google-services.json is gitignored and must never be committed, so
        // the repo always builds without it. This test doubles as a tripwire:
        // if a real config file ever lands in the repo, it fails loudly.
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertNull(resolveWebClientId(context))
    }

    @Test
    fun `sync manager degrades without firebase instead of crashing`() = runTest {
        // Regression: Firebase.auth threw on uninitialized FirebaseApp,
        // crashing the app on launch without google-services.json.
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = FirebaseSyncManager(context)
        assertNull(manager.currentUser.value)

        val activity = Robolectric.buildActivity(Activity::class.java).get()
        var signInError: String? = null
        manager.signInWithGoogle(
            activity = activity,
            scope = this,
            onSuccess = {},
            onError = { signInError = it }
        )
        assertEquals("Google Sign-In unavailable: Firebase is not configured", signInError)

        var deleteError: String? = null
        manager.deleteAccount(
            scope = this,
            onSuccess = {},
            onError = { deleteError = it }
        )
        assertEquals("No signed-in account to delete", deleteError)

        // Sync and silent sign-in are silent no-ops without Firebase.
        manager.syncWithCloud(this)
        manager.attemptSilentSignIn(this)
        var signedOut = false
        manager.signOut(this) { signedOut = true }
        testScheduler.runCurrent()
        testScheduler.advanceTimeBy(11_000)
        testScheduler.runCurrent()
        assertTrue("signOut did not complete", signedOut)
    }
}
