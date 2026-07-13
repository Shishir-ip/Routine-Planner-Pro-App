# Routine Planner Pro

Routine Planner Pro is a modern Android routine management app for **Daily Routine**, **Class Routine**, and unlimited **custom routines** (exam routine, gym routine, work routine, etc.).

## Features

- ✅ Two always-available core routines:
  - **Daily Routine**
  - **Class Routine**
- ✅ Add unlimited custom routines with **Add routine +**
- ✅ Add routine activities with:
  - Activity/Course name
  - Time range (start/end)
  - Date range (single date or start/end)
  - Day filters (specific weekdays or everyday)
  - Optional details via **+ More options**
- ✅ Class routine support fields:
  - Course name, room number, class type (Theory/Lab), teacher, section
- ✅ Smooth detail expansion animation on tap
- ✅ Optional **Reminder** per activity with minutes-before option (default 5)
- ✅ Optional **Alarm** per activity with minutes-before option
- ✅ Daily routine view includes summaries from other routines in the same timeline
- ✅ Theme settings: Day/Night mode toggle
- ✅ JSON import/export for routine data
- ✅ AI JSON generator (OpenRouter) from natural language text
- ✅ Secure OpenRouter API key save/delete with masked display
- ✅ Developer info section

## Tech Stack

- Kotlin + Jetpack Compose (Material 3)
- Room Database
- DataStore Preferences
- AlarmManager + Notifications
- WorkManager (rescheduling)
- OkHttp + Kotlin Serialization

## Project Structure

- `app/src/main/java/com/shishir/routineplannerpro/` — app source code
- `app/src/main/java/com/shishir/routineplannerpro/data` — Room DB and repository
- `app/src/main/java/com/shishir/routineplannerpro/reminder` — alarms/reminders
- `app/src/main/java/com/shishir/routineplannerpro/settings` — theme/API key settings
- `.github/workflows/` — CI and release workflows

## Build Locally

### Requirements

- JDK 17
- Android SDK (API 35)

### Commands

```bash
./gradlew assembleDebug
./gradlew lint test
```

## Build in GitHub Actions

This repository includes:

- **Android CI workflow**: `.github/workflows/android-ci.yml`
  - Runs lint, unit tests, and debug build
- **Release workflow**: `.github/workflows/release.yml`
  - Builds release APK when pushing a tag like `v1.0.0`
  - Publishes APK to GitHub Releases

## How to Publish in GitHub Releases

1. Push your changes to default branch.
2. Create and push a version tag:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
3. GitHub Action `Android Release` runs automatically.
4. Open the repository **Releases** section and find the generated APK asset.

## OpenRouter AI Generator Setup

1. Open app **Settings**.
2. Paste your OpenRouter API key.
3. Save key.
4. Enter natural language routine prompt.
5. Generate JSON and import it.

> Model used: `openrouter/free`

## Developer

- **Name:** Shishir
- **GitHub:** https://github.com/Shishir-ip

## SEO Keywords

Routine Planner Pro, Android routine app, daily routine planner, class routine manager, student schedule app, timetable app, reminder alarm app, exam routine planner, JSON routine import export, AI routine generator, OpenRouter Android app

## License

MIT (recommended to add LICENSE file if needed)
