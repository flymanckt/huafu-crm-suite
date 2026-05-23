-- Enterprise WeChat message receive settings. Webhook sends CRM -> WeCom; callback receives WeCom -> CRM.

INSERT INTO sys_config (config_key, config_value, config_name, config_group, type, editable, visible, description)
VALUES
('wecom.receive_enabled', 'true', '启用企微消息接收', 'WECOM', 'BOOLEAN', true, true, '启用后，企微群内 @机器人 的消息可通过回调写入CRM'),
('wecom.receive_callback_url', '/api/wecom/callback', '企微接收回调地址', 'WECOM', 'STRING', true, true, '配置到企业微信接收消息的回调URL'),
('wecom.receive_token', '', '企微接收Token', 'WECOM', 'STRING', true, true, '企业微信回调URL验证Token'),
('wecom.receive_encoding_aes_key', '', '企微接收EncodingAESKey', 'WECOM', 'STRING', true, true, '企业微信回调消息加解密EncodingAESKey；当前系统优先支持明文/已解密消息入库'),
('wecom.receive_write_mode', 'KEYWORD_DAILY_REPORT', '企微写入策略', 'WECOM', 'STRING', true, true, 'KEYWORD_DAILY_REPORT=命中关键词写日报并AI解析；ALL_DAILY_REPORT=所有文本写日报并AI解析'),
('wecom.receive_keywords', '日报,商机,商情,丢单,拜访,客户', '企微写入关键词', 'WECOM', 'STRING', true, true, '命中这些关键词的@机器人内容会写入CRM日报并触发AI解析，多个用逗号分隔')
ON CONFLICT (config_key) DO UPDATE SET
    config_name = EXCLUDED.config_name,
    config_group = EXCLUDED.config_group,
    type = EXCLUDED.type,
    editable = EXCLUDED.editable,
    visible = EXCLUDED.visible,
    description = EXCLUDED.description,
    updated_time = CURRENT_TIMESTAMP;
