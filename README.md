# Respondo

Respondo is a native Android sales workspace built with Kotlin and Jetpack Compose. This initial app provides a branded dashboard and navigation for Dashboard, Prospects, Conversations, Campaigns, and Settings. Feature areas are grouped under the app's `ui` package so they can be extracted into Gradle modules when they gain independent domain and data ownership.

## Build

The project includes the Gradle wrapper. For a local build, use JDK 17 and install Android SDK platform 36 and build tools 35.0.0, then run:

```bash
./gradlew :app:assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

GitHub Actions builds the same debug APK on GitHub's hosted Ubuntu runner for pushes to `main` or `master`, pull requests, and manual workflow runs. The workflow configures Java and the Android SDK, installs the required SDK platform/build tools, runs the Gradle wrapper, and uploads the APK as the `respondo-debug-apk` artifact. Android SDK or Gradle installation is not needed on a development phone.

## Project structure

```text
app/
  src/main/java/com/respondo/app/
    MainActivity.kt             # Android entry point / composition root
    ui/RespondoApp.kt           # Home dashboard and top-level navigation
    ui/theme/RespondoTheme.kt   # App color scheme
  src/main/AndroidManifest.xml
gradle/wrapper/                 # Pinned Gradle wrapper
.github/workflows/android.yml  # Hosted debug APK build
```

The app has no AI provider or messaging integrations yet. Keep future business rules and provider/channel contracts outside Compose UI, and add modules when a focused feature benefits from a separate build and ownership boundary.
