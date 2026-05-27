package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.customer.dto.RoleCreateDTO;
import com.huafu.crm.customer.dto.RoleUpdateDTO;
import com.huafu.crm.customer.dto.RoleVO;
import com.huafu.crm.customer.dto.MenuNodeVO;
import com.huafu.crm.customer.entity.Role;
import com.huafu.crm.customer.entity.RoleMenu;
import com.huafu.crm.customer.entity.UserRole;
import com.huafu.crm.customer.mapper.RoleMapper;
import com.huafu.crm.customer.mapper.RoleMenuMapper;
import com.huafu.crm.customer.mapper.UserRoleMapper;
import com.huafu.crm.customer.service.AdminPermissionCatalog;
import com.huafu.crm.customer.service.AdminRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminRoleServiceImpl implements AdminRoleService {
    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final UserRoleMapper userRoleMapper;
    private final AdminPermissionCatalog permissionCatalog;

    public AdminRoleServiceImpl(
            RoleMapper roleMapper,
            RoleMenuMapper roleMenuMapper,
            UserRoleMapper userRoleMapper,
            AdminPermissionCatalog permissionCatalog) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.userRoleMapper = userRoleMapper;
        this.permissionCatalog = permissionCatalog;
    }

    @Override
    @Transactional
    public RoleVO create(RoleCreateDTO dto) {
        Role existing = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleKey, dto.roleKey()));
        if (existing != null) throw new BizException(1002, "角色标识已存在");
        Role r = new Role();
        r.setRoleName(dto.roleName());
        r.setRoleKey(dto.roleKey());
        r.setDescription(dto.description());
        r.setStatus(dto.status() != null ? dto.status() : 1);
        r.setDataScope(dto.dataScope() != null ? dto.dataScope() : 4);
        if (r.getTenantId() == null) r.setTenantId(1L);
        roleMapper.insert(r);
        updateMenus(r.getId(), dto.menuIds() == null ? List.of() : dto.menuIds());
        return toVO(roleMapper.selectById(r.getId()));
    }

    @Override
    @Transactional
    public RoleVO update(Long id, RoleUpdateDTO dto) {
        Role r = roleMapper.selectById(id);
        if (r == null) throw new BizException(1002, "角色不存在");
        if (dto.roleName() != null) r.setRoleName(dto.roleName());
        if (dto.description() != null) r.setDescription(dto.description());
        if (dto.status() != null) r.setStatus(dto.status());
        if (dto.dataScope() != null) r.setDataScope(dto.dataScope());
        roleMapper.updateById(r);
        if (dto.menuIds() != null) updateMenus(id, dto.menuIds());
        return toVO(roleMapper.selectById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public RoleVO getById(Long id) {
        Role r = roleMapper.selectById(id);
        if (r == null) throw new BizException(1002, "角色不存在");
        return toVO(r);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<RoleVO> page(String roleName, String roleKey, Integer status, Integer dataScope, long current, long size) {
        Page<Role> p = new Page<>(current, size);
        LambdaQueryWrapper<Role> qw = new LambdaQueryWrapper<>();
        if (roleName != null && !roleName.isBlank()) qw.like(Role::getRoleName, roleName);
        if (roleKey != null && !roleKey.isBlank()) qw.like(Role::getRoleKey, roleKey);
        if (status != null) qw.eq(Role::getStatus, status);
        if (dataScope != null) qw.eq(Role::getDataScope, dataScope);
        qw.orderByDesc(Role::getCreatedTime);
        Page<Role> result = roleMapper.selectPage(p, qw);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(),
            result.getRecords().stream().map(this::toVO).toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Role r = roleMapper.selectById(id);
        if (r == null) throw new BizException(1002, "角色不存在");
        if ("ROLE_ADMIN".equals(r.getRoleKey())) throw new BizException(1002, "系统管理员角色不可删除");
        Long count = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));
        if (count != null && count > 0) throw new BizException(1002, "该角色已分配给用户，不能删除");
        roleMapper.deleteById(id);
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getMenuIds(Long roleId) {
        List<RoleMenu> list = roleMenuMapper.selectList(
            new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId));
        return list.stream().map(RoleMenu::getMenuId).toList();
    }

    @Override
    @Transactional
    public void updateMenus(Long roleId, List<Long> menuIds) {
        Role r = roleMapper.selectById(roleId);
        if (r == null) throw new BizException(1002, "角色不存在");
        // delete existing
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId));
        // insert new
        List<Long> normalized = "ROLE_ADMIN".equals(r.getRoleKey()) ? permissionCatalog.allMenuIds() : menuIds;
        for (Long menuId : normalized == null ? List.<Long>of() : normalized.stream().distinct().toList()) {
            RoleMenu rm = new RoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            if (rm.getTenantId() == null) rm.setTenantId(1L);
            roleMenuMapper.insert(rm);
        }
    }

    @Override
    public List<MenuNodeVO> menuTree() {
        return permissionCatalog.menuTree();
    }

    private RoleVO toVO(Role r) {
        List<Long> menuIds = getMenuIds(r.getId());
        Long userCount = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, r.getId()));
        return new RoleVO(
            r.getId(),
            r.getRoleName(),
            r.getRoleKey(),
            r.getDescription(),
            r.getStatus() != null ? r.getStatus() : 1,
            r.getDataScope() != null ? r.getDataScope() : 4,
            userCount == null ? 0 : userCount.intValue(),
            menuIds,
            r.getCreatedTime()
        );
    }
}
