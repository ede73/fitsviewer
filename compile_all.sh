#!/bin/sh
set -eu
. ./android-sdk-env.sh
./gradlew :app:compileDebugSources :desktopApp:compileKotlinDesktop
