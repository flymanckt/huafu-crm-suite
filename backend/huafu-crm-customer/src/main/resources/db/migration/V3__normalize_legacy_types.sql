-- Normalize legacy Phase 0 column types to current MyBatis-Plus entity types.
-- Physical names are English only; Chinese meanings stay in comments.

ALTER TABLE IF EXISTS crm_customer ALTER COLUMN deleted DROP DEFAULT;
ALTER TABLE IF EXISTS crm_lead ALTER COLUMN deleted DROP DEFAULT;
ALTER TABLE IF EXISTS crm_opportunity ALTER COLUMN deleted DROP DEFAULT;
ALTER TABLE IF EXISTS crm_lost_order ALTER COLUMN deleted DROP DEFAULT;
ALTER TABLE IF EXISTS crm_daily_report ALTER COLUMN deleted DROP DEFAULT;
ALTER TABLE IF EXISTS crm_customer ALTER COLUMN owner_user_id DROP DEFAULT;
ALTER TABLE IF EXISTS crm_lead ALTER COLUMN source DROP DEFAULT;
ALTER TABLE IF EXISTS crm_opportunity ALTER COLUMN stage DROP DEFAULT;

ALTER TABLE IF EXISTS crm_customer ALTER COLUMN deleted TYPE SMALLINT USING CASE WHEN deleted::text IN ('true','1') THEN 1 ELSE 0 END;
ALTER TABLE IF EXISTS crm_lead ALTER COLUMN deleted TYPE SMALLINT USING CASE WHEN deleted::text IN ('true','1') THEN 1 ELSE 0 END;
ALTER TABLE IF EXISTS crm_opportunity ALTER COLUMN deleted TYPE SMALLINT USING CASE WHEN deleted::text IN ('true','1') THEN 1 ELSE 0 END;
ALTER TABLE IF EXISTS crm_lost_order ALTER COLUMN deleted TYPE SMALLINT USING CASE WHEN deleted::text IN ('true','1') THEN 1 ELSE 0 END;
ALTER TABLE IF EXISTS crm_daily_report ALTER COLUMN deleted TYPE SMALLINT USING CASE WHEN deleted::text IN ('true','1') THEN 1 ELSE 0 END;

ALTER TABLE IF EXISTS crm_customer ALTER COLUMN deleted SET DEFAULT 0;
ALTER TABLE IF EXISTS crm_lead ALTER COLUMN deleted SET DEFAULT 0;
ALTER TABLE IF EXISTS crm_opportunity ALTER COLUMN deleted SET DEFAULT 0;
ALTER TABLE IF EXISTS crm_lost_order ALTER COLUMN deleted SET DEFAULT 0;
ALTER TABLE IF EXISTS crm_daily_report ALTER COLUMN deleted SET DEFAULT 0;

ALTER TABLE IF EXISTS crm_customer ALTER COLUMN owner_user_id TYPE BIGINT USING NULLIF(owner_user_id::text, '')::BIGINT;
ALTER TABLE IF EXISTS crm_lead ALTER COLUMN source TYPE SMALLINT USING CASE WHEN source::text ~ '^[0-9]+$' THEN source::text::SMALLINT ELSE 1 END;
ALTER TABLE IF EXISTS crm_opportunity ALTER COLUMN stage TYPE SMALLINT USING CASE WHEN stage::text ~ '^[0-9]+$' THEN stage::text::SMALLINT ELSE 1 END;
