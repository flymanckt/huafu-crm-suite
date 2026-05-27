#!/usr/bin/env bash
set -euo pipefail

# 在服务器上执行的本地一键部署脚本。
# 用法：
#   1. 把整个 huafu-crm-suite 文件夹上传到服务器
#   2. cd huafu-crm-suite
#   3. sudo bash scripts/simple-server-deploy.sh

if [[ "${EUID:-$(id -u)}" -ne 0 ]]; then
  exec sudo -E bash "$0" "$@"
fi

SOURCE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP_DIR="${APP_DIR:-/opt/huafu-crm-suite}"
SERVER_NAME="${SERVER_NAME:-_}"
RUN_USER="${SUDO_USER:-deploy}"
ENV_DIR="$APP_DIR/config"
ENV_FILE="$ENV_DIR/huafu-crm.env"
WEB_ROOT="$APP_DIR/frontend/dist"

random_text() {
  local len="${1:-32}"
  if command -v openssl >/dev/null 2>&1; then
    openssl rand -hex "$(((len + 1) / 2))" | cut -c "1-$len"
  else
    date +%s%N | sha256sum | head -c "$len"
  fi
}

install_nodejs_22() {
  local major=0
  local minor=0
  if command -v node >/dev/null 2>&1; then
    major="$(node -v | sed 's/^v//' | cut -d. -f1)"
    minor="$(node -v | sed 's/^v//' | cut -d. -f2)"
  fi
  if [[ "$major" -gt 22 || ( "$major" -eq 22 && "$minor" -ge 12 ) ]]; then
    echo "[server-deploy] Node.js $(node -v) 已满足要求"
    return
  fi
  echo "[server-deploy] 安装 Node.js 22，当前版本：$(node -v 2>/dev/null || echo 未安装)"
  apt-get remove -y nodejs npm nodejs-doc libnode-dev libnode72 || true
  dpkg --remove --force-depends nodejs npm nodejs-doc libnode-dev libnode72 2>/dev/null || true
  apt-get -f install -y || true
  apt-get autoremove -y || true
  apt-get install -y ca-certificates curl gnupg
  curl -fsSL https://deb.nodesource.com/setup_22.x | bash -
  apt-get install -y nodejs
  node -v
  npm -v
}

copy_to_app_dir() {
  if [[ "$SOURCE_DIR" == "$APP_DIR" ]]; then
    return
  fi
  echo "[server-deploy] 复制项目到 $APP_DIR"
  mkdir -p "$APP_DIR"
  tar -C "$SOURCE_DIR" \
    --exclude='.git' \
    --exclude='frontend/node_modules' \
    --exclude='frontend/dist' \
    --exclude='logs' \
    --exclude='backups' \
    -czf /tmp/huafu-crm-suite-current.tar.gz .
  find "$APP_DIR" -mindepth 1 -maxdepth 1 \
    ! -name config \
    ! -name logs \
    -exec rm -rf {} +
  tar -xzf /tmp/huafu-crm-suite-current.tar.gz -C "$APP_DIR"
}

echo "[server-deploy] 1/8 安装基础软件"
export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y openjdk-21-jdk maven nginx postgresql postgresql-contrib curl procps psmisc openssl
install_nodejs_22

echo "[server-deploy] 2/8 准备项目目录"
copy_to_app_dir
mkdir -p "$ENV_DIR" "$APP_DIR/logs"
chown -R "$RUN_USER:$RUN_USER" "$APP_DIR"
chmod +x "$APP_DIR/scripts/"*.sh

DB_PASSWORD="$(random_text 32)"
JWT_SECRET="$(random_text 48)"
if [[ -f "$ENV_FILE" ]]; then
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  DB_PASSWORD="${CRM_DB_PASSWORD:-$DB_PASSWORD}"
  JWT_SECRET="${CRM_JWT_SECRET:-$JWT_SECRET}"
fi

echo "[server-deploy] 3/8 初始化数据库"
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

echo "[server-deploy] 4/8 构建后端和前端"
sudo -u "$RUN_USER" bash -lc "cd '$APP_DIR/backend' && mvn clean package -DskipTests"
sudo -u "$RUN_USER" bash -lc "cd '$APP_DIR/frontend' && npm ci && npm run build"

echo "[server-deploy] 5/8 配置 Nginx"
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

echo "[server-deploy] 6/8 配置 systemd"
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

echo "[server-deploy] 7/8 启动 CRM"
systemctl restart huafu-crm-suite

echo "[server-deploy] 8/8 健康检查"
sleep 15
curl -fsS http://127.0.0.1:8080/actuator/health
echo
curl -fsS -I http://127.0.0.1/ | head -n 1
echo
echo "[server-deploy] 部署完成"
echo "访问地址：http://服务器IP/"
echo "如果配置了域名： http://$SERVER_NAME/"
