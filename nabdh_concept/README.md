# Nabdh Browser (نبض) - Concept & Source Code

This repository contains the core source code for **Nabdh Browser**, an avant-garde, privacy-focused Android browser powered by Mozilla's GeckoView engine.

## 🚀 Features

*   **GeckoView Engine**: Powered by the same engine as Firefox for robust web compatibility and security.
*   **Pulse UI**: A living, breathing interface with a dynamic "heartbeat" indicator that reacts to network traffic.
*   **Ghost Mode**: Hardened privacy mode (no cookies, enhanced tracking protection, distinct UI theme).
*   **AdBlocker & Cosmetic Filtering**: Built-in protection against ads and trackers using custom JS injection.
*   **Dark Mode Enforcer**: Force dark mode on any website via GeckoView settings.
*   **File Downloads**: Integrated download manager support.
*   **Speed Dial**: Quick access to favorite sites.

## 🛠️ Project Structure & Setup

To build this project, create a new **Android Studio** project (Kotlin) and arrange the provided files as follows:

### 1. Package Structure (`app/src/main/java/`)

Ensure your package name is `com.nabdh.browser`.

*   `NabdhApp.kt` -> Root application class.
*   `core/`
    *   `NabdhContentDelegate.kt` -> Handles downloads and ad-blocking script injection.
    *   `AdBlocker.kt` (Legacy, replaced by ContentDelegate but useful for reference).
*   `data/`
    *   `SearchRepository.kt` -> Handles search suggestions logic.
*   `ui/`
    *   `main/`
        *   `MainActivity.kt` -> The browser window.
        *   `SettingsActivity.kt` -> User preferences.
        *   `PulseViewModel.kt` -> The brain (MVVM) managing the engine state.
        *   `BrowserMenuFragment.kt` -> Bottom sheet menu.
        *   `SpeedDialAdapter.kt` -> RecyclerView adapter for start page.
    *   `components/`
        *   `PulseIndicatorView.kt` -> The custom heartbeat view.

### 2. Resources (`app/src/main/res/`)

*   `layout/`
    *   `activity_main.xml`
    *   `activity_settings.xml`
    *   `layout_menu_bottom_sheet.xml`
*   `drawable/`
    *   `bg_search_bar.xml`
    *   `progress_drawable.xml`
*   `values/`
    *   `themes.xml` (Define `Theme.NabdhBrowser` here).

### 3. Manifest (`AndroidManifest.xml`)

Ensure you include the permissions (`INTERNET`, `WRITE_EXTERNAL_STORAGE`) and register the Activities.

## 📦 Dependencies (`build.gradle`)

Add the following to your module-level `build.gradle`:

```groovy
dependencies {
    // Core Android
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

    // GeckoView (The Engine)
    implementation "org.mozilla.geckoview:geckoview-stable:121.0.20240213153646"

    // Coroutines & Lifecycle
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2"
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.6.2"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
}
```

**IMPORTANT**: You must add the Mozilla Maven repository to your `settings.gradle`:

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url "https://maven.mozilla.org/maven2/" }
    }
}
```

## 📱 How to Run

1.  Open the project in Android Studio.
2.  Sync Gradle files.
3.  Connect a device or Emulator.
4.  Run `MainActivity`.

---
*Built with ❤️ by Antigravity & You.*
