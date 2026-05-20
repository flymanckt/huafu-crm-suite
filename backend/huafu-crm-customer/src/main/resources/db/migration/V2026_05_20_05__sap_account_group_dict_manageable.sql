-- Make SAP account group visible and maintainable in the dictionary management page.

UPDATE sys_dict_type
SET dict_type = 'CUSTOM',
    dict_name = 'SAP账户组',
    description = '客户SAP信息账户组，可在字典管理维护',
    status = 1,
    deleted = 0,
    updated_at = CURRENT_TIMESTAMP
WHERE dict_code = 'sap_account_group';

INSERT INTO sys_dict_type (dict_code, dict_name, dict_type, description, sort_order, status, deleted)
SELECT 'sap_account_group', 'SAP账户组', 'CUSTOM', '客户SAP信息账户组，可在字典管理维护', 109, 1, 0
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_type WHERE dict_code = 'sap_account_group' AND deleted = 0
);
