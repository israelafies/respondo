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

## Android backend connection

The Android app uses a Retrofit/OkHttp API repository for the Worker health, system information, and D1 connectivity endpoints. Requests run off the main thread with bounded timeouts. The app displays a small backend status indicator; Android network connectivity and Worker reachability are reported separately. When offline, Room-based prospect, offer profile, and campaign features continue to work locally. No user records are sent to the backend.

Configure the base URL at build time, with a trailing slash, using `-PrespondoBackendEnvironment=development|staging|production` and the matching `-PrespondoDevelopmentBackendUrl`, `-PrespondoStagingBackendUrl`, or `-PrespondoProductionBackendUrl` Gradle property. Matching environment variables are also supported: `RESPONDO_BACKEND_ENVIRONMENT` and `RESPONDO_<ENV>_BACKEND_URL`. For example, use your own deployed HTTPS Worker URL; no URL is bundled by default. Production URLs must use HTTPS. These values are public endpoint configuration, not credentials. Missing configuration is shown in the app and does not disable local features.

Authentication and cloud synchronization are not implemented. Future authenticated requests belong at the shared HTTP client interceptor/authenticator boundary and must be backed by real account/session APIs. A future sync repository can be added alongside the local Room repositories; this milestone does not upload prospect, conversation, campaign, or offer data.
