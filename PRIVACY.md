# AlarmBoss Privacy Policy

*Last updated: 10 November 2026*

AlarmBoss ("the app") is an alarm clock with wake-up challenges. This policy explains what the app does
with information. In short: **the app has no accounts, no ads, no analytics and no tracking, and almost
everything stays on your phone.** The only time anything leaves your phone is when you choose to use the
optional online counselor, described below.

## What stays on your phone

All of the following is stored only on your device, in the app's private storage, and is never sent to
the developer or to anyone else:

- **Alarms and settings:** times, labels, repeat days, sounds, modes, snooze, volume, holidays you skip, and
  your theme choice.
- **Progress:** wake-ups, streak, experience points, level, avatars and the stats calendar.
- **Parent Mode (optional):** the parent or guardian's name, an optional email address, the child's first
  name and age, the school wake-up time, and a PIN. The PIN is stored only as a salted one-way hash, never
  in the clear. The email address is not used or sent anywhere.
- **Content you add:** reading passages, lyric lines and a registered barcode used by wake-up challenges.

## Permissions and what they are used for

| Permission | Why the app asks |
| --- | --- |
| Alarms and full-screen notifications | To ring on time and show the wake-up screen over the lock screen. |
| Notifications | To show the ringing alarm and follow-up rings. |
| Camera | To scan a barcode and to count exercises (squats, jumping jacks) in the Strict wake-up challenge. Camera images are analysed on your device as they are captured. **They are not saved, recorded or sent anywhere.** The camera light is also used, if you turn it on, to flash with an alarm. |
| Microphone | For wake-up challenges where you read a passage or sing a lyric aloud, and for dictation in the counselor chat. See "Voice" below. |
| Physical activity | To count steps in the step-counter challenge, using your phone's own step sensor. |
| Internet and network state | Only for the optional online counselor and AI-written reading passages. The app works fully without them. |
| Vibration, wake lock, run at start-up, foreground service | To ring and vibrate reliably and to re-register your alarms after a restart or an update. |

You can refuse any permission. The alarm still works; only the features that need it are unavailable.

## Voice

When a challenge or the counselor chat listens to you, the app asks Android's speech recognition service to
turn your speech into text, and asks it to work on the device where that is possible. Which service does this,
and whether any audio is processed by it online, depends on your phone and its settings (on many phones it
is Google's). **AlarmBoss itself does not record, store or send your audio.** It only receives the text the
recognizer returns.

## The online counselor (optional, off by default)

The sleep counselor works **entirely on your device**, with no internet, by default.

If you choose to add your own Google Gemini API key in Settings, the counselor can use Google's Gemini
service for fuller answers, and "AI Generate" can write reading passages. **Only then** are the following
sent to Google, using your key: the message you typed, a few recent messages of that chat for context, and
a short instruction describing how the counselor should behave. Nothing else (no alarms, progress,
Parent Mode details, name or device identifiers) is sent. Google's handling of that data is governed by
Google's terms and privacy policy and your Gemini account, not by this app. The key is stored only on your
device, is excluded from Android backups, and you can remove it at any time in Settings.

**Online features are switched off while Parent Mode is on**, so a child's messages are never sent to an
online service; the counselor then answers on the device only.

Answers written by the online AI have a "Report this answer" link so you can tell us about anything harmful
or inappropriate. The report opens a pre-filled public issue on GitHub that you review before sending.

## Not medical advice

The counselor and the tips in the app are general wellness information. They are not medical advice,
diagnosis or treatment, and they do not replace a doctor. If you are worried about your sleep or health, or
you are in distress, please contact a health professional or your local emergency services.

## Children

The app is not directed at children under 13 and does not knowingly collect their personal information
for any purpose. Parent Mode is a tool for a parent or guardian to set up and lock a child's alarms; the
details entered stay on the device as described above. A parent can switch Parent Mode off at any time with
the PIN.

## Backups

Android may back up the app's alarms, settings and progress to your Google account if you have device backup
turned on. The Parent Mode PIN and the counselor key are excluded from backups.

## Sharing, selling and retention

We do not sell or share your data, and there is no advertising. We keep nothing, because we receive nothing.
The data above lives on your device until you delete it.

## Your choices and deleting your data

You can delete alarms in the app. To remove **all** the app's data, clear its storage in Android Settings
(Apps, AlarmBoss, Storage) or uninstall it. If you used the online counselor, data you sent to Google can be
managed through your Google account.

## Open source

AlarmBoss is open source. You can read exactly what it does at
<https://github.com/ARIEA-hub/Mobile-Application>.

## Changes and contact

If this policy changes, the new version will be published at the same address with a new date. For questions
or requests, email <amsampat1@gmail.com> or open an issue at
<https://github.com/ARIEA-hub/Mobile-Application/issues>.
