package com.example.ads

import android.app.Activity
import android.util.Log
import com.example.BuildConfig
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * Google User Messaging Platform (UMP) consent flow, required before
 * initializing Mobile Ads for users in regions with consent requirements
 * (EEA/UK). Outside those regions the form never appears and [onComplete]
 * fires immediately with the current [canRequestAds] value.
 *
 * Debug builds force EEA geography so the form can be verified on an
 * emulator; consent failures always fall through to [onComplete] so a
 * consent outage can never hang app startup.
 */
object ConsentManager {
    private const val TAG = "ConsentManager"

    fun gatherConsent(activity: Activity, onComplete: (canRequestAds: Boolean) -> Unit) {
        val debugSettings = if (BuildConfig.DEBUG) {
            ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .build()
        } else {
            null
        }
        val paramsBuilder = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
        if (debugSettings != null) {
            paramsBuilder.setConsentDebugSettings(debugSettings)
        }
        val consentInfo = UserMessagingPlatform.getConsentInformation(activity)
        consentInfo.requestConsentInfoUpdate(
            activity,
            paramsBuilder.build(),
            {
                if (consentInfo.isConsentFormAvailable) {
                    loadAndShowIfRequired(activity, onComplete)
                } else {
                    onComplete(consentInfo.canRequestAds())
                }
            },
            { error ->
                Log.w(TAG, "Consent info update failed: ${error.message}")
                // Debug builds use demo units only, so a consent outage must
                // not hide the ad UI under test; release stays gated.
                onComplete(BuildConfig.DEBUG || consentInfo.canRequestAds())
            }
        )
    }

    private fun loadAndShowIfRequired(activity: Activity, onComplete: (canRequestAds: Boolean) -> Unit) {
        val consentInfo = UserMessagingPlatform.getConsentInformation(activity)
        UserMessagingPlatform.loadConsentForm(
            activity,
            { form ->
                if (consentInfo.consentStatus == com.google.android.ump.ConsentInformation.ConsentStatus.REQUIRED) {
                    form.show(activity) {
                        // Dismissed: re-check, a second form may be required.
                        loadAndShowIfRequired(activity, onComplete)
                    }
                } else {
                    onComplete(consentInfo.canRequestAds())
                }
            },
            { error ->
                Log.w(TAG, "Consent form load failed: ${error.message}")
                onComplete(BuildConfig.DEBUG || consentInfo.canRequestAds())
            }
        )
    }

    /** Re-opens the privacy-options form from Settings. No-op when unavailable. */
    fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            if (error != null) {
                Log.w(TAG, "Privacy options form failed: ${error.message}")
            }
        }
    }
}
