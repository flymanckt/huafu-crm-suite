package com.huafu.crm.customer.dto;

import jakarta.validation.constraints.NotBlank;

public record DeptCreateDTO(
    @NotBlank String deptName,
    Long parentId,
    Long leaderUserId
) {}
