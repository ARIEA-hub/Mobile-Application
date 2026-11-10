# Google Play submission pack

Everything below is prepared from what the app actually does. Start with `release-guide.md`.

| File | Use |
| --- | --- |
| `listing.md` | Title, short and full description, release notes, category |
| `permissions-and-declarations.md` | Exact-alarm, full-screen-intent and foreground-service justifications |
| `data-safety.md` | Data safety form answers |
| `content-rating-and-audience.md` | Target audience (13+), IARC answers, health declaration |
| `release-guide.md` | Upload key, GitHub secrets, Play Console steps, closed test |
| `icon-512.png`, `feature-graphic.png`, `screenshots/` | Store graphics |

## Done in the repository

- Application ID `io.github.arieahub.alarmboss` (Play rejects `com.example`)
- Restricted permissions removed (`SCHEDULE_EXACT_ALARM`, battery-optimisation request)
- Signed AAB pipeline in `.github/workflows/release.yml`, 16 KB page alignment verified
- Privacy policy (`PRIVACY.md`), in-app link, AI answer reporting, not-medical-advice note
- Parent Mode turns every online feature off

## Only you can do

1. Create the Play developer account and verify identity
2. Generate the upload keystore and add the four GitHub secrets
3. Add a support email to the listing
4. Run a closed test (12 testers, 14 days) for new personal accounts
5. Fill the Play Console forms from these files
6. Decide whether Android 16 only (`minSdk 36`) is acceptable
