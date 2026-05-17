-- Huafu Fashion CRM Phase 0 schema, PostgreSQL dialect.
-- Physical column names are English. Chinese business meanings are kept in COMMENT statements only.

CREATE TABLE IF NOT EXISTS crm_customer (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    name VARCHAR(200) NOT NULL,
    code VARCHAR(64),
    industry VARCHAR(100),
    contact_name VARCHAR(100),
    contact_phone VARCHAR(50),
    owner_user_id VARCHAR(64),
    public_pool BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE crm_customer IS 'CRM客户主表';
COMMENT ON COLUMN crm_customer.public_pool IS '是否公海客户';

CREATE TABLE IF NOT EXISTS crm_lead (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    customer_id BIGINT,
    source VARCHAR(100),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'NEW',
    owner_user_id VARCHAR(64),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE crm_lead IS '线索表';

CREATE TABLE IF NOT EXISTS crm_opportunity (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    customer_id BIGINT,
    lead_id BIGINT,
    name VARCHAR(200) NOT NULL,
    stage VARCHAR(64) NOT NULL DEFAULT 'DISCOVERY',
    expected_amount NUMERIC(18,2),
    expected_close_date DATE,
    owner_user_id VARCHAR(64),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE crm_opportunity IS '商机表';

CREATE TABLE IF NOT EXISTS crm_lost_order (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    customer_id BIGINT,
    opportunity_id BIGINT,
    reason VARCHAR(500),
    competitor VARCHAR(200),
    amount NUMERIC(18,2),
    owner_user_id VARCHAR(64),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE crm_lost_order IS '丢单记录表';

CREATE TABLE IF NOT EXISTS crm_daily_report (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    user_id VARCHAR(64) NOT NULL,
    report_date DATE NOT NULL,
    raw_content TEXT NOT NULL,
    opportunity_count INTEGER NOT NULL DEFAULT 0,
    market_intelligence_count INTEGER NOT NULL DEFAULT 0,
    lost_order_count INTEGER NOT NULL DEFAULT 0,
    ai_parse_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE crm_daily_report IS '销售日报表';
COMMENT ON COLUMN crm_daily_report.opportunity_count IS '商机数量';
COMMENT ON COLUMN crm_daily_report.market_intelligence_count IS '商情数量';
COMMENT ON COLUMN crm_daily_report.lost_order_count IS '丢单数量';

CREATE TABLE IF NOT EXISTS crm_ai_parse_log (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    source_type VARCHAR(64) NOT NULL,
    source_id BIGINT,
    provider VARCHAR(64) NOT NULL DEFAULT 'MOCK_MINIMAX',
    request_payload TEXT,
    response_payload TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE crm_ai_parse_log IS 'AI解析日志表';

CREATE INDEX IF NOT EXISTS idx_crm_customer_tenant_name ON crm_customer(tenant_id, name);
CREATE INDEX IF NOT EXISTS idx_crm_lead_tenant_status ON crm_lead(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_crm_opportunity_tenant_stage ON crm_opportunity(tenant_id, stage);
CREATE INDEX IF NOT EXISTS idx_crm_daily_report_user_date ON crm_daily_report(tenant_id, user_id, report_date);
