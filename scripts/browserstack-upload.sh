#!/usr/bin/env bash
# Uploads an app to BrowserStack App Automate and prints its bs:// id.
#
# Usage:   scripts/browserstack-upload.sh <android|ios> <path-to-apk-or-ipa>
# Needs:   BROWSERSTACK_USERNAME, BROWSERSTACK_ACCESS_KEY (env vars / CI secrets)
# Output:  bs://<hash> on stdout; in GitHub Actions also exported as BROWSERSTACK_APP_ANDROID / _IOS
#
# custom_id lets BrowserStack de-duplicate: re-uploading the same build is cheap and the id is stable.
# iOS needs the REAL-DEVICE .ipa (iOS.RealDevice...ipa), not the simulator .zip.
set -euo pipefail

PLATFORM="${1:?platform (android|ios) required}"
APP_FILE="${2:?path to .apk/.ipa required}"
: "${BROWSERSTACK_USERNAME:?BROWSERSTACK_USERNAME is not set}"
: "${BROWSERSTACK_ACCESS_KEY:?BROWSERSTACK_ACCESS_KEY is not set}"
[ -f "$APP_FILE" ] || { echo "App file not found: $APP_FILE" >&2; exit 1; }

RESPONSE=$(curl -sS -u "$BROWSERSTACK_USERNAME:$BROWSERSTACK_ACCESS_KEY" \
  -X POST "https://api-cloud.browserstack.com/app-automate/upload" \
  -F "file=@${APP_FILE}" \
  -F "custom_id=SauceLabsSample-${PLATFORM}")

APP_URL=$(echo "$RESPONSE" | sed -n 's/.*"app_url"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
if [ -z "$APP_URL" ]; then
  echo "Upload failed: $RESPONSE" >&2
  exit 1
fi

VAR="BROWSERSTACK_APP_$(echo "$PLATFORM" | tr '[:lower:]' '[:upper:]')"
if [ -n "${GITHUB_ENV:-}" ]; then
  echo "${VAR}=${APP_URL}" >> "$GITHUB_ENV"
fi
echo "$APP_URL"
