-- Re-seed all dictionary types referenced by the current frontend. This is
-- intentionally idempotent so existing deployed databases can be repaired.

CREATE SEQUENCE IF NOT EXISTS seq_dict_type START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_dict_item START WITH 1 INCREMENT BY 1;

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
    show_code SMALLINT DEFAULT 0,
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

ALTER TABLE sys_dict_type ALTER COLUMN id SET DEFAULT nextval('seq_dict_type');
ALTER TABLE sys_dict_item ALTER COLUMN id SET DEFAULT nextval('seq_dict_item');
ALTER TABLE sys_dict_item ADD COLUMN IF NOT EXISTS show_code SMALLINT DEFAULT 0;
ALTER TABLE sys_dict_item ADD COLUMN IF NOT EXISTS color VARCHAR(32);
ALTER TABLE sys_dict_item ADD COLUMN IF NOT EXISTS css_class VARCHAR(64);

DELETE FROM sys_dict_item a
USING sys_dict_item b
WHERE a.dict_id = b.dict_id
  AND a.item_code = b.item_code
  AND a.id > b.id;

DELETE FROM sys_dict_type a
USING sys_dict_type b
WHERE a.dict_code = b.dict_code
  AND a.id > b.id;

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
END
$$;

SELECT setval('seq_dict_type', GREATEST((SELECT COALESCE(MAX(id), 0) + 1 FROM sys_dict_type), 1), false);
SELECT setval('seq_dict_item', GREATEST((SELECT COALESCE(MAX(id), 0) + 1 FROM sys_dict_item), 1), false);

WITH types(dict_code, dict_name, dict_type, description, sort_order) AS (
    VALUES
    ('customer_type', '客户类型', 'business', '客户主数据客户类型', 10),
    ('customer_status', '客户状态', 'business', '客户主数据状态', 11),
    ('customer_level', '客户等级', 'business', '客户主数据等级', 12),
    ('biz_type', '业务类型', 'business', '客户业务类型', 13),
    ('customer_category', '客户分类', 'business', '客户分类', 14),
    ('customer_source', '客户来源', 'business', '客户来源', 15),
    ('customer_group', '主要客户群体', 'business', '主要客户群体', 16),
    ('customer_stage', '客户阶段', 'business', '客户生命周期阶段', 17),
    ('asset_type', '资产类型', 'business', '客户资产类型', 18),
    ('yes_no', '是否', 'system', '通用是否选项', 19),
    ('risk_level', '风险等级', 'business', '客户风险等级', 20),
    ('contact_role', '联系人角色', 'business', '客户联系人角色类型', 21),
    ('contact_decision_level', '决策层级', 'business', '联系人决策层级', 22),
    ('contact_status', '联系人状态', 'business', '联系人在职状态', 23),
    ('address_type', '地址类型', 'business', '客户地址类型', 24),
    ('transfer_status', '转移状态', 'business', '客户转移状态', 25),
    ('visit_type', '拜访类型', 'business', '勤力度拜访类型', 30),
    ('checkin_result', '地址校验结果', 'business', '客户拜访地址校验结果', 31),
    ('daily_report_parse_status', '日报解析状态', 'business', 'AI日报解析状态', 32),
    ('wecom_parse_status', '企微解析状态', 'business', '企微消息解析状态', 33),
    ('performance_grade', '勤力度等级', 'business', '勤力度评分等级', 34),
    ('lead_type', '线索类型', 'business', '线索与商情类型', 40),
    ('lead_status', '线索状态', 'business', '线索处理状态', 41),
    ('lead_source', '线索来源', 'business', '线索来源', 42),
    ('opportunity_stage', '商机阶段', 'business', '商机推进阶段', 43),
    ('lost_type', '丢单原因', 'business', '丢单原因类型', 44),
    ('recovery_possible', '挽回可能', 'business', '丢单挽回可能性', 45),
    ('quote_status', '报价状态', 'business', '报价单状态', 50),
    ('target_metric_type', '目标指标类型', 'business', '目标管理指标类型', 51),
    ('user_status', '用户状态', 'system', '系统用户状态', 60),
    ('data_scope', '数据权限范围', 'system', '角色数据权限范围', 61),
    ('dict_status', '字典状态', 'system', '字典启禁用状态', 62),
    ('dict_type_category', '字典类型类别', 'system', '字典类型类别', 63),
    ('dict_color', '字典颜色', 'system', '字典标签颜色', 64),
    ('config_type', '配置值类型', 'system', '系统配置值类型', 65),
    ('module_record_status', '业务台账状态', 'business', '配置化业务台账状态', 70),
    ('ai_protocol', 'AI协议类型', 'system', 'AI接口协议类型', 80),
    ('company_code', '公司代码', 'sap_org', 'SAP公司代码', 100),
    ('sales_org', '销售组织', 'sap_org', 'SAP销售组织', 101),
    ('sales_office', '销售办公室', 'sap_org', 'SAP销售办公室', 102),
    ('sales_group', '销售组', 'sap_org', 'SAP销售组', 103),
    ('price_list', '价格清单', 'sap_org', 'SAP价格清单', 104),
    ('currency', '货币', 'sap_org', 'SAP交易货币', 105),
    ('delivery_plant', '交货工厂', 'sap_org', 'SAP交货工厂', 106),
    ('payment_terms', '付款条件', 'sap_org', 'SAP付款条件', 107),
    ('tax_classification', '税分类', 'sap_org', 'SAP税分类', 108),
    ('account_assignment_group', '账户分配组', 'sap_org', 'SAP账户分配组', 109),
    ('sap_account_group', 'SAP账户组', 'sap_org', 'SAP客户账户组', 110)
)
INSERT INTO sys_dict_type (dict_code, dict_name, dict_type, description, sort_order, status, deleted)
SELECT dict_code, dict_name, dict_type, description, sort_order, 1, 0
FROM types
ON CONFLICT (dict_code) DO UPDATE
SET dict_name = EXCLUDED.dict_name,
    dict_type = EXCLUDED.dict_type,
    description = EXCLUDED.description,
    sort_order = EXCLUDED.sort_order,
    status = 1,
    deleted = 0,
    updated_at = CURRENT_TIMESTAMP;

WITH seed(dict_code, item_code, item_name, item_value, sort_order, color, show_code) AS (
    VALUES
    ('customer_type', '1', '直接客户', '1', 1, 'primary', 0),
    ('customer_type', '2', '代理', '2', 2, 'warning', 0),
    ('customer_type', '3', '终端品牌', '3', 3, 'success', 0),
    ('customer_status', '1', '潜在客户', '1', 1, 'info', 0),
    ('customer_status', '2', '活跃客户', '2', 2, 'success', 0),
    ('customer_status', '3', '非活跃', '3', 3, 'warning', 0),
    ('customer_status', '4', '流失', '4', 4, 'danger', 0),
    ('customer_status', '5', '新客户', '5', 5, 'primary', 0),
    ('customer_status', '6', '重点客户', '6', 6, 'success', 0),
    ('customer_level', '1', 'AAA', '1', 1, 'success', 0),
    ('customer_level', '2', 'AA', '2', 2, 'success', 0),
    ('customer_level', '3', 'A', '3', 3, 'primary', 0),
    ('customer_level', '4', 'B', '4', 4, 'warning', 0),
    ('customer_level', '5', 'C', '5', 5, 'info', 0),
    ('biz_type', '1', '内销', '1', 1, 'primary', 0),
    ('biz_type', '2', '外销', '2', 2, 'success', 0),
    ('biz_type', '3', '转口', '3', 3, 'warning', 0),
    ('customer_category', '1', '纱线厂', '1', 1, 'primary', 0),
    ('customer_category', '2', '面料厂', '2', 2, 'success', 0),
    ('customer_category', '3', '服装厂', '3', 3, 'warning', 0),
    ('customer_category', '4', '贸易商', '4', 4, 'info', 0),
    ('customer_category', '5', '品牌商', '5', 5, 'success', 0),
    ('customer_source', '1', '展会', '1', 1, 'primary', 0),
    ('customer_source', '2', '转介绍', '2', 2, 'success', 0),
    ('customer_source', '3', '自主开发', '3', 3, 'warning', 0),
    ('customer_source', '4', '平台', '4', 4, 'info', 0),
    ('customer_group', '1', '外销', '1', 1, 'success', 0),
    ('customer_group', '2', '内销流通', '2', 2, 'primary', 0),
    ('customer_group', '3', '电商', '3', 3, 'warning', 0),
    ('customer_stage', '1', '初期接触', '1', 1, 'info', 0),
    ('customer_stage', '2', '意向阶段', '2', 2, 'primary', 0),
    ('customer_stage', '3', '谈判阶段', '3', 3, 'warning', 0),
    ('customer_stage', '4', '成交客户', '4', 4, 'success', 0),
    ('customer_stage', '5', '已流失', '5', 5, 'danger', 0),
    ('asset_type', '1', '国有企业', '1', 1, 'primary', 0),
    ('asset_type', '2', '民营企业', '2', 2, 'success', 0),
    ('asset_type', '3', '外资企业', '3', 3, 'warning', 0),
    ('asset_type', '4', '合资企业', '4', 4, 'info', 0),
    ('yes_no', '1', '是', '1', 1, 'success', 0),
    ('yes_no', '0', '否', '0', 2, 'info', 0),
    ('risk_level', '1', '高风险', '1', 1, 'danger', 0),
    ('risk_level', '2', '中风险', '2', 2, 'warning', 0),
    ('risk_level', '3', '低风险', '3', 3, 'success', 0),
    ('risk_level', '4', '无风险', '4', 4, 'info', 0),
    ('contact_role', '1', '老板', '1', 1, 'danger', 0),
    ('contact_role', '2', '采购', '2', 2, 'primary', 0),
    ('contact_role', '3', '营业', '3', 3, 'success', 0),
    ('contact_role', '4', '开发', '4', 4, 'warning', 0),
    ('contact_role', '5', '生产', '5', 5, 'info', 0),
    ('contact_role', '6', '财务', '6', 6, 'primary', 0),
    ('contact_role', '7', '品质', '7', 7, 'success', 0),
    ('contact_decision_level', '1', '拍板', '1', 1, 'danger', 0),
    ('contact_decision_level', '2', '否决', '2', 2, 'warning', 0),
    ('contact_decision_level', '3', '参与', '3', 3, 'primary', 0),
    ('contact_decision_level', '4', '知情', '4', 4, 'info', 0),
    ('contact_status', '1', '在职', '1', 1, 'success', 0),
    ('contact_status', '0', '离职', '0', 2, 'info', 0),
    ('address_type', '1', '注册地', '1', 1, 'primary', 0),
    ('address_type', '2', '工厂', '2', 2, 'success', 0),
    ('address_type', '3', '办事处', '3', 3, 'warning', 0),
    ('address_type', '4', '仓库', '4', 4, 'info', 0),
    ('address_type', '5', '收货地址', '5', 5, 'success', 0),
    ('address_type', '9', '其他', '9', 9, 'info', 0),
    ('visit_type', '1', '上门拜访', '1', 1, 'primary', 0),
    ('visit_type', '2', '电话拜访', '2', 2, 'success', 0),
    ('visit_type', '3', '微信拜访', '3', 3, 'warning', 0),
    ('visit_type', '4', '展会', '4', 4, 'info', 0),
    ('checkin_result', 'MATCH', '地址匹配', 'MATCH', 1, 'success', 0),
    ('checkin_result', 'NEAR', '附近', 'NEAR', 2, 'warning', 0),
    ('checkin_result', 'MISMATCH', '地址不匹配', 'MISMATCH', 3, 'danger', 0),
    ('checkin_result', 'UNKNOWN', '未校验', 'UNKNOWN', 4, 'info', 0),
    ('daily_report_parse_status', '0', '未解析', '0', 1, 'info', 0),
    ('daily_report_parse_status', '1', '解析中', '1', 2, 'warning', 0),
    ('daily_report_parse_status', '2', '成功', '2', 3, 'success', 0),
    ('daily_report_parse_status', '3', '失败', '3', 4, 'danger', 0),
    ('wecom_parse_status', '0', '未处理', '0', 1, 'info', 0),
    ('wecom_parse_status', '1', '已解析', '1', 2, 'success', 0),
    ('wecom_parse_status', '2', '解析失败', '2', 3, 'danger', 0),
    ('performance_grade', '1', 'G', '1', 1, 'success', 0),
    ('performance_grade', '2', 'C', '2', 2, 'warning', 0),
    ('performance_grade', '3', 'O', '3', 3, 'danger', 0),
    ('lead_type', '1', '商情', '1', 1, 'primary', 0),
    ('lead_type', '2', '线索', '2', 2, 'success', 0),
    ('lead_status', '1', '新线索', '1', 1, 'info', 0),
    ('lead_status', '2', '跟进中', '2', 2, 'warning', 0),
    ('lead_status', '3', '已转化', '3', 3, 'success', 0),
    ('lead_status', '4', '无效', '4', 4, 'danger', 0),
    ('lead_source', '1', '日报AI', '1', 1, 'success', 0),
    ('lead_source', '2', '展会', '2', 2, 'primary', 0),
    ('lead_source', '3', '主动开发', '3', 3, 'warning', 0),
    ('lead_source', '4', '转介绍', '4', 4, 'info', 0),
    ('opportunity_stage', '1', '初步接触', '1', 1, 'info', 0),
    ('opportunity_stage', '2', '需求确认', '2', 2, 'primary', 0),
    ('opportunity_stage', '3', '方案报价', '3', 3, 'warning', 0),
    ('opportunity_stage', '4', '合同谈判', '4', 4, 'danger', 0),
    ('opportunity_stage', '5', '成交', '5', 5, 'success', 0),
    ('lost_type', '1', '价格', '1', 1, 'danger', 0),
    ('lost_type', '2', '交期', '2', 2, 'warning', 0),
    ('lost_type', '3', '质量', '3', 3, 'danger', 0),
    ('lost_type', '4', '现货', '4', 4, 'info', 0),
    ('lost_type', '5', '其他', '5', 5, 'info', 0),
    ('recovery_possible', '1', '可能', '1', 1, 'warning', 0),
    ('recovery_possible', '2', '不可能', '2', 2, 'danger', 0),
    ('quote_status', '1', '草稿', '1', 1, 'info', 0),
    ('quote_status', '2', '已提交', '2', 2, 'warning', 0),
    ('quote_status', '3', '已确认', '3', 3, 'success', 0),
    ('quote_status', '4', '已拒绝', '4', 4, 'danger', 0),
    ('target_metric_type', '1', '销售额', '1', 1, 'primary', 0),
    ('target_metric_type', '2', '毛利', '2', 2, 'success', 0),
    ('target_metric_type', '3', '客户数', '3', 3, 'warning', 0),
    ('target_metric_type', '4', '拜访次数', '4', 4, 'info', 0),
    ('user_status', '1', '启用', '1', 1, 'success', 0),
    ('user_status', '2', '停用', '2', 2, 'info', 0),
    ('data_scope', '1', '全部数据', '1', 1, 'danger', 0),
    ('data_scope', '2', '本部门及下级', '2', 2, 'warning', 0),
    ('data_scope', '3', '本部门', '3', 3, 'primary', 0),
    ('data_scope', '4', '仅本人', '4', 4, 'info', 0),
    ('dict_status', '0', '禁用', '0', 1, 'info', 0),
    ('dict_status', '1', '启用', '1', 2, 'success', 0),
    ('dict_type_category', 'SYSTEM', '系统', 'SYSTEM', 1, 'primary', 0),
    ('dict_type_category', 'CUSTOM', '自定义', 'CUSTOM', 2, 'success', 0),
    ('dict_color', 'primary', 'primary', 'primary', 1, 'primary', 0),
    ('dict_color', 'success', 'success', 'success', 2, 'success', 0),
    ('dict_color', 'warning', 'warning', 'warning', 3, 'warning', 0),
    ('dict_color', 'danger', 'danger', 'danger', 4, 'danger', 0),
    ('dict_color', 'info', 'info', 'info', 5, 'info', 0),
    ('config_type', 'STRING', '字符串', 'STRING', 1, 'primary', 0),
    ('config_type', 'INT', '整数', 'INT', 2, 'success', 0),
    ('config_type', 'FLOAT', '小数', 'FLOAT', 3, 'warning', 0),
    ('config_type', 'BOOLEAN', '布尔', 'BOOLEAN', 4, 'info', 0),
    ('config_type', 'JSON', 'JSON', 'JSON', 5, 'primary', 0),
    ('module_record_status', 'DRAFT', '草稿', 'DRAFT', 1, 'info', 0),
    ('module_record_status', 'ACTIVE', '进行中', 'ACTIVE', 2, 'warning', 0),
    ('module_record_status', 'DONE', '已完成', 'DONE', 3, 'success', 0),
    ('ai_protocol', 'OPENAI_CHAT', 'OpenAI兼容', 'OPENAI_CHAT', 1, 'primary', 0),
    ('ai_protocol', 'ANTHROPIC_MESSAGES', 'Claude Messages', 'ANTHROPIC_MESSAGES', 2, 'success', 0),
    ('company_code', '1000', '华孚总部', '1000', 1, 'primary', 1),
    ('company_code', '1100', '华孚国内', '1100', 2, 'success', 1),
    ('company_code', '1200', '华孚国际', '1200', 3, 'warning', 1),
    ('sales_org', '1000', '国内销售组织', '1000', 1, 'primary', 1),
    ('sales_org', '2000', '海外销售组织', '2000', 2, 'success', 1),
    ('sales_office', 'CN01', '国内销售办公室', 'CN01', 1, 'primary', 1),
    ('sales_office', 'EX01', '海外销售办公室', 'EX01', 2, 'success', 1),
    ('sales_group', 'G001', '重点客户组', 'G001', 1, 'primary', 1),
    ('sales_group', 'G002', '普通客户组', 'G002', 2, 'success', 1),
    ('price_list', 'PL01', '标准价格', 'PL01', 1, 'primary', 1),
    ('price_list', 'PL02', '重点客户价格', 'PL02', 2, 'success', 1),
    ('currency', 'CNY', '人民币', 'CNY', 1, 'primary', 1),
    ('currency', 'USD', '美元', 'USD', 2, 'success', 1),
    ('currency', 'EUR', '欧元', 'EUR', 3, 'warning', 1),
    ('delivery_plant', 'P001', '默认交货工厂', 'P001', 1, 'primary', 1),
    ('delivery_plant', 'P002', '备用交货工厂', 'P002', 2, 'success', 1),
    ('payment_terms', 'Z001', '月结30天', 'Z001', 1, 'primary', 1),
    ('payment_terms', 'Z002', '月结60天', 'Z002', 2, 'success', 1),
    ('payment_terms', 'Z003', '预付款', 'Z003', 3, 'warning', 1),
    ('tax_classification', '1', '应税', '1', 1, 'primary', 1),
    ('tax_classification', '0', '免税', '0', 2, 'info', 1),
    ('account_assignment_group', '01', '国内销售', '01', 1, 'primary', 1),
    ('account_assignment_group', '02', '海外销售', '02', 2, 'success', 1),
    ('account_assignment_group', '03', '关联交易', '03', 3, 'warning', 1),
    ('sap_account_group', 'Z001', '国内客户', 'Z001', 1, 'primary', 1),
    ('sap_account_group', 'Z002', '海外客户', 'Z002', 2, 'success', 1),
    ('sap_account_group', 'Z003', '集团客户', 'Z003', 3, 'warning', 1)
)
INSERT INTO sys_dict_item (dict_id, item_code, item_name, item_value, sort_order, color, status, deleted, show_code)
SELECT t.id, s.item_code, s.item_name, s.item_value, s.sort_order, s.color, 1, 0, s.show_code
FROM seed s
JOIN sys_dict_type t ON t.dict_code = s.dict_code
ON CONFLICT (dict_id, item_code) DO UPDATE
SET item_name = EXCLUDED.item_name,
    item_value = EXCLUDED.item_value,
    sort_order = EXCLUDED.sort_order,
    color = EXCLUDED.color,
    status = 1,
    deleted = 0,
    show_code = EXCLUDED.show_code,
    updated_at = CURRENT_TIMESTAMP;
