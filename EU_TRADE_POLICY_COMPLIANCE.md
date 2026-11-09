# European Union (EU) Digital Services Act (DSA) & Trader Policy Compliance

**Application:** Bazar Hisab  
**Package Name:** `com.vivescriptsolutions.bazarhisab`  
**Developer:** ViveScript Solutions LLC  
**Last Updated:** November 2026 (EU Digital Services Act & GDPR Ready)

This guide provides exact, copy-paste ready instructions for complying with the **European Union (EU) Digital Services Act (DSA)**, **EU Trader Status Declaration**, and **GDPR Data Protection Regulations** on Google Play Console.

---

## 1. EU Digital Services Act (DSA) Trader Status Declaration

Google Play requires all developers distributing apps in the European Economic Area (EEA) to declare their **Trader Status** under Article 30 of the EU Digital Services Act.

### Where to configure in Play Console:
1. Open **Google Play Console**.
2. In the left navigation menu, go to **Policy and programs** → **Trader status** (or under Developer Account Details).
3. Select your declaration based on your operational model (Option A or Option B below).

---

### Option A: Declaring as a "Trader" (Recommended for ViveScript Solutions LLC)

Under EU law (Directive 2005/29/EC), any entity acting for purposes relating to their commercial trade, business, craft, or profession is considered a **Trader**. The app carries advertising, so declare as a Trader:

| Google Play Console Field | Value to Enter |
| :--- | :--- |
| **Trader Status** | **Yes, I am a trader** |
| **Legal Entity / Trader Name** | `ViveScript Solutions LLC` |
| **Email Address** | `support@vivescriptsolutions.com` (verified by OTP) |
| **Phone Number** | Verified company phone number (with international dial code) |
| **Physical Address** | Registered business address of ViveScript Solutions LLC |
| **Commercial Register Number / DUNS** | Your state registration number or D-U-N-S number |
| **Website URL** | `https://www.vivescriptsolutions.com/` |

> **Note for EU Consumers:** When declared as a Trader, Google Play displays your verified business name, address, email, and phone number publicly on the Bazar Hisab store listing for users browsing from the European Economic Area.

---

### Option B: Declaring as a "Non-Trader"

Not recommended for this app: Bazar Hisab contains Google advertising, which is commercial activity. Option B applies only to strictly non-commercial distribution with zero monetization.

---

## 2. EU GDPR Compliance & User Data Rights

Bazar Hisab follows data-minimization and consent principles for its cloud and advertising features:

### 2.1 Lawful Basis & Consent (GDPR Articles 6–7)
- **Cloud backup/sync/AI:** processed on user consent — every flow is opt-in (sign-in, Sync tap, scan/voice submit) and the AI returns drafts the user reviews.
- **Advertising:** users in consent regions see a Google UMP consent form **before** the ad SDK initializes; declining means no ad requests at all. Consent can be revisited anytime via **Settings → Ad Privacy Options**.

### 2.2 Data Minimization (GDPR Article 5(1)(c))
- Local-first design: expense records, lists, and settings stay on-device by default.
- Only user-initiated data goes to Google Firebase (account profile, synced records, extraction inputs) and Google AdMob (ad ID, ad diagnostics).
- No location, contacts, SMS, health, or calendar access exists in the app.

### 2.3 Right to Erasure (GDPR Article 17)
- **Cloud + account:** **Settings → Delete Account & Cloud Data** permanently deletes the Firebase account and every cloud document (expenses including line items, shopping lists).
- **Local:** any expense can be deleted in History; uninstalling wipes the on-device database. OS cloud backup excludes the database and preferences.
- **Ads:** Google ad personalization is managed through Google's ad settings plus the in-app privacy options.

### 2.4 ePrivacy Directive & Consent Banners
- The UMP consent form is the app's consent banner for advertising storage/access where EU law requires it. Strictly-necessary local storage (settings, database) needs no banner.

---

## 3. EU Consumer Rights & Disclosures (Directive 2011/83/EU)

- **Price Disclosure:** free with ads; no digital goods, subscriptions, or paywalls.
- **AI Disclosure:** extraction produces drafts the user must review (also stated in-app and in Terms §3).
- **Financial Disclosure:** the app is a manual notebook — it moves no money and gives no financial advice (Terms §4).

---

## 4. Play Console Review Checklist for EU Distribution

- [ ] Trader status configured under **Policy and programs > Trader status**.
- [ ] Privacy Policy accessible via public HTTPS link (`https://www.vivescriptsolutions.com/en/apps/bazarhisab/privacy`).
- [ ] Terms of Service accessible via public HTTPS link (`https://www.vivescriptsolutions.com/en/apps/bazarhisab/terms`).
- [ ] Data Safety questionnaire answered per `PLAY_STORE_DATA_SAFETY.md`.
- [ ] Advertising ID declaration = Yes; UMP consent verified for EEA.
- [ ] Target audience declared as 18+ with no appeal to children.
- [ ] Camera/microphone justifications reference user-initiated scan/voice only.
