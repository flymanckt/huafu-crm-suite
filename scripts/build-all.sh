#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"

check_node_version() {
  if ! command -v node >/dev/null 2>&1; then
    echo "[build] Node.js 未安装。请安装 Node.js 22 后重试。" >&2
    exit 1
  fi
  local version major minor
  version="$(node -v | sed 's/^v//')"
  major="$(echo "$version" | cut -d. -f1)"
  minor="$(echo "$version" | cut -d. -f2)"
  if [[ "$major" -lt 20 || "$major" -eq 21 || ( "$major" -eq 20 && "$minor" -lt 19 ) || ( "$major" -eq 22 && "$minor" -lt 12 ) ]]; then
    echo "[build] 当前 Node.js 版本是 v$version，前端构建要求 Node.js >=20.19 或 >=22.12。建议安装 Node.js 22。" >&2
    exit 1
  fi
}

echo "[build] backend: $BACKEND_DIR"
if [[ "${SKIP_TESTS:-1}" == "1" || "${SKIP_TESTS:-true}" == "true" ]]; then
  (cd "$BACKEND_DIR" && mvn package -DskipTests)
else
  (cd "$BACKEND_DIR" && mvn package)
fi

echo "[build] frontend: $FRONTEND_DIR"
check_node_version
if [[ ! -d "$FRONTEND_DIR/node_modules" ]]; then
  (cd "$FRONTEND_DIR" && npm install)
fi
(cd "$FRONTEND_DIR" && npm run build)

echo "[build] done"
