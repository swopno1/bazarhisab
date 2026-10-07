# Bazar Hisab (বাজার হিসাব)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue.svg)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android_15_(API_36)-green.svg)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose_M3-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A simple, offline-first Android expense tracker built with modern Jetpack Compose, designed specifically for Bangladeshi families and homemakers to record daily grocery and household expenditures effortlessly.

---

## 🌟 Key Features

* **3 Flexible Entry Modes:**
  * 📸 **Receipt Scan:** Capture or pick grocery receipts to automatically extract items and amounts.
  * 🎙️ **Voice Entry:** Speak naturally in **Bengali** (*"আজ আলু ৮০ টাকা, পেঁয়াজ ১০০ টাকা, ডিম ১৪০ টাকা"*) or English to log expenses.
  * 📝 **Shopping List to Expense:** Create a bazaar checklist and convert purchased items into a finalized expense with a single tap.
* **14 Tailored Household Categories:** Pre-configured for Bangladeshi households (Rice & Grains, Fish, Meat, Vegetables, Oil & Spices, Groceries, Utilities, etc.).
* **Bilingual Support:** 1-tap dynamic toggle between **বাংলা** and **English** across all screens.
* **Monthly Budget Tracking:** Set household budget limits and view real-time remaining allowances with visual progress indicators.
* **100% Offline-First:** Powered by a local SQLite Room database. All financial records stay securely on your device.
* **Expense History & CSV Export:** Search, filter by time (today, this week, this month), and export data to CSV.
* **Integrated AdMob Monetization:** Configured with Google Mobile Ads SDK for banners and throttled interstitial ads.

---

## 🏗️ Architecture & Tech Stack

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose with Material Design 3 (M3)
* **Architecture Pattern:** Model-View-ViewModel (MVVM) + Unidirectional Data Flow (UDF)
* **Local Persistence:** Room Database (SQLite) + Coroutine Flows
* **Natural Language Processing:** Hybrid On-Device NLP (`LocalRuleBasedExtractor`) + Multimodal Gemini REST Service
* **Networking & Image Loading:** Retrofit 2, OkHttp 3, Coil
* **Monetization:** Google Mobile Ads SDK (`play-services-ads:23.6.0`)
* **Testing:** JUnit 4 + Robolectric local JVM testing

---

## 📁 Repository Structure

```text
bazarhisab/
├── LICENSE                     # MIT License for source code
├── README.md                   # Project documentation
├── NOTICE.md                   # Legal and trademark notices
├── THIRD-PARTY-NOTICES.md      # Open source dependencies attributions
├── play_store_assets/          # Google Play Console publishing graphics
│   ├── app_icon_512x512.png
│   ├── feature_graphic_1024x500.png
│   ├── screenshot_1_dashboard.png
│   ├── screenshot_2_voice_scan.png
│   └── screenshot_3_shopping_list.png
└── app/
    ├── build.gradle.kts        # App build configuration (package: com.vivescriptsolutions.bazarhisab)
    └── src/main/
        ├── AndroidManifest.xml # Permissions, activities & AdMob App ID
        ├── java/com/example/
        │   ├── MainActivity.kt
        │   ├── ads/            # AdMob AdManager, AdConfig, and AdmobBanner
        │   ├── ai/             # On-device Bengali parser & AI services
        │   ├── data/           # Room DB, DAOs, entities & repositories
        │   └── ui/             # Jetpack Compose screens, viewmodels & themes
        └── res/
            ├── values/strings.xml    # English strings
            └── values-bn/strings.xml # Bengali strings (বাংলা)
```

---

## 🚀 Building & Running

### Prerequisites
* Android Studio Meerkat or later
* JDK 11 or 17
* Android SDK Platform 36 (Min SDK 24)

### Build Commands
To assemble a debug build:
```bash
gradle :app:assembleDebug
```

To run the unit and Robolectric test suite:
```bash
gradle :app:testDebugUnitTest
```

To build and sign the production release Android App Bundle (AAB):
```bash
gradle :app:bundleRelease
```

---

## 🔒 Configuration & Privacy

* **AdMob Configuration:** Configured in `app/src/main/java/com/example/ads/AdConfig.kt` and `app/src/main/AndroidManifest.xml`.
* **API Keys & Secrets:** Sensitive credentials, API keys, and keystores are never committed to this repository. Optional Gemini API keys are injected securely at build time via `.env` / `BuildConfig`.

---

## 📄 License & Legal Notices

© 2026 ViveScript Solutions LLC.  
The source code is licensed under the [MIT License](LICENSE).

### Brand & Assets Exception
The app name (**Bazar Hisab**, **বাজার হিসাব**), logos, icons, marketing screenshots, feature graphics, trademarks, and associated brand presentation are the proprietary property of **ViveScript Solutions LLC** and are **not** licensed under the MIT License.

### Commercial Distribution
Google Play Store publication and commercial distribution rights are retained exclusively by ViveScript Solutions LLC.
