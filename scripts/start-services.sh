#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
LOG_DIR="$ROOT_DIR/logs"
PID_DIR="$LOG_DIR/pids"
SERVICES="${SERVICES:-gateway customer opportunity performance target ai wecom}"

mkdir -p "$LOG_DIR" "$PID_DIR"

module_of() {
  case "$1" in
    gateway) echo huafu-crm-gateway ;;
    customer) echo huafu-crm-customer ;;
    opportunity) echo huafu-crm-opportunity ;;
    performance) echo huafu-crm-performance ;;
    target) echo huafu-crm-target ;;
    ai) echo huafu-crm-ai ;;
    wecom) echo huafu-crm-wecom ;;
    *) echo "" ;;
  esac
}

find_jar() {
  local module="$1"
  local target="$BACKEND_DIR/$module/target"
  local exec_jar
  exec_jar="$(find "$target" -maxdepth 1 -name "${module}-*-exec.jar" | sort | tail -n 1 || true)"
  if [[ -n "$exec_jar" ]]; then
    echo "$exec_jar"
    return
  fi
  find "$target" -maxdepth 1 -name "${module}-*.jar" ! -name "*.original" | sort | tail -n 1
}

for service in $SERVICES; do
  module="$(module_of "$service")"
  if [[ -z "$module" ]]; then
    echo "[start] unknown service: $service" >&2
    exit 1
  fi

  jar="$(find_jar "$module")"
  if [[ ! -f "$jar" ]]; then
    echo "[start] jar not found for $service ($module)" >&2
    exit 1
  fi

  log_file="$LOG_DIR/$service.log"
  echo "[start] $service -> $jar"
  nohup java -jar "$jar" > "$log_file" 2>&1 &
  echo $! > "$PID_DIR/$service.pid"
done

echo "[start] done"
