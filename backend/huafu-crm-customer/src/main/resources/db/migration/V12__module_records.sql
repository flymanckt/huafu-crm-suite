CREATE TABLE IF NOT EXISTS crm_module_record (
    id BIGINT PRIMARY KEY,
    module_key VARCHAR(64) NOT NULL,
    record_no VARCHAR(64),
    title VARCHAR(255),
    status VARCHAR(64),
    owner_name VARCHAR(64),
    record_date DATE,
    payload_json TEXT,
    remark TEXT,
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_crm_module_record_module ON crm_module_record(module_key, deleted, updated_time DESC);
CREATE INDEX IF NOT EXISTS idx_crm_module_record_status ON crm_module_record(module_key, status);
