package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.customer.dto.RoleCreateDTO;
import com.huafu.crm.customer.dto.RoleUpdateDTO;
import com.huafu.crm.customer.dto.RoleVO;
import com.huafu.crm.customer.entity.Role;
import com.huafu.crm.customer.entity.RoleMenu;
import com.huafu.crm.customer.mapper.RoleMapper;
import com.huafu.crm.customer.mapper.RoleMenuMapper;
import com.huafu.crm.customer.service.AdminRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminRoleServiceImpl implements AdminRoleService {
    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;

    public AdminRoleServiceImpl(RoleMapper roleMapper, RoleMenuMapper roleMenuMapper) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
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
        if (r.getTenantId() == null) r.setTenantId(1L);
        roleMapper.insert(r);
        return toVO(r);
    }

    @Override
    @Transactional
    public RoleVO update(Long id, RoleUpdateDTO dto) {
        Role r = roleMapper.selectById(id);
        if (r == null) throw new BizException(1002, "角色不存在");
        if (dto.roleName() != null) r.setRoleName(dto.roleName());
        if (dto.description() != null) r.setDescription(dto.description());
        roleMapper.updateById(r);
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
    public PageResult<RoleVO> page(String roleName, long current, long size) {
        Page<Role> p = new Page<>(current, size);
        LambdaQueryWrapper<Role> qw = new LambdaQueryWrapper<>();
        if (roleName != null && !roleName.isBlank()) qw.like(Role::getRoleName, roleName);
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
        roleMapper.deleteById(id);
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
        for (Long menuId : menuIds) {
            RoleMenu rm = new RoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            if (rm.getTenantId() == null) rm.setTenantId(1L);
            roleMenuMapper.insert(rm);
        }
    }

    private RoleVO toVO(Role r) {
        return new RoleVO(r.getId(), r.getRoleName(), r.getRoleKey(), r.getDescription(), r.getCreatedTime());
    }
}
