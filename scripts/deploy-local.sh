#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontend"
WEB_ROOT="${WEB_ROOT:-/var/www/huafu-crm}"

if [[ "${SKIP_BUILD:-0}" != "1" ]]; then
  "$ROOT_DIR/scripts/build-all.sh"
fi

echo "[deploy] frontend -> $WEB_ROOT"
sudo mkdir -p "$WEB_ROOT"
sudo rm -rf "$WEB_ROOT"/*
sudo cp -r "$FRONTEND_DIR/dist/"* "$WEB_ROOT/"

echo "[deploy] restart backend services"
"$ROOT_DIR/scripts/stop-services.sh"
"$ROOT_DIR/scripts/start-services.sh"

echo "[deploy] done"
