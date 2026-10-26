# FITS Viewer repository guide

## Main modules

- `shared/` contains the KMP FITS parser, source-backed crop renderer, and Compose Multiplatform viewer.
- `app/` is the Android app shell and Open Document integration.
- `desktopApp/` is the desktop JVM app and file chooser.
- `fitsreader/` and `imagefuncs/` are earlier FITS processing modules, still built and tested by the Android project.

## Useful commands

- Build Android and desktop sources: `./compile_all.sh`
- Run tests: `./gradlew :shared:desktopTest :fitsreader:testDebugUnitTest :imagefuncs:testDebugUnitTest`
- Run shared parser tests: `./gradlew :shared:desktopTest`
- Run Android app in an emulator: `./gradlew :app:installDebug`
- Run desktop: `./gradlew :desktopApp:run`
- Run desktop with automatic Compose Hot Reload: `./gradlew :desktopApp:hotRunDesktop --auto`
- Android SDK is discovered from `ANDROID_HOME`, `ANDROID_SDK_ROOT`, `local.properties`, or the standard macOS SDK path by the project scripts.

## FITS viewer behavior

The viewer retains the original FITS bytes, applies contrast to source samples, and renders only the requested crop into a bounded display bitmap. Preserve that source-backed behavior when changing zoom or contrast. The current parser intentionally limits input to 128 MiB and to one image plane or three RGB planes.
