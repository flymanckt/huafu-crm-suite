-- Default external-system and integration-platform configuration.
-- Sensitive values are intentionally blank so packaged releases and GitHub do not
-- leak API keys, SAP passwords, or WeCom secrets.

WITH cfg(config_key, config_value, config_name, config_group, type, editable, visible, description) AS (
    VALUES
    ('ai.provider', 'OPENAI_COMPATIBLE', 'AI供应商', 'AI', 'STRING', true, true, 'AI模型供应商，可选 OpenAI兼容、MiniMax、通义千问、智谱、DeepSeek、Claude 等'),
    ('ai.protocol', 'OPENAI_CHAT', 'AI调用协议', 'AI', 'STRING', true, true, 'OPENAI_CHAT=OpenAI兼容Chat Completions，ANTHROPIC_MESSAGES=Claude Messages'),
    ('ai.base_url', '', 'AI服务接口地址', 'AI', 'STRING', true, true, 'AI服务Base URL，例如 https://api.openai.com/v1 或供应商兼容地址'),
    ('ai.model', '', 'AI默认模型', 'AI', 'STRING', true, true, 'AI默认调用模型，例如 gpt-4.1、deepseek-chat、qwen-plus、MiniMax-M2 等'),
    ('ai.api_key', '', 'AI服务 API Key', 'AI', 'STRING', true, true, 'AI服务API Key，请在系统部署后通过页面配置'),
    ('ai.timeout_ms', '120000', 'AI调用超时时间', 'AI', 'INT', true, true, 'AI日报/企微解析超时时间，单位毫秒'),
    ('amap.js_key', '', '高德地图 JS Key', 'AMAP', 'STRING', true, true, '高德地图 Web端 JS API Key，请在系统部署后通过页面配置'),
    ('amap.web_key', '', '高德地图 Web服务Key', 'AMAP', 'STRING', true, true, '高德地图 Web服务 Key，用于地址解析/逆解析'),
    ('amap.security_code', '', '高德地图安全密钥', 'AMAP', 'STRING', true, true, '高德 JS API 2.0 安全密钥 securityJsCode'),
    ('sap.default_connection', 'SAP_DEV', 'SAP默认RFC连接', 'SAP', 'STRING', true, true, 'SAP RFC 默认连接编码，对应集成平台SAP RFC连接配置'),
    ('sap.retry_limit', '3', 'SAP接口默认重试次数', 'SAP', 'INT', true, true, '集成平台异常数据默认可重推次数'),
    ('sap.timeout_ms', '30000', 'SAP RFC默认超时时间', 'SAP', 'INT', true, true, 'SAP RFC 调用默认超时时间，单位毫秒'),
    ('integration.default_timeout_ms', '30000', '集成默认超时时间', 'INTEGRATION', 'INT', true, true, '通用集成接口默认超时时间，单位毫秒'),
    ('integration.default_retry_limit', '3', '集成默认重试次数', 'INTEGRATION', 'INT', true, true, '通用集成接口异常重推默认次数'),
    ('wecom.receive_enabled', 'true', '启用企微消息写入', 'WECOM', 'BOOLEAN', true, true, '启用后，企业微信智能机器人/应用回调消息可写入CRM'),
    ('wecom.receive_mode', 'WECOM_AI_BOT', '企微消息接收模式', 'WECOM', 'STRING', true, true, 'WECOM_AI_BOT=企微智能机器人官方长连接；CALLBACK=企业微信应用回调；WECOM_CLI_AGENT=wecom-cli备用采集'),
    ('wecom.receive_callback_url', '/api/wecom/callback', '企微消息投递地址', 'WECOM', 'STRING', true, true, '配置到企业微信应用接收消息的回调URL路径'),
    ('wecom.receive_token', '', '企微应用回调Token', 'WECOM', 'STRING', true, true, '企业微信应用回调 Token，请在系统部署后通过页面配置'),
    ('wecom.receive_encoding_aes_key', '', '企微应用EncodingAESKey', 'WECOM', 'STRING', true, true, '企业微信应用回调 EncodingAESKey，请在系统部署后通过页面配置'),
    ('wecom.receive_write_mode', 'KEYWORD_DAILY_REPORT', '企微写入策略', 'WECOM', 'STRING', true, true, 'KEYWORD_DAILY_REPORT=命中关键词写日报并AI解析；ALL_DAILY_REPORT=所有文本写日报并AI解析'),
    ('wecom.receive_keywords', '日报,商机,商情,丢单,拜访,客户,工作总结', '企微写入关键词', 'WECOM', 'STRING', true, true, '命中这些关键词的@机器人内容会写入CRM日报并触发AI解析，多个用逗号分隔'),
    ('wecom.aibot_websocket_url', 'wss://openws.work.weixin.qq.com', '企微智能机器人WebSocket地址', 'WECOM', 'STRING', true, true, '企业微信智能机器人官方长连接网关地址'),
    ('wecom.cli_bot_id', '', '企微智能机器人Bot ID', 'WECOM', 'STRING', true, true, '企业微信智能机器人 Bot ID，请在系统部署后通过页面配置'),
    ('wecom.cli_bot_secret', '', '企微智能机器人Secret', 'WECOM', 'STRING', true, true, '企业微信智能机器人 Secret，请在系统部署后通过页面配置'),
    ('wecom.cli_binary', 'wecom-cli', 'wecom-cli命令路径', 'WECOM', 'STRING', true, true, 'wecom-cli可执行文件路径或命令名'),
    ('wecom.cli_command_template', 'wecom-cli msg {method} ''{jsonArgs}''', 'wecom-cli命令模板', 'WECOM', 'STRING', true, true, 'wecom-cli消息拉取命令模板'),
    ('wecom.cli_poll_interval_seconds', '15', 'wecom-cli轮询间隔秒', 'WECOM', 'INT', true, true, 'wecom-cli备用采集轮询间隔'),
    ('wecom.robot_webhook_url', 'https://qyapi.weixin.qq.com/cgi-bin/webhook/send', '企微群机器人Webhook', 'WECOM', 'STRING', true, true, '企业微信群机器人 Webhook 地址'),
    ('wecom.robot_key', '', '企微机器人Key', 'WECOM', 'STRING', true, true, 'Webhook URL 中的 key 参数；如果 Webhook 地址已包含 key 可留空'),
    ('wecom.default_msg_type', 'text', '企微默认消息类型', 'WECOM', 'STRING', true, true, '群机器人默认消息类型：text/markdown'),
    ('wecom.robot_supported_msg_types', 'text,markdown,image,file,news', '企微群机器人支持消息类型', 'WECOM', 'STRING', true, true, '群机器人Webhook可发送的消息类型'),
    ('wecom.robot_rate_limit_per_minute', '20', '企微群机器人发送频率限制', 'WECOM', 'INT', true, true, '企微群机器人发送频率参考值')
)
UPDATE sys_config target
SET config_name = cfg.config_name,
    config_group = cfg.config_group,
    type = cfg.type,
    editable = cfg.editable,
    visible = cfg.visible,
    description = cfg.description,
    updated_time = CURRENT_TIMESTAMP
FROM cfg
WHERE target.config_key = cfg.config_key;

WITH cfg(config_key, config_value, config_name, config_group, type, editable, visible, description) AS (
    VALUES
    ('ai.provider', 'OPENAI_COMPATIBLE', 'AI供应商', 'AI', 'STRING', true, true, 'AI模型供应商，可选 OpenAI兼容、MiniMax、通义千问、智谱、DeepSeek、Claude 等'),
    ('ai.protocol', 'OPENAI_CHAT', 'AI调用协议', 'AI', 'STRING', true, true, 'OPENAI_CHAT=OpenAI兼容Chat Completions，ANTHROPIC_MESSAGES=Claude Messages'),
    ('ai.base_url', '', 'AI服务接口地址', 'AI', 'STRING', true, true, 'AI服务Base URL，例如 https://api.openai.com/v1 或供应商兼容地址'),
    ('ai.model', '', 'AI默认模型', 'AI', 'STRING', true, true, 'AI默认调用模型，例如 gpt-4.1、deepseek-chat、qwen-plus、MiniMax-M2 等'),
    ('ai.api_key', '', 'AI服务 API Key', 'AI', 'STRING', true, true, 'AI服务API Key，请在系统部署后通过页面配置'),
    ('ai.timeout_ms', '120000', 'AI调用超时时间', 'AI', 'INT', true, true, 'AI日报/企微解析超时时间，单位毫秒'),
    ('amap.js_key', '', '高德地图 JS Key', 'AMAP', 'STRING', true, true, '高德地图 Web端 JS API Key，请在系统部署后通过页面配置'),
    ('amap.web_key', '', '高德地图 Web服务Key', 'AMAP', 'STRING', true, true, '高德地图 Web服务 Key，用于地址解析/逆解析'),
    ('amap.security_code', '', '高德地图安全密钥', 'AMAP', 'STRING', true, true, '高德 JS API 2.0 安全密钥 securityJsCode'),
    ('sap.default_connection', 'SAP_DEV', 'SAP默认RFC连接', 'SAP', 'STRING', true, true, 'SAP RFC 默认连接编码，对应集成平台SAP RFC连接配置'),
    ('sap.retry_limit', '3', 'SAP接口默认重试次数', 'SAP', 'INT', true, true, '集成平台异常数据默认可重推次数'),
    ('sap.timeout_ms', '30000', 'SAP RFC默认超时时间', 'SAP', 'INT', true, true, 'SAP RFC 调用默认超时时间，单位毫秒'),
    ('integration.default_timeout_ms', '30000', '集成默认超时时间', 'INTEGRATION', 'INT', true, true, '通用集成接口默认超时时间，单位毫秒'),
    ('integration.default_retry_limit', '3', '集成默认重试次数', 'INTEGRATION', 'INT', true, true, '通用集成接口异常重推默认次数'),
    ('wecom.receive_enabled', 'true', '启用企微消息写入', 'WECOM', 'BOOLEAN', true, true, '启用后，企业微信智能机器人/应用回调消息可写入CRM'),
    ('wecom.receive_mode', 'WECOM_AI_BOT', '企微消息接收模式', 'WECOM', 'STRING', true, true, 'WECOM_AI_BOT=企微智能机器人官方长连接；CALLBACK=企业微信应用回调；WECOM_CLI_AGENT=wecom-cli备用采集'),
    ('wecom.receive_callback_url', '/api/wecom/callback', '企微消息投递地址', 'WECOM', 'STRING', true, true, '配置到企业微信应用接收消息的回调URL路径'),
    ('wecom.receive_token', '', '企微应用回调Token', 'WECOM', 'STRING', true, true, '企业微信应用回调 Token，请在系统部署后通过页面配置'),
    ('wecom.receive_encoding_aes_key', '', '企微应用EncodingAESKey', 'WECOM', 'STRING', true, true, '企业微信应用回调 EncodingAESKey，请在系统部署后通过页面配置'),
    ('wecom.receive_write_mode', 'KEYWORD_DAILY_REPORT', '企微写入策略', 'WECOM', 'STRING', true, true, 'KEYWORD_DAILY_REPORT=命中关键词写日报并AI解析；ALL_DAILY_REPORT=所有文本写日报并AI解析'),
    ('wecom.receive_keywords', '日报,商机,商情,丢单,拜访,客户,工作总结', '企微写入关键词', 'WECOM', 'STRING', true, true, '命中这些关键词的@机器人内容会写入CRM日报并触发AI解析，多个用逗号分隔'),
    ('wecom.aibot_websocket_url', 'wss://openws.work.weixin.qq.com', '企微智能机器人WebSocket地址', 'WECOM', 'STRING', true, true, '企业微信智能机器人官方长连接网关地址'),
    ('wecom.cli_bot_id', '', '企微智能机器人Bot ID', 'WECOM', 'STRING', true, true, '企业微信智能机器人 Bot ID，请在系统部署后通过页面配置'),
    ('wecom.cli_bot_secret', '', '企微智能机器人Secret', 'WECOM', 'STRING', true, true, '企业微信智能机器人 Secret，请在系统部署后通过页面配置'),
    ('wecom.cli_binary', 'wecom-cli', 'wecom-cli命令路径', 'WECOM', 'STRING', true, true, 'wecom-cli可执行文件路径或命令名'),
    ('wecom.cli_command_template', 'wecom-cli msg {method} ''{jsonArgs}''', 'wecom-cli命令模板', 'WECOM', 'STRING', true, true, 'wecom-cli消息拉取命令模板'),
    ('wecom.cli_poll_interval_seconds', '15', 'wecom-cli轮询间隔秒', 'WECOM', 'INT', true, true, 'wecom-cli备用采集轮询间隔'),
    ('wecom.robot_webhook_url', 'https://qyapi.weixin.qq.com/cgi-bin/webhook/send', '企微群机器人Webhook', 'WECOM', 'STRING', true, true, '企业微信群机器人 Webhook 地址'),
    ('wecom.robot_key', '', '企微机器人Key', 'WECOM', 'STRING', true, true, 'Webhook URL 中的 key 参数；如果 Webhook 地址已包含 key 可留空'),
    ('wecom.default_msg_type', 'text', '企微默认消息类型', 'WECOM', 'STRING', true, true, '群机器人默认消息类型：text/markdown'),
    ('wecom.robot_supported_msg_types', 'text,markdown,image,file,news', '企微群机器人支持消息类型', 'WECOM', 'STRING', true, true, '群机器人Webhook可发送的消息类型'),
    ('wecom.robot_rate_limit_per_minute', '20', '企微群机器人发送频率限制', 'WECOM', 'INT', true, true, '企微群机器人发送频率参考值')
)
INSERT INTO sys_config (config_key, config_value, config_name, config_group, type, editable, visible, description)
SELECT cfg.config_key, cfg.config_value, cfg.config_name, cfg.config_group, cfg.type, cfg.editable, cfg.visible, cfg.description
FROM cfg
WHERE NOT EXISTS (
    SELECT 1 FROM sys_config target WHERE target.config_key = cfg.config_key
);

INSERT INTO sap_rfc_config (
    config_code, config_name, enabled, language, pool_capacity, peak_limit,
    connection_timeout, remark, tenant_id, deleted, created_time, updated_time
)
SELECT
    'SAP_DEV',
    'SAP开发/测试RFC连接',
    0,
    'ZH',
    5,
    10,
    30000,
    '默认SAP RFC连接模板。请部署后在集成平台填写主机、系统号、客户端、用户名和密码，并启用。',
    1,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM sap_rfc_config WHERE config_code = 'SAP_DEV' AND deleted = 0
);

INSERT INTO integration_connection_config (
    connection_code, connection_name, connection_type, enabled, base_url,
    auth_type, auth_config, header_config, timeout_ms, remark,
    tenant_id, deleted, created_time, updated_time
)
SELECT
    'WECOM_DEFAULT',
    '企微群机器人默认Webhook',
    'WECOM',
    1,
    'https://qyapi.weixin.qq.com/cgi-bin/webhook/send',
    'WECOM_WEBHOOK',
    '{"key":"","mentionedList":[],"mentionedMobileList":[],"defaultMsgType":"text","defaultContent":"","supportedMsgTypes":["text","markdown","image","file","news"],"rateLimitPerMinute":20}',
    '{"Content-Type":"application/json"}',
    30000,
    '企业微信群机器人 Webhook 默认连接，可在外围系统配置中同步维护',
    1,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM integration_connection_config WHERE connection_code = 'WECOM_DEFAULT' AND deleted = 0
);

UPDATE integration_interface
SET interface_name = '客户基础数据维护',
    system_code = 'SAP',
    connection_code = 'SAP_DEV',
    protocol = 'SAP_RFC',
    direction = 'OUTBOUND',
    business_module = 'CUSTOMER',
    sap_function_name = 'ZRFC_CRM_CUS_BASEINFO_MAINTAIN',
    trigger_mode = 'ON_SAVE',
    trigger_resource = 'CUSTOMER_SAP_INFO',
    trigger_condition_json = '{"changedOnly":true,"resources":["CUSTOMER_SAP_INFO","CUSTOMER_SAP_ORG"],"description":"客户主数据SAP信息或SAP组织明细保存后，仅同步本次变更明细"}',
    retry_limit = 3,
    success_rule_type = 'SAP_RETURN',
    success_field_path = 'RETURN',
    success_expected_values = 'S',
    failure_expected_values = 'E,A,X',
    success_message_path = 'RETURN[].MESSAGE',
    description = '客户主数据保存SAP信息时触发SAP RFC，同步本次变更明细并按SAP RETURN表判断成功。',
    deleted = 0,
    updated_time = CURRENT_TIMESTAMP
WHERE interface_code = 'SAP_CUS';

INSERT INTO integration_interface (
    interface_code, interface_name, system_code, connection_code, protocol,
    direction, business_module, sap_function_name, http_method, endpoint_path,
    content_type, trigger_mode, trigger_resource, trigger_condition_json,
    enabled, retry_limit, success_rule_type, success_field_path,
    success_expected_values, failure_expected_values, success_message_path,
    description, tenant_id, deleted, created_time, updated_time
)
SELECT
    'SAP_CUS',
    '客户基础数据维护',
    'SAP',
    'SAP_DEV',
    'SAP_RFC',
    'OUTBOUND',
    'CUSTOMER',
    'ZRFC_CRM_CUS_BASEINFO_MAINTAIN',
    NULL,
    NULL,
    NULL,
    'ON_SAVE',
    'CUSTOMER_SAP_INFO',
    '{"changedOnly":true,"resources":["CUSTOMER_SAP_INFO","CUSTOMER_SAP_ORG"],"description":"客户主数据SAP信息或SAP组织明细保存后，仅同步本次变更明细"}',
    1,
    3,
    'SAP_RETURN',
    'RETURN',
    'S',
    'E,A,X',
    'RETURN[].MESSAGE',
    '客户主数据保存SAP信息时触发SAP RFC，同步本次变更明细并按SAP RETURN表判断成功。',
    1,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
 
WHERE NOT EXISTS (
    SELECT 1 FROM integration_interface WHERE interface_code = 'SAP_CUS'
);

WITH sap_cus AS (
    SELECT id FROM integration_interface WHERE interface_code = 'SAP_CUS' AND deleted = 0 LIMIT 1
)
INSERT INTO integration_field_mapping (
    interface_id, parameter_mode, parameter_group, mapping_direction,
    source_module, source_field, source_field_label, target_field, target_field_label,
    table_field_mappings, field_type, required, default_value, transform_rule,
    sort_order, remark, tenant_id, deleted, created_time, updated_time
)
SELECT
    sap_cus.id,
    'TABLE',
    'IT_CUSTOMER',
    'OUTBOUND',
    'MULTI',
    '__TABLE__',
    '客户SAP信息（本次变更明细）',
    '__TABLE__',
    'IT_CUSTOMER',
    '[{"sourceModule":"customer","sourceField":"customerName","sourceFieldLabel":"客户名称","targetField":"NAME1","targetFieldLabel":"名称1","fieldType":"STRING","required":1,"defaultValue":"","transformRule":"","remark":""},{"sourceModule":"customer","sourceField":"customerShortName","sourceFieldLabel":"客户简称","targetField":"SORTL","targetFieldLabel":"检索项1","fieldType":"STRING","required":0,"defaultValue":"","transformRule":"","remark":""},{"sourceModule":"customer","sourceField":"countryRegion","sourceFieldLabel":"国家区域","targetField":"LAND1","targetFieldLabel":"国家代码","fieldType":"STRING","required":0,"defaultValue":"","transformRule":"","remark":""},{"sourceModule":"customer","sourceField":"province","sourceFieldLabel":"省","targetField":"REGIO","targetFieldLabel":"地区","fieldType":"STRING","required":0,"defaultValue":"","transformRule":"","remark":""},{"sourceModule":"customer","sourceField":"city","sourceFieldLabel":"市","targetField":"ORT01","targetFieldLabel":"城市","fieldType":"STRING","required":0,"defaultValue":"","transformRule":"","remark":""},{"sourceModule":"customer","sourceField":"district","sourceFieldLabel":"区县","targetField":"CITY2","targetFieldLabel":"地区","fieldType":"STRING","required":0,"defaultValue":"","transformRule":"","remark":""},{"sourceModule":"customer","sourceField":"detailAddress","sourceFieldLabel":"详细地址","targetField":"STRAS","targetFieldLabel":"街道地址","fieldType":"STRING","required":0,"defaultValue":"","transformRule":"","remark":""},{"sourceModule":"customer","sourceField":"mainContactName","sourceFieldLabel":"主联系人","targetField":"NAME_CO","targetFieldLabel":"c/o 名","fieldType":"STRING","required":0,"defaultValue":"","transformRule":"","remark":""},{"sourceModule":"customer","sourceField":"mainContactPhone","sourceFieldLabel":"主联系人电话","targetField":"TELF1","targetFieldLabel":"电话","fieldType":"STRING","required":0,"defaultValue":"","transformRule":"","remark":""},{"sourceModule":"customerSapInfo","sourceField":"sapInfos[].sapCode","sourceFieldLabel":"SAP信息-SAP编号","targetField":"KUNNR","targetFieldLabel":"客户编号","fieldType":"STRING","required":0,"defaultValue":"","transformRule":"","remark":"SAP编号可为空，SAP可返回后写回"},{"sourceModule":"customerSapInfo","sourceField":"sapInfos[].accountGroup","sourceFieldLabel":"SAP信息-账户组","targetField":"KTOKD","targetFieldLabel":"账户组","fieldType":"STRING","required":0,"defaultValue":"","transformRule":"","remark":""},{"sourceModule":"customerSapInfo","sourceField":"sapInfos[].countryCode","sourceFieldLabel":"SAP信息-国家代码","targetField":"LAND1","targetFieldLabel":"国家代码","fieldType":"STRING","required":0,"defaultValue":"","transformRule":"","remark":""},{"sourceModule":"customerSapOrg","sourceField":"sapOrgs[].sapCode","sourceFieldLabel":"SAP组织-SAP编号","targetField":"KUNNR","targetFieldLabel":"客户编号","fieldType":"STRING","required":0,"defaultValue":"","transformRule":"","remark":""},{"sourceModule":"customerSapOrg","sourceField":"sapOrgs[].companyCode","sourceFieldLabel":"SAP组织-公司代码","targetField":"BUKRS","targetFieldLabel":"公司代码","fieldType":"STRING","required":0,"defaultValue":"","transformRule":"","remark":""},{"sourceModule":"customerSapOrg","sourceField":"sapOrgs[].salesOrg","sourceFieldLabel":"SAP组织-销售组织","targetField":"VKORG","targetFieldLabel":"销售组织","fieldType":"STRING","required":0,"defaultValue":"","transformRule":"","remark":""},{"sourceModule":"customerSapOrg","sourceField":"sapOrgs[].distributionChannel","sourceFieldLabel":"SAP组织-分销渠道","targetField":"VTWEG","targetFieldLabel":"分销渠道","fieldType":"STRING","required":0,"defaultValue":"","transformRule":"","remark":""},{"sourceModule":"customerSapOrg","sourceField":"sapOrgs[].division","sourceFieldLabel":"SAP组织-产品组","targetField":"SPART","targetFieldLabel":"产品组","fieldType":"STRING","required":0,"defaultValue":"","transformRule":"","remark":""},{"sourceModule":"customerSapOrg","sourceField":"sapOrgs[].paymentTerms","sourceFieldLabel":"SAP组织-付款条件","targetField":"ZTERM","targetFieldLabel":"付款条件","fieldType":"STRING","required":0,"defaultValue":"","transformRule":"","remark":""},{"sourceModule":"customerSapOrg","sourceField":"sapOrgs[].currency","sourceFieldLabel":"SAP组织-货币","targetField":"WAERS","targetFieldLabel":"货币","fieldType":"STRING","required":0,"defaultValue":"","transformRule":"","remark":""}]',
    'JSON',
    0,
    '',
    '',
    0,
    'SAP_CUS默认表参数字段映射；同一个映射中维护表内多字段。',
    1,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM sap_cus
WHERE NOT EXISTS (
    SELECT 1
    FROM integration_field_mapping m
    WHERE m.interface_id = sap_cus.id
      AND m.deleted = 0
      AND m.parameter_mode = 'TABLE'
      AND m.parameter_group = 'IT_CUSTOMER'
      AND m.mapping_direction = 'OUTBOUND'
      AND m.source_field = '__TABLE__'
);

WITH sap_cus AS (
    SELECT id FROM integration_interface WHERE interface_code = 'SAP_CUS' AND deleted = 0 LIMIT 1
)
INSERT INTO integration_field_mapping (
    interface_id, parameter_mode, parameter_group, mapping_direction,
    source_module, source_field, source_field_label, target_field, target_field_label,
    table_field_mappings, field_type, required, sort_order, remark,
    tenant_id, deleted, created_time, updated_time
)
SELECT
    sap_cus.id,
    'TABLE',
    'RETURN',
    'INBOUND',
    'customerSapInfo',
    'sapInfos[].sapCode',
    'SAP信息-SAP编号',
    'KUNNR',
    '客户编号',
    '[{"sourceModule":"customerSapInfo","sourceField":"sapInfos[].sapCode","sourceFieldLabel":"SAP信息-SAP编号","targetField":"KUNNR","targetFieldLabel":"客户编号","fieldType":"STRING","required":0,"defaultValue":"","transformRule":"","remark":"SAP返回客户编号后写回SAP信息"}]',
    'STRING',
    0,
    10,
    'SAP_CUS默认回写映射，SAP返回客户编号后写回SAP信息SAP编号字段。',
    1,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM sap_cus
WHERE NOT EXISTS (
    SELECT 1
    FROM integration_field_mapping m
    WHERE m.interface_id = sap_cus.id
      AND m.deleted = 0
      AND m.parameter_mode = 'TABLE'
      AND m.parameter_group = 'RETURN'
      AND m.mapping_direction = 'INBOUND'
      AND m.source_field = 'sapInfos[].sapCode'
      AND m.target_field = 'KUNNR'
);
