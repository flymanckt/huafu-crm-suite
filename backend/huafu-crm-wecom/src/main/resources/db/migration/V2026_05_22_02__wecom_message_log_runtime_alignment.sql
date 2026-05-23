-- Keep the legacy CRM message-log shape and the WeCom service runtime shape compatible.

ALTER TABLE crm_wecom_message_log ADD COLUMN IF NOT EXISTS from_user_id VARCHAR(128);
ALTER TABLE crm_wecom_message_log ADD COLUMN IF NOT EXISTS to_user_id VARCHAR(128);
ALTER TABLE crm_wecom_message_log ADD COLUMN IF NOT EXISTS status VARCHAR(32) DEFAULT 'RECEIVED';
ALTER TABLE crm_wecom_message_log ADD COLUMN IF NOT EXISTS error_message TEXT;
ALTER TABLE crm_wecom_message_log ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE crm_wecom_message_log ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE crm_wecom_message_log ADD COLUMN IF NOT EXISTS from_user VARCHAR(128);
ALTER TABLE crm_wecom_message_log ADD COLUMN IF NOT EXISTS to_user VARCHAR(128);
ALTER TABLE crm_wecom_message_log ADD COLUMN IF NOT EXISTS parse_status SMALLINT DEFAULT 0;
ALTER TABLE crm_wecom_message_log ADD COLUMN IF NOT EXISTS created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE crm_wecom_message_log ADD COLUMN IF NOT EXISTS updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE crm_wecom_message_log ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0;
ALTER TABLE crm_wecom_message_log ADD COLUMN IF NOT EXISTS version INT DEFAULT 0;

UPDATE crm_wecom_message_log
SET from_user_id = COALESCE(from_user_id, from_user),
    to_user_id = COALESCE(to_user_id, to_user),
    from_user = COALESCE(from_user, from_user_id),
    to_user = COALESCE(to_user, to_user_id),
    parse_status = COALESCE(parse_status,
        CASE
            WHEN status IN ('PARSED', 'SUCCESS') THEN 1
            WHEN status IN ('FAILED', 'ERROR') THEN 2
            ELSE 0
        END::SMALLINT),
    status = COALESCE(status,
        CASE
            WHEN parse_status = 1 THEN 'PARSED'
            WHEN parse_status = 2 THEN 'FAILED'
            ELSE 'RECEIVED'
        END),
    created_at = COALESCE(created_at, created_time::timestamp),
    updated_at = COALESCE(updated_at, updated_time::timestamp),
    created_time = COALESCE(created_time, created_at),
    updated_time = COALESCE(updated_time, updated_at);

CREATE INDEX IF NOT EXISTS idx_crm_wecom_message_log_created_time
    ON crm_wecom_message_log(created_time DESC);
CREATE INDEX IF NOT EXISTS idx_crm_wecom_message_log_parse_status
    ON crm_wecom_message_log(parse_status, deleted);
