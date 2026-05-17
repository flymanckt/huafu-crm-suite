package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.customer.dto.UserCreateDTO;
import com.huafu.crm.customer.dto.UserUpdateDTO;
import com.huafu.crm.customer.dto.UserVO;
import com.huafu.crm.customer.entity.User;
import com.huafu.crm.customer.mapper.UserMapper;
import com.huafu.crm.customer.service.AdminUserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserServiceImpl implements AdminUserService {
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminUserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
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
        return toVO(u);
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
        String newPwd = "Huafu2026!";
        u.setPassword(passwordEncoder.encode(newPwd));
        userMapper.updateById(u);
        return newPwd;
    }

    private UserVO toVO(User u) {
        return new UserVO(u.getId(), u.getUsername(), u.getRealName(), u.getPhone(), u.getEmail(),
            u.getDeptId(), u.getPost(), u.getStatus(), u.getLastLoginTime(), u.getLastLoginIp(),
            u.getLoginCount(), u.getCreatedTime());
    }
}
