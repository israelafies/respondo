# Respondo

Respondo is a native Android sales workspace built with Kotlin and Jetpack Compose. The app provides a branded dashboard and navigation for Dashboard, Prospects, Conversations, Campaigns, and Settings. Prospects can be created, searched, viewed, edited, and deleted. Records are stored locally in SQLite through Room and remain available offline. A repository boundary allows future sync storage to replace the local implementation without changing the UI.

## Build

Routine Android builds run in GitHub Actions. The `android-build.yml` workflow runs on pushes to `main` or `master` and supports manual runs. It configures JDK 17, Gradle dependency caching, Android SDK platform 36 and build tools 35.0.0, then compiles sources, runs JVM unit tests, and assembles the debug APK. After every preceding step succeeds, the APK at `app/build/outputs/apk/debug/app-debug.apk` is uploaded as the `Respondo-Android-Debug-APK` artifact.

Pull requests are checked by `android.yml`. Local Android SDK installation is not required for the GitHub Actions build.

## Project structure

```text
app/
  src/main/java/com/respondo/app/
    MainActivity.kt             # Android entry point / composition root
    ui/RespondoApp.kt           # Home dashboard and top-level navigation
    ui/prospects/               # Prospect list, form, details, and ViewModel
    data/prospects/             # Room entity, DAO, database, and repository
    ui/theme/RespondoTheme.kt   # App color scheme
  src/main/AndroidManifest.xml
gradle/wrapper/                 # Pinned Gradle wrapper
.github/workflows/android.yml  # Hosted debug APK build
```

The app has no AI provider or messaging integrations yet. Keep future business rules and provider/channel contracts outside Compose UI, and add modules when a focused feature benefits from a separate build and ownership boundary.

## Cloud backend

The `backend/` directory contains a separate Cloudflare Worker API and D1 schema foundation. Android Room remains the local database; Android cloud requests will go through the Worker, and no Room data is synchronized by this milestone. See [backend/README.md](backend/README.md) for local setup, migrations, endpoints, security boundaries, and deployment prerequisites. The Worker and D1 database have not been deployed remotely.
