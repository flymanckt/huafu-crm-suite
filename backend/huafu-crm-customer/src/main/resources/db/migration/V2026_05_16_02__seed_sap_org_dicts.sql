-- Seed SAP organization dictionaries used by the customer SAP organization tab.

CREATE SEQUENCE IF NOT EXISTS seq_dict_type START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_dict_item START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_user_column_config START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_user_filter_config START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS sys_dict_type (
    id BIGINT NOT NULL DEFAULT nextval('seq_dict_type'),
    dict_code VARCHAR(64) NOT NULL,
    dict_name VARCHAR(128) NOT NULL,
    dict_type VARCHAR(32) NOT NULL,
    description VARCHAR(256),
    sort_order INT DEFAULT 0,
    status SMALLINT DEFAULT 1,
    created_by BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    version INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_dict_item (
    id BIGINT NOT NULL DEFAULT nextval('seq_dict_item'),
    dict_id BIGINT NOT NULL,
    item_code VARCHAR(64) NOT NULL,
    item_name VARCHAR(128) NOT NULL,
    item_value VARCHAR(256),
    sort_order INT DEFAULT 0,
    status SMALLINT DEFAULT 1,
    default_flag SMALLINT DEFAULT 0,
    show_code SMALLINT DEFAULT 1,
    description VARCHAR(256),
    color VARCHAR(32),
    css_class VARCHAR(64),
    created_by BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    version INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_user_column_config (
    id BIGINT NOT NULL DEFAULT nextval('seq_user_column_config'),
    user_id BIGINT NOT NULL,
    page_code VARCHAR(64) NOT NULL,
    column_configs TEXT NOT NULL,
    is_default SMALLINT DEFAULT 0,
    config_name VARCHAR(64),
    created_by BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    version INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_user_filter_config (
    id BIGINT NOT NULL DEFAULT nextval('seq_user_filter_config'),
    user_id BIGINT NOT NULL,
    page_code VARCHAR(64) NOT NULL,
    filter_configs TEXT NOT NULL,
    config_name VARCHAR(64),
    is_default SMALLINT DEFAULT 0,
    created_by BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    version INT DEFAULT 0
);

ALTER TABLE sys_dict_type ALTER COLUMN id SET DEFAULT nextval('seq_dict_type');
ALTER TABLE sys_dict_item ALTER COLUMN id SET DEFAULT nextval('seq_dict_item');
ALTER TABLE sys_user_column_config ALTER COLUMN id SET DEFAULT nextval('seq_user_column_config');
ALTER TABLE sys_user_filter_config ALTER COLUMN id SET DEFAULT nextval('seq_user_filter_config');

ALTER TABLE sys_dict_item ADD COLUMN IF NOT EXISTS show_code SMALLINT DEFAULT 1;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'sys_dict_type'::regclass AND contype = 'p') THEN
        ALTER TABLE sys_dict_type ADD CONSTRAINT pk_sys_dict_type PRIMARY KEY (id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_dict_code') THEN
        ALTER TABLE sys_dict_type ADD CONSTRAINT uk_dict_code UNIQUE (dict_code);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'sys_dict_item'::regclass AND contype = 'p') THEN
        ALTER TABLE sys_dict_item ADD CONSTRAINT pk_sys_dict_item PRIMARY KEY (id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_dict_id_item_code') THEN
        ALTER TABLE sys_dict_item ADD CONSTRAINT uk_dict_id_item_code UNIQUE (dict_id, item_code);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'sys_user_column_config'::regclass AND contype = 'p') THEN
        ALTER TABLE sys_user_column_config ADD CONSTRAINT pk_sys_user_column_config PRIMARY KEY (id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_user_page') THEN
        ALTER TABLE sys_user_column_config ADD CONSTRAINT uk_user_page UNIQUE (user_id, page_code, config_name);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'sys_user_filter_config'::regclass AND contype = 'p') THEN
        ALTER TABLE sys_user_filter_config ADD CONSTRAINT pk_sys_user_filter_config PRIMARY KEY (id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_user_page_name') THEN
        ALTER TABLE sys_user_filter_config ADD CONSTRAINT uk_user_page_name UNIQUE (user_id, page_code, config_name);
    END IF;
END
$$;

SELECT setval('seq_dict_type', GREATEST((SELECT COALESCE(MAX(id), 0) + 1 FROM sys_dict_type), 1), false);
SELECT setval('seq_dict_item', GREATEST((SELECT COALESCE(MAX(id), 0) + 1 FROM sys_dict_item), 1), false);
SELECT setval('seq_user_column_config', GREATEST((SELECT COALESCE(MAX(id), 0) + 1 FROM sys_user_column_config), 1), false);
SELECT setval('seq_user_filter_config', GREATEST((SELECT COALESCE(MAX(id), 0) + 1 FROM sys_user_filter_config), 1), false);

INSERT INTO sys_dict_type (dict_code, dict_name, dict_type, description, sort_order, status, deleted)
VALUES
('company_code', '公司代码', 'sap_org', 'SAP组织公司代码', 100, 1, 0),
('sales_org', '销售组织', 'sap_org', 'SAP销售组织', 101, 1, 0),
('sales_office', '销售办公室', 'sap_org', 'SAP销售办公室', 102, 1, 0),
('sales_group', '销售组', 'sap_org', 'SAP销售组', 103, 1, 0),
('price_list', '价格清单', 'sap_org', 'SAP价格清单', 104, 1, 0),
('currency', '货币', 'sap_org', 'SAP交易货币', 105, 1, 0),
('delivery_plant', '交货工厂', 'sap_org', 'SAP交货工厂', 106, 1, 0),
('payment_terms', '付款条件', 'sap_org', 'SAP付款条件', 107, 1, 0),
('tax_classification', '税分类', 'sap_org', 'SAP税分类', 108, 1, 0)
ON CONFLICT (dict_code) DO UPDATE
SET dict_name = EXCLUDED.dict_name,
    dict_type = EXCLUDED.dict_type,
    description = EXCLUDED.description,
    status = 1,
    deleted = 0;

WITH seed(dict_code, item_code, item_name, item_value, sort_order) AS (
    VALUES
    ('company_code', '1000', '华孚总部', '1000', 1),
    ('company_code', '1100', '华孚国内', '1100', 2),
    ('company_code', '1200', '华孚国际', '1200', 3),
    ('sales_org', '1000', '国内销售组织', '1000', 1),
    ('sales_org', '2000', '海外销售组织', '2000', 2),
    ('sales_office', 'CN01', '国内销售办公室', 'CN01', 1),
    ('sales_office', 'EX01', '海外销售办公室', 'EX01', 2),
    ('sales_group', 'G001', '重点客户组', 'G001', 1),
    ('sales_group', 'G002', '普通客户组', 'G002', 2),
    ('price_list', 'PL01', '标准价格', 'PL01', 1),
    ('price_list', 'PL02', '重点客户价格', 'PL02', 2),
    ('currency', 'CNY', '人民币', 'CNY', 1),
    ('currency', 'USD', '美元', 'USD', 2),
    ('currency', 'EUR', '欧元', 'EUR', 3),
    ('delivery_plant', 'P001', '默认交货工厂', 'P001', 1),
    ('delivery_plant', 'P002', '备用交货工厂', 'P002', 2),
    ('payment_terms', 'Z001', '月结30天', 'Z001', 1),
    ('payment_terms', 'Z002', '月结60天', 'Z002', 2),
    ('payment_terms', 'Z003', '预付款', 'Z003', 3),
    ('tax_classification', '1', '应税', '1', 1),
    ('tax_classification', '0', '免税', '0', 2)
)
INSERT INTO sys_dict_item (dict_id, item_code, item_name, item_value, sort_order, status, deleted)
SELECT t.id, s.item_code, s.item_name, s.item_value, s.sort_order, 1, 0
FROM seed s
JOIN sys_dict_type t ON t.dict_code = s.dict_code
ON CONFLICT (dict_id, item_code) DO UPDATE
SET item_name = EXCLUDED.item_name,
    item_value = EXCLUDED.item_value,
    sort_order = EXCLUDED.sort_order,
    status = 1,
    deleted = 0;
