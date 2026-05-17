#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

compose() {
  if docker ps >/dev/null 2>&1; then
    docker compose -f docker-compose-dev.yml "$@"
  else
    sg docker -c "docker compose -f docker-compose-dev.yml $*"
  fi
}

docker_cmd() {
  if docker ps >/dev/null 2>&1; then
    docker "$@"
  else
    sg docker -c "docker $*"
  fi
}

echo "[round2] Starting PostgreSQL..."
compose up -d postgres

echo "[round2] Waiting for PostgreSQL healthcheck..."
for i in $(seq 1 60); do
  status="$(docker_cmd inspect --format='{{.State.Health.Status}}' huafu-crm-postgres 2>/dev/null || true)"
  if [[ "$status" == "healthy" ]]; then
    echo "[round2] PostgreSQL is healthy."
    break
  fi
  if [[ "$i" == "60" ]]; then
    echo "[round2] PostgreSQL did not become healthy; last status: ${status:-unknown}" >&2
    compose ps postgres || true
    exit 1
  fi
  sleep 2
done

echo "[round2] Running structure verification..."
python3 scripts/verify_structure.py

echo "[round2] Running full Maven unit verification..."
mvn -q clean verify -DskipTests=false

echo "[round2] Running customer PostgreSQL integration tests..."
mvn -q -pl huafu-crm-customer -am -Dtest=CustomerServicePostgresIT -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test

echo "[round2] Docker PostgreSQL status:"
compose ps postgres

echo "[round2] Verification completed successfully."
