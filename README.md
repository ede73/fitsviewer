# FITS Viewer

A small Kotlin Multiplatform app for opening local FITS astronomy images and experimenting with contrast stretches. It is intended as a viewer, not an image-stacking or scientific reduction tool.

The Android and desktop apps share the FITS parser, source-backed renderer, and Compose UI. Open a local file with **Open FITS** (Android document picker) or the desktop file chooser, then adjust brightness and gamma, switch stretch algorithms, and zoom or pan around the image.

## Build and run

Requirements: JDK 17 or newer, Android SDK for Android builds, and the Android platform tools for emulator use.

```sh
./gradlew :app:assembleDebug
./gradlew :desktopApp:run
```

The Android debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

For desktop UI iteration with Compose Hot Reload:

```sh
./gradlew :desktopApp:hotRunDesktop --auto
```

The desktop target is the Hot Reload sandbox. To trigger a reload explicitly, run `./gradlew :desktopApp:hotReloadDesktopMain`. See the [Compose Hot Reload guide](https://kotlinlang.org/docs/multiplatform/compose-hot-reload.html).

## FITS and memory limits

- Supports BITPIX 8, 16, 32, 64, -32, and -64, with one image plane or three RGB planes.
- FITS files and image rasters are limited to 128 MiB. Larger files are rejected with an explanation.
- The original FITS samples stay available for zooming. Each view is rendered from the selected source crop; zoom does not enlarge a reduced preview.
- Display bitmaps are capped at 4 million pixels so a full-resolution FITS image is never copied into a full-size Android bitmap.
- This is a focused viewer/parser experiment. It does not stack images and does not aim to implement every FITS extension or convention.

## Development and tests

```sh
./compile_all.sh
./gradlew :shared:desktopTest :fitsreader:testDebugUnitTest :imagefuncs:testDebugUnitTest
```

See [DEVELOPMENT.md](DEVELOPMENT.md) and [TESTING.md](TESTING.md) for the project layout and focused Gradle tasks.

## License

Application source is licensed under MIT; see [LICENSE](LICENSE).

# Screenshots
<img width="438" height="956" alt="image" src="https://github.com/user-attachments/assets/6b5f8c6b-3506-45b1-b11f-c1a51499a76e" />

<img width="1191" height="900" alt="image" src="https://github.com/user-attachments/assets/84ef4f1c-6b9c-4459-b214-1cc742bff949" />
