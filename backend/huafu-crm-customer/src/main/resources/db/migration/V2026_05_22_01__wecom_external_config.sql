-- Enterprise WeChat external-system configuration and integration platform support.

INSERT INTO sys_config (config_key, config_value, config_name, config_group, type, editable, visible, description)
VALUES
('wecom.robot_webhook_url', 'https://qyapi.weixin.qq.com/cgi-bin/webhook/send', '企微群机器人Webhook', 'WECOM', 'STRING', true, true, '企业微信群机器人 Webhook 完整地址'),
('wecom.robot_key', '', '企微机器人Key', 'WECOM', 'STRING', true, true, 'Webhook URL 中的 key 参数；如果 Webhook 地址已包含 key 可留空'),
('wecom.mentioned_list', '', '企微默认@成员ID', 'WECOM', 'STRING', true, true, '文本消息默认 @ 的成员ID，多个用逗号分隔，@all 表示所有人'),
('wecom.mentioned_mobile_list', '', '企微默认@手机号', 'WECOM', 'STRING', true, true, '文本消息默认 @ 的手机号，多个用逗号分隔'),
('wecom.default_msg_type', 'text', '企微默认消息类型', 'WECOM', 'STRING', true, true, '群机器人默认消息类型：text/markdown'),
('wecom.default_content', '', '企微默认消息内容', 'WECOM', 'STRING', true, true, '接口未映射消息内容时使用的兜底文本')
ON CONFLICT (config_key) DO UPDATE SET
    config_name = EXCLUDED.config_name,
    config_group = EXCLUDED.config_group,
    type = EXCLUDED.type,
    editable = EXCLUDED.editable,
    visible = EXCLUDED.visible,
    description = EXCLUDED.description,
    updated_time = CURRENT_TIMESTAMP;

COMMENT ON COLUMN integration_connection_config.connection_type IS
    'REST, SOAP, WEBHOOK, WECOM, SAP_RFC, SAP_ODATA, SAP_IDOC, SFTP, FTP, DATABASE, KAFKA, RABBITMQ, CUSTOM';

COMMENT ON COLUMN integration_interface.protocol IS
    'REST, SOAP, WEBHOOK, WECOM, SAP_RFC, SAP_ODATA, SAP_IDOC, SFTP, FTP, DATABASE, KAFKA, RABBITMQ, CUSTOM';
