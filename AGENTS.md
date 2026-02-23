# AGENTS.md — AI Coding Guide for AppLaunchLoop

## Project Overview

AppLaunchLoop is a minimal, privacy-first Android utility that helps indie developers coordinate Google Play's mandatory 14-day / 20-tester closed testing requirement. It manages deep-linked onboarding and triggers daily on-device notification prompts to open a target app.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose with Material 3
- **Architecture:** MVVM (ViewModel per screen, repository pattern)
- **Local Persistence:** Jetpack DataStore (Preferences) — do NOT use SharedPreferences
- **Background Processing:** WorkManager (PeriodicWorkRequest) — do NOT use exact alarms
- **Cloud Database:** Firebase Firestore (free tier, no authentication)
- **Min API:** 26, **Target API:** 34+, **Compile SDK:** 34
- **Build System:** Gradle 8.7, AGP 8.5.2, Kotlin 2.0.21

## Project Structure

```
app/src/main/java/com/baijum/applaunchloop/
├── AppLaunchLoopApplication.kt   # Application class, initializes CampaignRepository
├── MainActivity.kt               # Entry point, deep link handling, navigation setup
├── data/repository/
│   ├── CampaignRepository.kt     # Interface for all local persistence
│   └── CampaignRepositoryImpl.kt # DataStore-backed implementation
├── ui/
│   ├── navigation/
│   │   ├── AppDestinations.kt    # Sealed class defining nav routes
│   │   └── AppNavHost.kt         # NavHost wiring all screens
│   ├── screen/
│   │   ├── chooser/              # First-launch role selection (Creator / Tester)
│   │   ├── creator/              # Campaign creation form + Firestore write
│   │   ├── tester/               # 14-day streak tracker + app launcher
│   │   └── onboarding/           # 3-step deep-link onboarding flow
│   └── theme/                    # Material 3 colors, typography, dynamic theme
└── worker/
    ├── DailyTestWorker.kt        # PeriodicWorkRequest, fires daily notification
    └── WorkScheduler.kt          # Enqueue/cancel helper
```

## Key Architecture Rules

1. **No SharedPreferences.** All local state goes through `CampaignRepository` backed by DataStore.
2. **No exact alarms.** Background work uses `WorkManager` with `PeriodicWorkRequest`.
3. **No QUERY_ALL_PACKAGES.** Package visibility uses the `<queries>` workaround in the manifest.
4. **No user authentication.** Firestore documents are keyed by campaign ID; creator identity uses `ANDROID_ID`.
5. **ViewModels** receive `CampaignRepository` via a `ViewModelProvider.Factory`. Do not use dependency injection frameworks.
6. **Navigation** uses Jetpack Navigation Compose. Routes are defined in `AppDestinations.kt`.

## Data Flow

- **Creator flow:** Form input → Firestore write → campaign ID generated → shareable deep link
- **Tester flow:** Deep link → Onboarding (3 steps) → DataStore save → Tester Dashboard
- **Daily loop:** WorkManager triggers → check DataStore streak → fire notification → user taps → target app launches

## Firestore Schema

Collection `campaigns`, document ID = campaign ID:
- `campaignId` (String)
- `googleGroupEmail` (String)
- `packageName` (String)
- `package_names` (Array)
- `created_at` (Long timestamp)
- `creator_device_id` (String)

## DataStore Keys

| Key | Type | Purpose |
|-----|------|---------|
| `active_campaign_id` | String | Currently joined campaign |
| `target_package_names` | StringSet | Target apps to test |
| `current_streak_count` | Int | Days completed (0–14) |
| `last_run_timestamp` | Long | Last successful test run |
| `my_created_campaigns` | StringSet | Creator's campaign IDs |
| `last_dashboard` | String | Last used screen ("creator" / "tester" / "") |

## Deep Linking

- Scheme: `https`
- Host: `baijum.github.io`
- Path pattern: `/applaunchloop/join/{campaignId}`
- `autoVerify="true"` requires `docs/.well-known/assetlinks.json`

## Files That Must Not Be Committed

- `app/google-services.json` (Firebase config with API keys)
- `local.properties` (local SDK path)
- `*.jks` / `*.keystore` (signing keys)

## Testing Commands (macOS)

```bash
# Build
./gradlew assembleDebug

# Install on emulator
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch
~/Library/Android/sdk/platform-tools/adb shell am start -n com.baijum.applaunchloop/.MainActivity

# Test deep link
~/Library/Android/sdk/platform-tools/adb shell am start -a android.intent.action.VIEW -d "https://baijum.github.io/applaunchloop/join/TEST123"
```
