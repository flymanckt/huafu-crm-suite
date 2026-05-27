package com.huafu.crm.customer.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record UserVO(
    Long id,
    String username,
    String realName,
    String phone,
    String email,
    Long deptId,
    String deptName,
    String post,
    Integer status,
    List<Long> roleIds,
    List<String> roleNames,
    List<String> roleKeys,
    OffsetDateTime lastLoginTime,
    String lastLoginIp,
    Integer loginCount,
    OffsetDateTime createdTime
) {}
