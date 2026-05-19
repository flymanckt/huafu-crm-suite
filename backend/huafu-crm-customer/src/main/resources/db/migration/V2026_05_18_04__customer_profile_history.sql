-- P0-3: 客户画像历史版本
CREATE TABLE IF NOT EXISTS crm_customer_profile_history (
    id              BIGSERIAL PRIMARY KEY,
    profile_id      BIGINT        NOT NULL,
    version         INT           NOT NULL DEFAULT 1,
    operated_by     VARCHAR(64)             DEFAULT 'system',
    operated_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    -- 快照字段（与 crm_customer_profile 保持一致）
    customer_id              BIGINT,
    industry_position        VARCHAR(256),
    main_customer_group      VARCHAR(256),
    main_brands             VARCHAR(512),
    yarn_volume_summary      VARCHAR(512),
    competitor_summary      VARCHAR(512),
    machine_summary         VARCHAR(512),
    other_assets            VARCHAR(512),
    overview_auto           TEXT,
    overview_manual         TEXT,
    overview_editable       SMALLINT,
    tags                    TEXT,
    remark                  VARCHAR(1024),
    customer_stage          VARCHAR(64),
    created_by              VARCHAR(64),
    created_time            TIMESTAMPTZ,
    updated_by              VARCHAR(64),
    updated_time            TIMESTAMPTZ,
    deleted                 SMALLINT DEFAULT 0,
    tenant_id               BIGINT,
    version_num             INT
);

COMMENT ON TABLE crm_customer_profile_history IS '客户画像历史版本表';
COMMENT ON COLUMN crm_customer_profile_history.profile_id IS '关联的画像ID';
COMMENT ON COLUMN crm_customer_profile_history.version IS '版本号';
COMMENT ON COLUMN crm_customer_profile_history.operated_by IS '操作人';
COMMENT ON COLUMN crm_customer_profile_history.operated_at IS '操作时间';
CREATE INDEX IF NOT EXISTS idx_profile_history_profile_id ON crm_customer_profile_history(profile_id);
CREATE INDEX IF NOT EXISTS idx_profile_history_customer_id ON crm_customer_profile_history(customer_id);
CREATE INDEX IF NOT EXISTS idx_profile_history_version ON crm_customer_profile_history(version DESC);
