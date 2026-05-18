-- Extend integration field mapping to support SAP/table parameters and CRM module fields.

ALTER TABLE integration_field_mapping ADD COLUMN IF NOT EXISTS parameter_mode VARCHAR(16) NOT NULL DEFAULT 'SINGLE';
ALTER TABLE integration_field_mapping ADD COLUMN IF NOT EXISTS parameter_group VARCHAR(128);
ALTER TABLE integration_field_mapping ADD COLUMN IF NOT EXISTS mapping_direction VARCHAR(16) NOT NULL DEFAULT 'OUTBOUND';
ALTER TABLE integration_field_mapping ADD COLUMN IF NOT EXISTS source_module VARCHAR(64) NOT NULL DEFAULT 'customer';
ALTER TABLE integration_field_mapping ADD COLUMN IF NOT EXISTS source_field_label VARCHAR(128);
ALTER TABLE integration_field_mapping ADD COLUMN IF NOT EXISTS target_field_label VARCHAR(128);

CREATE INDEX IF NOT EXISTS idx_integration_field_mapping_param
    ON integration_field_mapping(interface_id, parameter_mode, parameter_group, deleted, sort_order);

COMMENT ON COLUMN integration_field_mapping.parameter_mode IS 'SINGLE for scalar parameter, TABLE for table/array row field';
COMMENT ON COLUMN integration_field_mapping.parameter_group IS 'SAP IMPORT/EXPORT/TABLES table name or generic JSON group/array name';
COMMENT ON COLUMN integration_field_mapping.mapping_direction IS 'OUTBOUND, INBOUND or BIDIRECTIONAL';
COMMENT ON COLUMN integration_field_mapping.source_module IS 'CRM source module key, for example customer or product-master';
COMMENT ON COLUMN integration_field_mapping.source_field_label IS 'Display label of CRM source field';
COMMENT ON COLUMN integration_field_mapping.target_field_label IS 'Display label of external interface field';
