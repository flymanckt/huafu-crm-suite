-- V11: persist customer fields that are editable in the CRM UI.
-- Hermes previously advanced some local databases to V10, so this uses V11.

ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS district VARCHAR(64);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS bundle_customer_name VARCHAR(255);
ALTER TABLE crm_customer ADD COLUMN IF NOT EXISTS bundle_brand VARCHAR(255);
