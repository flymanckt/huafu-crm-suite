-- Store push-time mapping values and configurable response success rules.
ALTER TABLE integration_log
    ADD COLUMN IF NOT EXISTS mapping_detail TEXT;

COMMENT ON COLUMN integration_log.mapping_detail IS
    'JSON array recording each field mapping value resolved during interface execution';

ALTER TABLE integration_interface
    ADD COLUMN IF NOT EXISTS success_rule_type VARCHAR(32) NOT NULL DEFAULT 'AUTO',
    ADD COLUMN IF NOT EXISTS success_field_path VARCHAR(256),
    ADD COLUMN IF NOT EXISTS success_expected_values VARCHAR(500),
    ADD COLUMN IF NOT EXISTS failure_expected_values VARCHAR(500),
    ADD COLUMN IF NOT EXISTS success_message_path VARCHAR(256);

COMMENT ON COLUMN integration_interface.success_rule_type IS
    'AUTO, HTTP_STATUS, TEXT_CONTAINS, JSON_FIELD, XML_FIELD or SAP_RETURN';
COMMENT ON COLUMN integration_interface.success_field_path IS
    'Response JSON/XML/message field path used to determine execution success';
COMMENT ON COLUMN integration_interface.success_expected_values IS
    'Comma or pipe separated values/keywords treated as success';
COMMENT ON COLUMN integration_interface.failure_expected_values IS
    'Comma or pipe separated values/keywords treated as failure';
COMMENT ON COLUMN integration_interface.success_message_path IS
    'Response field path shown as business return message';
