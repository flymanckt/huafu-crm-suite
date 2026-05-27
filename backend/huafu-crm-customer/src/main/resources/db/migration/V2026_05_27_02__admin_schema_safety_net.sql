-- Safety net for admin/login tables when Flyway history exists but runtime tables were removed.

CREATE SEQUENCE IF NOT EXISTS crm_base_id_seq START WITH 10000;

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
    username VARCHAR(64) NOT NULL,
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
    role_key VARCHAR(64) NOT NULL,
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
    tenant_id BIGINT DEFAULT 1
);

CREATE TABLE IF NOT EXISTS crm_role_menu (
    id BIGINT PRIMARY KEY DEFAULT nextval('crm_base_id_seq'),
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    created_by VARCHAR(64) DEFAULT '',
    created_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    tenant_id BIGINT DEFAULT 1
);

ALTER TABLE crm_user ADD COLUMN IF NOT EXISTS last_login_time TIMESTAMPTZ;
ALTER TABLE crm_user ADD COLUMN IF NOT EXISTS last_login_ip VARCHAR(64);
ALTER TABLE crm_user ADD COLUMN IF NOT EXISTS login_count INT DEFAULT 0;
ALTER TABLE crm_user ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_user ADD COLUMN IF NOT EXISTS version INT DEFAULT 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_crm_user_username
    ON crm_user(username)
    WHERE deleted = 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_crm_role_key
    ON crm_role(role_key)
    WHERE deleted = 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_crm_user_role_active
    ON crm_user_role(user_id, role_id)
    WHERE deleted = 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_crm_role_menu_active
    ON crm_role_menu(role_id, menu_id)
    WHERE deleted = 0;

INSERT INTO crm_dept (dept_name, parent_id, created_by, tenant_id)
SELECT '华孚时尚', 0, 'system', 1
WHERE NOT EXISTS (
    SELECT 1 FROM crm_dept WHERE dept_name = '华孚时尚' AND deleted = 0
);

INSERT INTO crm_role (role_name, role_key, description, created_by, created_time, tenant_id)
SELECT '系统管理员', 'ROLE_ADMIN', '系统全部权限', 'system', NOW(), 1
WHERE NOT EXISTS (SELECT 1 FROM crm_role WHERE role_key = 'ROLE_ADMIN' AND deleted = 0);

INSERT INTO crm_role (role_name, role_key, description, created_by, created_time, tenant_id)
SELECT '销售经理', 'ROLE_MANAGER', '本部门客户管理', 'system', NOW(), 1
WHERE NOT EXISTS (SELECT 1 FROM crm_role WHERE role_key = 'ROLE_MANAGER' AND deleted = 0);

INSERT INTO crm_role (role_name, role_key, description, created_by, created_time, tenant_id)
SELECT '销售', 'ROLE_SALES', '本人客户管理', 'system', NOW(), 1
WHERE NOT EXISTS (SELECT 1 FROM crm_role WHERE role_key = 'ROLE_SALES' AND deleted = 0);

INSERT INTO crm_user (username, password, real_name, status, dept_id, created_by, tenant_id)
SELECT 'admin', '$2b$10$PTPXVWSiKp8HB0tSW1yMc.QfqC.yzT2IntE0ig0uKaTOsmWOpaaEO', '系统管理员', 1, d.id, 'system', 1
FROM crm_dept d
WHERE d.dept_name = '华孚时尚'
  AND d.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM crm_user WHERE username = 'admin' AND deleted = 0)
ORDER BY d.id
LIMIT 1;

INSERT INTO crm_user_role (user_id, role_id, created_by, tenant_id)
SELECT u.id, r.id, 'system', 1
FROM crm_user u
JOIN crm_role r ON r.role_key = 'ROLE_ADMIN' AND r.deleted = 0
WHERE u.username = 'admin'
  AND u.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM crm_user_role ur
      WHERE ur.user_id = u.id AND ur.role_id = r.id AND ur.deleted = 0
  );
