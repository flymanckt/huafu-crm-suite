-- =====================================================
-- 华孚CRM数据字典表 DDL
-- 数据库: PostgreSQL (jdbc:postgresql://localhost:5432/huafu_crm)
-- 创建时间: 2026-05-09
-- =====================================================

-- 1. 字典类型表 sys_dict_type
CREATE TABLE sys_dict_type (
    id              BIGINT          NOT NULL,
    dict_code       VARCHAR(64)     NOT NULL,
    dict_name       VARCHAR(128)    NOT NULL,
    dict_type       VARCHAR(32)     NOT NULL,
    description     VARCHAR(256),
    sort_order      INT             DEFAULT 0,
    status          SMALLINT        DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted         SMALLINT        DEFAULT 0,
    version         INT             DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_dict_code UNIQUE (dict_code)
);

-- 2. 字典项表 sys_dict_item
CREATE TABLE sys_dict_item (
    id              BIGINT          NOT NULL,
    dict_id         BIGINT          NOT NULL,
    item_code       VARCHAR(64)     NOT NULL,
    item_name       VARCHAR(128)    NOT NULL,
    item_value      VARCHAR(256),
    sort_order      INT             DEFAULT 0,
    status          SMALLINT        DEFAULT 1,
    default_flag    SMALLINT        DEFAULT 0,
    description     VARCHAR(256),
    color           VARCHAR(32),
    css_class       VARCHAR(64),
    created_by      BIGINT,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted         SMALLINT        DEFAULT 0,
    version         INT             DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_dict_id_item_code UNIQUE (dict_id, item_code)
);

-- 3. 用户列配置表 sys_user_column_config
CREATE TABLE sys_user_column_config (
    id              BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    page_code       VARCHAR(64)     NOT NULL,
    column_configs  TEXT            NOT NULL,
    is_default      SMALLINT        DEFAULT 0,
    config_name     VARCHAR(64),
    created_by      BIGINT,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    version         INT             DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_page UNIQUE (user_id, page_code, config_name)
);

-- 4. 用户筛选配置表 sys_user_filter_config
CREATE TABLE sys_user_filter_config (
    id              BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    page_code       VARCHAR(64)     NOT NULL,
    filter_configs  TEXT            NOT NULL,
    config_name     VARCHAR(64),
    is_default      SMALLINT        DEFAULT 0,
    created_by      BIGINT,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    version         INT             DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_page_name UNIQUE (user_id, page_code, config_name)
);

-- 创建序列
CREATE SEQUENCE IF NOT EXISTS seq_dict_type START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_dict_item START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_user_column_config START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_user_filter_config START WITH 1 INCREMENT BY 1;

-- =====================================================
-- 初始化数据：10类字典
-- =====================================================

-- 客户类型
INSERT INTO sys_dict_type (id, dict_code, dict_name, dict_type, description, sort_order) 
VALUES (1, 'customer_type', '客户类型', 'SYSTEM', '客户类型枚举', 1);

INSERT INTO sys_dict_item (id, dict_id, item_code, item_name, sort_order, color) VALUES
(1, 1, 'ENTITY', '实体客户', 1, 'primary'),
(2, 1, 'TRADE', '贸易商', 2, 'warning');

-- 客户状态
INSERT INTO sys_dict_type (id, dict_code, dict_name, dict_type, description, sort_order) 
VALUES (2, 'customer_status', '客户状态', 'SYSTEM', '客户生命周期状态', 2);

INSERT INTO sys_dict_item (id, dict_id, item_code, item_name, sort_order, color) VALUES
(3, 2, 'POTENTIAL', '潜在客户', 1, 'info'),
(4, 2, 'ACTIVE', '活跃客户', 2, 'success'),
(5, 2, 'INACTIVE', '非活跃', 3, 'warning'),
(6, 2, 'LOST', '流失客户', 4, 'danger');

-- 客户等级
INSERT INTO sys_dict_type (id, dict_code, dict_name, dict_type, description, sort_order) 
VALUES (3, 'customer_level', '客户等级', 'SYSTEM', '客户分级A/B/C/D', 3);

INSERT INTO sys_dict_item (id, dict_id, item_code, item_name, sort_order, color) VALUES
(7, 3, 'A', 'A级（核心）', 1, 'danger'),
(8, 3, 'B', 'B级（重要）', 2, 'warning'),
(9, 3, 'C', 'C级（普通）', 3, 'success'),
(10, 3, 'D', 'D级（潜在）', 4, 'info');

-- 客户分类（行业分类）
INSERT INTO sys_dict_type (id, dict_code, dict_name, dict_type, description, sort_order) 
VALUES (4, 'customer_category', '客户分类', 'SYSTEM', '客户所属行业分类', 4);

INSERT INTO sys_dict_item (id, dict_id, item_code, item_name, sort_order, color) VALUES
(11, 4, 'YARN_FACTORY', '纱线厂', 1, 'primary'),
(12, 4, 'FABRIC_FACTORY', '面料厂', 2, 'success'),
(13, 4, 'GARMENT_FACTORY', '服装厂', 3, 'warning'),
(14, 4, 'TRADER', '贸易商', 4, 'info'),
(15, 4, 'OTHER', '其他', 5, '');

-- 业务类型
INSERT INTO sys_dict_type (id, dict_code, dict_name, dict_type, description, sort_order) 
VALUES (5, 'biz_type', '业务类型', 'SYSTEM', '内销/外销/转口', 5);

INSERT INTO sys_dict_item (id, dict_id, item_code, item_name, sort_order, color) VALUES
(16, 5, 'DOMESTIC', '内销', 1, 'primary'),
(17, 5, 'EXPORT', '外销', 2, 'success'),
(18, 5, 'ENTRADE', '转口', 3, 'warning');

-- 国家区域
INSERT INTO sys_dict_type (id, dict_code, dict_name, dict_type, description, sort_order) 
VALUES (6, 'region', '国家区域', 'SYSTEM', '客户所属国家/区域', 6);

INSERT INTO sys_dict_item (id, dict_id, item_code, item_name, sort_order, color) VALUES
(19, 6, 'EAST_CHINA', '华东', 1, 'primary'),
(20, 6, 'SOUTH_CHINA', '华南', 2, 'success'),
(21, 6, 'NORTH_CHINA', '华北', 3, 'info'),
(22, 6, 'OVERSEAS', '海外', 4, 'warning');

-- 客户来源
INSERT INTO sys_dict_type (id, dict_code, dict_name, dict_type, description, sort_order) 
VALUES (7, 'customer_source', '客户来源', 'SYSTEM', '客户从何而来', 7);

INSERT INTO sys_dict_item (id, dict_id, item_code, item_name, sort_order, color) VALUES
(23, 7, 'EXHIBITION', '展会', 1, 'primary'),
(24, 7, 'REFERRAL', '转介绍', 2, 'success'),
(25, 7, 'SELF_DEVELOP', '自主开发', 3, 'info'),
(26, 7, 'PLATFORM', '平台获客', 4, 'warning');

-- 资产类型
INSERT INTO sys_dict_type (id, dict_code, dict_name, dict_type, description, sort_order) 
VALUES (8, 'asset_type', '资产类型', 'SYSTEM', '企业资产类型', 8);

INSERT INTO sys_dict_item (id, dict_id, item_code, item_name, sort_order, color) VALUES
(27, 8, 'STATE_OWNED', '国有企业', 1, 'danger'),
(28, 8, 'PRIVATE', '民营企业', 2, 'primary'),
(29, 8, 'FOREIGN', '外资企业', 3, 'success'),
(30, 8, 'JOINT', '合资企业', 4, 'warning');

-- 纱线类型
INSERT INTO sys_dict_type (id, dict_code, dict_name, dict_type, description, sort_order) 
VALUES (9, 'yarn_type', '纱线类型', 'SYSTEM', '纱线品类分类', 9);

INSERT INTO sys_dict_item (id, dict_id, item_code, item_name, sort_order, color) VALUES
(31, 9, 'COLORED_YARN', '分色纺纱', 1, 'primary'),
(32, 9, 'COTTON_YARN', '全棉坯纱', 2, 'success'),
(33, 9, 'NON_COTTON_YARN', '非棉坯纱', 3, 'info'),
(34, 9, 'BLENDED_YARN', '混纺纱', 4, 'warning'),
(35, 9, 'OTHER', '其他', 5, '');

-- 合作伙伴类型
INSERT INTO sys_dict_type (id, dict_code, dict_name, dict_type, description, sort_order) 
VALUES (10, 'partner_type', '合作伙伴类型', 'SYSTEM', 'SAP合作伙伴类型', 10);

INSERT INTO sys_dict_item (id, dict_id, item_code, item_name, sort_order, color) VALUES
(36, 10, 'SHIP_TO', '收货方', 1, 'primary'),
(37, 10, 'BILL_TO', '开票方', 2, 'success'),
(38, 10, 'PAYER', '付款方', 3, 'warning'),
(39, 10, 'CONTACT', '联系人', 4, 'info'),
(40, 10, 'CARRIER', '承运商', 5, '');
