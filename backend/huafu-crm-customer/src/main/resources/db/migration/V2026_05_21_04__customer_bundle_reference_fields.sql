-- Add reference fields for the customer master bundle customer/brand selector.

ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS bundle_customer_id BIGINT;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS bundle_customer_sap_code VARCHAR(64);

CREATE INDEX IF NOT EXISTS idx_crm_customer_bundle_customer_id
    ON crm_customer(bundle_customer_id)
    WHERE deleted = 0 AND bundle_customer_id IS NOT NULL;

COMMENT ON COLUMN crm_customer.bundle_customer_id IS 'Referenced bundled customer id';
COMMENT ON COLUMN crm_customer.bundle_customer_sap_code IS 'Referenced bundled customer SAP code';
