-- Switch WeCom inbound integration to the official AI Bot websocket mode.
INSERT INTO sys_config(config_key, config_value, config_name, config_group, type, editable, visible, description)
VALUES
('wecom.aibot_websocket_url', 'wss://openws.work.weixin.qq.com', '企微智能机器人WebSocket地址', 'WECOM', 'STRING', true, true, '企业微信智能机器人官方长连接网关地址，默认 wss://openws.work.weixin.qq.com')
ON CONFLICT (config_key) DO UPDATE SET
    config_name = EXCLUDED.config_name,
    description = EXCLUDED.description,
    visible = EXCLUDED.visible,
    editable = EXCLUDED.editable,
    updated_time = CURRENT_TIMESTAMP;

UPDATE sys_config
SET config_value = 'WECOM_AI_BOT',
    config_name = '企微消息接收模式',
    description = 'WECOM_AI_BOT=企业微信智能机器人官方WebSocket长连接；WECOM_CLI_AGENT=wecom-cli备用采集；CALLBACK=企业微信应用回调投递CRM',
    updated_time = CURRENT_TIMESTAMP
WHERE config_key = 'wecom.receive_mode';

UPDATE sys_config
SET description = '启用后，企业微信智能机器人长连接、wecom-cli Agent或企业微信应用回调投递的消息可写入CRM',
    updated_time = CURRENT_TIMESTAMP
WHERE config_key = 'wecom.receive_enabled';
