# Bazar Hisab — Firebase / AI Studio Setup

The app code is fully wired for Firebase. AI extraction runs through
**Firebase AI Logic** (`firebase-ai`, model `gemini-3.8-flash`), with an
on-device ML Kit OCR + rule-based fallback that works with no Firebase
project at all. What remains is one-time **console provisioning** — no
code changes needed.

## 1. Canonical project (DECIDED)

Canonical Firebase project: **`workroster-498716`** (matches the
Settings screen label). Do NOT use the `google-services.json` on branch
`origin/feat/firebase-ai-logic` — it belongs to a different project
(`bazarhisab-9bdaf`).

| Item | Value |
|---|---|
| Firebase project | `workroster-498716` |
| Android package | `com.vivescriptsolutions.bazarhisab` |
| Named Firestore DB (`firebase_applet_config.xml`) | `ai-studio-android-bazarhis-4f234be8-ba0a-4ece-b4e4-3f38400b98a8` |

Confirm the named database exists in `workroster-498716` during the
console pass below (rename in `firebase_applet_config.xml` if AI Studio
provisions a different id).

## 2. Drop in the config file

Copy the chosen project's `google-services.json` into `bazarhisab/app/`.
It is gitignored on purpose — every checkout provisions its own copy.
Without it the app still builds and runs; only Google sign-in, cloud
sync, and Gemini fallback report "configuration missing".

## 3. Firebase console checklist

In [Firebase console](https://console.firebase.google.com/) for the
chosen project:

1. **AI Logic**: run the guided setup, provider **Gemini Developer API**,
   and confirm model `gemini-3.8-flash` exists in the model catalog
   (rename in `GeminiExpenseAiService.kt` if AI Studio gives a new name).
2. **App registration**: package `com.vivescriptsolutions.bazarhisab`
   with your release **SHA-1/SHA-256** fingerprints (Play App Signing
   + upload key).
3. **Authentication**: enable the Google provider; confirm the web
   client ID (it becomes `default_web_client_id` in the json).
4. **Firestore**: create/confirm the database id from
   `firebase_applet_config.xml`, then **deploy** `firestore.rules`
   (owner-scoped reads/writes/deletes) and verify the deployed rules
   match the repo file.
5. **App Check**: register the reCAPTCHA site key from
   `firebase_applet_config.xml`, add your debug SHA for development,
   and **enforce** App Check for Firestore, Auth, and AI Logic.

## 4. Verify end to end

1. `./gradlew assembleDebug`, install, sign in with Google.
2. Sync an expense, check it appears in the Firestore console.
3. Scan a Bengali receipt with no Latin text → Gemini fallback path.
4. Revoke consent / delete the account from Settings → confirm the
   `users/{uid}` tree disappears from Firestore.
