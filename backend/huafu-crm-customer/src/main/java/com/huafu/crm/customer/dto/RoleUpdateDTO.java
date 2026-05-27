package com.huafu.crm.customer.dto;

public record RoleUpdateDTO(
    String roleName,
    String description,
    Integer status,
    Integer dataScope,
    java.util.List<Long> menuIds
) {}
