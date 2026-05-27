-- Global runtime safety net for deployments where Flyway history exists but
-- one or more runtime tables were removed or created from an old package.

CREATE SEQUENCE IF NOT EXISTS crm_base_id_seq START WITH 10000;

CREATE TABLE IF NOT EXISTS crm_customer (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    customer_code VARCHAR(64),
    customer_name VARCHAR(256) NOT NULL,
    customer_short_name VARCHAR(128),
    type INTEGER,
    level INTEGER,
    status INTEGER DEFAULT 1,
    province VARCHAR(64),
    city VARCHAR(64),
    district VARCHAR(64),
    address VARCHAR(512),
    main_contact_name VARCHAR(128),
    main_contact_phone VARCHAR(32),
    main_contact_role VARCHAR(64),
    annual_revenue DECIMAL(18,2),
    credit_limit DECIMAL(18,2),
    tax_rate DECIMAL(10,4),
    payment_days INTEGER,
    sap_customer_code VARCHAR(64),
    remark TEXT,
    last_visit_time TIMESTAMPTZ,
    owner_user_id BIGINT,
    public_pool_time TIMESTAMPTZ,
    customer_category VARCHAR(64),
    customer_segment VARCHAR(64),
    business_type INTEGER,
    country_region VARCHAR(64),
    main_brand VARCHAR(256),
    annual_yarn_volume DECIMAL(18,2),
    machine_count INTEGER,
    production_capacity VARCHAR(256),
    industry_position VARCHAR(512),
    main_customer_group VARCHAR(512),
    bundle_customer_name VARCHAR(256),
    bundle_brand VARCHAR(256),
    bundle_customer_id BIGINT,
    bundle_customer_sap_code VARCHAR(64),
    owner_dept_id BIGINT,
    sales_merchandiser VARCHAR(128),
    location_lat DECIMAL(12,6),
    location_lng DECIMAL(12,6),
    unified_social_credit_code VARCHAR(64),
    english_name VARCHAR(256),
    asset_type VARCHAR(64),
    customer_source VARCHAR(64),
    customer_stage INTEGER,
    competitor_share_json TEXT,
    cooperation_brand_json TEXT,
    blacklist INTEGER,
    risk_level INTEGER,
    tax_id VARCHAR(128),
    bank_name VARCHAR(256),
    bank_account VARCHAR(128),
    invoice_title VARCHAR(256),
    company_code VARCHAR(64),
    sales_group VARCHAR(64),
    price_list VARCHAR(64),
    currency VARCHAR(32),
    delivery_factory VARCHAR(64),
    account_assignment_group VARCHAR(64),
    tax_classification VARCHAR(64),
    ship_to_party VARCHAR(64),
    sold_to_party VARCHAR(64),
    payer_party VARCHAR(64),
    country_code VARCHAR(64),
    region VARCHAR(64),
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_customer_contact (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    customer_id BIGINT,
    contact_name VARCHAR(128),
    phone VARCHAR(32),
    wechat VARCHAR(128),
    email VARCHAR(128),
    position VARCHAR(64),
    role_type INTEGER,
    decision_level INTEGER,
    is_main INTEGER DEFAULT 0,
    birthday DATE,
    remark TEXT,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_customer_address (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    customer_id BIGINT,
    address_type INTEGER,
    contact_name VARCHAR(128),
    phone VARCHAR(32),
    country VARCHAR(64),
    province VARCHAR(64),
    city VARCHAR(64),
    district VARCHAR(64),
    address_detail VARCHAR(512),
    full_address VARCHAR(1024),
    longitude DECIMAL(12,6),
    latitude DECIMAL(12,6),
    amap_poi_id VARCHAR(128),
    amap_poi_name VARCHAR(256),
    amap_adcode VARCHAR(64),
    amap_level VARCHAR(64),
    location_source VARCHAR(64),
    location_verified INTEGER DEFAULT 0,
    location_verified_time TIMESTAMPTZ,
    checkin_radius_meters INTEGER DEFAULT 500,
    address_remark TEXT,
    is_default INTEGER DEFAULT 0,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_customer_bundle (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    customer_id BIGINT,
    bundle_type INTEGER,
    bundle_customer_id BIGINT,
    bundle_customer_name VARCHAR(256),
    bundle_customer_sap_code VARCHAR(64),
    relation_remark VARCHAR(256),
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_customer_ext (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    customer_id BIGINT,
    unified_credit_code VARCHAR(64),
    english_name VARCHAR(256),
    asset_type VARCHAR(64),
    customer_category VARCHAR(64),
    customer_source VARCHAR(64),
    sap_account_group VARCHAR(64),
    sap_country_code VARCHAR(64),
    sap_region VARCHAR(64),
    phone VARCHAR(32),
    fax VARCHAR(32),
    tax_rate_category VARCHAR(64),
    pod_relevance VARCHAR(64),
    account_assignment_group VARCHAR(64),
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_customer_overview (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    customer_id BIGINT,
    overview_summary TEXT,
    strategy_position TEXT,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_customer_profile (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    customer_id BIGINT,
    industry_position TEXT,
    main_customer_group TEXT,
    main_brands TEXT,
    yarn_volume_summary TEXT,
    competitor_summary TEXT,
    machine_summary TEXT,
    other_assets TEXT,
    overview_auto TEXT,
    overview_manual TEXT,
    overview_editable INTEGER DEFAULT 1,
    tags TEXT,
    remark TEXT,
    customer_stage VARCHAR(64),
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_customer_profile_history (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    customer_id BIGINT,
    profile_id BIGINT,
    snapshot_json TEXT,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1
);

CREATE TABLE IF NOT EXISTS crm_customer_sap_info (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    tenant_id BIGINT DEFAULT 1,
    customer_id BIGINT,
    sap_code VARCHAR(64),
    account_group VARCHAR(64),
    country_code VARCHAR(64),
    company_code VARCHAR(64),
    sales_org VARCHAR(64),
    distribution_channel VARCHAR(64),
    division VARCHAR(64),
    description VARCHAR(512),
    is_default INTEGER DEFAULT 0,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_customer_sap_org (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    customer_id BIGINT,
    sap_code VARCHAR(64),
    company_code VARCHAR(64),
    sales_org VARCHAR(64),
    sales_office VARCHAR(64),
    sales_group VARCHAR(64),
    price_list VARCHAR(64),
    currency VARCHAR(32),
    delivery_plant VARCHAR(64),
    payment_terms VARCHAR(64),
    tax_classification VARCHAR(64),
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_customer_sap_partner (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    customer_id BIGINT,
    sap_code VARCHAR(64),
    partner_function VARCHAR(64),
    partner_code VARCHAR(64),
    partner_name VARCHAR(256),
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_customer_attachment (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    customer_id BIGINT,
    file_name VARCHAR(256),
    file_url VARCHAR(512),
    file_type VARCHAR(64),
    file_size BIGINT,
    remark TEXT,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_customer_yarn_usage (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    customer_id BIGINT,
    yarn_type VARCHAR(128),
    annual_volume DECIMAL(18,2),
    unit VARCHAR(32),
    remark TEXT,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_customer_transfer (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    customer_id BIGINT,
    from_user_id BIGINT,
    to_user_id BIGINT,
    supervisor_user_id BIGINT,
    transfer_date DATE,
    reason TEXT,
    business_summary TEXT,
    contact_info_snapshot TEXT,
    receivable_info TEXT,
    cooperation_issues TEXT,
    future_opportunities TEXT,
    handover_attachments TEXT,
    status INTEGER,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_module_record (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    module_key VARCHAR(64),
    record_no VARCHAR(64),
    title VARCHAR(256),
    status VARCHAR(64),
    owner_name VARCHAR(128),
    record_date DATE,
    payload_json TEXT,
    remark TEXT,
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1
);

CREATE TABLE IF NOT EXISTS crm_public_pool_rule (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    rule_name VARCHAR(128),
    condition_type INTEGER,
    condition_days INTEGER DEFAULT 90,
    action_type INTEGER,
    target_user_id BIGINT,
    dept_id BIGINT,
    enabled INTEGER DEFAULT 1,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_lead (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    lead_no VARCHAR(64),
    lead_type INTEGER,
    customer_id BIGINT,
    customer_name VARCHAR(256),
    contact_name VARCHAR(128),
    contact_phone VARCHAR(32),
    province VARCHAR(64),
    city VARCHAR(64),
    product_name VARCHAR(256),
    competitor_name VARCHAR(256),
    competitor_price DECIMAL(18,2),
    competitor_discount DECIMAL(10,4),
    margin_rate DECIMAL(10,4),
    source INTEGER,
    creator_user_id BIGINT,
    handler_user_id BIGINT,
    status INTEGER DEFAULT 1,
    convert_time TIMESTAMPTZ,
    converted_opp_id BIGINT,
    remark TEXT,
    raw_text TEXT,
    parsed_json JSONB,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_opportunity (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    opp_no VARCHAR(64),
    opportunity_name VARCHAR(256),
    customer_id BIGINT,
    handler_user_id BIGINT,
    stage INTEGER,
    stage_update_time TIMESTAMPTZ,
    product_name VARCHAR(256),
    quantity DECIMAL(18,2),
    unit VARCHAR(16),
    estimated_amount DECIMAL(18,2),
    expected_close_date DATE,
    actual_close_date DATE,
    win_probability INTEGER,
    lost_reason VARCHAR(512),
    lost_type INTEGER,
    competitor_name VARCHAR(256),
    quote_id BIGINT,
    order_no VARCHAR(64),
    remark TEXT,
    source_lead_id BIGINT,
    raw_text TEXT,
    parsed_json JSONB,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_quote (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    quote_no VARCHAR(64),
    customer_id BIGINT,
    opportunity_id BIGINT,
    contact_id BIGINT,
    contact_name VARCHAR(128),
    contact_phone VARCHAR(32),
    sales_user_id BIGINT,
    quote_date DATE,
    valid_date DATE,
    total_amount DECIMAL(18,2),
    discount_rate DECIMAL(10,4),
    final_amount DECIMAL(18,2),
    payment_terms VARCHAR(256),
    delivery_terms VARCHAR(256),
    status INTEGER DEFAULT 1,
    sent_time TIMESTAMPTZ,
    confirmed_time TIMESTAMPTZ,
    remark TEXT,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_quote_item (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    quote_id BIGINT,
    product_name VARCHAR(256),
    product_code VARCHAR(64),
    quantity DECIMAL(18,2),
    unit VARCHAR(16),
    unit_price DECIMAL(18,4),
    amount DECIMAL(18,2),
    lead_time VARCHAR(64),
    remark VARCHAR(512),
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_lost_order (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    lost_no VARCHAR(64),
    opportunity_id BIGINT,
    customer_id BIGINT,
    lost_type INTEGER,
    competitor_name VARCHAR(256),
    competitor_price DECIMAL(18,2),
    our_price DECIMAL(18,2),
    margin_diff DECIMAL(18,4),
    competitor_discount DECIMAL(10,4),
    our_discount DECIMAL(10,4),
    reason_detail TEXT,
    recovery_possible INTEGER,
    follow_up_date DATE,
    handler_user_id BIGINT,
    raw_text TEXT,
    parsed_json JSONB,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_target (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    target_no VARCHAR(64),
    target_year INTEGER,
    target_month INTEGER,
    dept_id BIGINT,
    user_id BIGINT,
    product_category VARCHAR(128),
    metric_type INTEGER,
    target_value DECIMAL(18,2),
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_target_achieve (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    target_id BIGINT,
    achieve_year INTEGER,
    achieve_month INTEGER,
    achieve_value DECIMAL(18,2),
    achieve_rate DECIMAL(10,2),
    source INTEGER,
    source_data_date DATE,
    synced_time TIMESTAMPTZ,
    remark TEXT,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_visit_record (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    visit_no VARCHAR(64),
    user_id BIGINT,
    customer_id BIGINT,
    customer_name VARCHAR(256),
    contact_id BIGINT,
    contact_name VARCHAR(128),
    visit_date DATE,
    visit_type INTEGER,
    visit_purpose VARCHAR(256),
    visit_content TEXT,
    next_visit_plan DATE,
    is_new_customer INTEGER DEFAULT 0,
    longitude DECIMAL(12,6),
    latitude DECIMAL(12,6),
    location_name VARCHAR(256),
    remark TEXT,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_daily_report (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    report_no VARCHAR(64),
    user_id BIGINT,
    report_date DATE,
    content_text TEXT,
    content_html TEXT,
    parse_status INTEGER DEFAULT 0,
    parsed_json JSONB,
    opportunity_count INTEGER DEFAULT 0,
    market_intelligence_count INTEGER DEFAULT 0,
    lost_order_count INTEGER DEFAULT 0,
    parse_error VARCHAR(512),
    parse_time TIMESTAMPTZ,
    wecom_msg_id VARCHAR(128),
    wecom_chat_id VARCHAR(128),
    wecom_sender VARCHAR(128),
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_performance (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    user_id BIGINT,
    stat_year INTEGER,
    stat_month INTEGER,
    visit_count INTEGER DEFAULT 0,
    visit_target INTEGER DEFAULT 0,
    visit_rate DECIMAL(10,2) DEFAULT 0,
    report_count INTEGER DEFAULT 0,
    report_target INTEGER DEFAULT 0,
    report_rate DECIMAL(10,2) DEFAULT 0,
    new_customer_count INTEGER DEFAULT 0,
    new_customer_target INTEGER DEFAULT 0,
    new_customer_rate DECIMAL(10,2) DEFAULT 0,
    composite_score DECIMAL(10,2) DEFAULT 0,
    grade INTEGER,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_wecom_agent (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    agent_name VARCHAR(128),
    agent_id VARCHAR(64),
    app_id VARCHAR(128),
    app_secret VARCHAR(256),
    token VARCHAR(256),
    encoding_aes_key VARCHAR(256),
    callback_url VARCHAR(512),
    enabled INTEGER DEFAULT 1,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_wecom_message_log (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    msg_id VARCHAR(128),
    msg_type VARCHAR(32),
    from_user VARCHAR(128),
    to_user VARCHAR(128),
    chat_id VARCHAR(128),
    content TEXT,
    raw_xml TEXT,
    raw_json TEXT,
    parse_status INTEGER DEFAULT 0,
    related_report_id BIGINT,
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_ai_parse_log (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    business_type INTEGER,
    biz_id BIGINT,
    input_text TEXT,
    output_json JSONB,
    prompt_tokens INTEGER,
    completion_tokens INTEGER,
    model_name VARCHAR(64),
    cost DECIMAL(10,4),
    duration_ms INTEGER,
    status INTEGER,
    error_msg VARCHAR(512),
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_user_column_config (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    page_code VARCHAR(128),
    column_configs TEXT,
    is_default INTEGER DEFAULT 0,
    config_name VARCHAR(128),
    created_by BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_user_filter_config (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    page_code VARCHAR(128),
    filter_configs TEXT,
    config_name VARCHAR(128),
    is_default INTEGER DEFAULT 0,
    created_by BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0
);

ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS district VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS customer_short_name VARCHAR(128);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS country_region VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS owner_dept_id BIGINT;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS bundle_customer_id BIGINT;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS bundle_customer_name VARCHAR(256);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS bundle_customer_sap_code VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS location_lat DECIMAL(12,6);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS location_lng DECIMAL(12,6);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS customer_category VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS customer_segment VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS business_type INTEGER;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS customer_source VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS customer_stage INTEGER;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS industry_position VARCHAR(512);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS main_customer_group VARCHAR(512);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS cooperation_brand_json TEXT;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS competitor_share_json TEXT;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;

ALTER TABLE crm_customer_sap_info ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_customer_sap_info ADD COLUMN IF NOT EXISTS account_group VARCHAR(64);
ALTER TABLE crm_customer_sap_info ADD COLUMN IF NOT EXISTS country_code VARCHAR(64);
ALTER TABLE crm_customer_sap_info ADD COLUMN IF NOT EXISTS is_default INTEGER DEFAULT 0;

ALTER TABLE crm_customer_sap_org ADD COLUMN IF NOT EXISTS sap_code VARCHAR(64);
ALTER TABLE crm_customer_sap_org ADD COLUMN IF NOT EXISTS sales_office VARCHAR(64);
ALTER TABLE crm_customer_sap_org ADD COLUMN IF NOT EXISTS payment_terms VARCHAR(64);
ALTER TABLE crm_customer_sap_org ADD COLUMN IF NOT EXISTS tax_classification VARCHAR(64);
ALTER TABLE crm_customer_sap_partner ADD COLUMN IF NOT EXISTS partner_type VARCHAR(64);
ALTER TABLE crm_customer_attachment ADD COLUMN IF NOT EXISTS attachment_type VARCHAR(64);
ALTER TABLE crm_customer_attachment ADD COLUMN IF NOT EXISTS mime_type VARCHAR(128);
ALTER TABLE crm_customer_attachment ADD COLUMN IF NOT EXISTS uploaded_by BIGINT;
ALTER TABLE crm_customer_attachment ADD COLUMN IF NOT EXISTS uploaded_at TIMESTAMPTZ;
ALTER TABLE crm_customer_yarn_usage ADD COLUMN IF NOT EXISTS usage_amount DECIMAL(18,2);
ALTER TABLE crm_customer_yarn_usage ADD COLUMN IF NOT EXISTS usage_unit VARCHAR(32);
ALTER TABLE crm_customer_profile_history ADD COLUMN IF NOT EXISTS operated_by VARCHAR(64);
ALTER TABLE crm_customer_profile_history ADD COLUMN IF NOT EXISTS operated_at TIMESTAMPTZ;
ALTER TABLE crm_customer_profile_history ADD COLUMN IF NOT EXISTS version INTEGER;
ALTER TABLE crm_customer_profile_history ADD COLUMN IF NOT EXISTS industry_position TEXT;
ALTER TABLE crm_customer_profile_history ADD COLUMN IF NOT EXISTS main_customer_group TEXT;
ALTER TABLE crm_customer_profile_history ADD COLUMN IF NOT EXISTS main_brands TEXT;
ALTER TABLE crm_customer_profile_history ADD COLUMN IF NOT EXISTS yarn_volume_summary TEXT;
ALTER TABLE crm_customer_profile_history ADD COLUMN IF NOT EXISTS competitor_summary TEXT;
ALTER TABLE crm_customer_profile_history ADD COLUMN IF NOT EXISTS machine_summary TEXT;
ALTER TABLE crm_customer_profile_history ADD COLUMN IF NOT EXISTS other_assets TEXT;
ALTER TABLE crm_customer_profile_history ADD COLUMN IF NOT EXISTS overview_auto TEXT;
ALTER TABLE crm_customer_profile_history ADD COLUMN IF NOT EXISTS overview_manual TEXT;
ALTER TABLE crm_customer_profile_history ADD COLUMN IF NOT EXISTS overview_editable INTEGER;
ALTER TABLE crm_customer_profile_history ADD COLUMN IF NOT EXISTS tags TEXT;
ALTER TABLE crm_customer_profile_history ADD COLUMN IF NOT EXISTS remark TEXT;
ALTER TABLE crm_customer_profile_history ADD COLUMN IF NOT EXISTS customer_stage VARCHAR(64);
ALTER TABLE crm_customer_profile_history ADD COLUMN IF NOT EXISTS updated_by VARCHAR(64) DEFAULT '';
ALTER TABLE crm_customer_profile_history ADD COLUMN IF NOT EXISTS updated_time TIMESTAMPTZ;
ALTER TABLE crm_customer_profile_history ADD COLUMN IF NOT EXISTS version_num INTEGER DEFAULT 0;

ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS wecom_chat_id VARCHAR(128);
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS wecom_sender VARCHAR(128);
ALTER TABLE crm_wecom_message_log ADD COLUMN IF NOT EXISTS chat_id VARCHAR(128);
ALTER TABLE crm_wecom_message_log ADD COLUMN IF NOT EXISTS raw_json TEXT;

ALTER TABLE crm_customer_contact ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_customer_contact ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_customer_address ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_customer_address ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_customer_bundle ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_customer_bundle ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_customer_ext ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_customer_ext ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_customer_overview ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_customer_overview ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_customer_profile ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_customer_profile ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_customer_sap_info ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_customer_sap_org ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_customer_sap_org ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_customer_sap_partner ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_customer_sap_partner ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_module_record ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_module_record ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_lead ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_opportunity ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_daily_report ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_visit_record ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_visit_record ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_target ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_target ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;

CREATE INDEX IF NOT EXISTS idx_crm_customer_owner ON crm_customer(owner_user_id) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_crm_customer_status ON crm_customer(status) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_crm_customer_name ON crm_customer(customer_name) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_crm_customer_address_customer ON crm_customer_address(customer_id, deleted);
CREATE INDEX IF NOT EXISTS idx_crm_customer_sap_info_customer ON crm_customer_sap_info(customer_id, deleted);
CREATE INDEX IF NOT EXISTS idx_crm_customer_sap_org_customer ON crm_customer_sap_org(customer_id, deleted);
CREATE INDEX IF NOT EXISTS idx_crm_customer_contact_customer ON crm_customer_contact(customer_id, deleted);
CREATE INDEX IF NOT EXISTS idx_crm_module_record_module ON crm_module_record(module_key, deleted, created_time DESC);
CREATE INDEX IF NOT EXISTS idx_crm_opportunity_customer ON crm_opportunity(customer_id) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_crm_lead_handler ON crm_lead(handler_user_id) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_crm_daily_report_user_date ON crm_daily_report(user_id, report_date) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_crm_visit_record_user ON crm_visit_record(user_id) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_crm_target_user_period ON crm_target(user_id, target_year, target_month) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_sys_user_column_page ON sys_user_column_config(user_id, page_code);
CREATE INDEX IF NOT EXISTS idx_sys_user_filter_page ON sys_user_filter_config(user_id, page_code);
