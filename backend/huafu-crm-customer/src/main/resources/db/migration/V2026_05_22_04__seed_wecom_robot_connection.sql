-- Seed the default Enterprise WeChat group robot connection for the integration platform.

INSERT INTO integration_connection_config (
    connection_code,
    connection_name,
    connection_type,
    enabled,
    base_url,
    auth_type,
    auth_config,
    header_config,
    timeout_ms,
    remark,
    created_time,
    updated_time,
    deleted,
    tenant_id
)
SELECT
    'WECOM_DEFAULT',
    '企微群机器人默认Webhook',
    'WECOM',
    1,
    COALESCE(NULLIF((SELECT config_value FROM sys_config WHERE config_key = 'wecom.robot_webhook_url'), ''), 'https://qyapi.weixin.qq.com/cgi-bin/webhook/send'),
    'WECOM_WEBHOOK',
    json_build_object(
        'key', COALESCE((SELECT config_value FROM sys_config WHERE config_key = 'wecom.robot_key'), ''),
        'defaultMsgType', COALESCE(NULLIF((SELECT config_value FROM sys_config WHERE config_key = 'wecom.default_msg_type'), ''), 'text'),
        'defaultContent', COALESCE((SELECT config_value FROM sys_config WHERE config_key = 'wecom.default_content'), ''),
        'mentionedList', string_to_array(COALESCE((SELECT NULLIF(config_value, '') FROM sys_config WHERE config_key = 'wecom.mentioned_list'), ''), ','),
        'mentionedMobileList', string_to_array(COALESCE((SELECT NULLIF(config_value, '') FROM sys_config WHERE config_key = 'wecom.mentioned_mobile_list'), ''), ',')
    )::text,
    '{"Content-Type":"application/json"}',
    30000,
    '企业微信群机器人 Webhook 默认连接，可在外围系统配置中同步维护',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0,
    1
WHERE NOT EXISTS (
    SELECT 1
    FROM integration_connection_config
    WHERE connection_code = 'WECOM_DEFAULT'
      AND deleted = 0
);
