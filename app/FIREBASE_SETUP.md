# Firebase setup for BazarHisab (founder steps)

AI features (receipt scan in Bengali, voice parsing, shopping-list parsing)
run through **Firebase AI Logic (Gemini 2.5 Flash)** with **App Check**.
No Gemini API key is stored in the app, in `BuildConfig`, or in any request
— the Firebase SDK attests the app itself. Until the steps below are done,
the app still works: manual entry and on-device parsing keep working, and
AI calls fail with a clear "AI isn't set up yet" message instead of
inventing data.

> `google-services.json` **is committed in this branch** (`app/google-services.json`,
> project `bazarhisab-9bdaf`, package `com.vivescriptsolutions.bazarhisab`) —
> the founder supplied it 2026-11-08 and asked for it to be used for Firebase
> configuration, so clean checkouts and release builds work without a manual
> file drop. This reverses the earlier "do not commit" rule in this doc.
> It is not a Gemini API key and no secret is in it, but do restrict the
> Android API key in Google Cloud Console (Android-app restriction + API
> restrictions) and keep App Check enforcement as the gate. (`.env` is still
> ignored — the old `.env` Gemini key path is deprecated.)

## 1. Create / pick the Firebase project

1. Open the [Firebase console](https://console.firebase.google.com) and
   create a project (or reuse an existing ViveScript project). Google
   Analytics is **not** required for AI Logic.
2. In the console go to **AI Services > AI Logic** and click **Get started**.
   Follow the guided workflow; when asked for a Gemini API provider, choose
   **Gemini Developer API** (no billing needed to start). This enables the
   required APIs and automatically enforces App Check for AI Logic.

## 2. Register the Android app and download the config

1. In **Project settings > Your apps**, click **Add app > Android**.
2. Android package name: `com.vivescriptsolutions.bazarhisab`
   (this must match `applicationId` in `app/build.gradle.kts` exactly).
3. App nickname: `BazarHisab` (optional).
4. Add the **SHA-1 and SHA-256** fingerprints of the signing keys:
   - the Play upload key (release), and
   - the debug key (for local builds).
   Get them with `./gradlew signingReport` in Android Studio, or
   `keytool -list -v -keystore <keystore> -alias <alias>`.
5. Download **`google-services.json`** and place it at
   `app/google-services.json` (next to `app/build.gradle.kts`).
6. Verify: a release build **without** this file now fails fast with a
   clear message (`preReleaseBuild` check in `app/build.gradle.kts`).
   Debug builds are exempt so the app can still run offline without it.

## 3. Link Play Integrity (App Check, release)

1. In the **Play Console**, open the app, go to **Release > App integrity
   > Play Integrity API**, click **Link Cloud project** and select the
   same Firebase/Google Cloud project as step 1.
2. In the **Firebase console**, go to **Security > App Check > Apps**,
   register the Android app with the **Play Integrity** provider using the
   SHA-256 fingerprint from step 2. Leave the token TTL at the default
   (1 hour) unless you have a reason to change it.
3. **Do not enable enforcement yet.** Ship first, watch the App Check
   request metrics for AI Logic in the console, and enable enforcement only
   once legitimate traffic is passing. Enforcement blocks unregistered
   apps — including your own test builds if their fingerprints are missing.

## 4. Debug builds (local development)

1. Run a debug build on an emulator or device.
2. Find the App Check debug token in logcat, e.g.
   `D DebugAppCheckProvider: Enter this debug secret into the allow list
   in the Firebase Console for your project: 123a4567-...`.
3. In the Firebase console go to **Security > App Check > Apps**, open the
   app's overflow menu, **Manage debug tokens**, and add the token.
4. Debug builds use `firebase-appcheck-debug` (`debugImplementation`
   only — it is never compiled into release builds).

## 5. Quotas and cost

- Firebase AI Logic with the Gemini Developer API has a free tier; check
  current quotas in the console (**AI Services > AI Logic**) and set a
  billing budget alert in Google Cloud so a spike can't surprise you.
- The app sends one Gemini request per receipt scan / voice parse /
  shopping-list parse, with the image resized to max 1024 px.

## 6. Verify on a real device (after steps 1–3)

1. `./gradlew :app:assembleRelease` (or `bundleRelease`) succeeds with
   `app/google-services.json` in place.
2. Scan a **Bengali** receipt — items and amounts should come back from
   Gemini Vision (ML Kit alone cannot read Bengali).
3. Voice entry in Bengali, and paste a shopping list — both should parse.
4. Turn on airplane mode and try voice entry: it should fall back to
   on-device parsing and say so, not fail silently.
5. Confirm no Gemini key is in the app: search the built APK/AAB for
   `generativelanguage.googleapis.com` with a `?key=` parameter — there
   must be none (the old REST path in `GeminiExpenseAiService` is no
   longer wired in; delete that file once the P0 branch lands).

## Reference

- Firebase AI Logic get started:
  <https://firebase.google.com/docs/ai-logic/get-started>
- App Check with Play Integrity on Android:
  <https://firebase.google.com/docs/app-check/android/play-integrity-provider>
- App Check with the debug provider on Android:
  <https://firebase.google.com/docs/app-check/android/debug-provider>
