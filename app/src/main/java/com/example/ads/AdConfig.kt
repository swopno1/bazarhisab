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
     * When you are ready for production release, replace these with your actual IDs
     * from your Google AdMob dashboard:
     * 1. Go to apps.admob.com -> Apps -> Add App / App Settings
     * 2. Replace APPLICATION_ID in AndroidManifest.xml with your App ID (ca-app-pub-XXXXX~XXXXX)
     * 3. Set your production Banner & Interstitial IDs here.
     */
    var bannerAdUnitId: String = BANNER_TEST_AD_UNIT_ID
    var interstitialAdUnitId: String = INTERSTITIAL_TEST_AD_UNIT_ID

    // Enable/disable ads globally (useful if user purchases ad-free or in testing)
    var adsEnabled: Boolean = true
}
