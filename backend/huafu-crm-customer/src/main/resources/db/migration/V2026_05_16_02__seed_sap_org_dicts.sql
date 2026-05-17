-- Seed SAP organization dictionaries used by the customer SAP organization tab.

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
