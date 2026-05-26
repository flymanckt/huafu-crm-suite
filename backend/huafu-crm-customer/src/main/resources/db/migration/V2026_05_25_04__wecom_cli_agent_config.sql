-- Configure WeCom inbound collection through a wecom-cli based external agent.
INSERT INTO sys_config(config_key, config_value, config_name, config_group, type, editable, visible, description)
VALUES
('wecom.receive_mode', 'WECOM_CLI_AGENT', '企微消息接收模式', 'WECOM', 'STRING', true, true, 'WECOM_CLI_AGENT=由wecom-cli Agent采集群消息并投递CRM；CALLBACK=企业微信应用回调投递CRM'),
('wecom.cli_binary', 'wecom-cli', 'wecom-cli命令路径', 'WECOM', 'STRING', true, true, 'Agent执行的wecom-cli命令路径；可填写绝对路径或PATH中的命令名'),
('wecom.cli_config_dir', '', 'wecom-cli配置目录', 'WECOM', 'STRING', true, true, 'wecom-cli init生成的凭证目录；为空时使用默认 ~/.config/wecom'),
('wecom.cli_poll_interval_seconds', '15', 'wecom-cli轮询间隔秒', 'WECOM', 'INT', true, true, '外部Agent拉取群消息的轮询间隔，建议不低于15秒'),
('wecom.cli_message_category', 'msg', 'wecom-cli消息品类', 'WECOM', 'STRING', true, true, 'wecom-cli命令品类，群消息相关通常使用msg'),
('wecom.cli_message_method', '', 'wecom-cli消息方法', 'WECOM', 'STRING', true, true, '通过wecom-cli msg --help查询后填写实际拉取群消息的方法名'),
('wecom.cli_room_allowlist', '', 'wecom-cli群白名单', 'WECOM', 'STRING', true, true, '允许Agent采集的群ID或群名，多个用逗号分隔；为空表示由Agent自行控制'),
('wecom.cli_command_template', 'wecom-cli msg {method} ''{jsonArgs}''', 'wecom-cli命令模板', 'WECOM', 'STRING', true, true, 'Agent执行命令模板，支持{method}和{jsonArgs}占位')
ON CONFLICT (config_key) DO UPDATE SET
    config_value = EXCLUDED.config_value,
    config_name = EXCLUDED.config_name,
    description = EXCLUDED.description,
    visible = EXCLUDED.visible,
    editable = EXCLUDED.editable,
    updated_time = CURRENT_TIMESTAMP;

UPDATE sys_config
SET config_name = '启用企微消息写入',
    description = '启用后，wecom-cli Agent或企业微信应用回调投递的消息可写入CRM；群机器人Webhook不支持接收消息',
    updated_time = CURRENT_TIMESTAMP
WHERE config_key = 'wecom.receive_enabled';

UPDATE sys_config
SET config_name = '企微消息投递地址',
    description = 'wecom-cli Agent或企业微信应用回调投递消息到CRM的URL',
    updated_time = CURRENT_TIMESTAMP
WHERE config_key = 'wecom.receive_callback_url';
