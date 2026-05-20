-- Allow CRM to create customer SAP info before SAP returns the final customer number,
-- and ensure the SAP_CUS interface definition exists for outbound queue logs.

ALTER TABLE crm_customer_sap_info
    ALTER COLUMN sap_code DROP NOT NULL;

COMMENT ON COLUMN crm_customer_sap_info.sap_code IS
    'SAP客户编号，可由SAP_CUS接口返回后回填';

INSERT INTO integration_interface (
    interface_code,
    interface_name,
    system_code,
    connection_code,
    protocol,
    direction,
    business_module,
    sap_function_name,
    enabled,
    retry_limit,
    description,
    created_time,
    updated_time,
    deleted
)
VALUES (
    'SAP_CUS',
    'SAP客户主数据维护',
    'SAP',
    COALESCE((SELECT config_value FROM sys_config WHERE config_key = 'sap.default_connection' LIMIT 1), 'SAP_PRD'),
    'SAP_RFC',
    'OUTBOUND',
    'CUSTOMER',
    'ZRFC_CRM_CUS_BASEINFO_MAINTAIN',
    1,
    COALESCE((SELECT NULLIF(config_value, '')::INTEGER FROM sys_config WHERE config_key = 'sap.retry_limit' LIMIT 1), 3),
    '客户主数据SAP信息保存后自动生成待推送任务，SAP可通过回传接口回填SAP编号。',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
)
ON CONFLICT (interface_code) WHERE deleted = 0 DO UPDATE SET
    interface_name = COALESCE(NULLIF(integration_interface.interface_name, ''), EXCLUDED.interface_name),
    system_code = COALESCE(NULLIF(integration_interface.system_code, ''), EXCLUDED.system_code),
    connection_code = COALESCE(NULLIF(integration_interface.connection_code, ''), EXCLUDED.connection_code),
    protocol = COALESCE(NULLIF(integration_interface.protocol, ''), EXCLUDED.protocol),
    direction = COALESCE(NULLIF(integration_interface.direction, ''), EXCLUDED.direction),
    business_module = COALESCE(NULLIF(integration_interface.business_module, ''), EXCLUDED.business_module),
    sap_function_name = COALESCE(NULLIF(integration_interface.sap_function_name, ''), EXCLUDED.sap_function_name),
    enabled = 1,
    retry_limit = COALESCE(integration_interface.retry_limit, EXCLUDED.retry_limit),
    description = COALESCE(NULLIF(integration_interface.description, ''), EXCLUDED.description),
    updated_time = CURRENT_TIMESTAMP,
    deleted = 0;
