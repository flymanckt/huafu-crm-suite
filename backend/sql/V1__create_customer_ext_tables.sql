-- =====================================================
-- 华孚CRM客户主数据扩展表 DDL
-- 数据库: PostgreSQL (jdbc:postgresql://localhost:5432/huafu_crm)
-- 创建时间: 2026-05-09
-- =====================================================

-- 1. crm_customer_ext（客户扩展信息）
CREATE TABLE crm_customer_ext (
    id BIGINT PRIMARY KEY DEFAULT nextval('seq_global'::regclass),
    customer_id BIGINT NOT NULL,
    unified_credit_code VARCHAR(18),
    english_name VARCHAR(256),
    asset_type VARCHAR(32),
    customer_category VARCHAR(32) NOT NULL,
    customer_source VARCHAR(32),
    sap_account_group VARCHAR(32),
    sap_country_code VARCHAR(8),
    sap_region VARCHAR(64),
    phone VARCHAR(32),
    fax VARCHAR(32),
    tax_rate_category VARCHAR(16),
    pod_relevance VARCHAR(64),
    account_assignment_group VARCHAR(32),
    version INT DEFAULT 0,
    created_by VARCHAR(64),
    created_time TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(64),
    updated_time TIMESTAMP WITH TIME ZONE,
    deleted INT DEFAULT 0,
    CONSTRAINT fk_ext_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id)
);
CREATE INDEX idx_ext_customer_id ON crm_customer_ext(customer_id);

-- 2. crm_customer_overview（客户整体概述）
CREATE TABLE crm_customer_overview (
    id BIGINT PRIMARY KEY DEFAULT nextval('seq_global'::regclass),
    customer_id BIGINT NOT NULL UNIQUE,
    overview_summary TEXT,
    strategy_position VARCHAR(128),
    version INT DEFAULT 0,
    created_by VARCHAR(64),
    created_time TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(64),
    updated_time TIMESTAMP WITH TIME ZONE,
    deleted INT DEFAULT 0,
    CONSTRAINT fk_overview_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id)
);
CREATE INDEX idx_overview_customer_id ON crm_customer_overview(customer_id);

-- 3. crm_customer_yarn_usage（纱线用量）
CREATE TABLE crm_customer_yarn_usage (
    id BIGINT PRIMARY KEY DEFAULT nextval('seq_global'::regclass),
    customer_id BIGINT NOT NULL,
    yarn_type VARCHAR(32) NOT NULL,
    usage_amount DECIMAL(12,2) NOT NULL,
    usage_unit VARCHAR(16) DEFAULT '吨',
    remark VARCHAR(256),
    version INT DEFAULT 0,
    created_by VARCHAR(64),
    created_time TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(64),
    updated_time TIMESTAMP WITH TIME ZONE,
    deleted INT DEFAULT 0,
    CONSTRAINT fk_yarn_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id)
);
CREATE INDEX idx_yarn_customer_id ON crm_customer_yarn_usage(customer_id);

-- 4. crm_customer_sap_org（SAP组织信息）
CREATE TABLE crm_customer_sap_org (
    id BIGINT PRIMARY KEY DEFAULT nextval('seq_global'::regclass),
    customer_id BIGINT NOT NULL,
    company_code VARCHAR(16) NOT NULL,
    sales_org VARCHAR(16) NOT NULL,
    sales_office VARCHAR(16),
    sales_group VARCHAR(16),
    price_list VARCHAR(16),
    currency VARCHAR(8) DEFAULT 'CNY',
    delivery_plant VARCHAR(16),
    payment_terms VARCHAR(16),
    version INT DEFAULT 0,
    created_by VARCHAR(64),
    created_time TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(64),
    updated_time TIMESTAMP WITH TIME ZONE,
    deleted INT DEFAULT 0,
    CONSTRAINT fk_sap_org_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id)
);
CREATE INDEX idx_sap_org_customer_id ON crm_customer_sap_org(customer_id);

-- 5. crm_customer_sap_partner（SAP合作伙伴）
CREATE TABLE crm_customer_sap_partner (
    id BIGINT PRIMARY KEY DEFAULT nextval('seq_global'::regclass),
    customer_id BIGINT NOT NULL,
    partner_type VARCHAR(32) NOT NULL,
    partner_code VARCHAR(32) NOT NULL,
    partner_name VARCHAR(128),
    version INT DEFAULT 0,
    created_by VARCHAR(64),
    created_time TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(64),
    updated_time TIMESTAMP WITH TIME ZONE,
    deleted INT DEFAULT 0,
    CONSTRAINT fk_sap_partner_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id)
);
CREATE INDEX idx_sap_partner_customer_id ON crm_customer_sap_partner(customer_id);

-- 6. crm_customer_contact（联系人）
CREATE TABLE crm_customer_contact (
    id BIGINT PRIMARY KEY DEFAULT nextval('seq_global'::regclass),
    customer_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    job_title VARCHAR(64),
    mobile VARCHAR(32),
    phone VARCHAR(32),
    fax VARCHAR(32),
    email VARCHAR(128),
    gender VARCHAR(16) DEFAULT 'UNKNOWN',
    is_primary INT DEFAULT 0,
    parent_contact_id BIGINT,
    remark TEXT,
    version INT DEFAULT 0,
    created_by VARCHAR(64),
    created_time TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(64),
    updated_time TIMESTAMP WITH TIME ZONE,
    deleted INT DEFAULT 0,
    CONSTRAINT fk_contact_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id),
    CONSTRAINT fk_contact_parent FOREIGN KEY (parent_contact_id) REFERENCES crm_customer_contact(id)
);
CREATE INDEX idx_contact_customer_id ON crm_customer_contact(customer_id);
CREATE INDEX idx_contact_parent_id ON crm_customer_contact(parent_contact_id);

-- 7. crm_customer_bundle（捆绑关系）
CREATE TABLE crm_customer_bundle (
    id BIGINT PRIMARY KEY DEFAULT nextval('seq_global'::regclass),
    parent_customer_id BIGINT NOT NULL,
    child_customer_id BIGINT NOT NULL,
    bundle_type VARCHAR(32),
    bundle_brand VARCHAR(64),
    version INT DEFAULT 0,
    created_by VARCHAR(64),
    created_time TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(64),
    updated_time TIMESTAMP WITH TIME ZONE,
    deleted INT DEFAULT 0,
    CONSTRAINT fk_bundle_parent FOREIGN KEY (parent_customer_id) REFERENCES crm_customer(id),
    CONSTRAINT fk_bundle_child FOREIGN KEY (child_customer_id) REFERENCES crm_customer(id)
);
CREATE INDEX idx_bundle_parent_id ON crm_customer_bundle(parent_customer_id);
CREATE INDEX idx_bundle_child_id ON crm_customer_bundle(child_customer_id);

-- 8. crm_customer_attachment（附件）
CREATE TABLE crm_customer_attachment (
    id BIGINT PRIMARY KEY DEFAULT nextval('seq_global'::regclass),
    customer_id BIGINT NOT NULL,
    attachment_type VARCHAR(32) NOT NULL,
    file_name VARCHAR(256) NOT NULL,
    file_url VARCHAR(512) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(64),
    uploaded_by BIGINT,
    uploaded_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(64),
    created_time TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(64),
    updated_time TIMESTAMP WITH TIME ZONE,
    deleted INT DEFAULT 0,
    CONSTRAINT fk_attachment_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id)
);
CREATE INDEX idx_attachment_customer_id ON crm_customer_attachment(customer_id);
