# Testing

## Run the project checks

## Focused Gradle tasks

```sh
./compile_all.sh
./gradlew :shared:desktopTest
./gradlew :fitsreader:testDebugUnitTest :imagefuncs:testDebugUnitTest
./gradlew :app:assembleDebug :desktopApp:compileKotlinDesktop
```

The shared test builds a minimal FITS image in memory and checks header parsing, source pixel access, and crop rendering. For a manual Android smoke check, install the debug APK on a running emulator and open a bundled or local FITS file; use zoom and the stretch controls to inspect the rendered crop.

## Continuous integration

GitHub Actions runs the Android debug build, desktop compilation, shared JVM tests, and legacy library unit tests on pushes and pull requests to `main`.
