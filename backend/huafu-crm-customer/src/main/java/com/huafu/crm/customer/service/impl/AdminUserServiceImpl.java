package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.customer.dto.UserCreateDTO;
import com.huafu.crm.customer.dto.UserUpdateDTO;
import com.huafu.crm.customer.dto.UserVO;
import com.huafu.crm.customer.entity.Dept;
import com.huafu.crm.customer.entity.Role;
import com.huafu.crm.customer.entity.User;
import com.huafu.crm.customer.entity.UserRole;
import com.huafu.crm.customer.mapper.DeptMapper;
import com.huafu.crm.customer.mapper.RoleMapper;
import com.huafu.crm.customer.mapper.UserRoleMapper;
import com.huafu.crm.customer.mapper.UserMapper;
import com.huafu.crm.customer.service.AdminUserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminUserServiceImpl implements AdminUserService {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final DeptMapper deptMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    public AdminUserServiceImpl(
            UserMapper userMapper,
            RoleMapper roleMapper,
            UserRoleMapper userRoleMapper,
            DeptMapper deptMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.deptMapper = deptMapper;
    }

    @Override
    @Transactional
    public UserVO create(UserCreateDTO dto) {
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.username()));
        if (existing != null) throw new BizException(1001, "用户名已存在");
        User u = new User();
        u.setUsername(dto.username());
        u.setPassword(passwordEncoder.encode(dto.password()));
        u.setRealName(dto.realName());
        u.setPhone(dto.phone());
        u.setEmail(dto.email());
        u.setDeptId(dto.deptId());
        u.setPost(dto.post());
        u.setStatus(dto.status() != null ? dto.status() : 1);
        u.setLoginCount(0);
        if (u.getTenantId() == null) u.setTenantId(1L);
        userMapper.insert(u);
        updateUserRoles(u.getId(), dto.roleIds());
        return toVO(userMapper.selectById(u.getId()));
    }

    @Override
    @Transactional
    public UserVO update(Long id, UserUpdateDTO dto) {
        User u = userMapper.selectById(id);
        if (u == null) throw new BizException(1001, "用户不存在");
        if (dto.realName() != null) u.setRealName(dto.realName());
        if (dto.phone() != null) u.setPhone(dto.phone());
        if (dto.email() != null) u.setEmail(dto.email());
        if (dto.deptId() != null) u.setDeptId(dto.deptId());
        if (dto.post() != null) u.setPost(dto.post());
        if (dto.status() != null) u.setStatus(dto.status());
        userMapper.updateById(u);
        if (dto.roleIds() != null) updateUserRoles(id, dto.roleIds());
        return toVO(userMapper.selectById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserVO getById(Long id) {
        User u = userMapper.selectById(id);
        if (u == null) throw new BizException(1001, "用户不存在");
        return toVO(u);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<UserVO> page(String username, String realName, Long deptId, Integer status, long current, long size) {
        Page<User> p = new Page<>(current, size);
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        if (username != null && !username.isBlank()) qw.like(User::getUsername, username);
        if (realName != null && !realName.isBlank()) qw.like(User::getRealName, realName);
        if (deptId != null) qw.eq(User::getDeptId, deptId);
        if (status != null) qw.eq(User::getStatus, status);
        qw.orderByDesc(User::getCreatedTime);
        Page<User> result = userMapper.selectPage(p, qw);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(),
            result.getRecords().stream().map(this::toVO).toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User u = userMapper.selectById(id);
        if (u == null) throw new BizException(1001, "用户不存在");
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        userMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        User u = userMapper.selectById(id);
        if (u == null) throw new BizException(1001, "用户不存在");
        u.setStatus(status);
        userMapper.updateById(u);
    }

    @Override
    @Transactional
    public String resetPassword(Long id) {
        User u = userMapper.selectById(id);
        if (u == null) throw new BizException(1001, "用户不存在");
        String newPwd = randomPassword();
        u.setPassword(passwordEncoder.encode(newPwd));
        userMapper.updateById(u);
        return newPwd;
    }

    private String randomPassword() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        String symbols = "!@#$%";
        StringBuilder builder = new StringBuilder("Hf");
        for (int i = 0; i < 10; i++) {
            builder.append(alphabet.charAt(secureRandom.nextInt(alphabet.length())));
        }
        builder.append(symbols.charAt(secureRandom.nextInt(symbols.length())));
        builder.append(secureRandom.nextInt(10));
        return builder.toString();
    }

    private void updateUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) return;
        List<Role> roles = roleMapper.selectBatchIds(roleIds);
        Map<Long, Role> validRoles = roles.stream()
            .filter(role -> role.getStatus() == null || role.getStatus() == 1)
            .collect(Collectors.toMap(Role::getId, Function.identity()));
        for (Long roleId : roleIds.stream().distinct().toList()) {
            if (!validRoles.containsKey(roleId)) continue;
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setTenantId(1L);
            userRoleMapper.insert(userRole);
        }
    }

    private UserVO toVO(User u) {
        List<UserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, u.getId()));
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).distinct().toList();
        List<Role> roles = roleIds.isEmpty() ? List.of() : roleMapper.selectBatchIds(roleIds);
        List<String> roleNames = roles.stream().map(Role::getRoleName).toList();
        List<String> roleKeys = roles.stream().map(Role::getRoleKey).toList();
        Dept dept = u.getDeptId() == null ? null : deptMapper.selectById(u.getDeptId());
        return new UserVO(u.getId(), u.getUsername(), u.getRealName(), u.getPhone(), u.getEmail(),
            u.getDeptId(), dept == null ? null : dept.getDeptName(), u.getPost(), u.getStatus(),
            roleIds, roleNames, roleKeys, u.getLastLoginTime(), u.getLastLoginIp(),
            u.getLoginCount(), u.getCreatedTime());
    }
}
