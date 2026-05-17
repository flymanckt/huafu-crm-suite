#!/usr/bin/env bash
# =====================================================================
# 重新注册所有华孚CRM systemd服务（修正路径）
# 用途：修复 /home/hermes/workspace → 正确路径
# =====================================================================
set -euo pipefail

BACKEND_DIR="/home/kent/.hermes/profiles/dev/workspace/huafu-crm"
SYSTEMD_DIR="/etc/systemd/system"
SERVICES=(
  "huafu-crm-gateway:huafu-crm-gateway-0.0.1-SNAPSHOT.jar:8080"
  "huafu-crm-customer:huafu-crm-customer-0.0.1-SNAPSHOT.jar:8081"
  "huafu-crm-opportunity:huafu-crm-opportunity-0.0.1-SNAPSHOT.jar:8082"
  "huafu-crm-ai:huafu-crm-ai-0.0.1-SNAPSHOT-exec.jar:8085"
  "huafu-crm-performance:huafu-crm-performance-0.0.1-SNAPSHOT.jar:8083"
  "huafu-crm-target:huafu-crm-target-0.0.1-SNAPSHOT.jar:8084"
  "huafu-crm-wecom:huafu-crm-wecom-0.0.1-SNAPSHOT.jar:8086"
)

gen_service() {
  local svc="$1" jar="$2" port="$3"
  cat > "/tmp/${svc}.service" << EOF
[Unit]
Description=Huafu CRM ${svc#huafu-crm-}
After=network-online.target docker.service
Wants=network-online.target docker.service

[Service]
Type=simple
User=kent
Group=kent
WorkingDirectory=$BACKEND_DIR
Environment=JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
Environment=SPRING_PROFILES_ACTIVE=default
ExecStart=/usr/bin/java -jar $BACKEND_DIR/${jar%/*}/target/$jar
Restart=on-failure
RestartSec=5
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
EOF
}

for entry in "${SERVICES[@]}"; do
  IFS=':' read -r svc jar port <<< "$entry"
  echo "处理 $svc (port=$port)"
  gen_service "$svc" "$jar" "$port"
  sudo cp "/tmp/${svc}.service" "$SYSTEMD_DIR/${svc}.service"
  sudo systemctl daemon-reload
  sudo systemctl enable "$svc"
done

echo "所有服务已重新注册，运行 'sudo systemctl daemon-reload && sudo systemctl restart huafu-crm-gateway' 使其生效"
