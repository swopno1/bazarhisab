# Google Play Console Declarations & Approval Master Reference

**App Name:** Bazar Hisab  
**Package Name:** `com.vivescriptsolutions.bazarhisab`  
**Current Release:** Version Code `9`, Version Name `9.0`  
**Developer:** ViveScript Solutions LLC  
**Support Email:** support@vivescriptsolutions.com  
**Privacy Policy URL:** https://www.vivescriptsolutions.com/en/apps/bazarhisab/privacy *(TODO: publish)*  
**Terms of Service URL:** https://www.vivescriptsolutions.com/en/apps/bazarhisab/terms *(TODO: publish)*

---

## Quick Navigation for Play Console Forms
1. [App Access / Login Credentials](#1-app-access)
2. [Ads Declaration](#2-ads-declaration)
3. [Content Rating Questionnaire (IARC)](#3-content-rating-questionnaire)
4. [Target Audience & Content](#4-target-audience--content)
5. [News / Financial / Government Apps](#5-special-category-declarations)
6. [Data Safety Form Questionnaire](#6-data-safety-form)
7. [Foreground Service Declaration](#7-foreground-service-declaration)
8. [Sensitive Permissions Declarations](#8-sensitive-permissions-declarations)
9. [Reviewer Testing & Verification Guidance](#9-reviewer-testing-guidance)
10. [Copy-Paste Rejection Appeal Templates](#10-copy-paste-appeal-templates)

---

## 1. App Access
**Question:** Is access to any part of your app restricted?  
**Selection:**  
`All functionality is available without special access`  
**Reviewer Notes:**  
> *"Bazar Hisab works fully without any login: manual entry, voice, receipt scan, shopping lists, budgets, history, and CSV export are all available offline on first launch. Google sign-in is optional and only enables cloud backup/sync. If reviewers wish to test sync, any personal Google account works — no demo account is required."*

---

## 2. Ads Declaration
**Question:** Does your app contain advertising?  
**Selection:**  
`Yes`  
**Explanation for reviewers:** *"The app embeds the Google Mobile Ads SDK (banner + interstitial). Users in consent regions see a Google UMP consent form before any ad loads, changeable anytime in Settings → Ad Privacy Options. Debug builds request only Google demo ads."*

---

## 3. Content Rating Questionnaire (IARC)
- **Category:** Finance / Utility (personal expense tracker)
- **Does the app share user-provided personal info with third parties?** `Yes` → disclose Google Firebase/AdMob as service providers (see Data Safety)
- **Does the app share physical location with other users?** `No`
- **Does the app allow users to purchase digital goods?** `No`
- **Does the app contain user-to-user communication or UGC sharing?** `No` (records are private; CSV share exports the user's own file)
- **Resulting Rating:** `Everyone`

---

## 4. Target Audience & Content
- **Target Age Group:** `18 and over` (household budgeting; Google accounts required for sync have their own age gates)
- **Could the store listing unintentionally appeal to children?** `No`

---

## 5. Special Category Declarations
- **COVID-19 Contact Tracing or Status App:** `No`
- **Government App:** `No`
- **Financial Services (banking, loans, crypto, money movement):** `No` — the app is a manual record-keeping notebook; it moves no money and connects to no financial institutions
- **News App:** `No`
- **Health / Medical / Health Connect App:** `No`

---

## 6. Data Safety Form
See `PLAY_STORE_DATA_SAFETY.md` for copy-paste answers. Summary:
- **Data Collection:** `Yes` (account profile, expense records, receipt photos, voice, ad IDs — all user-initiated/optional)
- **Data Sharing:** `Yes` (Google Firebase + AdMob as service providers only)
- **Data Encryption in Transit:** `Yes` (HTTPS everywhere)
- **Data Deletion:** `Yes` (in-app account + cloud deletion; per-expense local delete)

---

## 7. Foreground Service Declaration
**Not applicable.** The app declares no foreground service and runs no background workers — all work happens in the foreground while the user interacts with it. No FGS declaration or demo video is required.

---

## 8. Sensitive Permissions Declarations

### 8.1 Camera (`CAMERA`)
- **Declaration Statement:**
```text
Used when the user taps receipt scan, to photograph a paper receipt for on-device text extraction (ML Kit). Photos the phone cannot read may be sent to Firebase AI Logic for extraction only after the user initiates a scan. No other camera use exists.
```

### 8.2 Microphone (`RECORD_AUDIO`)
- **Declaration Statement:**
```text
Used when the user taps the microphone for voice expense entry, transcribed by the Android speech recognizer. Recording starts only on user tap and stops when the entry completes. No background or unrelated recording.
```

### 8.3 Internet (`INTERNET`)
- Standard non-runtime permission for cloud backup/sync, AI extraction, and ads. Offline features never use it.

### 8.4 Advertising ID (`com.google.android.gms.permission.AD_ID`, merged from the Ads SDK)
- **Advertising ID declaration:** `Yes` — the Google Mobile Ads SDK uses the ad ID for ad serving/measurement, behind UMP consent where required.

---

## 9. Reviewer Testing Guidance

```text
STEP-BY-STEP REVIEWER TESTING INSTRUCTIONS:

Bazar Hisab is a local-first household expense tracker (Bangla/English). No login needed.

1. Launch the app: the Home screen shows today's/month's totals and quick-entry buttons.
2. Tap manual entry (pencil): enter title "Rice", amount 450, tap Save. The entry appears in History.
3. Tap the microphone: speak or type "chal 5 kg 450 taka" and confirm the parsed draft on the reconciliation screen, then Save.
4. Tap receipt scan: photograph any receipt or pick a gallery image; confirm the extracted draft, then Save.
5. Open History: filter by day/week/month, open an expense, export CSV to verify the file.
6. Open Settings: switch language (EN/BN), set a monthly budget, and read the Privacy & Local First card.
7. Sign-in/sync is optional: any Google account can test backup, and Settings → Delete Account & Cloud Data removes it again.
8. Ads: a banner shows at the bottom of screens and an interstitial after saving; consent regions see a Google consent form first.
```

---

## 10. Copy-Paste Appeal Templates

### Appeal for Data Safety / Privacy Inquiry:
> **Subject:** Clarification on Data Safety Declaration - `com.vivescriptsolutions.bazarhisab`  
> **Dear Google Play Review Team,**  
> Bazar Hisab is a local-first expense tracker: records stay in the on-device Room database (excluded from OS cloud backup) unless the user signs in and syncs to their private, owner-rule-protected Firestore space. Receipt photos are read on-device first; only unreadable images and voice/shopping text go to Firebase AI Logic for draft extraction the user reviews before saving. Ads run through the Google Mobile Ads SDK behind UMP consent where required. Users delete cloud data and the account in-app (Settings → Delete Account & Cloud Data). All transit is HTTPS.

### Appeal for Camera / Microphone Inquiry:
> **Subject:** Permission use clarification - `com.vivescriptsolutions.bazarhisab`  
> The CAMERA permission activates only when the user taps receipt scan, to photograph a paper receipt for text extraction. RECORD_AUDIO activates only when the user taps the microphone for voice expense entry. Both flows show an in-app explanation before the system prompt, handle denial gracefully with a Settings redirect, and never run in the background.

---

## 11. EU Digital Services Act (DSA) & Trader Status Declaration

Google Play requires all developers distributing in the European Economic Area (EEA) to declare their trader status under **Policy and programs > Trader status**:

- **Recommended Selection:** **"Yes, I am a trader"** (for ViveScript Solutions LLC)  
  - Legal Name: `ViveScript Solutions LLC`
  - Email: `support@vivescriptsolutions.com`
  - Website: `https://www.vivescriptsolutions.com/`
  - Address: Registered corporate address of ViveScript Solutions LLC.
- See full GDPR/deletion analysis in `EU_TRADE_POLICY_COMPLIANCE.md`.
