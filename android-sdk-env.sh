# Source this file from project scripts before invoking Gradle.
if [ -z "${ANDROID_HOME:-}" ]; then
    if [ -n "${ANDROID_SDK_ROOT:-}" ]; then
        ANDROID_HOME=$ANDROID_SDK_ROOT
    elif [ -f local.properties ]; then
        ANDROID_HOME=$(sed -n 's/^sdk\.dir=//p' local.properties | head -n 1 | tr -d '\r')
    elif [ -d "$HOME/Library/Android/sdk" ]; then
        ANDROID_HOME="$HOME/Library/Android/sdk"
    fi
fi

if [ -n "${ANDROID_HOME:-}" ]; then
    export ANDROID_HOME
fi
