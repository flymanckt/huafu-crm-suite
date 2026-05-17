package com.huafu.crm.customer.dto;

public record DeptUpdateDTO(
    String deptName,
    Long parentId,
    Long leaderUserId
) {}
