# Google Play Store Metadata — Bazar Hisab

**Application:** Bazar Hisab  
**Package Name:** `com.vivescriptsolutions.bazarhisab`  
**Version Code:** `9`  
**Version Name:** `9.0`  
**Target SDK:** `36`  
**Min SDK:** `24` (Android 7.0 Nougat)  
**Category:** Finance  
**Developer:** ViveScript Solutions LLC  
**Support Email:** support@vivescriptsolutions.com  
**Website:** https://www.vivescriptsolutions.com/  
**App Page:** https://www.vivescriptsolutions.com/en/apps/bazarhisab

---

## 1. Store Listing Information

### App Title (29 chars / max 30)
```text
Bazar Hisab - Expense Tracker
```

### Short Description (77 chars / max 80)
```text
Grocery expense tracker in Bangla & English. Voice, receipt scan and budgets.
```

### Full Description (max 4000 chars)
```text
Bazar Hisab — বাজার হিসাব
Your household grocery expense tracker in Bangla and English.

Record daily bazaar costs in seconds: type them, speak them, or photograph the paper receipt. Bazar Hisab keeps every taka accounted for, even with no internet connection.

★ WORKS FULLY OFFLINE
Your records live in a private database on your phone. Manual entry, lists, budgets, and history never need the internet.

★ VOICE ENTRY (BANGLA + ENGLISH)
Say "chal 5 kg 450 taka" and confirm the parsed draft. Bengali digits, grouped amounts (১,২৫০), and number words are all understood.

★ RECEIPT SCANNER
Photograph a paper receipt or pick a gallery image. Text is read on your phone first; hard-to-read images get AI help. You always review the draft before saving.

★ SMART RECONCILIATION
Line items, receipt totals, and mismatch warnings side by side. Exact Bengali-taka math with paisa precision and lakh/crore formatting (৳ 1,00,000.50).

★ BUDGETS & HISTORY
Monthly budgets with progress tracking, day/week/month filters, category breakdowns, and one-tap CSV export for your own records.

★ OPTIONAL CLOUD BACKUP
Sign in with Google to back up and sync across devices. Your cloud space is private to your account, and you can delete it anytime in Settings.

★ BANGLA FIRST
Full Bangla interface with Bengali digits, plus English. Switch anytime in Settings.

Free with ads. No account needed unless you want cloud backup.

Developer: ViveScript Solutions LLC
Website: https://www.vivescriptsolutions.com/
Support: support@vivescriptsolutions.com

Note: Bazar Hisab is a personal record-keeping notebook. It moves no money, connects to no banks, and AI drafts should always be reviewed before saving.
```

---

## 2. Store Graphic Assets

| Asset Type | Specs | Path |
| :--- | :--- | :--- |
| **App Icon** | 512 x 512 px PNG | `play_store_assets/app_icon_512x512.png` |
| **Feature Graphic** | 1024 x 500 px PNG | `play_store_assets/feature_graphic_1024x500.png` |
| **Screenshot 1** | Dashboard | `play_store_assets/screenshot_1_dashboard.png` |
| **Screenshot 2** | Voice + scan | `play_store_assets/screenshot_2_voice_scan.png` |
| **Screenshot 3** | Shopping list | `play_store_assets/screenshot_3_shopping_list.png` |

TODO: re-capture screenshots from the v9.0 release build if the UI has changed since these were taken.

---

## 3. App Content & Policy Declarations (Copy-Paste)

### 3.1 App Access / Login Credentials
- **Question:** Is any part of your app restricted?
- **Answer:** **All functionality is available without special access**
- **Instructions for Reviewers:** *"Bazar Hisab needs no login: manual entry, voice, receipt scan, lists, budgets, history, and CSV export all work offline on first launch. Google sign-in is optional (cloud backup only); any personal Google account can test it."*

### 3.2 Ads Declaration
- **Question:** Does your app contain ads?
- **Answer:** **Yes**
- **Explanation:** *"Google Mobile Ads SDK (banner + interstitial), gated by a Google UMP consent form where required."*

### 3.3 Target Audience & Content
- **Target Age Groups:** 18 and over
- **Could your store listing unintentionally appeal to children?** **No**

### 3.4 Government Apps / Financial Features / Health
- **Is this a government app?** **No**
- **Does this app provide financial features?** **No** (manual notebook; moves no money, connects to no banks)
- **Is this app a health or medical app?** **No**
- **Is this a news app?** **No**

---

## 4. Permission Declarations & Justifications

### 4.1 Camera (`CAMERA`)
```text
Used only when the user taps receipt scan, to photograph a paper receipt for on-device text extraction (ML Kit). Unreadable images may be sent to Firebase AI Logic for extraction after the user initiates a scan.
```

### 4.2 Microphone (`RECORD_AUDIO`)
```text
Used only when the user taps the microphone for voice expense entry, transcribed by the Android speech recognizer. No background recording.
```

### 4.3 No Foreground Service / No Location
The app declares no foreground service and no location permission; no FGS declaration or demo video is required.

---

## 5. Reviewer Testing Instructions

```text
TESTING INSTRUCTIONS FOR GOOGLE PLAY REVIEW TEAM:

Bazar Hisab is a local-first expense tracker (Bangla/English). No login needed.

1. Launch the app: Home shows today's/month's totals and quick-entry buttons.
2. Tap manual entry (pencil): title "Rice", amount 450, Save. It appears in History.
3. Tap the microphone: speak or type "chal 5 kg 450 taka", confirm the draft on the reconciliation screen, Save.
4. Tap receipt scan: photograph any receipt or pick a gallery image, confirm the draft, Save.
5. Open History: filter by day/week/month, open an expense, export CSV.
6. Open Settings: switch language EN/BN, set a budget, read the Privacy & Local First card.
7. Optional: sign in with any Google account to test backup; delete it again in Settings.
8. Ads: bottom banner + interstitial after saving; consent regions see a Google consent form first.
```

---

## 6. What's New in Version 9.0 (Release Notes)

```text
- Paisa-accurate Bengali-taka math with lakh/crore formatting
- Smarter Bengali voice parsing: grouped amounts, number words, lakh
- Receipt scanner with on-device reading + AI fallback for hard images
- Google sign-in with private cloud backup & sync, deletable anytime
- Consent-respecting ads with in-app privacy options
- Full Bangla + English interface, budgets, CSV export
```
