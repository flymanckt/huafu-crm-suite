-- Add configurable trigger conditions for integration interfaces.

ALTER TABLE integration_interface
    ADD COLUMN IF NOT EXISTS trigger_mode VARCHAR(32) NOT NULL DEFAULT 'MANUAL',
    ADD COLUMN IF NOT EXISTS trigger_resource VARCHAR(64),
    ADD COLUMN IF NOT EXISTS trigger_condition_json TEXT;

COMMENT ON COLUMN integration_interface.trigger_mode IS 'MANUAL, ON_CREATE, ON_UPDATE, ON_SAVE, ON_DELETE, CUSTOM';
COMMENT ON COLUMN integration_interface.trigger_resource IS 'CRM resource that can trigger this interface, such as CUSTOMER, CUSTOMER_SAP_INFO, CUSTOMER_SAP_ORG, PRODUCT, ANY';
COMMENT ON COLUMN integration_interface.trigger_condition_json IS 'Optional JSON condition used by the triggering module to decide whether to enqueue the interface';

UPDATE integration_interface
SET trigger_mode = 'ON_SAVE',
    trigger_resource = 'CUSTOMER_SAP_INFO',
    updated_time = CURRENT_TIMESTAMP
WHERE interface_code = 'SAP_CUS'
  AND deleted = 0;
