package com.huafu.crm.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginDTO(
    @NotBlank(message = "用户名不能为空") @Size(max = 64, message = "用户名长度不能超过64位") String username,
    @NotBlank(message = "密码不能为空") @Size(max = 128, message = "密码长度不能超过128位") String password
) {}
