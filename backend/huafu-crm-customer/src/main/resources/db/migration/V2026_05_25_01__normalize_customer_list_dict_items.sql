-- Normalize customer list dictionaries so list/search/batch UI uses the current master-data vocabulary.
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
('customer_type', '客户类型', 'business', '客户主数据客户类型', 10, 1, 0),
('customer_status', '客户状态', 'business', '客户主数据状态', 11, 1, 0),
('biz_type', '业务类型', 'business', '客户业务类型', 13, 1, 0),
('customer_category', '客户分类', 'business', '客户分类', 14, 1, 0),
('customer_source', '客户来源', 'business', '客户来源', 15, 1, 0),
('customer_group', '主要客户群体', 'business', '客户主要客户群体', 16, 1, 0)
ON CONFLICT (dict_code) DO UPDATE
SET dict_name = EXCLUDED.dict_name,
    dict_type = EXCLUDED.dict_type,
    description = EXCLUDED.description,
    sort_order = EXCLUDED.sort_order,
    status = 1,
    deleted = 0;

WITH seed(dict_code, item_code, item_name, item_value, sort_order, color) AS (
    VALUES
    ('customer_type', '1', '直接客户', '1', 1, 'primary'),
    ('customer_type', '2', '代理', '2', 2, 'warning'),
    ('customer_type', '3', '终端品牌', '3', 3, 'success'),
    ('customer_status', '1', '潜在客户', '1', 1, 'info'),
    ('customer_status', '2', '活跃客户', '2', 2, 'success'),
    ('customer_status', '3', '非活跃', '3', 3, 'warning'),
    ('customer_status', '4', '流失', '4', 4, 'danger'),
    ('customer_status', '5', '新客户', '5', 5, 'primary'),
    ('customer_status', '6', '重点客户', '6', 6, 'success'),
    ('biz_type', '1', '内销', '1', 1, 'primary'),
    ('biz_type', '2', '外销', '2', 2, 'success'),
    ('biz_type', '3', '转口', '3', 3, 'warning'),
    ('customer_category', '1', '纱线厂', '1', 1, 'primary'),
    ('customer_category', '2', '面料厂', '2', 2, 'success'),
    ('customer_category', '3', '服装厂', '3', 3, 'warning'),
    ('customer_category', '4', '贸易商', '4', 4, 'info'),
    ('customer_category', '5', '品牌商', '5', 5, 'success'),
    ('customer_source', '1', '展会', '1', 1, 'primary'),
    ('customer_source', '2', '转介绍', '2', 2, 'success'),
    ('customer_source', '3', '自主开发', '3', 3, 'warning'),
    ('customer_source', '4', '平台', '4', 4, 'info'),
    ('customer_group', '1', '外销', '1', 1, 'success'),
    ('customer_group', '2', '内销流通', '2', 2, 'primary'),
    ('customer_group', '3', '电商', '3', 3, 'warning')
)
INSERT INTO sys_dict_item (dict_id, item_code, item_name, item_value, sort_order, color, status, deleted, show_code)
SELECT t.id, s.item_code, s.item_name, s.item_value, s.sort_order, s.color, 1, 0, 0
FROM seed s
JOIN sys_dict_type t ON t.dict_code = s.dict_code
ON CONFLICT (dict_id, item_code) DO UPDATE
SET item_name = EXCLUDED.item_name,
    item_value = EXCLUDED.item_value,
    sort_order = EXCLUDED.sort_order,
    color = EXCLUDED.color,
    status = 1,
    deleted = 0,
    show_code = 0;

WITH target_types AS (
    SELECT id, dict_code
    FROM sys_dict_type
    WHERE dict_code IN (
        'customer_type',
        'customer_status',
        'biz_type',
        'customer_category',
        'customer_source',
        'customer_group'
    )
)
UPDATE sys_dict_item i
SET status = CASE
        WHEN t.dict_code = 'customer_type' AND i.item_code IN ('1', '2') THEN 1
        WHEN t.dict_code = 'customer_status' AND i.item_code IN ('1', '2') THEN 1
        WHEN t.dict_code = 'biz_type' AND i.item_code IN ('1', '2', '3') THEN 1
        WHEN t.dict_code = 'customer_category' AND i.item_code IN (
            '0001', '0002', '0003', '0004', '0005', '0006', '0007',
            '0008', '0009', '0010', '0011', '0012', '0013'
        ) THEN 1
        WHEN t.dict_code = 'customer_source' AND i.item_code IN ('1', '2', '3', '4') THEN 1
        WHEN t.dict_code = 'customer_group' AND i.item_code IN ('1', '2', '3') THEN 1
        ELSE 0
    END,
    show_code = 0,
    updated_at = CURRENT_TIMESTAMP
FROM target_types t
WHERE i.dict_id = t.id
  AND i.deleted = 0;
