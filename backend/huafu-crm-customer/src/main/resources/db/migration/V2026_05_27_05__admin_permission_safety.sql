-- Make admin permission recovery idempotent for deployed databases that already
-- passed earlier safety-net migrations before role-menu/user-role data existed.

ALTER TABLE crm_role ADD COLUMN IF NOT EXISTS status INTEGER DEFAULT 1;
ALTER TABLE crm_role ADD COLUMN IF NOT EXISTS data_scope INTEGER DEFAULT 4;

INSERT INTO crm_role (role_name, role_key, description, status, data_scope, created_by, tenant_id)
SELECT '系统管理员', 'ROLE_ADMIN', '系统全部权限', 1, 1, 'system', 1
WHERE NOT EXISTS (
    SELECT 1 FROM crm_role WHERE role_key = 'ROLE_ADMIN' AND deleted = 0
);

UPDATE crm_role
SET status = 1,
    data_scope = 1
WHERE role_key = 'ROLE_ADMIN'
  AND deleted = 0;

INSERT INTO crm_user_role (user_id, role_id, created_by, tenant_id)
SELECT u.id, r.id, 'system', 1
FROM crm_user u
JOIN crm_role r ON r.role_key = 'ROLE_ADMIN' AND r.deleted = 0
WHERE u.username = 'admin'
  AND u.deleted = 0
  AND NOT EXISTS (
      SELECT 1
      FROM crm_user_role ur
      WHERE ur.user_id = u.id
        AND ur.role_id = r.id
        AND ur.deleted = 0
  );

WITH menu_ids(menu_id) AS (
    VALUES
        (1000),(2000),(2100),(2200),(3000),(3100),(3200),(3300),
        (4000),(4100),(4200),(4300),(4400),(5000),(5100),(5200),(5300),
        (6000),(6100),(6200),(7000),(7100),(7200),(8000),(8100),
        (9000),(9100),(10000),(10100),(10200),(10300),(10400),(10500),
        (10600),(10700),(11000),(11100),(11200),(11300),(12000),(12100),
        (12200),(13000)
)
INSERT INTO crm_role_menu (role_id, menu_id, created_by, tenant_id)
SELECT r.id, m.menu_id, 'system', 1
FROM crm_role r
CROSS JOIN menu_ids m
WHERE r.role_key = 'ROLE_ADMIN'
  AND r.deleted = 0
ON CONFLICT DO NOTHING;
