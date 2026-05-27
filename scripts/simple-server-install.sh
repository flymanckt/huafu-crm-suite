#!/usr/bin/env bash
set -euo pipefail

PACKAGE_PATH="${1:-}"
APP_DIR="${2:-/opt/huafu-crm-suite}"
SERVER_NAME="${3:-_}"
RUN_USER="${SUDO_USER:-$(whoami)}"
ENV_DIR="$APP_DIR/config"
ENV_FILE="$ENV_DIR/huafu-crm.env"
WEB_ROOT="$APP_DIR/frontend/dist"

if [[ -z "$PACKAGE_PATH" || ! -f "$PACKAGE_PATH" ]]; then
  echo "[server-install] 找不到部署包: $PACKAGE_PATH" >&2
  exit 1
fi

random_text() {
  local len="${1:-32}"
  if command -v openssl >/dev/null 2>&1; then
    openssl rand -hex "$(((len + 1) / 2))" | cut -c "1-$len"
  else
    date +%s%N | sha256sum | head -c "$len"
  fi
}

echo "[server-install] 1/7 安装基础软件"
export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y openjdk-21-jre-headless nginx postgresql postgresql-contrib curl procps psmisc openssl

echo "[server-install] 2/7 准备目录: $APP_DIR"
mkdir -p "$APP_DIR" "$ENV_DIR" "$APP_DIR/logs"

DB_PASSWORD="$(random_text 32)"
JWT_SECRET="$(random_text 48)"
if [[ -f "$ENV_FILE" ]]; then
  # 保留已有密钥和数据库密码，避免重复部署后登录全部失效。
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  DB_PASSWORD="${CRM_DB_PASSWORD:-$DB_PASSWORD}"
  JWT_SECRET="${CRM_JWT_SECRET:-$JWT_SECRET}"
fi

echo "[server-install] 3/7 初始化 PostgreSQL"
systemctl enable --now postgresql
sudo -u postgres psql -v ON_ERROR_STOP=1 <<SQL
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'huafu') THEN
    CREATE ROLE huafu LOGIN PASSWORD '$DB_PASSWORD';
  ELSE
    ALTER ROLE huafu WITH LOGIN PASSWORD '$DB_PASSWORD';
  END IF;
END
\$\$;
SQL
if ! sudo -u postgres psql -tAc "SELECT 1 FROM pg_database WHERE datname='huafu_crm'" | grep -q 1; then
  sudo -u postgres createdb -O huafu huafu_crm
fi

echo "[server-install] 4/7 发布程序文件"
find "$APP_DIR" -mindepth 1 -maxdepth 1 \
  ! -name config \
  ! -name logs \
  -exec rm -rf {} +
tar -xzf "$PACKAGE_PATH" -C "$APP_DIR"
chmod +x "$APP_DIR/scripts/"*.sh
chown -R "$RUN_USER:$RUN_USER" "$APP_DIR"

cat > "$ENV_FILE" <<EOF
CRM_DB_URL=jdbc:postgresql://127.0.0.1:5432/huafu_crm
CRM_DB_USERNAME=huafu
CRM_DB_PASSWORD=$DB_PASSWORD
CRM_JWT_SECRET=$JWT_SECRET
CRM_GATEWAY_ADDRESS=0.0.0.0
CRM_SERVICE_ADDRESS=127.0.0.1
LD_LIBRARY_PATH=$APP_DIR/backend/lib
EOF
chmod 600 "$ENV_FILE"
chown "$RUN_USER:$RUN_USER" "$ENV_FILE"

echo "[server-install] 5/7 配置 Nginx"
cat > /etc/nginx/sites-available/huafu-crm <<EOF
server {
    listen 80 default_server;
    listen [::]:80 default_server;
    server_name $SERVER_NAME;

    root $WEB_ROOT;
    index index.html;

    location / {
        try_files \$uri \$uri/ /index.html;
    }

    location ^~ /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host \$http_host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_read_timeout 180s;
    }
}
EOF
rm -f /etc/nginx/sites-enabled/default
ln -sf /etc/nginx/sites-available/huafu-crm /etc/nginx/sites-enabled/huafu-crm
nginx -t
systemctl enable --now nginx
systemctl reload nginx

echo "[server-install] 6/7 配置并启动 CRM 服务"
cat > /etc/systemd/system/huafu-crm-suite.service <<EOF
[Unit]
Description=Huafu CRM Suite
After=network.target postgresql.service

[Service]
Type=forking
User=$RUN_USER
WorkingDirectory=$APP_DIR
EnvironmentFile=$ENV_FILE
ExecStart=$APP_DIR/scripts/start-services.sh
ExecStop=$APP_DIR/scripts/stop-services.sh
RemainAfterExit=yes
Restart=no

[Install]
WantedBy=multi-user.target
EOF
systemctl daemon-reload
systemctl enable huafu-crm-suite
systemctl restart huafu-crm-suite

echo "[server-install] 7/7 健康检查"
sleep 12
curl -fsS http://127.0.0.1:8080/actuator/health
echo
curl -fsS -I http://127.0.0.1/ | head -n 1
echo "[server-install] 部署完成"
