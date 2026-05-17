package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.common.utils.JwtUtil;
import com.huafu.crm.customer.dto.LoginDTO;
import com.huafu.crm.customer.dto.LoginVO;
import com.huafu.crm.customer.entity.User;
import com.huafu.crm.customer.mapper.UserMapper;
import com.huafu.crm.customer.service.AuthService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
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
        if (user.getStatus() == 0) {
            throw new BizException(401, "账号已禁用");
        }
        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }

        // 更新登录信息
        user.setLastLoginTime(OffsetDateTime.now());
        user.setLoginCount(user.getLoginCount() == null ? 1 : user.getLoginCount() + 1);
        userMapper.updateById(user);

        // 生成 token
        String token = jwtUtil.generate(user.getId(), user.getUsername(), null);

        return new LoginVO(
            token,
            new LoginVO.UserInfoVO(user.getId(), user.getUsername(), user.getRealName(), user.getStatus())
        );
    }
}
