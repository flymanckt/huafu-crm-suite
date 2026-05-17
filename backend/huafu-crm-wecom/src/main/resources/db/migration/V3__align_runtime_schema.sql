-- Align runtime schema with current Java entities.
-- Physical names are English only; Chinese meanings stay in comments.

CREATE SEQUENCE IF NOT EXISTS crm_base_id_seq START WITH 10000;

-- customer domain
CREATE TABLE IF NOT EXISTS crm_customer (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq')
);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS customer_code VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS customer_name VARCHAR(256);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS customer_short_name VARCHAR(128);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS type SMALLINT;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS level SMALLINT;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS status SMALLINT DEFAULT 1;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS province VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS city VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS address VARCHAR(512);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS main_contact_name VARCHAR(128);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS main_contact_phone VARCHAR(32);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS main_contact_role VARCHAR(32);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS annual_revenue DECIMAL(18,2);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS credit_limit DECIMAL(18,2);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS tax_rate DECIMAL(5,4);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS payment_days INT;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS sap_customer_code VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS remark TEXT;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS last_visit_time TIMESTAMPTZ;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS owner_user_id BIGINT;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS public_pool_time TIMESTAMPTZ;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS created_by VARCHAR(64) DEFAULT '';
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS updated_by VARCHAR(64) DEFAULT '';
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS version INT DEFAULT 0;
COMMENT ON TABLE crm_customer IS '客户主表';

CREATE TABLE IF NOT EXISTS crm_customer_contact (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    customer_id BIGINT NOT NULL,
    contact_name VARCHAR(128) NOT NULL,
    phone VARCHAR(32),
    wechat VARCHAR(128),
    email VARCHAR(128),
    position VARCHAR(64),
    role_type SMALLINT,
    decision_level SMALLINT,
    is_main SMALLINT DEFAULT 0,
    birthday DATE,
    remark TEXT,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0
);
COMMENT ON TABLE crm_customer_contact IS '客户联系人表';

CREATE TABLE IF NOT EXISTS crm_customer_bundle (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    customer_id BIGINT NOT NULL,
    bundle_type SMALLINT NOT NULL,
    bundle_customer_id BIGINT NOT NULL,
    relation_remark VARCHAR(256),
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0
);
COMMENT ON TABLE crm_customer_bundle IS '客户关联表';

CREATE TABLE IF NOT EXISTS crm_public_pool_rule (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    rule_name VARCHAR(128) NOT NULL,
    condition_type SMALLINT,
    condition_days INT NOT NULL DEFAULT 90,
    action_type SMALLINT,
    target_user_id BIGINT,
    dept_id BIGINT,
    enabled SMALLINT DEFAULT 1,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0
);
COMMENT ON TABLE crm_public_pool_rule IS '公海规则表';

-- opportunity domain
CREATE TABLE IF NOT EXISTS crm_lead (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq')
);
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS lead_no VARCHAR(64);
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS lead_type SMALLINT;
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS customer_id BIGINT;
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS customer_name VARCHAR(256);
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS contact_name VARCHAR(128);
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(32);
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS province VARCHAR(64);
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS city VARCHAR(64);
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS product_name VARCHAR(256);
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS competitor_name VARCHAR(256);
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS competitor_price DECIMAL(18,2);
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS competitor_discount DECIMAL(5,4);
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS margin_rate DECIMAL(5,4);
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS source SMALLINT;
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS creator_user_id BIGINT;
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS handler_user_id BIGINT;
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS status SMALLINT DEFAULT 1;
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS convert_time TIMESTAMPTZ;
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS converted_opp_id BIGINT;
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS remark TEXT;
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS raw_text TEXT;
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS parsed_json JSONB;
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS created_by VARCHAR(64) DEFAULT '';
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS updated_by VARCHAR(64) DEFAULT '';
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS version INT DEFAULT 0;
COMMENT ON TABLE crm_lead IS '线索/商情表';

CREATE TABLE IF NOT EXISTS crm_opportunity (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq')
);
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS opp_no VARCHAR(64);
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS opportunity_name VARCHAR(256);
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS customer_id BIGINT;
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS handler_user_id BIGINT;
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS stage SMALLINT;
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS stage_update_time TIMESTAMPTZ;
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS product_name VARCHAR(256);
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS quantity DECIMAL(18,2);
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS unit VARCHAR(16);
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS estimated_amount DECIMAL(18,2);
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS expected_close_date DATE;
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS actual_close_date DATE;
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS win_probability SMALLINT;
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS lost_reason VARCHAR(512);
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS lost_type SMALLINT;
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS competitor_name VARCHAR(256);
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS quote_id BIGINT;
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS order_no VARCHAR(64);
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS remark TEXT;
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS source_lead_id BIGINT;
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS raw_text TEXT;
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS parsed_json JSONB;
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS created_by VARCHAR(64) DEFAULT '';
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS updated_by VARCHAR(64) DEFAULT '';
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS version INT DEFAULT 0;
COMMENT ON TABLE crm_opportunity IS '商机表';

CREATE TABLE IF NOT EXISTS crm_lost_order (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq')
);
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS lost_no VARCHAR(64);
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS opportunity_id BIGINT;
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS customer_id BIGINT;
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS lost_type SMALLINT;
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS competitor_name VARCHAR(256);
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS competitor_price DECIMAL(18,2);
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS our_price DECIMAL(18,2);
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS margin_diff DECIMAL(18,4);
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS competitor_discount DECIMAL(5,4);
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS our_discount DECIMAL(5,4);
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS reason_detail TEXT;
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS recovery_possible SMALLINT;
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS follow_up_date DATE;
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS handler_user_id BIGINT;
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS raw_text TEXT;
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS parsed_json JSONB;
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS created_by VARCHAR(64) DEFAULT '';
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS updated_by VARCHAR(64) DEFAULT '';
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_lost_order ADD COLUMN IF NOT EXISTS version INT DEFAULT 0;
COMMENT ON TABLE crm_lost_order IS '丢单记录表';

CREATE TABLE IF NOT EXISTS crm_quote (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    quote_no VARCHAR(64), customer_id BIGINT, opportunity_id BIGINT, contact_id BIGINT,
    contact_name VARCHAR(128), contact_phone VARCHAR(32), sales_user_id BIGINT,
    quote_date DATE, valid_date DATE, total_amount DECIMAL(18,2), discount_rate DECIMAL(5,4),
    final_amount DECIMAL(18,2), payment_terms VARCHAR(256), delivery_terms VARCHAR(256),
    status SMALLINT DEFAULT 1, sent_time TIMESTAMPTZ, confirmed_time TIMESTAMPTZ, remark TEXT,
    created_by VARCHAR(64) DEFAULT '', created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '', updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0, tenant_id BIGINT DEFAULT 1, version INT DEFAULT 0
);
COMMENT ON TABLE crm_quote IS '报价单表';

-- target domain
CREATE TABLE IF NOT EXISTS crm_target (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    target_no VARCHAR(64), target_year INT, target_month INT, dept_id BIGINT, user_id BIGINT,
    product_category VARCHAR(128), metric_type SMALLINT, target_value DECIMAL(18,2),
    created_by VARCHAR(64) DEFAULT '', created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '', updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0, tenant_id BIGINT DEFAULT 1, version INT DEFAULT 0
);
COMMENT ON TABLE crm_target IS '目标表';

CREATE TABLE IF NOT EXISTS crm_target_achieve (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    target_id BIGINT, achieve_year INT, achieve_month INT, achieve_value DECIMAL(18,2),
    achieve_rate DECIMAL(6,2), source SMALLINT, source_data_date DATE, synced_time TIMESTAMPTZ, remark TEXT,
    created_by VARCHAR(64) DEFAULT '', created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '', updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0, tenant_id BIGINT DEFAULT 1, version INT DEFAULT 0
);
COMMENT ON TABLE crm_target_achieve IS '目标达成表';

-- performance domain
CREATE TABLE IF NOT EXISTS crm_visit_record (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    visit_no VARCHAR(64), user_id BIGINT, customer_id BIGINT, customer_name VARCHAR(256),
    contact_id BIGINT, contact_name VARCHAR(128), visit_date DATE, visit_type SMALLINT,
    visit_purpose VARCHAR(256), visit_content TEXT, next_visit_plan DATE, is_new_customer SMALLINT DEFAULT 0,
    longitude DECIMAL(10,6), latitude DECIMAL(10,6), location_name VARCHAR(256), remark TEXT,
    created_by VARCHAR(64) DEFAULT '', created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '', updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0, tenant_id BIGINT DEFAULT 1, version INT DEFAULT 0
);
COMMENT ON TABLE crm_visit_record IS '拜访记录表';

CREATE TABLE IF NOT EXISTS crm_daily_report (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq')
);
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS report_no VARCHAR(64);
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS user_id BIGINT;
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS report_date DATE;
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS content_text TEXT;
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS content_html TEXT;
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS parse_status SMALLINT DEFAULT 0;
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS parsed_json JSONB;
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS opportunity_count INT DEFAULT 0;
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS market_intelligence_count INT DEFAULT 0;
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS lost_order_count INT DEFAULT 0;
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS parse_error VARCHAR(512);
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS parse_time TIMESTAMPTZ;
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS wecom_msg_id VARCHAR(128);
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS created_by VARCHAR(64) DEFAULT '';
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS updated_by VARCHAR(64) DEFAULT '';
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS version INT DEFAULT 0;
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS raw_content TEXT;
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS ai_parse_status VARCHAR(32) DEFAULT 'PENDING';
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS wecom_message_id BIGINT;
COMMENT ON TABLE crm_daily_report IS '销售日报表';

CREATE TABLE IF NOT EXISTS crm_performance (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    user_id BIGINT, stat_year INT, stat_month INT, visit_count INT DEFAULT 0, visit_target INT DEFAULT 0,
    visit_rate DECIMAL(5,2) DEFAULT 0, report_count INT DEFAULT 0, report_target INT DEFAULT 0,
    report_rate DECIMAL(5,2) DEFAULT 0, new_customer_count INT DEFAULT 0, new_customer_target INT DEFAULT 0,
    new_customer_rate DECIMAL(5,2) DEFAULT 0, composite_score DECIMAL(5,2) DEFAULT 0, grade SMALLINT,
    created_by VARCHAR(64) DEFAULT '', created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '', updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0, tenant_id BIGINT DEFAULT 1, version INT DEFAULT 0
);
COMMENT ON TABLE crm_performance IS '勤力度评分表';

-- wecom support tables
CREATE TABLE IF NOT EXISTS crm_wecom_push_outbox (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    tenant_id BIGINT DEFAULT 1,
    to_user_id VARCHAR(128),
    payload TEXT,
    status VARCHAR(32) DEFAULT 'MOCK_SENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE crm_wecom_push_outbox IS '企微确认推送出站记录';

CREATE TABLE IF NOT EXISTS crm_ai_parse_log (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    tenant_id BIGINT DEFAULT 1,
    source_type VARCHAR(64),
    source_id BIGINT,
    provider VARCHAR(64) DEFAULT 'MOCK_MINIMAX',
    request_payload TEXT,
    response_payload TEXT,
    status VARCHAR(32) DEFAULT 'PENDING',
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE crm_ai_parse_log IS 'AI解析日志表';
