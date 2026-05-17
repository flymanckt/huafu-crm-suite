-- V2026.05.15.03: 客户多SAP主数据与SAP组织关联
-- 2026-05-15

CREATE TABLE IF NOT EXISTS crm_customer_sap_info (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    tenant_id BIGINT DEFAULT 1,
    customer_id BIGINT NOT NULL,
    sap_code VARCHAR(64) NOT NULL,
    account_group VARCHAR(64),
    country_code VARCHAR(16),
    company_code VARCHAR(64),
    sales_org VARCHAR(64),
    distribution_channel VARCHAR(64),
    division VARCHAR(64),
    description VARCHAR(512),
    is_default SMALLINT DEFAULT 0,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    version INT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_customer_sap_info_customer ON crm_customer_sap_info(customer_id, deleted);
CREATE UNIQUE INDEX IF NOT EXISTS uk_customer_sap_info_code ON crm_customer_sap_info(customer_id, sap_code) WHERE deleted = 0;

INSERT INTO crm_customer_sap_info (tenant_id, customer_id, sap_code, country_code, company_code, sales_org, is_default)
SELECT tenant_id, id, sap_customer_code, country_code, company_code, sales_group, 1
FROM crm_customer
WHERE sap_customer_code IS NOT NULL AND sap_customer_code <> ''
ON CONFLICT DO NOTHING;

ALTER TABLE crm_customer_sap_org ADD COLUMN IF NOT EXISTS sap_code VARCHAR(64);
CREATE INDEX IF NOT EXISTS idx_customer_sap_org_code ON crm_customer_sap_org(customer_id, sap_code);

UPDATE crm_customer_sap_org org
SET sap_code = c.sap_customer_code
FROM crm_customer c
WHERE org.customer_id = c.id
  AND (org.sap_code IS NULL OR org.sap_code = '')
  AND c.sap_customer_code IS NOT NULL
  AND c.sap_customer_code <> '';

COMMENT ON TABLE crm_customer_sap_info IS '客户SAP主数据，多SAP编号';
COMMENT ON COLUMN crm_customer_sap_info.sap_code IS 'SAP客户编号';
COMMENT ON COLUMN crm_customer_sap_org.sap_code IS '关联客户SAP编号';
