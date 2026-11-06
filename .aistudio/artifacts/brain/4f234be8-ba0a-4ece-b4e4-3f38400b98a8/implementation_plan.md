# Google Play DEX Code Optimization, Obfuscation & Policy Compliance Plan

Deliver a production-ready release build (Version 7.0 / versionCode 7) that resolves the Google Play Console warning: **"DEX code optimization is below our threshold (Obfuscation 0%)"**, brings obfuscation and code shrinking well above the mandated 25% threshold, and ensures full compliance with recent Google Play Developer Program policies.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The user already initiated `versionCode = 7` and `versionName = "7.0"` in `app/build.gradle.kts`. This plan directly implements the user's explicit request to enable R8 minification (`isMinifyEnabled = true`, `isShrinkResources = true`) and provides comprehensive keep rules to prevent runtime crashes.

- **DEX Optimization & Obfuscation**: Enable R8 code shrinking and resource shrinking on the `release` build type.
- **ProGuard / R8 Rules**: Safeguard Room DAOs/entities, Moshi reflection & codegen, Retrofit HTTP interfaces, ML Kit text recognition, AdMob GMA SDK, and Jetpack Compose runtime artifacts from over-aggressive stripping.
- **Deobfuscation File**: Generate `mapping.txt` alongside the AAB so crashes and ANRs can be symbolicated in Google Play Console.
- **Policy Compliance**: Verify zero dynamic code loading, strict permission minimization (`CAMERA` and `RECORD_AUDIO` marked optional, zero broad storage access via PhotoPicker), and compliant targetSdk (36).

---

## 1. Overview & Core Concept

- **The Problem**: Google Play now mandates a minimum of 25% across code optimization, shrinking, and obfuscation metrics. Version 6.0 was built with `isMinifyEnabled = false`, triggering the warning: *"DEX code optimization is below our threshold - Obfuscation (0%)"*.
- **The Solution**: 
  1. Configure R8 optimizer in `app/build.gradle.kts` with `isMinifyEnabled = true` and `isShrinkResources = true`.
  2. Bolster `proguard-rules.pro` with robust keep rules for all data persistence (Room), JSON serialization (Moshi), network/HTTP models, ML Kit OCR, and AdMob ads.
  3. Verify release compilation and run unit / Robolectric test suites to ensure R8 doesn't break reflection, database queries, or UI state serialization.

---

## 2. Technical Architecture & Component Flow

```
┌────────────────────────────────────────────────────────┐
│                   Release Build Pipeline               │
└────────────────────────────────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│       Gradle :app:bundleRelease (R8 Engine)            │
│  - Shrinks unused bytecode (removes dead classes)      │
│  - Optimizes DEX instructions and inlines methods      │
│  - Obfuscates class/method names (threshold > 25%)     │
│  - Strips unused drawables & strings (shrinkResources) │
└────────────────────────────────────────────────────────┘
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
┌───────────────────────────┐ ┌───────────────────────────┐
│     proguard-rules.pro    │ │    mapping.txt Produced   │
│  - Keep Room Entities/DAOs│ │  - Play Console Symbol    │
│  - Keep Moshi JSON Models │ │    deobfuscation support  │
│  - Keep AdMob GMA SDK     │ │    for crash diagnostics  │
│  - Keep ML Kit Vision OCR │ └───────────────────────────┘
│  - Keep Compose Lambdas   │
└───────────────────────────┘
              │
              ▼
┌────────────────────────────────────────────────────────┐
│   Final Output: app-release.aab (Version 7.0 / vCode 7)│
│   Compliant with Google Play Vitals & Policies         │
└────────────────────────────────────────────────────────┘
```

---

## 3. Implementation Steps

### Step 1: Update `app/build.gradle.kts`
Configure release build type with minification, resource shrinking, and deobfuscation file mappings:
```kotlin
buildTypes {
    release {
        isCrunchPngs = false
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
        signingConfig = signingConfigs.getByName("release")
    }
    debug { signingConfig = signingConfigs.getByName("debugConfig") }
}
```

### Step 2: Comprehensive `proguard-rules.pro` Hardening
Ensure the following components are preserved:
1. **Source & Line Attributes**: Keep `SourceFile`, `LineNumberTable`, `Signature`, `*Annotation*` for accurate crash stack traces.
2. **Room Database**: Preserve entities (`com.example.data.model.**`), DAOs (`com.example.data.dao.**`), and database builders.
3. **Moshi & Serialization**: Preserve `@JsonClass(generateAdapter = true)` adapters, enum values, and model classes (`com.example.ai.AiModels**`).
4. **Retrofit**: Preserve interfaces and annotations (`retrofit2.http.*`).
5. **Google Mobile Ads (AdMob)**: Protect public API classes (`com.google.android.gms.ads.**`, `com.google.ads.**`).
6. **ML Kit Text Recognition**: Protect vision client and input image processors (`com.google.mlkit.vision.**`).
7. **Jetpack Compose**: Protect `@Composable` functions, animated content, and state holders from improper inlining.

### Step 3: Google Play Policy Compliance Verification
- **Permission Policy**: Confirm `CAMERA` and `RECORD_AUDIO` are runtime-only with `android:required="false"`. Media picking uses zero-permission `PickVisualMedia`.
- **Target SDK**: Meets latest Google Play target API level requirements (`targetSdk = 36`).
- **No Dynamic Code Loading (DCL)**: Confirmed 100% native Kotlin, no external dex loading.

### Step 4: Verification & Compilation
- Execute `compile_applet` to verify compilation.
- Run `gradle :app:testDebugUnitTest` to ensure business logic and Robolectric tests pass.
- Run `gradle :app:bundleRelease` to produce the optimized `app-release.aab` with `mapping.txt`.

---

## 4. Play Store Console Guidance for User
Provide clear instructions on where to upload the new AAB and associated mapping file:
1. Upload the newly generated `app-release.aab` under the production or testing track.
2. If prompted for deobfuscation files, locate `mapping.txt` generated under `app/build/outputs/mapping/release/mapping.txt` and upload it in the App Bundle Explorer under **Deobfuscation files**.
