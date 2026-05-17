-- Add tax classification to customer SAP organization records.

ALTER TABLE crm_customer_sap_org ADD COLUMN IF NOT EXISTS tax_classification VARCHAR(64);

COMMENT ON COLUMN crm_customer_sap_org.tax_classification IS 'SAP组织税分类';
