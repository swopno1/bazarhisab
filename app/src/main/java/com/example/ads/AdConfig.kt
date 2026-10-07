package com.example.ads

object AdConfig {
    /**
     * Official Google Test AdMob App ID:
     * ca-app-pub-3940256099942544~3347511713 (configured in AndroidManifest.xml)
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
     * Production Ad Unit IDs:
     */
    var bannerAdUnitId: String = "ca-app-pub-5222053984568989/8503913140"
    var interstitialAdUnitId: String = "ca-app-pub-5222053984568989/8064929448"

    // Enable/disable ads globally (useful if user purchases ad-free or in testing)
    var adsEnabled: Boolean = true
}
