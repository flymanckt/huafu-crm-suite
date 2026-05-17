#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

run_docker() {
  if docker ps >/dev/null 2>&1; then
    docker "$@"
  else
    sg docker -c "docker $*"
  fi
}

run_compose() {
  if docker ps >/dev/null 2>&1; then
    docker compose -f docker-compose-dev.yml "$@"
  else
    sg docker -c "docker compose -f docker-compose-dev.yml $*"
  fi
}

echo "[1/6] Java/Maven/Docker prerequisites"
java -version
mvn -version
run_docker --version
run_compose version

echo "[2/6] Start PostgreSQL"
run_compose up -d postgres
run_compose ps postgres

echo "[3/6] Static structure verification"
python3 scripts/verify_structure.py

echo "[4/6] Full Maven unit/build verification"
mvn -q clean verify -DskipTests=false

echo "[5/6] PostgreSQL integration tests: Customer + WeCom E2E"
mvn -q -pl huafu-crm-customer -am -Dtest=CustomerServicePostgresIT -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test
mvn -q -pl huafu-crm-wecom -am -Dtest=WeComEndToEndPostgresIT -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test

echo "[6/6] Flyway migration status and Surefire totals"
FLYWAY_SQL="select version, description, success from flyway_schema_history order by installed_rank;"
if docker ps >/dev/null 2>&1; then
  docker exec huafu-crm-postgres psql -U huafu -d huafu_crm -c "$FLYWAY_SQL"
else
  sg docker -c "docker exec huafu-crm-postgres psql -U huafu -d huafu_crm -c \"$FLYWAY_SQL\""
fi
python3 - <<'PY'
from pathlib import Path
import xml.etree.ElementTree as ET
root = Path('.')
files = sorted(root.glob('*/target/surefire-reports/TEST-*.xml'))
tests = failures = errors = skipped = 0
for f in files:
    r = ET.parse(f).getroot()
    tests += int(r.attrib.get('tests', 0))
    failures += int(r.attrib.get('failures', 0))
    errors += int(r.attrib.get('errors', 0))
    skipped += int(r.attrib.get('skipped', 0))
print(f"surefire_files={len(files)} tests={tests} failures={failures} errors={errors} skipped={skipped}")
if failures or errors:
    raise SystemExit(1)
PY

echo "[PASS] Phase 0 round 3 verification passed"
