package com.huafu.crm.customer.dto;

import java.time.OffsetDateTime;

public record UserVO(
    Long id,
    String username,
    String realName,
    String phone,
    String email,
    Long deptId,
    String post,
    Integer status,
    OffsetDateTime lastLoginTime,
    String lastLoginIp,
    Integer loginCount,
    OffsetDateTime createdTime
) {}
