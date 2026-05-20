-- Store row-level fields inside one TABLE parameter mapping.
ALTER TABLE integration_field_mapping
    ADD COLUMN IF NOT EXISTS table_field_mappings TEXT;

COMMENT ON COLUMN integration_field_mapping.table_field_mappings IS
    'JSON array of row field mappings for TABLE parameter mode';
