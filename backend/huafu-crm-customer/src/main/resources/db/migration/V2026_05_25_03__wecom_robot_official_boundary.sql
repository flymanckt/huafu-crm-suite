-- Align WeCom configuration with group robot webhook semantics.
INSERT INTO sys_config(config_key, config_value, config_name, config_group, type, editable, visible, description)
VALUES
('wecom.robot_supported_msg_types', 'text,markdown,image,file,news', '企微群机器人支持消息类型', 'WECOM', 'STRING', true, true, '群机器人Webhook可发送的消息类型；完整msgtype消息体会按企微官方格式原样发送'),
('wecom.robot_rate_limit_per_minute', '20', '企微群机器人发送频率限制', 'WECOM', 'INT', true, true, '企微群机器人发送频率参考值：每个机器人每分钟最多20条，超过后可能被限流')
ON CONFLICT (config_key) DO UPDATE SET
    config_value = EXCLUDED.config_value,
    config_name = EXCLUDED.config_name,
    description = EXCLUDED.description,
    visible = EXCLUDED.visible,
    editable = EXCLUDED.editable,
    updated_time = CURRENT_TIMESTAMP;

UPDATE sys_config
SET config_name = '启用企微应用回调接收',
    description = '启用后，企业微信应用回调或中间服务投递的消息可写入CRM；群机器人Webhook不支持接收消息',
    updated_time = CURRENT_TIMESTAMP
WHERE config_key = 'wecom.receive_enabled';

UPDATE sys_config
SET config_name = '企微应用回调地址',
    description = '配置到企业微信应用回调或自建中间服务的消息投递URL；不是群机器人Webhook地址',
    updated_time = CURRENT_TIMESTAMP
WHERE config_key = 'wecom.receive_callback_url';

UPDATE sys_config
SET config_name = '企微应用回调Token',
    description = '企业微信应用回调URL验证Token',
    updated_time = CURRENT_TIMESTAMP
WHERE config_key = 'wecom.receive_token';

UPDATE sys_config
SET config_name = '企微应用EncodingAESKey',
    description = '企业微信应用回调消息加解密EncodingAESKey；当前系统优先支持明文/已解密消息入库',
    updated_time = CURRENT_TIMESTAMP
WHERE config_key = 'wecom.receive_encoding_aes_key';

UPDATE sys_config
SET description = '群机器人默认消息类型：text/markdown/image/file/news；完整msgtype消息体会原样发送',
    updated_time = CURRENT_TIMESTAMP
WHERE config_key = 'wecom.default_msg_type';
