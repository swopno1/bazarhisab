package com.example.ai

import com.google.firebase.Firebase
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.ktx.appCheck

/**
 * Debug builds use the App Check debug provider. The debug token printed to
 * logcat on first run must be registered in the Firebase console
 * (App Check > Manage debug tokens) — see `app/FIREBASE_SETUP.md`.
 * This file lives in `src/debug` so the debug provider (a
 * `debugImplementation` dependency) is never compiled into release builds.
 */
object AppCheckInitializer {

    fun install() {
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance(),
        )
    }
}
