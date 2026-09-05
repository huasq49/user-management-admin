#!/usr/bin/env bash
# No root installation, SDK downloads, or automatic license acceptance.
set -u
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MODULE="${1:-admin-android}"
case "$MODULE" in admin-android|reviewer-android) ;; *) echo "Invalid module" >&2; exit 2;; esac
command -v gradle >/dev/null || { echo "Install Gradle 8.9 and JDK 17 first" >&2; exit 1; }
[ -n "${ANDROID_HOME:-}" ] || { echo "Set ANDROID_HOME to Android SDK 35" >&2; exit 1; }
exec gradle -p "$ROOT/$MODULE" --no-daemon --max-workers=2 assembleDebug
