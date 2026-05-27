package com.huafu.crm.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateDTO(
    @NotBlank @Size(max = 64) String username,
    @NotBlank
    @Size(min = 10, max = 128, message = "密码长度需在10到128位之间")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
        message = "密码需包含大小写字母、数字和特殊字符"
    )
    String password,
    String realName,
    String phone,
    String email,
    Long deptId,
    String post,
    Integer status,
    java.util.List<Long> roleIds
) {}
