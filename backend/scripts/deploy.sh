#!/usr/bin/env bash
# =====================================================================
# 华孚CRM 一键打包部署脚本
# 用法: ./scripts/deploy.sh [--skip-frontend]
# =====================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR"
FRONTEND_DIR="/home/kent/.hermes/profiles/dev/workspace/huafu-crm-frontend"
WEB_ROOT="/var/www/huafu-crm"
SKIP_FRONTEND=false

for arg in "$@"; do
  case $arg in
    --skip-frontend) SKIP_FRONTEND=true ;;
    --help)
      echo "用法: $0 [--skip-frontend]"
      echo "  --skip-frontend  仅部署后端，跳过前端构建"
      exit 0
      ;;
  esac
done

log() { echo "[$(date '+%H:%M:%S')] $1"; }
warn() { echo "[$(date '+%H:%M:%S')] WARNING: $1" >&2; }
die() { echo "[$(date '+%H:%M:%S')] ERROR: $1" >&2; exit 1; }

# =====================================================================
# 0. 前置检查
# =====================================================================
log "=== 前置检查 ==="
command -v mvn >/dev/null 2>&1 || die "Maven 未安装"
command -v npm >/dev/null 2>&1 || die "npm 未安装"
command -v rsync >/dev/null 2>&1 || die "rsync 未安装"
[ -d "$BACKEND_DIR" ] || die "后端目录不存在: $BACKEND_DIR"
[ -d "$FRONTEND_DIR" ] || die "前端目录不存在: $FRONTEND_DIR"
[ -d "$WEB_ROOT" ] || warn "Web根目录不存在，将被创建: $WEB_ROOT"

# =====================================================================
# 1. 停止旧服务（不中断部署）
# =====================================================================
log "=== 停止旧服务 ==="
for svc in huafu-crm-gateway huafu-crm-customer huafu-crm-opportunity \
           huafu-crm-ai huafu-crm-performance huafu-crm-target huafu-crm-wecom; do
  if systemctl is-active --quiet "$svc" 2>/dev/null; then
    systemctl stop "$svc" && log "已停止 $svc" || warn "停止 $svc 失败（可能未运行）"
  fi
done

# =====================================================================
# 2. 编译后端
# =====================================================================
log "=== 编译后端 (Maven) ==="
cd "$BACKEND_DIR"
mvn -q clean package -DskipTests=true -pl \
  huafu-crm-common,huafu-crm-gateway,huafu-crm-customer,huafu-crm-opportunity,\
huafu-crm-ai,huafu-crm-performance,huafu-crm-target,huafu-crm-wecom \
  -am

JAR_GATEWAY="$BACKEND_DIR/huafu-crm-gateway/target/huafu-crm-gateway-0.0.1-SNAPSHOT.jar"
JAR_CUSTOMER="$BACKEND_DIR/huafu-crm-customer/target/huafu-crm-customer-0.0.1-SNAPSHOT.jar"
JAR_OPPORTUNITY="$BACKEND_DIR/huafu-crm-opportunity/target/huafu-crm-opportunity-0.0.1-SNAPSHOT.jar"
JAR_AI="$BACKEND_DIR/huafu-crm-ai/target/huafu-crm-ai-0.0.1-SNAPSHOT-exec.jar"
JAR_PERF="$BACKEND_DIR/huafu-crm-performance/target/huafu-crm-performance-0.0.1-SNAPSHOT.jar"
JAR_TARGET="$BACKEND_DIR/huafu-crm-target/target/huafu-crm-target-0.0.1-SNAPSHOT.jar"
JAR_WECOM="$BACKEND_DIR/huafu-crm-wecom/target/huafu-crm-wecom-0.0.1-SNAPSHOT.jar"

for jar in "$JAR_GATEWAY" "$JAR_CUSTOMER" "$JAR_OPPORTUNITY"; do
  [ -f "$jar" ] || die "JAR未生成: $jar"
done
log "后端编译完成"

# =====================================================================
# 3. 编译前端
# =====================================================================
if [ "$SKIP_FRONTEND" = false ]; then
  log "=== 编译前端 (Vite) ==="
  cd "$FRONTEND_DIR"
  npm run build
  [ -d "$FRONTEND_DIR/dist" ] || die "前端dist目录未生成"
  log "前端编译完成"
else
  log "跳过前端构建（--skip-frontend）"
fi

# =====================================================================
# 4. 部署前端
# =====================================================================
if [ "$SKIP_FRONTEND" = false ]; then
  log "=== 部署前端到 $WEB_ROOT ==="
  mkdir -p "$WEB_ROOT"
  rsync -av --delete \
    --exclude='node_modules' \
    --exclude='.git' \
    --exclude='src' \
    "$FRONTEND_DIR/dist/" "$WEB_ROOT/"
  log "前端部署完成"
else
  log "跳过前端部署"
fi

# =====================================================================
# 5. 重启后端服务
# =====================================================================
log "=== 重启后端服务 ==="

restart_service() {
  local svc="$1"
  local jar="$2"
  local port="$3"
  local name="$4"
  log "启动 $name (port=$port)"
  systemctl start "$svc"
  # 等待服务就绪（最多30秒）
  for i in $(seq 1 30); do
    sleep 1
    if curl -sf "http://localhost:$port/actuator/health" >/dev/null 2>&1 || \
       curl -sf "http://localhost:$port/api/auth/health" >/dev/null 2>&1 2>/dev/null; then
      log "$name 启动就绪"
      return 0
    fi
  done
  warn "$name 启动可能未就绪，请检查日志: journalctl -u $svc -n 20"
}

restart_service "huafu-crm-customer"   "$JAR_CUSTOMER"   "8081" "Customer服务"
restart_service "huafu-crm-opportunity" "$JAR_OPPORTUNITY" "8082" "Opportunity服务"
restart_service "huafu-crm-ai"          "$JAR_AI"         "8085" "AI服务"
restart_service "huafu-crm-performance" "$JAR_PERF"      "8083" "Performance服务"
restart_service "huafu-crm-target"      "$JAR_TARGET"     "8084" "Target服务"
restart_service "huafu-crm-wecom"      "$JAR_WECOM"      "8086" "WeCom服务"
restart_service "huafu-crm-gateway"    "$JAR_GATEWAY"     "8080" "Gateway服务"

# =====================================================================
# 6. 重载Nginx
# =====================================================================
log "=== 重载Nginx ==="
nginx -t && systemctl reload nginx && log "Nginx重载成功" || die "Nginx配置有问题"

# =====================================================================
# 7. 最终验证
# =====================================================================
log "=== 最终验证 ==="
FAILED=0

check_api() {
  local path="$1"
  local name="$2"
  if curl -sf "http://localhost:8080$path" -o /dev/null 2>/dev/null; then
    log "$name ✓"
  else
    warn "$name 失败"
    FAILED=1
  fi
}

check_api "/api/auth/captcha"        "Auth接口"
check_api "/api/customer/customer/page"  "Customer接口"
check_api "/api/opportunity/opportunity/page" "Opportunity接口"

if [ $FAILED -eq 0 ]; then
  log "=== 部署完成 ✓ ==="
  log "Gateway:  http://localhost:8080"
  log "前端:     http://localhost (nginx)"
else
  warn "部分接口验证失败，请检查日志"
fi
