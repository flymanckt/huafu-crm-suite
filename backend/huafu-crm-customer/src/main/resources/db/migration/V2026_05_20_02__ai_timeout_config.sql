-- V2026.05.20.02: AI 调用超时配置
-- 2026-05-20

INSERT INTO sys_config (config_key, config_value, config_name, config_group, type, editable, visible, description) VALUES
('ai.timeout_ms', '120000', 'AI调用超时时间', 'AI', 'INT', true, true, 'AI 日报解析、客户概述等能力调用模型服务的超时时间，单位毫秒')
ON CONFLICT (config_key) DO UPDATE SET
    config_name = EXCLUDED.config_name,
    config_group = EXCLUDED.config_group,
    type = EXCLUDED.type,
    editable = EXCLUDED.editable,
    visible = EXCLUDED.visible,
    description = EXCLUDED.description,
    updated_time = CURRENT_TIMESTAMP;
