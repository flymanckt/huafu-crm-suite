ALTER TABLE crm_wecom_message_log
    ADD COLUMN IF NOT EXISTS related_report_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_crm_wecom_message_log_related_report
    ON crm_wecom_message_log(related_report_id);
