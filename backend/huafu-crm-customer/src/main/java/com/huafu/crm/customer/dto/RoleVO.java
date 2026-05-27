package com.huafu.crm.customer.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record RoleVO(
    Long id,
    String roleName,
    String roleKey,
    String description,
    Integer status,
    Integer dataScope,
    Integer userCount,
    List<Long> menuIds,
    OffsetDateTime createdTime
) {}
