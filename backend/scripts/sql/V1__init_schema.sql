-- Huafu Fashion CRM - V1 Init Schema
-- 严格按照华孚时尚CRM系统开发说明书 V1.0 第3.2节

-- 公共序列
CREATE SEQUENCE crm_base_id_seq START WITH 10000;

-- =============================================
-- 1. 系统管理 admin
-- =============================================
CREATE TABLE crm_user (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    real_name VARCHAR(64),
    phone VARCHAR(32),
    email VARCHAR(128),
    dept_id BIGINT,
    post VARCHAR(64),
    status SMALLINT DEFAULT 1,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0
);
COMMENT ON TABLE crm_user IS '系统用户表';
COMMENT ON COLUMN crm_user.post IS '岗位：销售/跟单员/经理';
COMMENT ON COLUMN crm_user.status IS '1=正常 2=禁用';

CREATE TABLE crm_dept (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    dept_name VARCHAR(128) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    leader_user_id BIGINT,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0
);
COMMENT ON TABLE crm_dept IS '部门表';

CREATE TABLE crm_role (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    role_name VARCHAR(64) NOT NULL,
    role_key VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(256),
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0
);
COMMENT ON TABLE crm_role IS '角色表';
COMMENT ON COLUMN crm_role.role_key IS '英文标识，如 ROLE_SALES';

CREATE TABLE crm_user_role (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0,
    UNIQUE(user_id, role_id)
);
COMMENT ON TABLE crm_user_role IS '用户角色关联表';

-- =============================================
-- 2. 客户域 customer
-- =============================================
CREATE TABLE crm_customer (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    customer_code VARCHAR(64) NOT NULL UNIQUE,
    customer_name VARCHAR(256) NOT NULL,
    customer_short_name VARCHAR(128),
    type SMALLINT NOT NULL,
    level SMALLINT,
    status SMALLINT DEFAULT 1,
    province VARCHAR(64),
    city VARCHAR(64),
    address VARCHAR(512),
    main_contact_name VARCHAR(128),
    main_contact_phone VARCHAR(32),
    main_contact_role VARCHAR(32),
    annual_revenue DECIMAL(18,2),
    credit_limit DECIMAL(18,2),
    tax_rate DECIMAL(5,4),
    payment_days INT,
    sap_customer_code VARCHAR(64),
    remark TEXT,
    last_visit_time TIMESTAMPTZ,
    owner_user_id BIGINT,
    public_pool_time TIMESTAMPTZ,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0
);
COMMENT ON TABLE crm_customer IS '客户主表';
COMMENT ON COLUMN crm_customer.type IS '1=直接客户 2=代理 3=终端品牌';
COMMENT ON COLUMN crm_customer.level IS '1=AAA 2=AA 3=A 4=B 5=C';
COMMENT ON COLUMN crm_customer.status IS '1=正常 2=冻结 3=流失';
COMMENT ON COLUMN crm_customer.main_contact_role IS '老板/采购/营业/开发';
COMMENT ON COLUMN crm_customer.annual_revenue IS '年营业额（万元）';
COMMENT ON COLUMN crm_customer.credit_limit IS '信用额度（万元）';
COMMENT ON COLUMN crm_customer.payment_days IS '账期（天）';

CREATE TABLE crm_customer_contact (
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
COMMENT ON COLUMN crm_customer_contact.role_type IS '1=老板 2=采购 3=营业 4=开发 5=品质 6=财务';
COMMENT ON COLUMN crm_customer_contact.decision_level IS '1=决策层 2=管理层 3=执行层';
COMMENT ON COLUMN crm_customer_contact.is_main IS '是否主联系人';

CREATE TABLE crm_customer_bundle (
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
    version INT DEFAULT 0,
    UNIQUE(customer_id, bundle_type, bundle_customer_id)
);
COMMENT ON TABLE crm_customer_bundle IS '客户关联表';
COMMENT ON COLUMN crm_customer_bundle.bundle_type IS '1=布厂关联品牌 2=母子客户';

CREATE TABLE crm_public_pool_rule (
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
COMMENT ON COLUMN crm_public_pool_rule.condition_type IS '1=无跟进天数 2=未成交天数';
COMMENT ON COLUMN crm_public_pool_rule.action_type IS '1=自动进入公海 2=提醒';

-- =============================================
-- 3. 商机域 opportunity
-- =============================================
CREATE TABLE crm_lead (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    lead_no VARCHAR(64) NOT NULL UNIQUE,
    lead_type SMALLINT NOT NULL,
    customer_id BIGINT,
    customer_name VARCHAR(256),
    contact_name VARCHAR(128),
    contact_phone VARCHAR(32),
    province VARCHAR(64),
    city VARCHAR(64),
    product_name VARCHAR(256),
    competitor_name VARCHAR(256),
    competitor_price DECIMAL(18,2),
    competitor_discount DECIMAL(5,4),
    margin_rate DECIMAL(5,4),
    source SMALLINT,
    creator_user_id BIGINT NOT NULL,
    handler_user_id BIGINT,
    status SMALLINT DEFAULT 1,
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
    version INT DEFAULT 0
);
COMMENT ON TABLE crm_lead IS '线索/商情表';
COMMENT ON COLUMN crm_lead.lead_type IS '1=商情（竞争情报）2=线索（潜在客户）';
COMMENT ON COLUMN crm_lead.source IS '1=日报AI提取 2=展会 3=主动开发 4=转介绍';
COMMENT ON COLUMN crm_lead.status IS '1=新线索 2=跟进中 3=已转化 4=无效';

CREATE TABLE crm_opportunity (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    opp_no VARCHAR(64) NOT NULL UNIQUE,
    opportunity_name VARCHAR(256) NOT NULL,
    customer_id BIGINT NOT NULL,
    handler_user_id BIGINT NOT NULL,
    stage SMALLINT NOT NULL,
    stage_update_time TIMESTAMPTZ,
    product_name VARCHAR(256),
    quantity DECIMAL(18,2),
    unit VARCHAR(16),
    estimated_amount DECIMAL(18,2),
    expected_close_date DATE,
    actual_close_date DATE,
    win_probability SMALLINT,
    lost_reason VARCHAR(512),
    lost_type SMALLINT,
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
    version INT DEFAULT 0
);
COMMENT ON TABLE crm_opportunity IS '商机表';
COMMENT ON COLUMN crm_opportunity.stage IS '1=初步接触 2=需求确认 3=方案报价 4=合同谈判 5=成交';
COMMENT ON COLUMN crm_opportunity.win_probability IS '赢单概率 0-100';
COMMENT ON COLUMN crm_opportunity.lost_type IS '1=价格 2=交期 3=质量 4=现货 5=其他';
COMMENT ON COLUMN crm_opportunity.estimated_amount IS '预估金额（万元）';

CREATE TABLE crm_quote (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    quote_no VARCHAR(64) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    opportunity_id BIGINT,
    contact_id BIGINT,
    contact_name VARCHAR(128),
    contact_phone VARCHAR(32),
    sales_user_id BIGINT NOT NULL,
    quote_date DATE,
    valid_date DATE,
    total_amount DECIMAL(18,2),
    discount_rate DECIMAL(5,4),
    final_amount DECIMAL(18,2),
    payment_terms VARCHAR(256),
    delivery_terms VARCHAR(256),
    status SMALLINT DEFAULT 1,
    sent_time TIMESTAMPTZ,
    confirmed_time TIMESTAMPTZ,
    remark TEXT,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0
);
COMMENT ON TABLE crm_quote IS '报价单表';
COMMENT ON COLUMN crm_quote.status IS '1=草稿 2=已发送 3=客户确认 4=已成交 5=已失效';

CREATE TABLE crm_quote_item (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    quote_id BIGINT NOT NULL,
    product_name VARCHAR(256) NOT NULL,
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
    version INT DEFAULT 0
);
COMMENT ON TABLE crm_quote_item IS '报价单明细表';

CREATE TABLE crm_lost_order (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    lost_no VARCHAR(64) NOT NULL UNIQUE,
    opportunity_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    lost_type SMALLINT NOT NULL,
    competitor_name VARCHAR(256),
    competitor_price DECIMAL(18,2),
    our_price DECIMAL(18,2),
    margin_diff DECIMAL(18,4),
    competitor_discount DECIMAL(5,4),
    our_discount DECIMAL(5,4),
    reason_detail TEXT,
    recovery_possible SMALLINT,
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
    version INT DEFAULT 0
);
COMMENT ON TABLE crm_lost_order IS '丢单记录表';
COMMENT ON COLUMN crm_lost_order.lost_type IS '1=价格 2=交期 3=质量 4=现货 5=其他';
COMMENT ON COLUMN crm_lost_order.recovery_possible IS '1=可能 2=不可能';

-- =============================================
-- 4. 目标域 target
-- =============================================
CREATE TABLE crm_target (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    target_no VARCHAR(64) NOT NULL UNIQUE,
    target_year INT NOT NULL,
    target_month INT NOT NULL,
    dept_id BIGINT,
    user_id BIGINT,
    product_category VARCHAR(128),
    metric_type SMALLINT NOT NULL,
    target_value DECIMAL(18,2) NOT NULL,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0,
    UNIQUE(target_year, target_month, user_id, product_category, metric_type)
);
COMMENT ON TABLE crm_target IS '目标表';
COMMENT ON COLUMN crm_target.metric_type IS '1=销售额 2=毛利 3=客户数 4=拜访次数';

CREATE TABLE crm_target_achieve (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    target_id BIGINT NOT NULL,
    achieve_year INT NOT NULL,
    achieve_month INT NOT NULL,
    achieve_value DECIMAL(18,2),
    achieve_rate DECIMAL(6,2),
    source SMALLINT,
    source_data_date DATE,
    synced_time TIMESTAMPTZ,
    remark TEXT,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0
);
COMMENT ON TABLE crm_target_achieve IS '目标达成表';
COMMENT ON COLUMN crm_target_achieve.source IS '1=SAP同步 2=BI拉取 3=手工';

-- =============================================
-- 5. 勤力度域 performance
-- =============================================
CREATE TABLE crm_visit_record (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    visit_no VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    customer_id BIGINT,
    customer_name VARCHAR(256),
    contact_id BIGINT,
    contact_name VARCHAR(128),
    visit_date DATE NOT NULL,
    visit_type SMALLINT,
    visit_purpose VARCHAR(256),
    visit_content TEXT,
    next_visit_plan DATE,
    is_new_customer SMALLINT DEFAULT 0,
    longitude DECIMAL(10,6),
    latitude DECIMAL(10,6),
    location_name VARCHAR(256),
    remark TEXT,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0
);
COMMENT ON TABLE crm_visit_record IS '拜访记录表';
COMMENT ON COLUMN crm_visit_record.visit_type IS '1=上门拜访 2=电话拜访 3=微信拜访 4=展会';
COMMENT ON COLUMN crm_visit_record.is_new_customer IS '1=新客户';

CREATE TABLE crm_daily_report (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    report_no VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    report_date DATE NOT NULL,
    content_text TEXT,
    content_html TEXT,
    parse_status SMALLINT DEFAULT 0,
    parsed_json JSONB,
    opportunity_count INT DEFAULT 0,
    market_intelligence_count INT DEFAULT 0,
    lost_order_count INT DEFAULT 0,
    parse_error VARCHAR(512),
    parse_time TIMESTAMPTZ,
    wecom_msg_id VARCHAR(128),
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0
);
COMMENT ON TABLE crm_daily_report IS '销售日报表';
COMMENT ON COLUMN crm_daily_report.parse_status IS '0=未解析 1=解析中 2=成功 3=失败';

CREATE TABLE crm_performance (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    user_id BIGINT NOT NULL,
    stat_year INT NOT NULL,
    stat_month INT NOT NULL,
    visit_count INT DEFAULT 0,
    visit_target INT DEFAULT 0,
    visit_rate DECIMAL(5,2) DEFAULT 0,
    report_count INT DEFAULT 0,
    report_target INT DEFAULT 0,
    report_rate DECIMAL(5,2) DEFAULT 0,
    new_customer_count INT DEFAULT 0,
    new_customer_target INT DEFAULT 0,
    new_customer_rate DECIMAL(5,2) DEFAULT 0,
    composite_score DECIMAL(5,2) DEFAULT 0,
    grade SMALLINT,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0,
    UNIQUE(user_id, stat_year, stat_month)
);
COMMENT ON TABLE crm_performance IS '勤力度评分表';
COMMENT ON COLUMN crm_performance.grade IS 'G/C/O 等级';

-- =============================================
-- 6. 企微集成域 wecom
-- =============================================
CREATE TABLE crm_wecom_agent (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    agent_name VARCHAR(128) NOT NULL,
    agent_id VARCHAR(64) NOT NULL UNIQUE,
    app_id VARCHAR(128),
    app_secret VARCHAR(256),
    token VARCHAR(256),
    encoding_aes_key VARCHAR(256),
    callback_url VARCHAR(512),
    enabled SMALLINT DEFAULT 1,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0
);
COMMENT ON TABLE crm_wecom_agent IS '企微应用配置表';

CREATE TABLE crm_wecom_message_log (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    msg_id VARCHAR(128),
    msg_type VARCHAR(32),
    from_user VARCHAR(64),
    to_user VARCHAR(64),
    content TEXT,
    raw_xml TEXT,
    parse_status SMALLINT DEFAULT 0,
    related_report_id BIGINT,
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0
);
COMMENT ON TABLE crm_wecom_message_log IS '企微消息日志表';
COMMENT ON COLUMN crm_wecom_message_log.parse_status IS '0=未处理 1=已解析 2=解析失败';

-- =============================================
-- 7. AI解析域 ai
-- =============================================
CREATE TABLE crm_ai_parse_log (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    business_type SMALLINT NOT NULL,
    biz_id BIGINT,
    input_text TEXT,
    output_json JSONB,
    prompt_tokens INT,
    completion_tokens INT,
    model_name VARCHAR(64),
    cost DECIMAL(10,4),
    duration_ms INT,
    status SMALLINT,
    error_msg VARCHAR(512),
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0
);
COMMENT ON TABLE crm_ai_parse_log IS 'AI解析日志表';
COMMENT ON COLUMN crm_ai_parse_log.business_type IS '1=日报解析 2=名片OCR 3=展会扫描';
COMMENT ON COLUMN crm_ai_parse_log.status IS '1=成功 2=失败';

-- =============================================
-- 索引
-- =============================================
CREATE INDEX idx_crm_customer_owner ON crm_customer(owner_user_id) WHERE deleted=0;
CREATE INDEX idx_crm_customer_status ON crm_customer(status) WHERE deleted=0;
CREATE INDEX idx_crm_customer_code ON crm_customer(customer_code) WHERE deleted=0;
CREATE INDEX idx_crm_opportunity_customer ON crm_opportunity(customer_id) WHERE deleted=0;
CREATE INDEX idx_crm_opportunity_handler ON crm_opportunity(handler_user_id) WHERE deleted=0;
CREATE INDEX idx_crm_opportunity_stage ON crm_opportunity(stage) WHERE deleted=0;
CREATE INDEX idx_crm_lead_handler ON crm_lead(handler_user_id) WHERE deleted=0;
CREATE INDEX idx_crm_lead_creator ON crm_lead(creator_user_id) WHERE deleted=0;
CREATE INDEX idx_crm_daily_report_user_date ON crm_daily_report(user_id, report_date) WHERE deleted=0;
CREATE INDEX idx_crm_visit_record_user ON crm_visit_record(user_id) WHERE deleted=0;
CREATE INDEX idx_crm_visit_record_date ON crm_visit_record(visit_date) WHERE deleted=0;
CREATE INDEX idx_crm_performance_user_ym ON crm_performance(user_id, stat_year, stat_month) WHERE deleted=0;
