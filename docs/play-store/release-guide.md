# From this repository to Google Play

Everything the code and the paperwork can do is already done (see `README.md` in this folder). What is
left needs **your identity, your key and your Play Console account**, which cannot be created for you.

## 1. Make the upload key (once, on your own computer)

The upload key proves a build came from you. Google keeps the real app-signing key (Play App Signing), so a
lost upload key can be reset; keep it safe anyway and **never commit it**.

```bash
keytool -genkeypair -v -keystore alarmboss-upload.jks -alias alarmboss -keyalg RSA -keysize 4096 -validity 10000
```

It asks for two passwords (store and key) and your details. Back the `.jks` file up somewhere private
(password manager or encrypted drive).

## 2. Give the key to the release build

In GitHub: *Settings → Secrets and variables → Actions → New repository secret*, add four secrets.

| Secret | Value |
| --- | --- |
| `KEYSTORE_BASE64` | the keystore, base64-encoded: `certutil -encode alarmboss-upload.jks out.txt` (Windows) then paste the lines between the BEGIN/END markers, or `base64 -w0 alarmboss-upload.jks` (Linux/macOS) |
| `KEYSTORE_PASSWORD` | the store password |
| `KEY_ALIAS` | `alarmboss` |
| `KEY_PASSWORD` | the key password |

Tag a release (`git tag -a v1.8.0 -m "Version 1.8.0"` then `git push origin v1.8.0`). The workflow runs the
tests and lint, builds the APK and the bundle, and keeps the **signed bundle** as the workflow artifact
`alarmboss-play-bundle` (Actions → the run → Artifacts). Without the secrets it warns and keeps no bundle.

Before every upload, raise `versionCode` in `app/build.gradle.kts` (Play refuses a code it has seen).

## 3. Play Console (one-time setup)

1. Create a developer account at <https://play.google.com/console> (one-off fee) and verify your identity.
2. *Create app* → name `AlarmBoss: Wake-Up Challenges`, default language English, **App**, **Free**.
3. Application ID is fixed by the first upload: `io.github.arieahub.alarmboss`. **Change it in
   `app/build.gradle.kts` before the first upload if you want a different one**; it cannot be changed later.
4. Fill **Store listing** from `listing.md` and upload `icon-512.png`, `feature-graphic.png` and the
   screenshots.
5. Fill **App content** from `data-safety.md`, `content-rating-and-audience.md` and
   `permissions-and-declarations.md`, and enter the privacy policy URL.
6. Under *Setup → App signing*, accept **Play App Signing** when prompted at the first upload.

## 4. Testing before production

Personal developer accounts created after November 2023 **must run a closed test with at least 12 testers
opted in for 14 continuous days** before production access is granted. Plan for it:

1. *Testing → Closed testing → Create track* → upload the signed `.aab` → add testers by email (or a Google
   Group) → send them the opt-in link.
2. Keep the test running for 14 days with 12+ testers, fix anything they report, then *Apply for production*.
3. Check the **Pre-launch report** Google generates for each upload (crashes on a range of real devices).

## 5. After release

- Watch **Android vitals** (crashes and ANRs) and reply to reviews.
- Release updates through the same flow with a higher `versionCode`.
- The release notes go in the "What's new" field (see `listing.md`).

## Known limits to be aware of

- **Android 16 only.** `minSdk` is 36, so the app installs only on phones running Android 16 or newer. This
  keeps the code simple but limits the audience. Supporting older versions needs a compatibility pass and
  device testing; see the project notes before changing it.
- **Signing.** A release built without the secrets is signed with a debug key and **cannot** be uploaded to
  Google Play.
- **Support email.** `amsampat1@gmail.com` is the contact on the listing and in `PRIVACY.md`. It is shown
  publicly on the Play page; use a dedicated address instead if you prefer.
