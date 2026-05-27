#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PACKAGE_NAME="huafu-crm-suite-$(date +%Y%m%d%H%M%S).tar.gz"
PACKAGE_PATH="/tmp/$PACKAGE_NAME"

prompt_if_empty() {
  local var_name="$1"
  local prompt_text="$2"
  local default_value="${3:-}"
  local current_value="${!var_name:-}"
  if [[ -n "$current_value" ]]; then
    return
  fi
  if [[ -n "$default_value" ]]; then
    read -r -p "$prompt_text [$default_value]: " current_value
    current_value="${current_value:-$default_value}"
  else
    read -r -p "$prompt_text: " current_value
  fi
  printf -v "$var_name" '%s' "$current_value"
}

prompt_if_empty BASTION_HOST "堡垒机地址，例如 user@bastion.company.com"
prompt_if_empty SERVER_HOST "CRM服务器地址，例如 deploy@10.0.0.12"
prompt_if_empty SERVER_PATH "服务器部署目录" "/opt/huafu-crm-suite"
prompt_if_empty SERVER_NAME "访问域名/IP，暂时没有域名就填 _" "_"

if [[ -z "${BASTION_HOST:-}" || -z "${SERVER_HOST:-}" ]]; then
  echo "[simple-deploy] 堡垒机地址和CRM服务器地址不能为空" >&2
  exit 1
fi

echo "[simple-deploy] 1/5 构建前后端"
"$ROOT_DIR/scripts/build-all.sh"

echo "[simple-deploy] 2/5 打包部署文件: $PACKAGE_PATH"
tar -czf "$PACKAGE_PATH" -C "$ROOT_DIR" \
  --exclude='.git' \
  --exclude='frontend/node_modules' \
  --exclude='logs' \
  --exclude='backups' \
  --exclude='*.log' \
  .

echo "[simple-deploy] 3/5 上传到服务器"
scp -J "$BASTION_HOST" "$PACKAGE_PATH" "$SERVER_HOST:/tmp/$PACKAGE_NAME"
scp -J "$BASTION_HOST" "$ROOT_DIR/scripts/simple-server-install.sh" "$SERVER_HOST:/tmp/huafu-crm-server-install.sh"

echo "[simple-deploy] 4/5 服务器自动安装/发布/重启"
ssh -J "$BASTION_HOST" "$SERVER_HOST" "sudo bash /tmp/huafu-crm-server-install.sh /tmp/$PACKAGE_NAME '$SERVER_PATH' '$SERVER_NAME'"

echo "[simple-deploy] 5/5 完成"
echo
echo "访问地址："
if [[ "$SERVER_NAME" == "_" ]]; then
  echo "  http://服务器IP/"
else
  echo "  http://$SERVER_NAME/"
fi
echo
echo "如果页面还是旧的，请在浏览器按 Ctrl + F5 强制刷新。"
