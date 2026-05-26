#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CLI_BIN="${WECOM_CLI_BIN:-$HOME/.npm-global/bin/wecom-cli}"

if [[ ! -x "$CLI_BIN" ]]; then
  echo "[wecom] wecom-cli not found, installing @wecom/cli"
  npm install -g @wecom/cli
fi

echo "[wecom] initialize credentials from CRM config"
set +e
node "$ROOT_DIR/scripts/wecom-cli-init-from-crm.mjs"
init_status=$?
set -e

if [[ $init_status -ne 0 && $init_status -ne 3 ]]; then
  exit "$init_status"
fi

echo "[wecom] checking msg capability"
if "$CLI_BIN" msg --help >/tmp/wecom-msg-help.txt 2>/tmp/wecom-msg-help.err; then
  echo "[wecom] msg capability is available"
  cat /tmp/wecom-msg-help.txt
else
  echo "[wecom] msg capability is NOT available"
  cat /tmp/wecom-msg-help.err
  exit 3
fi
