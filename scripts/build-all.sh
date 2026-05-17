#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"

echo "[build] backend: $BACKEND_DIR"
if [[ "${SKIP_TESTS:-1}" == "1" || "${SKIP_TESTS:-true}" == "true" ]]; then
  (cd "$BACKEND_DIR" && mvn package -DskipTests)
else
  (cd "$BACKEND_DIR" && mvn package)
fi

echo "[build] frontend: $FRONTEND_DIR"
if [[ ! -d "$FRONTEND_DIR/node_modules" ]]; then
  (cd "$FRONTEND_DIR" && npm install)
fi
(cd "$FRONTEND_DIR" && npm run build)

echo "[build] done"
