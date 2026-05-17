-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(128) NOT NULL UNIQUE,
    config_value TEXT,
    config_name VARCHAR(255),
    config_group VARCHAR(64) NOT NULL DEFAULT 'SYSTEM',
    description VARCHAR(500),
    type VARCHAR(32) NOT NULL DEFAULT 'STRING', -- STRING/INT/FLOAT/BOOLEAN/JSON
    editable BOOLEAN NOT NULL DEFAULT TRUE,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE sys_config IS '系统配置表';
COMMENT ON COLUMN sys_config.config_key IS '配置键（唯一）';
COMMENT ON COLUMN sys_config.config_value IS '配置值';
COMMENT ON COLUMN sys_config.config_group IS '配置分组：SYSTEM/AMAP/API';
COMMENT ON COLUMN sys_config.type IS '类型：STRING/INT/FLOAT/BOOLEAN/JSON';
COMMENT ON COLUMN sys_config.editable IS '是否可编辑';
COMMENT ON COLUMN sys_config.visible IS '是否对用户可见';

-- 初始化默认配置
INSERT INTO sys_config (config_key, config_value, config_name, config_group, type, editable, visible, description) VALUES
-- 高德地图
('amap.web_key', '', '高德地图 Web服务Key', 'AMAP', 'STRING', true, true, '高德地图 JS API 和 Web服务 API Key，用于地址定位和地图展示'),
('amap.js_key', '', '高德地图 JS Key', 'AMAP', 'STRING', true, true, '高德地图 JS API Key，用于前端地图组件'),
-- API配置
('api.gateway_url', 'http://localhost:8080', '网关地址', 'API', 'STRING', true, true, 'CRM网关地址'),
('api.customer_service_url', 'http://localhost:8081', '客户服务地址', 'API', 'STRING', true, true, '客户微服务地址')
ON CONFLICT (config_key) DO NOTHING;
