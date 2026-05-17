package com.huafu.crm.customer.dto;

import jakarta.validation.constraints.NotBlank;

public record UserCreateDTO(
    @NotBlank String username,
    @NotBlank String password,
    String realName,
    String phone,
    String email,
    Long deptId,
    String post,
    Integer status
) {}
