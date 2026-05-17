#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
MODULES = ["huafu-crm-common", "huafu-crm-gateway", "huafu-crm-customer", "huafu-crm-wecom", "huafu-crm-ai"]
REQUIRED = [
    "pom.xml", "README.md", "docker-compose-dev.yml", "scripts/sql/V1__init_schema.sql",
    "scripts/dev_round2_verify.sh",
    "huafu-crm-common/src/main/java/com/huafu/crm/common/api/Result.java",
    "huafu-crm-common/src/main/java/com/huafu/crm/common/config/MybatisPlusConfig.java",
    "huafu-crm-gateway/src/main/java/com/huafu/crm/gateway/GatewayApplication.java",
    "huafu-crm-customer/src/main/java/com/huafu/crm/customer/controller/CustomerController.java",
    "huafu-crm-customer/src/main/java/com/huafu/crm/customer/service/impl/CustomerServiceImpl.java",
    "huafu-crm-customer/src/main/resources/db/migration/V1__init_schema.sql",
    "huafu-crm-customer/src/test/java/com/huafu/crm/customer/service/CustomerServicePostgresIT.java",
    "huafu-crm-wecom/src/main/java/com/huafu/crm/wecom/controller/WeComReceiveController.java",
    "huafu-crm-wecom/src/main/java/com/huafu/crm/wecom/consumer/WeComMessageConsumer.java",
    "huafu-crm-ai/src/main/java/com/huafu/crm/ai/controller/AiParseController.java",
    "huafu-crm-ai/src/main/java/com/huafu/crm/ai/client/MockMiniMaxAiClient.java",
]

def fail(msg):
    print(f"[FAIL] {msg}")
    sys.exit(1)

missing = [p for p in REQUIRED if not (ROOT / p).exists()]
if missing:
    fail("missing required files: " + ", ".join(missing))

pom = (ROOT / "pom.xml").read_text(encoding="utf-8")
for m in MODULES:
    if f"<module>{m}</module>" not in pom:
        fail(f"parent pom missing module {m}")
    if not (ROOT / m / "pom.xml").exists():
        fail(f"module pom missing for {m}")
    if not (ROOT / m / "src/main/java").exists():
        fail(f"src/main/java missing for {m}")
    if not (ROOT / m / "src/main/resources").exists():
        fail(f"src/main/resources missing for {m}")

sql = (ROOT / "scripts/sql/V1__init_schema.sql").read_text(encoding="utf-8")
for bad in ["商机_count", "商情_count", "丢单_count"]:
    if bad in sql:
        fail(f"SQL contains forbidden Chinese physical column name {bad}")
for table in ["crm_daily_report", "crm_ai_parse_log", "crm_customer", "crm_lead", "crm_opportunity", "crm_lost_order"]:
    if not re.search(rf"CREATE TABLE IF NOT EXISTS\s+{table}\b", sql, re.I):
        fail(f"SQL missing table {table}")

customer_migration = (ROOT / "huafu-crm-customer/src/main/resources/db/migration/V1__init_schema.sql").read_text(encoding="utf-8")
if customer_migration != sql:
    fail("customer Flyway migration is not synchronized with scripts/sql/V1__init_schema.sql")

compose = (ROOT / "docker-compose-dev.yml").read_text(encoding="utf-8")
for phrase in ["healthcheck", "pg_isready -U huafu -d huafu_crm"]:
    if phrase not in compose:
        fail(f"docker-compose-dev.yml missing PostgreSQL healthcheck phrase: {phrase}")

readme = (ROOT / "README.md").read_text(encoding="utf-8")
for phrase in ["Flyway", "Customer PostgreSQL 集成测试", "sg docker -c", "scripts/dev_round2_verify.sh"]:
    if phrase not in readme:
        fail(f"README missing Phase 0 round 2 phrase: {phrase}")

print("[PASS] Phase 0 structure verification passed")
print(f"root={ROOT}")
print(f"modules={','.join(MODULES)}")
print(f"required_files_checked={len(REQUIRED)}")
