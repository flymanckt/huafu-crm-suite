-- Round 3: auditable WeCom callback ingest + daily report AI mock pipeline.
-- English physical names only; Chinese meanings stay in comments.

CREATE TABLE IF NOT EXISTS crm_wecom_message_log (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    from_user_id VARCHAR(128),
    to_user_id VARCHAR(128),
    msg_type VARCHAR(32),
    msg_id VARCHAR(128),
    raw_xml TEXT NOT NULL,
    content TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'RECEIVED',
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE crm_wecom_message_log IS '企业微信消息接收审计日志';

CREATE TABLE IF NOT EXISTS crm_daily_report (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    wecom_message_id BIGINT,
    user_id VARCHAR(128) NOT NULL,
    report_date DATE NOT NULL,
    raw_content TEXT NOT NULL,
    parsed_json JSONB,
    opportunity_count INTEGER NOT NULL DEFAULT 0,
    market_intelligence_count INTEGER NOT NULL DEFAULT 0,
    lost_order_count INTEGER NOT NULL DEFAULT 0,
    ai_parse_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE crm_daily_report IS '销售日报表';

ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS wecom_message_id BIGINT;
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS parsed_json JSONB;

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

CREATE TABLE IF NOT EXISTS crm_wecom_push_outbox (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    to_user_id VARCHAR(128) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'MOCK_SENT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE crm_wecom_push_outbox IS '企微确认推送出站记录（Phase 0 Mock）';

CREATE INDEX IF NOT EXISTS idx_crm_wecom_message_log_msg_id ON crm_wecom_message_log(msg_id);
CREATE INDEX IF NOT EXISTS idx_crm_daily_report_user_date ON crm_daily_report(tenant_id, user_id, report_date);
CREATE INDEX IF NOT EXISTS idx_crm_ai_parse_log_source ON crm_ai_parse_log(source_type, source_id);
CREATE INDEX IF NOT EXISTS idx_crm_wecom_push_outbox_user ON crm_wecom_push_outbox(to_user_id, status);
