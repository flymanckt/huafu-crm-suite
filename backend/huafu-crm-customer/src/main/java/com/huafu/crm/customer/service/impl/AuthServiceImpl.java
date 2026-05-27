package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.common.utils.JwtUtil;
import com.huafu.crm.customer.dto.LoginDTO;
import com.huafu.crm.customer.dto.LoginVO;
import com.huafu.crm.customer.entity.Role;
import com.huafu.crm.customer.entity.RoleMenu;
import com.huafu.crm.customer.entity.User;
import com.huafu.crm.customer.entity.UserRole;
import com.huafu.crm.customer.mapper.RoleMapper;
import com.huafu.crm.customer.mapper.RoleMenuMapper;
import com.huafu.crm.customer.mapper.UserRoleMapper;
import com.huafu.crm.customer.mapper.UserMapper;
import com.huafu.crm.customer.service.AdminPermissionCatalog;
import com.huafu.crm.customer.service.AuthService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final AdminPermissionCatalog permissionCatalog;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(
            UserMapper userMapper,
            UserRoleMapper userRoleMapper,
            RoleMapper roleMapper,
            RoleMenuMapper roleMenuMapper,
            AdminPermissionCatalog permissionCatalog,
            JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.permissionCatalog = permissionCatalog;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.username())
                .eq(User::getDeleted, 0)
        );

        if (user == null) {
            throw new BizException(401, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(401, "账号已禁用");
        }
        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }

        // 更新登录信息
        user.setLastLoginTime(OffsetDateTime.now());
        user.setLoginCount(user.getLoginCount() == null ? 1 : user.getLoginCount() + 1);
        userMapper.updateById(user);

        List<Long> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getId()))
            .stream().map(UserRole::getRoleId).distinct().toList();
        List<Role> roles = roleIds.isEmpty() ? List.of() : roleMapper.selectBatchIds(roleIds).stream()
            .filter(role -> role.getStatus() == null || role.getStatus() == 1)
            .toList();
        boolean adminUsername = "admin".equalsIgnoreCase(user.getUsername());
        if (adminUsername && roles.stream().noneMatch(role -> "ROLE_ADMIN".equals(role.getRoleKey()))) {
            Role adminRole = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>()
                    .eq(Role::getRoleKey, "ROLE_ADMIN")
                    .eq(Role::getDeleted, 0)
            );
            if (adminRole != null && (adminRole.getStatus() == null || adminRole.getStatus() == 1)) {
                List<Role> patchedRoles = new ArrayList<>(roles);
                patchedRoles.add(adminRole);
                roles = patchedRoles;
                ensureUserRole(user.getId(), adminRole.getId());
            }
        }
        List<Long> activeRoleIds = roles.stream().map(Role::getId).toList();
        List<String> roleKeys = roles.stream().map(Role::getRoleKey).toList();
        List<String> roleNames = roles.stream().map(Role::getRoleName).toList();
        boolean superAdmin = adminUsername || roleKeys.contains("ROLE_ADMIN");
        if (superAdmin && !roleKeys.contains("ROLE_ADMIN")) {
            roleKeys = new ArrayList<>(roleKeys);
            roleKeys.add("ROLE_ADMIN");
            roleNames = new ArrayList<>(roleNames);
            roleNames.add("系统管理员");
        }
        List<Long> menuIds = superAdmin
            ? permissionCatalog.allMenuIds()
            : activeRoleIds.stream()
                .flatMap(roleId -> roleMenuMapper.selectList(
                    new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId)).stream())
                .map(RoleMenu::getMenuId)
                .distinct()
                .toList();
        List<String> permissions = superAdmin
            ? List.of("*")
            : permissionCatalog.permissions(menuIds);

        // 生成 token
        String token = jwtUtil.generate(user.getId(), user.getUsername(), Map.of(
            "roles", roleKeys,
            "menuIds", menuIds,
            "permissions", permissions
        ));

        return new LoginVO(
            token,
            new LoginVO.UserInfoVO(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getDeptId(),
                user.getStatus(),
                activeRoleIds,
                roleKeys,
                roleNames,
                menuIds,
                permissions
            )
        );
    }

    private void ensureUserRole(Long userId, Long roleId) {
        if (userId == null || roleId == null) return;
        Long existing = userRoleMapper.selectCount(
            new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, roleId)
        );
        if (existing != null && existing > 0) return;
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setTenantId(1L);
        userRoleMapper.insert(userRole);
    }
}
