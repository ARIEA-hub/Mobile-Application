# Supported phones

**Minimum: Android 8.0 (API 26). Target: Android 16 (API 36).** This build installs on roughly 97% of
active Android phones; the previous Android 16-only build reached about a quarter of them.

## How the minimum was chosen

Share of active Android devices by version, October 2026 (Google, via Android Authority; StatCounter in
brackets):

| Version | Share |
| --- | --- |
| Android 16 | ~25% (25.5%) |
| Android 15 | ~15% (17.2%) |
| Android 14 | ~15% (13.1%) |
| Android 13 and older | ~44% in total (13: 14.8%, 12: 10.1%, 11: 8.3%) |

Android 8 is the first version with notification channels, adaptive icons and foreground-service rules the
app relies on, so it is the lowest sensible floor; below it the remaining devices are a tiny slice.

## What the app needs from the phone

| Need | Why | If missing |
| --- | --- | --- |
| Android 8.0+ | notification channels, adaptive icons | cannot install |
| ~2 GB RAM or more | Compose UI, camera and ML Kit tasks | runs, camera tasks slower |
| Speaker or vibration motor | the alarm | vibration-only or sound-only works |
| Camera (optional) | scan, photo and face tasks | those tasks are hidden |
| Motion sensors (optional) | step and shake tasks | those tasks are hidden |
| Internet (optional) | online sleep counselor only | offline answers still work |

## Typical phones that qualify (examples, not a guarantee)

- **Samsung Galaxy:** A10 and newer, M and F series from 2019, S9 and newer, Note 9 and newer
- **Xiaomi, Redmi, POCO:** Redmi 7 and newer, Redmi Note 7 and newer, Mi 9 and newer
- **Realme, Oppo, Vivo, OnePlus:** most models from 2018 on (OnePlus 6 and newer)
- **Motorola:** Moto G7 and newer, Moto E7 and newer
- **Google Pixel:** Pixel 2 and newer
- **Nokia and Infinix, Tecno, Itel:** current budget models running Android 8+

Entry-level phones with 2 to 3 GB RAM and a 720p screen are the lowest tier the layout is built for.

## Behaviour by Android version

| Version | Notable differences handled by the app |
| --- | --- |
| 8 to 9 | window flags used to show over the lock screen; no notification permission prompt |
| 10 to 11 | exact alarms allowed without a grant |
| 12 and 12L | exact-alarm permission requested through system settings when missing |
| 13 | notification permission prompt; exact alarms granted to alarm apps |
| 14 and newer | full-screen notification permission can be revoked; the app shows a banner |

## What was verified

- Lint compatibility check (`NewApi`) is clean for API 26 and newer, plus unit tests and a release bundle.
- Runtime check on an Android 16-class (API 37) emulator.
- **Not yet verified on real older phones.** Use the Play Console pre-launch report and the closed test
  for that.
