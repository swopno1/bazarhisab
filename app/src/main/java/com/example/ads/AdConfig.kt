package com.example.ads

object AdConfig {
    /**
     * The manifest carries the production AdMob App ID (required: the SDK
     * refuses to init without the ID the store listing is linked to).
     * Test safety comes from the ad units instead: debug builds request
     * Google's demo units, release builds the production units below.
     */

    /**
     * Official Google Test Banner Ad Unit ID:
     * https://developers.google.com/admob/android/test-ads#sample_ad_units
     */
    const val BANNER_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    /**
     * Official Google Test Interstitial Ad Unit ID:
     */
    const val INTERSTITIAL_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    /**
     * Production Ad Unit IDs (release builds only; debug builds always use
     * the Google demo units below so testing can never taint ad traffic).
     */
    private const val BANNER_PROD_AD_UNIT_ID = "ca-app-pub-5222053984568989/8503913140"
    private const val INTERSTITIAL_PROD_AD_UNIT_ID = "ca-app-pub-5222053984568989/8064929448"

    var bannerAdUnitId: String = bannerIdForBuild()
        private set
    var interstitialAdUnitId: String = interstitialIdForBuild()
        private set

    private fun bannerIdForBuild(): String =
        if (com.example.BuildConfig.DEBUG) BANNER_TEST_AD_UNIT_ID else BANNER_PROD_AD_UNIT_ID

    private fun interstitialIdForBuild(): String =
        if (com.example.BuildConfig.DEBUG) INTERSTITIAL_TEST_AD_UNIT_ID else INTERSTITIAL_PROD_AD_UNIT_ID

    // Enable/disable ads globally (useful if user purchases ad-free or in testing)
    var adsEnabled: Boolean = true
}
