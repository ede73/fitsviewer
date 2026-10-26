# Development

## Project layout

- `shared/` — Kotlin Multiplatform FITS parsing, contrast rendering, and shared Compose UI (`android` and `desktop` targets).
- `app/` — Android application shell, local document picker, and bundled sample image.
- `desktopApp/` — desktop JVM entry point and local file chooser.
- `fitsreader/`, `imagefuncs/` — original Android/JVM FITS experiments and their tests.

## Requirements

- JDK 17 or newer for Gradle builds.
- Android SDK/platform tools for Android builds and emulator checks.
- The Gradle wrapper downloads the configured Gradle distribution and dependencies.

## Build and run

```sh
./compile_all.sh
./gradlew :app:installDebug
./gradlew :desktopApp:run
```

To develop the shared UI with Compose Hot Reload:

```sh
./gradlew :desktopApp:hotRunDesktop --auto
```

This starts the desktop JVM app and watches source changes. For an explicit reload, run `./gradlew :desktopApp:hotReloadDesktopMain` from another terminal.

## Local configuration

Set `ANDROID_HOME` (or `ANDROID_SDK_ROOT`) to the Android SDK directory, or add `sdk.dir` to an untracked `local.properties` file. Do not commit local SDK paths, signing credentials, or private service configuration.
