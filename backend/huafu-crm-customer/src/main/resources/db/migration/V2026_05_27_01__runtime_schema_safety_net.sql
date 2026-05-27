-- Safety net for deployments where tables were deleted while Flyway history was kept.

CREATE TABLE IF NOT EXISTS sys_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(128) NOT NULL UNIQUE,
    config_value TEXT,
    config_name VARCHAR(255),
    config_group VARCHAR(64) NOT NULL DEFAULT 'SYSTEM',
    description VARCHAR(500),
    type VARCHAR(32) NOT NULL DEFAULT 'STRING',
    editable BOOLEAN NOT NULL DEFAULT TRUE,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS integration_connection_config (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    connection_code VARCHAR(64) NOT NULL UNIQUE,
    connection_name VARCHAR(128) NOT NULL,
    connection_type VARCHAR(32) NOT NULL DEFAULT 'REST',
    enabled SMALLINT NOT NULL DEFAULT 1,
    base_url VARCHAR(500),
    auth_type VARCHAR(32) DEFAULT 'NONE',
    auth_config TEXT,
    header_config TEXT,
    timeout_ms INTEGER DEFAULT 30000,
    remark VARCHAR(500),
    created_by BIGINT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sap_rfc_config (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    config_code VARCHAR(64) NOT NULL UNIQUE,
    config_name VARCHAR(128) NOT NULL,
    enabled SMALLINT NOT NULL DEFAULT 1,
    app_server_host VARCHAR(255),
    system_number VARCHAR(16),
    client VARCHAR(16),
    user_name VARCHAR(128),
    password_cipher TEXT,
    language VARCHAR(8) DEFAULT 'ZH',
    pool_capacity INTEGER DEFAULT 5,
    peak_limit INTEGER DEFAULT 10,
    connection_timeout INTEGER DEFAULT 30000,
    remark VARCHAR(500),
    created_by BIGINT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS integration_interface (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    interface_code VARCHAR(64) NOT NULL UNIQUE,
    interface_name VARCHAR(128) NOT NULL,
    system_code VARCHAR(64) NOT NULL DEFAULT 'SAP',
    connection_code VARCHAR(64),
    protocol VARCHAR(32) NOT NULL DEFAULT 'SAP_RFC',
    direction VARCHAR(32) NOT NULL DEFAULT 'OUTBOUND',
    business_module VARCHAR(64),
    sap_function_name VARCHAR(128),
    http_method VARCHAR(16),
    endpoint_path VARCHAR(500),
    content_type VARCHAR(64),
    trigger_mode VARCHAR(32),
    trigger_resource VARCHAR(128),
    trigger_condition_json TEXT,
    enabled SMALLINT NOT NULL DEFAULT 1,
    retry_limit INTEGER NOT NULL DEFAULT 3,
    success_rule_type VARCHAR(32) NOT NULL DEFAULT 'AUTO',
    success_field_path VARCHAR(256),
    success_expected_values VARCHAR(500),
    failure_expected_values VARCHAR(500),
    success_message_path VARCHAR(256),
    description VARCHAR(500),
    created_by BIGINT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS integration_field_mapping (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    interface_id BIGINT NOT NULL,
    parameter_mode VARCHAR(16) NOT NULL DEFAULT 'SINGLE',
    parameter_group VARCHAR(128),
    mapping_direction VARCHAR(16) NOT NULL DEFAULT 'OUTBOUND',
    source_module VARCHAR(64) NOT NULL DEFAULT 'customer',
    source_field VARCHAR(128) NOT NULL,
    source_field_label VARCHAR(128),
    target_field VARCHAR(128) NOT NULL,
    target_field_label VARCHAR(128),
    table_field_mappings TEXT,
    field_type VARCHAR(32) DEFAULT 'STRING',
    required SMALLINT NOT NULL DEFAULT 0,
    default_value VARCHAR(500),
    transform_rule TEXT,
    sort_order INTEGER DEFAULT 0,
    remark VARCHAR(500),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS integration_log (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    interface_code VARCHAR(64) NOT NULL,
    interface_name VARCHAR(128),
    direction VARCHAR(32),
    business_key VARCHAR(128),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    request_payload TEXT,
    response_payload TEXT,
    mapping_detail TEXT,
    error_message TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    next_retry_time TIMESTAMP,
    pushed_time TIMESTAMP,
    created_by BIGINT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0
);

ALTER TABLE integration_connection_config ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE sap_rfc_config ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE integration_interface ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE integration_field_mapping ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE integration_log ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 1;

ALTER TABLE integration_interface ADD COLUMN IF NOT EXISTS trigger_mode VARCHAR(32);
ALTER TABLE integration_interface ADD COLUMN IF NOT EXISTS trigger_resource VARCHAR(128);
ALTER TABLE integration_interface ADD COLUMN IF NOT EXISTS trigger_condition_json TEXT;
ALTER TABLE integration_interface ADD COLUMN IF NOT EXISTS success_rule_type VARCHAR(32) NOT NULL DEFAULT 'AUTO';
ALTER TABLE integration_interface ADD COLUMN IF NOT EXISTS success_field_path VARCHAR(256);
ALTER TABLE integration_interface ADD COLUMN IF NOT EXISTS success_expected_values VARCHAR(500);
ALTER TABLE integration_interface ADD COLUMN IF NOT EXISTS failure_expected_values VARCHAR(500);
ALTER TABLE integration_interface ADD COLUMN IF NOT EXISTS success_message_path VARCHAR(256);

ALTER TABLE integration_field_mapping ADD COLUMN IF NOT EXISTS parameter_mode VARCHAR(16) NOT NULL DEFAULT 'SINGLE';
ALTER TABLE integration_field_mapping ADD COLUMN IF NOT EXISTS parameter_group VARCHAR(128);
ALTER TABLE integration_field_mapping ADD COLUMN IF NOT EXISTS mapping_direction VARCHAR(16) NOT NULL DEFAULT 'OUTBOUND';
ALTER TABLE integration_field_mapping ADD COLUMN IF NOT EXISTS source_module VARCHAR(64) NOT NULL DEFAULT 'customer';
ALTER TABLE integration_field_mapping ADD COLUMN IF NOT EXISTS source_field_label VARCHAR(128);
ALTER TABLE integration_field_mapping ADD COLUMN IF NOT EXISTS target_field_label VARCHAR(128);
ALTER TABLE integration_field_mapping ADD COLUMN IF NOT EXISTS table_field_mappings TEXT;

ALTER TABLE integration_log ADD COLUMN IF NOT EXISTS mapping_detail TEXT;

CREATE INDEX IF NOT EXISTS idx_integration_connection_tenant
    ON integration_connection_config(tenant_id, connection_type, deleted, enabled);
CREATE INDEX IF NOT EXISTS idx_sap_rfc_config_tenant
    ON sap_rfc_config(tenant_id, deleted, enabled);
CREATE INDEX IF NOT EXISTS idx_integration_interface_tenant
    ON integration_interface(tenant_id, system_code, protocol, deleted, enabled);
CREATE INDEX IF NOT EXISTS idx_integration_field_mapping_interface
    ON integration_field_mapping(interface_id, deleted, sort_order);
CREATE INDEX IF NOT EXISTS idx_integration_field_mapping_param
    ON integration_field_mapping(interface_id, parameter_mode, parameter_group, deleted, sort_order);
CREATE INDEX IF NOT EXISTS idx_integration_log_interface_status
    ON integration_log(interface_code, status, deleted, created_time DESC);
CREATE INDEX IF NOT EXISTS idx_integration_log_tenant
    ON integration_log(tenant_id, interface_code, status, deleted, created_time DESC);

INSERT INTO sys_config (config_key, config_value, config_name, config_group, type, editable, visible, description)
VALUES
('sap.default_connection', 'SAP_PRD', 'SAP默认RFC连接', 'SAP', 'STRING', true, true, 'SAP RFC 默认连接编码'),
('sap.retry_limit', '3', 'SAP接口默认重试次数', 'SAP', 'INT', true, true, '集成平台异常数据默认可重推次数'),
('sap.timeout_ms', '30000', 'SAP RFC默认超时时间', 'SAP', 'INT', true, true, 'SAP RFC 调用默认超时时间，单位毫秒'),
('integration.default_timeout_ms', '30000', '集成默认超时时间', 'INTEGRATION', 'INT', true, true, '通用集成接口默认超时时间，单位毫秒'),
('integration.default_retry_limit', '3', '集成默认重试次数', 'INTEGRATION', 'INT', true, true, '通用集成接口异常重推默认次数')
ON CONFLICT (config_key) DO NOTHING;
