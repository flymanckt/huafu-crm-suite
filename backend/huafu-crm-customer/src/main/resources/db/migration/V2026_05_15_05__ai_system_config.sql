-- V2026.05.15.05: AI 服务外围系统配置
-- 2026-05-15

INSERT INTO sys_config (config_key, config_value, config_name, config_group, type, editable, visible, description) VALUES
('ai.api_key', '', 'AI服务 API Key', 'AI', 'STRING', true, true, 'AI 服务调用密钥，用于客户概述、智能分析等能力'),
('ai.base_url', '', 'AI服务接口地址', 'AI', 'STRING', true, true, 'AI 服务接口 Base URL，可按实际供应商配置'),
('ai.model', '', 'AI默认模型', 'AI', 'STRING', true, true, 'AI 服务默认模型名称')
ON CONFLICT (config_key) DO UPDATE SET
    config_name = EXCLUDED.config_name,
    config_group = EXCLUDED.config_group,
    type = EXCLUDED.type,
    editable = EXCLUDED.editable,
    visible = EXCLUDED.visible,
    description = EXCLUDED.description,
    updated_time = CURRENT_TIMESTAMP;
