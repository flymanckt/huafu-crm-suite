package com.huafu.crm.customer.dto;

public record UserUpdateDTO(
    String realName,
    String phone,
    String email,
    Long deptId,
    String post,
    Integer status,
    java.util.List<Long> roleIds
) {}
