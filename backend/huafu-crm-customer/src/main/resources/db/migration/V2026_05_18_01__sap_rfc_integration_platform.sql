-- Generic integration platform foundation with SAP RFC support.

CREATE TABLE IF NOT EXISTS integration_connection_config (
    id BIGSERIAL PRIMARY KEY,
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

COMMENT ON TABLE integration_connection_config IS 'Generic external connection configuration';
COMMENT ON COLUMN integration_connection_config.connection_type IS 'REST, SOAP, WEBHOOK, SAP_RFC, SAP_ODATA, SAP_IDOC, SFTP, FTP, DATABASE, KAFKA, RABBITMQ, CUSTOM';
COMMENT ON COLUMN integration_connection_config.auth_type IS 'NONE, BASIC, BEARER, API_KEY, OAUTH2, SIGNATURE, CUSTOM';
COMMENT ON COLUMN integration_connection_config.auth_config IS 'Sensitive auth config JSON, should be encrypted or protected';
COMMENT ON COLUMN integration_connection_config.header_config IS 'Default request headers JSON';

CREATE TABLE IF NOT EXISTS sap_rfc_config (
    id BIGSERIAL PRIMARY KEY,
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

COMMENT ON TABLE sap_rfc_config IS 'SAP RFC connection configuration';
COMMENT ON COLUMN sap_rfc_config.config_code IS 'Unique connection code, for example SAP_PRD';
COMMENT ON COLUMN sap_rfc_config.password_cipher IS 'Encrypted or protected SAP RFC password';

CREATE TABLE IF NOT EXISTS integration_interface (
    id BIGSERIAL PRIMARY KEY,
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
    enabled SMALLINT NOT NULL DEFAULT 1,
    retry_limit INTEGER NOT NULL DEFAULT 3,
    description VARCHAR(500),
    created_by BIGINT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE integration_interface IS 'External integration interface definition';
COMMENT ON COLUMN integration_interface.direction IS 'OUTBOUND, INBOUND or BIDIRECTIONAL';
COMMENT ON COLUMN integration_interface.protocol IS 'REST, SOAP, WEBHOOK, SAP_RFC, SAP_ODATA, SAP_IDOC, SFTP, FTP, DATABASE, KAFKA, RABBITMQ, CUSTOM';
COMMENT ON COLUMN integration_interface.sap_function_name IS 'SAP RFC/BAPI function name';

CREATE TABLE IF NOT EXISTS integration_field_mapping (
    id BIGSERIAL PRIMARY KEY,
    interface_id BIGINT NOT NULL,
    source_field VARCHAR(128) NOT NULL,
    target_field VARCHAR(128) NOT NULL,
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

CREATE INDEX IF NOT EXISTS idx_integration_field_mapping_interface
    ON integration_field_mapping(interface_id, deleted, sort_order);

COMMENT ON TABLE integration_field_mapping IS 'Interface field mapping rule';
COMMENT ON COLUMN integration_field_mapping.transform_rule IS 'Simple transform rule or expression reserved for later executor';

CREATE TABLE IF NOT EXISTS integration_log (
    id BIGSERIAL PRIMARY KEY,
    interface_code VARCHAR(64) NOT NULL,
    interface_name VARCHAR(128),
    direction VARCHAR(32),
    business_key VARCHAR(128),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    request_payload TEXT,
    response_payload TEXT,
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

CREATE INDEX IF NOT EXISTS idx_integration_log_interface_status
    ON integration_log(interface_code, status, deleted, created_time DESC);

COMMENT ON TABLE integration_log IS 'Integration execution log and retry queue';
COMMENT ON COLUMN integration_log.status IS 'PENDING, RUNNING, SUCCESS, FAILED, RETRYING';

INSERT INTO sys_config (config_key, config_value, config_name, config_group, type, editable, visible, description)
VALUES
('sap.default_connection', 'SAP_PRD', 'SAP默认RFC连接', 'SAP', 'STRING', true, true, 'SAP RFC 默认连接编码'),
('sap.retry_limit', '3', 'SAP接口默认重试次数', 'SAP', 'INT', true, true, '集成平台异常数据默认可重推次数'),
('sap.timeout_ms', '30000', 'SAP RFC默认超时时间', 'SAP', 'INT', true, true, 'SAP RFC 调用默认超时时间，单位毫秒'),
('integration.default_timeout_ms', '30000', '集成默认超时时间', 'INTEGRATION', 'INT', true, true, '通用集成接口默认超时时间，单位毫秒'),
('integration.default_retry_limit', '3', '集成默认重试次数', 'INTEGRATION', 'INT', true, true, '通用集成接口异常重推默认次数')
ON CONFLICT (config_key) DO UPDATE SET
    config_name = EXCLUDED.config_name,
    config_group = EXCLUDED.config_group,
    type = EXCLUDED.type,
    editable = EXCLUDED.editable,
    visible = EXCLUDED.visible,
    description = EXCLUDED.description,
    updated_time = CURRENT_TIMESTAMP;
