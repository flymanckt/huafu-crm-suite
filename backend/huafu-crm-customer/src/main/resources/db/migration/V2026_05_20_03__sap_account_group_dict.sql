-- V2026.05.20.03: SAP 账户组字典
-- 2026-05-20

INSERT INTO sys_dict_type (dict_code, dict_name, dict_type, description, sort_order, status, deleted)
VALUES ('sap_account_group', 'SAP账户组', 'sap_info', '客户SAP信息账户组', 109, 1, 0)
ON CONFLICT (dict_code) DO UPDATE
SET dict_name = EXCLUDED.dict_name,
    dict_type = EXCLUDED.dict_type,
    description = EXCLUDED.description,
    sort_order = EXCLUDED.sort_order,
    status = 1,
    deleted = 0;

WITH seed(dict_code, item_code, item_name, item_value, sort_order) AS (
    VALUES
    ('sap_account_group', 'Z001', '国内客户', 'Z001', 1),
    ('sap_account_group', 'Z002', '国外客户', 'Z002', 2),
    ('sap_account_group', 'Z003', '集团客户', 'Z003', 3),
    ('sap_account_group', 'Z004', '一次性客户', 'Z004', 4)
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
