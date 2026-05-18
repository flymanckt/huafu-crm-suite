-- Align integration platform tables with the global tenant interceptor.

ALTER TABLE integration_connection_config ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE sap_rfc_config ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE integration_interface ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE integration_field_mapping ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE integration_log ADD COLUMN IF NOT EXISTS tenant_id BIGINT NOT NULL DEFAULT 1;

CREATE INDEX IF NOT EXISTS idx_integration_connection_tenant
    ON integration_connection_config(tenant_id, connection_type, deleted, enabled);

CREATE INDEX IF NOT EXISTS idx_sap_rfc_config_tenant
    ON sap_rfc_config(tenant_id, deleted, enabled);

CREATE INDEX IF NOT EXISTS idx_integration_interface_tenant
    ON integration_interface(tenant_id, system_code, protocol, deleted, enabled);

CREATE INDEX IF NOT EXISTS idx_integration_log_tenant
    ON integration_log(tenant_id, interface_code, status, deleted, created_time DESC);
