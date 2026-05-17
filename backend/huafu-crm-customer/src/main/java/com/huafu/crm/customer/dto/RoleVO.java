package com.huafu.crm.customer.dto;

import java.time.OffsetDateTime;

public record RoleVO(
    Long id,
    String roleName,
    String roleKey,
    String description,
    OffsetDateTime createdTime
) {}
