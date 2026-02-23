# AppLaunchLoop

A minimal, privacy-first Android utility that helps indie developers coordinate Google Play's mandatory **14-day / 20-tester closed testing** requirement.

AppLaunchLoop eliminates manual follow-ups by providing a deep-linked onboarding flow and automated daily on-device notification prompts to open a target app.

## Features

- **Creator Dashboard** — Create a testing campaign with a Google Group email and target package name. Get a shareable deep link to send to your testers.
- **Tester Onboarding** — Testers tap the link and are guided through 3 steps: join the Google Group, opt in to testing, and download the app.
- **14-Day Streak Tracker** — Visual progress bar showing how many days of the 14-day requirement have been completed.
- **Daily Reminders** — WorkManager-powered background notifications remind testers to open the target app each day.
- **Privacy-First** — No user accounts, no analytics, no tracking. All daily operations run locally via DataStore.

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM |
| Local Storage | Jetpack DataStore (Preferences) |
| Background Work | WorkManager |
| Cloud Database | Firebase Firestore (free tier) |
| Deep Linking | Android App Links via GitHub Pages |

## Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- Android SDK 34
- A Firebase project with Firestore enabled

## Getting Started

### 1. Clone the repository

```bash
git clone git@github.com:baijum/applaunchloop.git
cd applaunchloop
```

### 2. Add Firebase config

Download `google-services.json` from your [Firebase Console](https://console.firebase.google.com/) and place it in the `app/` directory.

### 3. Build and run

```bash
./gradlew assembleDebug
```

Or open the project in Android Studio and run on an emulator or device.

## GitHub Pages Setup

The `docs/` directory contains files for hosting on GitHub Pages to enable Android App Links:

| File | Purpose |
|------|---------|
| `docs/.nojekyll` | Prevents GitHub from ignoring `.well-known` |
| `docs/.well-known/assetlinks.json` | App Links verification (SHA-256 fingerprints) |
| `docs/index.html` | Fallback redirect to Play Store |

To enable:
1. Go to your repo **Settings → Pages**
2. Set source to **Deploy from a branch**, branch `main`, folder `/docs`
3. Update the SHA-256 fingerprints in `assetlinks.json` with your release signing key

### Generate SHA-256 fingerprint (macOS)

```bash
# Debug key
keytool -list -v -keystore ~/.android/debug.keystore -storepass android | grep SHA256

# Release key
keytool -list -v -keystore your-release-key.jks | grep SHA256
```

## How It Works

### Creator Flow

1. Open AppLaunchLoop → select "I'm a Creator"
2. Enter your Google Group email and target app package name
3. Tap "Create Campaign" — a campaign ID is generated and saved to Firestore
4. Share the deep link (`https://baijum.github.io/join/<ID>`) with your testers

### Tester Flow

1. Tap the deep link → AppLaunchLoop opens the onboarding flow
2. Step 1: Join the Google Group
3. Step 2: Opt in to closed testing on Google Play
4. Step 3: Download the target app
5. Each day, a notification reminds you to open and test the app
6. Progress is tracked locally (Day X / 14)

## Project Structure

```
app/src/main/java/com/baijum/applaunchloop/
├── data/repository/       # DataStore-backed persistence
├── ui/
│   ├── navigation/        # Jetpack Navigation routes
│   ├── screen/
│   │   ├── chooser/       # Role selection (Creator / Tester)
│   │   ├── creator/       # Campaign creation
│   │   ├── tester/        # Streak tracker + app launcher
│   │   └── onboarding/    # 3-step deep link onboarding
│   └── theme/             # Material 3 theme
└── worker/                # WorkManager daily notifications
```

## License

This project is Free and Open Source Software (FOSS). See [LICENSE](LICENSE) for details.
