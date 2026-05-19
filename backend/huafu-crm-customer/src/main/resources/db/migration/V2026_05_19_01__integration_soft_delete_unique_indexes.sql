-- Allow integration codes to be reused after a row is logically deleted.
ALTER TABLE integration_connection_config DROP CONSTRAINT IF EXISTS integration_connection_config_connection_code_key;
ALTER TABLE sap_rfc_config DROP CONSTRAINT IF EXISTS sap_rfc_config_config_code_key;
ALTER TABLE integration_interface DROP CONSTRAINT IF EXISTS integration_interface_interface_code_key;

CREATE UNIQUE INDEX IF NOT EXISTS uk_integration_connection_config_code_active
    ON integration_connection_config(connection_code)
    WHERE deleted = 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_sap_rfc_config_code_active
    ON sap_rfc_config(config_code)
    WHERE deleted = 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_integration_interface_code_active
    ON integration_interface(interface_code)
    WHERE deleted = 0;
