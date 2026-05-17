-- V2026.05.15.06: AI 服务供应商与协议配置
-- 2026-05-15

INSERT INTO sys_config (config_key, config_value, config_name, config_group, type, editable, visible, description) VALUES
('ai.provider', 'OPENAI', 'AI供应商', 'AI', 'STRING', true, true, 'AI服务供应商预设，如 OpenAI、Claude、DeepSeek、通义千问、Kimi、智谱、Gemini、火山方舟等'),
('ai.protocol', 'OPENAI_CHAT', 'AI调用协议', 'AI', 'STRING', true, true, 'OPENAI_CHAT 表示 OpenAI 兼容 Chat Completions；ANTHROPIC_MESSAGES 表示 Claude Messages API')
ON CONFLICT (config_key) DO UPDATE SET
    config_name = EXCLUDED.config_name,
    config_group = EXCLUDED.config_group,
    type = EXCLUDED.type,
    editable = EXCLUDED.editable,
    visible = EXCLUDED.visible,
    description = EXCLUDED.description,
    updated_time = CURRENT_TIMESTAMP;

UPDATE sys_config
SET config_value = 'https://api.openai.com/v1',
    updated_time = CURRENT_TIMESTAMP
WHERE config_key = 'ai.base_url'
  AND COALESCE(config_value, '') = '';

UPDATE sys_config
SET config_value = 'gpt-4.1',
    updated_time = CURRENT_TIMESTAMP
WHERE config_key = 'ai.model'
  AND COALESCE(config_value, '') = '';
