#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="$ROOT_DIR/logs"
PID_DIR="$LOG_DIR/pids"
SERVICES="${SERVICES:-gateway customer opportunity performance target ai wecom}"

port_of() {
  case "$1" in
    gateway) echo 8080 ;;
    customer) echo 8081 ;;
    opportunity) echo 8082 ;;
    performance) echo 8083 ;;
    target) echo 8084 ;;
    ai) echo 8085 ;;
    wecom) echo 8086 ;;
    *) echo "" ;;
  esac
}

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

stop_module_processes() {
  local module="$1"
  if [[ -z "$module" ]] || ! command -v pgrep >/dev/null 2>&1; then
    return
  fi
  local pids
  pids="$(pgrep -f "/${module}/target/${module}-.*\\.jar" || true)"
  if [[ -z "$pids" ]]; then
    return
  fi
  echo "[stop] module $module pids=$pids"
  for pid in $pids; do
    kill "$pid" 2>/dev/null || true
  done
}

for service in $SERVICES; do
  module="$(module_of "$service")"
  pid_file="$PID_DIR/$service.pid"
  if [[ -f "$pid_file" ]]; then
    pid="$(cat "$pid_file")"
    if [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null; then
      echo "[stop] $service pid=$pid"
      kill "$pid" 2>/dev/null || true
    fi
    rm -f "$pid_file"
  fi

  stop_module_processes "$module"

  port="$(port_of "$service")"
  if [[ -n "$port" ]] && command -v fuser >/dev/null 2>&1; then
    if fuser "$port/tcp" >/dev/null 2>&1; then
      echo "[stop] $service port=$port"
      fuser -k "$port/tcp" >/dev/null 2>&1 || true
    fi
  fi
done

echo "[stop] done"
