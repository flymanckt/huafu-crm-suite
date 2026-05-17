package com.huafu.crm.customer.service;

import com.huafu.crm.customer.dto.LoginDTO;
import com.huafu.crm.customer.dto.LoginVO;

public interface AuthService {
    LoginVO login(LoginDTO dto);
}
