-- Role/user permission model hardening for admin pages.

ALTER TABLE crm_role ADD COLUMN IF NOT EXISTS status INTEGER DEFAULT 1;
ALTER TABLE crm_role ADD COLUMN IF NOT EXISTS data_scope INTEGER DEFAULT 4;

ALTER TABLE crm_user_role ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;
ALTER TABLE crm_role_menu ADD COLUMN IF NOT EXISTS tenant_id BIGINT DEFAULT 1;

UPDATE crm_role SET status = 1 WHERE status IS NULL;
UPDATE crm_role SET data_scope = 4 WHERE data_scope IS NULL;
UPDATE crm_role SET data_scope = 1 WHERE role_key = 'ROLE_ADMIN' AND deleted = 0;
UPDATE crm_role SET data_scope = 2 WHERE role_key = 'ROLE_MANAGER' AND deleted = 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_crm_user_role_active
    ON crm_user_role(user_id, role_id)
    WHERE deleted = 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_crm_role_menu_active
    ON crm_role_menu(role_id, menu_id)
    WHERE deleted = 0;

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

WITH manager_menu_ids(menu_id) AS (
    VALUES
        (1000),(2000),(2100),(2200),(3000),(3100),(3200),(3300),
        (8000),(8100),(9000),(9100),(11000),(11100),(11200),(11300),
        (12000),(12100),(12200),(13000)
)
INSERT INTO crm_role_menu (role_id, menu_id, created_by, tenant_id)
SELECT r.id, m.menu_id, 'system', 1
FROM crm_role r
CROSS JOIN manager_menu_ids m
WHERE r.role_key = 'ROLE_MANAGER'
  AND r.deleted = 0
ON CONFLICT DO NOTHING;

WITH sales_menu_ids(menu_id) AS (
    VALUES
        (1000),(2000),(2100),(3000),(3100),(3200),(9000),(9100),
        (11000),(11100),(11200),(13000)
)
INSERT INTO crm_role_menu (role_id, menu_id, created_by, tenant_id)
SELECT r.id, m.menu_id, 'system', 1
FROM crm_role r
CROSS JOIN sales_menu_ids m
WHERE r.role_key = 'ROLE_SALES'
  AND r.deleted = 0
ON CONFLICT DO NOTHING;
