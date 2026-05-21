-- Restore compatibility columns used by the customer master pages and SAP detail tabs.
-- Some environments already created the tables before later field additions, so keep this idempotent.

ALTER TABLE crm_customer_sap_info ADD COLUMN IF NOT EXISTS account_group VARCHAR(64);
ALTER TABLE crm_customer_sap_info ADD COLUMN IF NOT EXISTS country_code VARCHAR(16);

ALTER TABLE crm_customer_sap_org ADD COLUMN IF NOT EXISTS sales_group VARCHAR(64);

ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS bundle_customer_name VARCHAR(255);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS bundle_brand VARCHAR(255);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS blacklist SMALLINT DEFAULT 0;
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS sales_group VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS account_assignment_group VARCHAR(32);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS country_code VARCHAR(8);

COMMENT ON COLUMN crm_customer_sap_info.account_group IS 'SAP account group';
COMMENT ON COLUMN crm_customer_sap_info.country_code IS 'SAP country code';
COMMENT ON COLUMN crm_customer_sap_org.sales_group IS 'SAP sales group';
COMMENT ON COLUMN crm_customer.bundle_customer_name IS 'Compatibility field: bundle customer name';
COMMENT ON COLUMN crm_customer.bundle_brand IS 'Compatibility field: bundle brand';
