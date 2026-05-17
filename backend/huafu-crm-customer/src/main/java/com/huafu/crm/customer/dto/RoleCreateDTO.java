package com.huafu.crm.customer.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleCreateDTO(
    @NotBlank String roleName,
    @NotBlank String roleKey,
    String description
) {}
