package com.example.ai

import com.google.firebase.Firebase
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/**
 * Release builds use the Play Integrity provider. The app must be registered
 * for App Check with Play Integrity in the Firebase console (with the
 * upload key's SHA-256 fingerprint) and the Play Integrity API linked to the
 * Firebase project in the Play Console — see `app/FIREBASE_SETUP.md`.
 * Do NOT enable App Check enforcement for AI Logic until the request metrics
 * in the console show legitimate traffic passing.
 */
object AppCheckInitializer {

    fun install() {
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )
    }
}
