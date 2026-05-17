-- V6: Seed admin test account
-- 2026-05-08
-- 管理员测试账号: admin / Huafu2026!

INSERT INTO crm_user (username, password, real_name, status, dept_id, created_by, tenant_id)
SELECT 'admin', '$2b$10$PTPXVWSiKp8HB0tSW1yMc.QfqC.yzT2IntE0ig0uKaTOsmWOpaaEO', '系统管理员', 1, id, 'system', 1
FROM crm_dept WHERE dept_name = '华孚时尚' AND deleted = 0
ON CONFLICT (username) DO NOTHING;

-- 绑定系统管理员角色
INSERT INTO crm_user_role (user_id, role_id, created_by)
SELECT u.id, r.id, 'system'
FROM crm_user u, crm_role r
WHERE u.username = 'admin' AND r.role_key = 'ROLE_ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;
