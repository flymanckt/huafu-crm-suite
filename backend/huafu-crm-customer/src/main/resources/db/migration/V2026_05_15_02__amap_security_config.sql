-- V2026.05.15.02: 高德 JS API 安全密钥配置
-- 2026-05-15

INSERT INTO sys_config (config_key, config_value, config_name, config_group, type, editable, visible, description) VALUES
('amap.security_code', '', '高德地图安全密钥', 'AMAP', 'STRING', true, true, '高德 JS API 2.0 安全密钥 securityJsCode，用于前端地图组件加载')
ON CONFLICT (config_key) DO UPDATE SET
    config_name = EXCLUDED.config_name,
    config_group = EXCLUDED.config_group,
    type = EXCLUDED.type,
    editable = EXCLUDED.editable,
    visible = EXCLUDED.visible,
    description = EXCLUDED.description,
    updated_time = CURRENT_TIMESTAMP;
