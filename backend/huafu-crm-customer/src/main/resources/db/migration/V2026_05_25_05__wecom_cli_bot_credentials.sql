-- Add simplified Bot ID / Secret configuration for the wecom-cli inbound agent.
INSERT INTO sys_config(config_key, config_value, config_name, config_group, type, editable, visible, description)
VALUES
('wecom.cli_bot_id', '', '企微智能机器人Bot ID', 'WECOM', 'STRING', true, true, 'wecom-cli init手动配置时使用的Bot ID'),
('wecom.cli_bot_secret', '', '企微智能机器人Secret', 'WECOM', 'STRING', true, true, 'wecom-cli init手动配置时使用的Secret；页面按敏感配置脱敏展示')
ON CONFLICT (config_key) DO UPDATE SET
    config_name = EXCLUDED.config_name,
    description = EXCLUDED.description,
    visible = EXCLUDED.visible,
    editable = EXCLUDED.editable,
    updated_time = CURRENT_TIMESTAMP;
