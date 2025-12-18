# Habit Tracker

An Android application that allows a user to track their daily habits.

## Features

* **Notifications** - Daily reminders for each habit with customizable times
* **Completion** - Mark habits as completed for any day with visual feedback
* **Tracking** - View completion history with calendar view and streak statistics
* **Export** - Export all habits and completion data to CSV format

## Tech Stack

* **Language**: Kotlin
* **UI Framework**: Jetpack Compose with Material 3
* **Architecture**: MVVM (Model-View-ViewModel)
* **Database**: Room
* **Notifications**: WorkManager
* **Target SDK**: Android 35 (minimum SDK 26)

## Building the Project

### Prerequisites

* Android Studio Hedgehog or later
* JDK 17
* Android SDK 35

### Local Build

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run on an emulator or physical device

### Command Line Build

```bash
./gradlew assembleDebug    # Build debug APK
./gradlew assembleRelease  # Build release APK
./gradlew test             # Run tests
```

## GitHub Actions CI/CD

This project includes a GitHub Actions workflow that automatically builds the app on:

* Push to `main` or `develop` branches
* Pull requests to `main` or `develop` branches
* Manual workflow dispatch

The workflow:
1. Sets up JDK 17 and Android SDK
2. Caches Gradle dependencies for faster builds
3. Runs tests
4. Builds both debug and release APKs
5. Uploads APK artifacts that can be downloaded from the Actions tab

### Accessing Build Artifacts

After a successful build, you can download the APKs:

1. Go to the Actions tab in your GitHub repository
2. Click on the latest workflow run
3. Scroll to the Artifacts section
4. Download `habit-tracker-debug` or `habit-tracker-release`

## Project Structure

```
app/src/main/java/com/habittracker/
├── MainActivity.kt
├── HabitTrackerApp.kt
├── data/
│   ├── model/           # Data models (Habit, HabitCompletion)
│   ├── dao/             # Room DAOs
│   ├── database/        # Room database
│   └── repository/      # Repository layer
├── ui/
│   ├── theme/           # Material theme configuration
│   ├── navigation/      # Navigation setup
│   ├── screens/         # Composable screens
│   └── viewmodel/       # ViewModels
└── notification/        # Notification scheduling and handling
```

## Permissions

The app requires the following permissions:

* `POST_NOTIFICATIONS` - To send habit reminders
* `SCHEDULE_EXACT_ALARM` - To schedule precise reminder times
* `RECEIVE_BOOT_COMPLETED` - To reschedule reminders after device restart