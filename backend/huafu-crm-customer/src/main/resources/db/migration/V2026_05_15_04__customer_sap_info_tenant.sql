-- V2026.05.15.04: SAP主数据补齐租户字段
-- 2026-05-15

ALTER TABLE crm_customer_sap_info ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;

UPDATE crm_customer_sap_info info
SET tenant_id = COALESCE(c.tenant_id, 1)
FROM crm_customer c
WHERE info.customer_id = c.id
  AND (info.tenant_id IS NULL OR info.tenant_id = 1);

CREATE INDEX IF NOT EXISTS idx_customer_sap_info_tenant ON crm_customer_sap_info(tenant_id, customer_id, deleted);
