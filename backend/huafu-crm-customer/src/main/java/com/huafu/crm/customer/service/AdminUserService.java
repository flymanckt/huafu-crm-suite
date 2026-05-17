package com.huafu.crm.customer.service;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.customer.dto.UserCreateDTO;
import com.huafu.crm.customer.dto.UserUpdateDTO;
import com.huafu.crm.customer.dto.UserVO;

public interface AdminUserService {
    UserVO create(UserCreateDTO dto);
    UserVO update(Long id, UserUpdateDTO dto);
    UserVO getById(Long id);
    PageResult<UserVO> page(String username, String realName, Long deptId, Integer status, long current, long size);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
    String resetPassword(Long id);
}
