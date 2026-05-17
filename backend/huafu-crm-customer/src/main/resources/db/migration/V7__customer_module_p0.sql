-- V7: 客户管理模块 P0 扩展
-- 2026-05-08
-- 遵循 PostgreSQL 语法，ADD COLUMN IF NOT EXISTS 确保可重复执行

-- ============================================================
-- 1. crm_customer 主档扩展字段
-- ============================================================
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS customer_category VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS customer_segment VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS business_type SMALLINT;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS country_region VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS main_brand VARCHAR(255);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS annual_yarn_volume DECIMAL(12,2);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS machine_count INT;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS production_capacity VARCHAR(255);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS industry_position VARCHAR(255);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS main_customer_group VARCHAR(255);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS owner_dept_id BIGINT;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS sales_merchandiser VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS location_lat DECIMAL(10,6);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS location_lng DECIMAL(10,6);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS unified_social_credit_code VARCHAR(32);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS english_name VARCHAR(255);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS asset_type VARCHAR(32);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS customer_source VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS customer_stage SMALLINT;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS competitor_share_json TEXT;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS cooperation_brand_json TEXT;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS blacklist SMALLINT DEFAULT 0;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS risk_level SMALLINT DEFAULT 1;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS tax_id VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS bank_name VARCHAR(128);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS bank_account VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS invoice_title VARCHAR(255);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS company_code VARCHAR(32);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS sales_group VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS price_list VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS currency VARCHAR(8);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS delivery_factory VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS account_assignment_group VARCHAR(32);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS tax_classification VARCHAR(32);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS ship_to_party VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS sold_to_party VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS payer_party VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS country_code VARCHAR(8);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS region VARCHAR(64);

-- ============================================================
-- 2. crm_customer_contact 扩展字段
-- ============================================================
ALTER TABLE crm_customer_contact ADD COLUMN IF NOT EXISTS telephone VARCHAR(32);
ALTER TABLE crm_customer_contact ADD COLUMN IF NOT EXISTS fax VARCHAR(32);
ALTER TABLE crm_customer_contact ADD COLUMN IF NOT EXISTS parent_contact_id BIGINT;
ALTER TABLE crm_customer_contact ADD COLUMN IF NOT EXISTS is_active SMALLINT DEFAULT 1;

-- ============================================================
-- 3. crm_customer_address（多地址表）
-- ============================================================
CREATE TABLE IF NOT EXISTS crm_customer_address (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    customer_id BIGINT NOT NULL,
    address_type SMALLINT NOT NULL DEFAULT 1,
    contact_name VARCHAR(64),
    phone VARCHAR(32),
    province VARCHAR(32),
    city VARCHAR(32),
    district VARCHAR(32),
    address_detail VARCHAR(255),
    is_default SMALLINT DEFAULT 0,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0
);
COMMENT ON TABLE crm_customer_address IS '客户地址表';
COMMENT ON COLUMN crm_customer_address.address_type IS '1注册地 2工厂 3办事处 4仓库 5收货地址';
CREATE INDEX IF NOT EXISTS idx_customer_address_cid ON crm_customer_address(customer_id);
CREATE INDEX IF NOT EXISTS idx_customer_address_type ON crm_customer_address(address_type);

-- ============================================================
-- 4. crm_customer_profile（画像文本表）
-- ============================================================
CREATE TABLE IF NOT EXISTS crm_customer_profile (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    customer_id BIGINT NOT NULL UNIQUE,
    industry_position TEXT,
    main_customer_group TEXT,
    main_brands TEXT,
    yarn_volume_summary TEXT,
    competitor_summary TEXT,
    machine_summary TEXT,
    other_assets TEXT,
    overview_auto TEXT,
    overview_manual TEXT,
    overview_editable SMALLINT DEFAULT 1,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0
);
COMMENT ON TABLE crm_customer_profile IS '客户画像表';

-- ============================================================
-- 5. crm_customer_transfer 扩展字段（交接记录增强）
-- ============================================================
ALTER TABLE crm_customer_transfer ADD COLUMN IF NOT EXISTS supervisor_user_id BIGINT;
ALTER TABLE crm_customer_transfer ADD COLUMN IF NOT EXISTS transfer_date DATE;
ALTER TABLE crm_customer_transfer ADD COLUMN IF NOT EXISTS business_summary TEXT;
ALTER TABLE crm_customer_transfer ADD COLUMN IF NOT EXISTS contact_info_snapshot TEXT;
ALTER TABLE crm_customer_transfer ADD COLUMN IF NOT EXISTS receivable_info TEXT;
ALTER TABLE crm_customer_transfer ADD COLUMN IF NOT EXISTS cooperation_issues TEXT;
ALTER TABLE crm_customer_transfer ADD COLUMN IF NOT EXISTS future_opportunities TEXT;
ALTER TABLE crm_customer_transfer ADD COLUMN IF NOT EXISTS handover_attachments TEXT;
ALTER TABLE crm_customer_transfer ADD COLUMN IF NOT EXISTS status SMALLINT DEFAULT 1;
ALTER TABLE crm_customer_transfer ADD COLUMN IF NOT EXISTS created_by VARCHAR(64) DEFAULT '';
ALTER TABLE crm_customer_transfer ADD COLUMN IF NOT EXISTS created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE crm_customer_transfer ADD COLUMN IF NOT EXISTS updated_by VARCHAR(64) DEFAULT '';
ALTER TABLE crm_customer_transfer ADD COLUMN IF NOT EXISTS updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE crm_customer_transfer ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_customer_transfer ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_customer_transfer ADD COLUMN IF NOT EXISTS version INT DEFAULT 0;
COMMENT ON COLUMN crm_customer_transfer.status IS '1待确认 2已确认 3已完成';

-- ============================================================
-- 6. 联系人表索引（parent_contact_id 树形结构）
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_customer_contact_parent ON crm_customer_contact(parent_contact_id);
CREATE INDEX IF NOT EXISTS idx_customer_contact_role ON crm_customer_contact(role_type);
CREATE INDEX IF NOT EXISTS idx_transfer_cid ON crm_customer_transfer(customer_id);
