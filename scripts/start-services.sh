#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
LOG_DIR="$ROOT_DIR/logs"
PID_DIR="$LOG_DIR/pids"
SERVICES="${SERVICES:-gateway customer opportunity performance target ai wecom}"
SAP_JCO_LIB_DIR="${SAP_JCO_LIB_DIR:-$BACKEND_DIR/lib}"
SAP_JCO_NATIVE_DIR="${SAP_JCO_NATIVE_DIR:-$SAP_JCO_LIB_DIR}"

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

stop_module_processes() {
  local module="$1"
  if ! command -v pgrep >/dev/null 2>&1; then
    return
  fi
  local pids
  pids="$(pgrep -f "/${module}/target/${module}-.*\\.jar" || true)"
  if [[ -z "$pids" ]]; then
    return
  fi
  echo "[start] stopping existing module processes for $module: $pids"
  for pid in $pids; do
    kill "$pid" 2>/dev/null || true
  done
  sleep 1
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

start_java_service() {
  local service="$1"
  local jar="$2"
  local log_file="$3"
  local -a java_cmd=(java)
  if [[ -n "${JAVA_OPTS:-}" ]]; then
    # shellcheck disable=SC2206
    java_cmd+=(${JAVA_OPTS})
  fi
  if [[ "$service" == "customer" && -f "$SAP_JCO_LIB_DIR/sapjco3.jar" ]]; then
    echo "[start] customer SAP JCo lib detected: $SAP_JCO_LIB_DIR"
    nohup "${java_cmd[@]}" \
      -Djava.library.path="$SAP_JCO_NATIVE_DIR${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}" \
      -cp "$jar:$SAP_JCO_LIB_DIR/*" \
      org.springframework.boot.loader.launch.JarLauncher > "$log_file" 2>&1 &
  else
    nohup "${java_cmd[@]}" -jar "$jar" > "$log_file" 2>&1 &
  fi
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
  pid_file="$PID_DIR/$service.pid"
  port="$(port_of "$service")"
  if [[ -f "$pid_file" ]]; then
    old_pid="$(cat "$pid_file" 2>/dev/null || true)"
    if [[ -n "$old_pid" ]] && kill -0 "$old_pid" 2>/dev/null; then
      echo "[start] stopping existing $service pid=$old_pid"
      kill "$old_pid" 2>/dev/null || true
      sleep 1
    fi
    rm -f "$pid_file"
  fi
  stop_module_processes "$module"
  if [[ -n "$port" ]] && command -v fuser >/dev/null 2>&1 && fuser "$port/tcp" >/dev/null 2>&1; then
    echo "[start] releasing $service port=$port"
    fuser -k "$port/tcp" >/dev/null 2>&1 || true
    sleep 1
  fi

  echo "[start] $service -> $jar"
  start_java_service "$service" "$jar" "$log_file"
  echo $! > "$pid_file"
done

echo "[start] done"
