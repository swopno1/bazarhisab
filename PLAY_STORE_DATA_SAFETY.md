# Google Play Data Safety Declaration Guidance — Bazar Hisab

**Application:** Bazar Hisab  
**Package ID:** `com.vivescriptsolutions.bazarhisab`  
**Developer:** ViveScript Solutions LLC  
**Last Updated:** November 2026 (Version 9.0)

This document provides exact, copy-paste ready answers for the **Google Play Console Data Safety Form**, matching the verified app behavior.

---

## 1. High-Level Summary

| Google Play Console Question | Exact Answer | Justification |
| :--- | :--- | :--- |
| **Does your app collect or share any of the required user data types?** | **Yes** | Google sign-in profile, cloud backup records, AI extraction inputs, and AdMob ad data. |
| **Is all user data collected encrypted in transit?** | **Yes** | All traffic is HTTPS (Firebase, Gemini, AdMob). |
| **Do you provide a way for users to request that their data be deleted?** | **Yes** | In-app **Settings → Delete Account & Cloud Data**, plus per-expense local delete. |
| **Data deletion link:** | `https://www.vivescriptsolutions.com/en/apps/bazarhisab/privacy` *(TODO: publish; deletion itself runs in-app)* | Section 7 of the policy. |

---

## 2. Data Types Questionnaire

### 2.1 Personal Info → Name, Email address, User IDs
- **Collected:** Yes (Google sign-in profile + Firebase UID; only when the user signs in)
- **Shared:** Yes (with Google Firebase as the service provider)
- **Ephemeral:** No (stored in Firestore until the user deletes the account)
- **Required or optional:** Optional (the full app works without signing in)
- **Purposes:** App functionality, Account management

### 2.2 Financial Info → Other financial info (expense records)
- **Collected:** Yes (expense titles, merchants, amounts, notes — only when the user syncs or submits them for AI extraction)
- **Shared:** Yes (Google Firebase / Gemini as service providers)
- **Ephemeral:** No (Firestore backup persists; AI inputs per Google's data handling)
- **Required or optional:** Optional (local-only tracking collects nothing)
- **Purposes:** App functionality
- **Note:** The app moves no money and connects to no banks; these are user-typed household records.

### 2.3 Photos and Videos → Photos
- **Collected:** Yes (receipt photos submitted for extraction; on-device OCR first, Gemini cloud fallback only for unreadable images)
- **Shared:** Yes (Google Firebase AI Logic, when the fallback runs)
- **Ephemeral:** No
- **Required or optional:** Optional
- **Purposes:** App functionality

### 2.4 Audio → Voice or sound recordings
- **Collected:** Yes (voice expense entry via the Android speech recognizer, which may transcribe through Google's speech service)
- **Shared:** Yes (Google speech/AI services)
- **Ephemeral:** No
- **Required or optional:** Optional
- **Purposes:** App functionality

### 2.5 App Activity → App interactions
- **Collected:** Yes (by the Google Mobile Ads SDK for ad serving/measurement)
- **Shared:** Yes (Google AdMob)
- **Ephemeral:** No
- **Required or optional:** Optional (EEA/UK users choose via the UMP consent form before any ad loads)
- **Purposes:** Advertising or marketing

### 2.6 App Info and Performance → Device or other IDs; Diagnostics
- **Collected:** Yes (Android advertising ID and ad diagnostics via the Google Mobile Ads SDK; Firebase App Check integrity signals)
- **Shared:** Yes (Google)
- **Ephemeral:** No
- **Required or optional:** Optional (consent-gated where required)
- **Purposes:** Advertising or marketing, Fraud prevention/security

### 2.7 Not Collected
- **Location** (no location permission or API), **Contacts**, **Messages/SMS**, **Health**, **Calendar** — the app has no such permissions or code paths.

---

## 3. Copy-Paste Reviewer Defense

> **To Google Play Review Team:**  
> Bazar Hisab (`com.vivescriptsolutions.bazarhisab`) is a local-first expense tracker. All records stay in the on-device Room database (`bazar_hisab_database`, excluded from OS cloud backup) unless the user signs in with Google and taps Sync, in which case records upload to their private, owner-rule-protected Firestore space. Receipt photos are read on-device first (ML Kit); only unreadable images and voice/shopping text go to Firebase AI Logic (Gemini) as extraction drafts the user reviews before saving. Ads are served by the Google Mobile Ads SDK behind a UMP consent form where required. Users delete cloud data and the account in-app via Settings → Delete Account & Cloud Data, and local data per-expense or by uninstalling. All transit is HTTPS.
