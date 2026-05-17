-- V5: Admin management + Business P0 enhancements
-- 2026-05-08

-- 1. Admin base tables (some runtime schemas did not contain these tables yet)
CREATE TABLE IF NOT EXISTS crm_dept (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    dept_name VARCHAR(128) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    leader_user_id BIGINT,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_user (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(128),
    real_name VARCHAR(64),
    phone VARCHAR(32),
    email VARCHAR(128),
    dept_id BIGINT,
    post VARCHAR(64),
    status SMALLINT DEFAULT 1,
    last_login_time TIMESTAMPTZ,
    last_login_ip VARCHAR(64),
    login_count INT DEFAULT 0,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_role (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    role_name VARCHAR(64) NOT NULL,
    role_key VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(256),
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) DEFAULT '',
    updated_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    version INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS crm_user_role (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    UNIQUE(user_id, role_id)
);

-- 1.1 crm_user extra fields for already-existing schemas
ALTER TABLE crm_user ADD COLUMN IF NOT EXISTS last_login_time TIMESTAMPTZ;
ALTER TABLE crm_user ADD COLUMN IF NOT EXISTS last_login_ip VARCHAR(64);
ALTER TABLE crm_user ADD COLUMN IF NOT EXISTS login_count INT DEFAULT 0;

-- Seed default department so user forms have a usable root node
INSERT INTO crm_dept (dept_name, parent_id, created_by, tenant_id)
VALUES ('华孚时尚', 0, 'system', 1)
ON CONFLICT DO NOTHING;

-- 2. crm_role_menu (role-menu permission binding)
CREATE TABLE IF NOT EXISTS crm_role_menu (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1,
    UNIQUE(role_id, menu_id)
);
COMMENT ON TABLE crm_role_menu IS '角色菜单关联表';

-- 3. crm_customer_transfer (customer transfer history)
CREATE TABLE IF NOT EXISTS crm_customer_transfer (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    customer_id BIGINT NOT NULL,
    from_user_id BIGINT,
    to_user_id BIGINT NOT NULL,
    reason VARCHAR(256),
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1
);
COMMENT ON TABLE crm_customer_transfer IS '客户转移记录表';

-- 4. Preseed roles
INSERT INTO crm_role (role_name, role_key, description, created_by, created_time, tenant_id)
VALUES
    ('系统管理员', 'ROLE_ADMIN', '系统全部权限', 'system', NOW(), 1),
    ('销售经理', 'ROLE_MANAGER', '本部门客户管理', 'system', NOW(), 1),
    ('销售', 'ROLE_SALES', '本人客户管理', 'system', NOW(), 1)
ON CONFLICT (role_key) DO NOTHING;
