# Privacy Policy — Bazar Hisab

**Effective Date:** November 2026  
**Developer:** ViveScript Solutions LLC  
**Application:** Bazar Hisab (`com.vivescriptsolutions.bazarhisab`)  
**Website:** https://www.vivescriptsolutions.com/  
**Live version of this policy:** https://www.vivescriptsolutions.com/en/apps/bazarhisab/privacy *(TODO: publish this page before store submission)*  
**Contact Email:** support@vivescriptsolutions.com

---

## 1. Overview

Bazar Hisab is a household grocery-expense tracker for Bangladeshi families, available in Bangla and English. The app is **local-first**: your expense records are stored in an on-device database and the app works fully offline. Optional cloud features (Google sign-in, backup & sync, AI extraction) and Google ads involve the data uses described below — nothing else leaves your phone.

---

## 2. Data Stored Only on Your Device

The following never leaves your device unless you turn on the optional features in Section 3:

- Expense records you enter (titles, merchants, amounts, quantities, categories, payment methods, notes, dates).
- Shopping lists and app settings (household name, monthly budget, language).
- Receipt photos you capture, which are read on-device first by ML Kit text recognition.

Local records can be deleted one by one in History, and uninstalling the app deletes the entire on-device database. Android cloud backup is configured to **exclude** the expense database and app preferences, so your history is not uploaded through OS backup either.

---

## 3. Optional Cloud Features (Firebase, Google)

These features activate only when you use them:

### 3.1 Google Sign-In & Cloud Backup (Firebase Auth + Firestore)
- When you sign in with Google, Firebase receives your Google account identifier, name, email address, and profile photo to create your private cloud space in Firebase project `workroster-498716`.
- Tapping Sync uploads your expense records and shopping lists to a per-user Firestore area that only your account can read or write (enforced by server-side security rules).
- You can use the whole app without ever signing in.

### 3.2 AI Extraction (Firebase AI Logic, Gemini)
- Receipt photos your phone cannot read on-device, voice transcripts, and shopping-list text you submit may be sent to Google's Gemini model for extraction into a draft expense.
- The AI returns **a draft only** — nothing is saved until you review and confirm it on the reconciliation screen.
- Voice input uses the Android speech recognizer, which may transcribe through Google's speech service.

### 3.3 App Integrity (App Check)
- Firebase App Check with reCAPTCHA verifies that traffic comes from the genuine app to protect the cloud backend from abuse.

---

## 4. Advertising (Google AdMob)

- The free app shows Google banner and interstitial ads, served by the Google Mobile Ads SDK.
- Google may collect the Android advertising ID, coarse location, and app-interaction data to serve and measure ads, per [Google's Partner Sites policy](https://business.safety.google/partners/).
- Users in regions with consent requirements (EEA/UK) see a Google consent form on first launch; your choice is honored before any ad loads, and you can change it anytime via **Settings → Ad Privacy Options**.
- Debug/test builds of the app only ever request Google's demo ads.

---

## 5. Device Permissions

| Permission | Purpose |
| :--- | :--- |
| `INTERNET` | Cloud backup & sync, AI extraction, and ad loading. Offline features never need it. |
| `CAMERA` | Photographing paper receipts for on-device text extraction. No other use. |
| `RECORD_AUDIO` | Voice expense entry through the system speech recognizer. No other use. |

The app requests **no** location, contacts, SMS, call, file-browsing, or health permissions. Gallery images are picked through the system photo picker and CSV files are shared through a private file provider.

---

## 6. Data Sharing

We do not sell your data. Data is transmitted only to:

- **Google (Firebase)**: account, backup, AI extraction, and integrity services described in Section 3.
- **Google (AdMob)**: advertising delivery described in Section 4.

No other third party receives your data.

---

## 7. Data Retention & Deletion

- **Cloud data + account:** **Settings → Firebase card → Delete Account & Cloud Data** permanently deletes your Firebase account and every cloud document (expenses including line items, shopping items). Records on your phone are kept.
- **Local data:** delete any expense in History, or uninstall the app to wipe the on-device database completely.
- **Ads:** manage personalization through your Google ad settings and the in-app Ad Privacy Options.
- Backup copies retained by Google's infrastructure expire per Google's own retention schedules.

To request deletion assistance, email support@vivescriptsolutions.com.

---

## 8. Security

- All network traffic uses encrypted HTTPS connections.
- Firestore access is restricted by owner-scoped server rules; App Check blocks traffic from unofficial clients.
- The local database and preferences are excluded from Android cloud backup.
- No method of transmission or storage is 100% secure, and you should never enter passwords or card PINs into expense notes.

---

## 9. Children's Privacy

Bazar Hisab is a general household utility and is not directed at children under 13. We do not knowingly collect data from children; signed-in cloud features require a Google account, which Google restricts by age.

---

## 10. Changes to This Policy

Material changes will be reflected in an updated effective date in the app listing. Continued use after changes constitutes acceptance.

---

## 11. Contact Us

**ViveScript Solutions LLC**  
Website: https://www.vivescriptsolutions.com/  
Email: support@vivescriptsolutions.com
