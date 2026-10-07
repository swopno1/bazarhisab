package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"

    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false
    private var actionCounter = 0
    private const val ACTIONS_BETWEEN_INTERSTITIALS = 3 // Throttled to avoid overwhelming the user

    fun initialize(context: Context) {
        try {
            val configuration = com.google.android.gms.ads.RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
            MobileAds.setRequestConfiguration(configuration)

            MobileAds.initialize(context) { status ->
                Log.d(TAG, "AdMob MobileAds initialized successfully: $status")
            }
            loadInterstitial(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MobileAds: ${e.message}", e)
        }
    }

    fun loadInterstitial(context: Context) {
        if (!AdConfig.adsEnabled || interstitialAd != null || isAdLoading) return

        isAdLoading = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            AdConfig.interstitialAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoading = false
                    Log.d(TAG, "Interstitial Ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isAdLoading = false
                    Log.w(TAG, "Failed to load Interstitial Ad: ${error.message}")
                }
            }
        )
    }

    /**
     * Show interstitial ad when appropriate (e.g. after saving an expense), throttled.
     */
    fun showInterstitialIfReady(activity: Activity, onAdClosed: () -> Unit = {}) {
        if (!AdConfig.adsEnabled) {
            onAdClosed()
            return
        }

        actionCounter++
        if (actionCounter < ACTIONS_BETWEEN_INTERSTITIALS) {
            onAdClosed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            actionCounter = 0
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitial(activity)
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    loadInterstitial(activity)
                    onAdClosed()
                }
            }
            ad.show(activity)
        } else {
            loadInterstitial(activity)
            onAdClosed()
        }
    }
}
