# Data safety form (Play Console → App content → Data safety)

Answers prepared from what the app actually does. Re-check them against the form's wording when you fill it
in, and keep them in step with `PRIVACY.md`: Google compares the two.

## Overview questions

| Question | Answer |
| --- | --- |
| Does your app collect or share any of the required user data types? | **Yes**: see "Messages" below. Everything else stays on the device. |
| Is all of the user data collected by your app encrypted in transit? | **Yes** (the only transmission is HTTPS to Google's Gemini API) |
| Do you provide a way for users to request that their data be deleted? | **Yes**: the app has no accounts and keeps data only on the device; users clear storage or uninstall. Data sent to Google is managed in the user's Google account. |
| Does your app follow the Families Policy? | **Not applicable**: the app is not directed at children (target audience 13+) |

## Data types

Only data that **leaves the device** counts as "collected" or "shared". On-device-only processing is not
declared.

| Data type | Collected? | Shared? | Notes |
| --- | --- | --- | --- |
| **Messages → Other in-app messages** | Yes, **only if the user adds their own Gemini key** | Yes, with **Google (Gemini API)** | Text the user types in the counselor chat, plus recent chat turns, sent to Google using the user's key. **Optional**, **user-initiated**. Purpose: **App functionality**. Not used for advertising. Off while Parent Mode is on. |
| Photos and videos | No | No | Camera frames are analysed on-device in memory and never stored or sent. |
| **Audio → Voice or sound recordings** | No | No | The app does not record or receive audio; it receives only the text returned by Android's speech recognizer. (If Google's review asks, explain that the recognizer is the phone's own system service.) |
| Location | No | No | Not used. |
| Personal info (name, email, address, etc.) | No | No | Parent Mode name, email and the child's name/age are stored only on the device and never transmitted. |
| Financial info, health records, contacts, calendar, web history | No | No | Not used. |
| Health and fitness → Fitness info | No | No | Step counts are read from the sensor and used on-device for the challenge; not stored or sent. |
| App activity, device or other IDs | No | No | No analytics, no advertising ID, no crash reporting service. |
| App info and performance | No | No | None collected. |

## Security practices

- Data encrypted in transit: **Yes**
- Users can request data deletion: **Yes** (see above)
- Committed to follow Play's Families policy: **No** (not a children's app)
- Independent security review: **No**

## Things that would change these answers

Adding analytics, crash reporting, ads, accounts, cloud sync, or any other network feature changes this
form **and** `PRIVACY.md`. Update both before releasing it.
