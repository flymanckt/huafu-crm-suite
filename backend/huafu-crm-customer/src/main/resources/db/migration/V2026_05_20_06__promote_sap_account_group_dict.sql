-- Keep SAP account group visible on the first dictionary-management page.

UPDATE sys_dict_type
SET sort_order = 26,
    dict_type = 'CUSTOM',
    status = 1,
    deleted = 0,
    updated_at = CURRENT_TIMESTAMP
WHERE dict_code = 'sap_account_group';
