# Permission and policy declarations (Play Console → Policy and programs → App content)

Application ID: `io.github.arieahub.alarmboss` · Target SDK 36 · Min SDK 36

Every sensitive permission the app declares, why, and what to answer. Paste the text in quotes.

## 1. Exact alarms (`USE_EXACT_ALARM`)

Google restricts this to apps whose **core function** is an alarm clock or calendar.

- **Declare:** "Alarm clock".
- **Justification:** "AlarmBoss is an alarm clock. Its core function is ringing at the exact time the user
  sets, which is why it uses USE_EXACT_ALARM with AlarmManager.setAlarmClock. An inexact alarm would
  defeat the purpose of the app."

## 2. Full-screen intent (`USE_FULL_SCREEN_INTENT`)

Allowed for apps whose core function is alarms or calls.

- **Declare:** "Alarms".
- **Justification:** "When an alarm rings while the phone is locked, AlarmBoss must show its dismiss and
  wake-up challenge screen over the lock screen, using a full-screen notification. This is the app's core
  purpose."

## 3. Foreground service (`FOREGROUND_SERVICE_MEDIA_PLAYBACK`)

The ringing service (`AlarmService`) uses the **mediaPlayback** foreground service type.

- **Type:** Media playback.
- **Justification:** "The service plays the alarm sound and vibration for as long as the alarm rings, and
  must keep running if the user leaves the screen. It starts only when a user-set alarm fires and stops
  when the alarm is dismissed."
- **Demo video:** Google asks for a short screen recording: set an alarm for one minute ahead, lock the
  phone, let it ring, show the full-screen screen, dismiss it.

## 4. Camera (`CAMERA`) · not a restricted permission, but needs a clear purpose

- **Use:** barcode-scanning challenge and counting exercises in the Strict challenge. Frames are analysed on
  the device in memory and never stored or sent.
- The camera is declared `required="false"`, so devices without one can still install.

## 5. Microphone (`RECORD_AUDIO`)

- **Use:** speech recognition for the "read aloud" and "sing the lyric" challenges and chat dictation. The app
  asks Android's recognizer for on-device recognition where available, and never records or stores audio.

## 6. Physical activity (`ACTIVITY_RECOGNITION`)

- **Use:** the step-counter challenge reads the device's step sensor. Falls back to the motion sensor if the
  permission is refused.

## 7. Permissions deliberately not requested

- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`: restricted on Google Play. The app opens the system settings list
  instead, which needs no permission.
- `SCHEDULE_EXACT_ALARM`: redundant with `USE_EXACT_ALARM`.
- `SYSTEM_ALERT_WINDOW`, device-admin: not used.

## 8. Other declarations

| Form | Answer |
| --- | --- |
| Ads | The app contains **no ads** |
| App access | All functionality available without login or special access |
| Target audience | **13 and over** (see `content-rating-and-audience.md`) |
| News app | No |
| COVID-19 contact tracing / status | No |
| Government app | No |
| Financial features | None |
| Health apps | The sleep counselor gives general wellness information. Complete the "Health apps" declaration: no medical device, no clinical features, not a diagnosis or treatment tool. The app says so on screen and in the listing. |
| AI-generated content | The optional online counselor (the user's own Gemini key) generates text. In-app reporting is provided ("Report this answer"). |
