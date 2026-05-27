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

check_port() {
  local port="$1"
  if command -v ss >/dev/null 2>&1; then
    ss -lnt | awk '{print $4}' | grep -Eq "(^|:)$port$"
    return
  fi
  if command -v netstat >/dev/null 2>&1; then
    netstat -lnt | awk '{print $4}' | grep -Eq "(^|:)$port$"
    return
  fi
  curl -fsS --max-time 2 "http://127.0.0.1:$port/actuator/health" >/dev/null 2>&1
}

echo "== systemd =="
systemctl --no-pager --full status huafu-crm-suite.service || true
echo

echo "== nginx =="
systemctl --no-pager --full status nginx.service || true
nginx -t || true
echo

echo "== ports =="
for service in $SERVICES; do
  port="$(port_of "$service")"
  pid_file="$PID_DIR/$service.pid"
  pid="-"
  if [[ -f "$pid_file" ]]; then
    pid="$(cat "$pid_file" 2>/dev/null || echo "-")"
  fi
  if [[ -n "$port" ]] && check_port "$port"; then
    printf "%-12s port=%s pid=%s status=LISTEN\n" "$service" "$port" "$pid"
  else
    printf "%-12s port=%s pid=%s status=DOWN\n" "$service" "$port" "$pid"
  fi
done
echo

echo "== api checks =="
curl -i --max-time 5 http://127.0.0.1:8080/actuator/health || true
echo
if check_port 8081; then
  echo "customer port 8081 is listening"
else
  echo "customer port 8081 is down"
fi
echo
curl -i --max-time 5 -X POST http://127.0.0.1/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Huafu2026!"}' || true
echo

echo "== recent logs =="
for service in gateway customer opportunity performance target ai wecom; do
  log_file="$LOG_DIR/$service.log"
  if [[ -f "$log_file" ]]; then
    echo
    echo "--- $service.log ---"
    tail -n 80 "$log_file" || true
  fi
done
