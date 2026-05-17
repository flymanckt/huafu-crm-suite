package com.huafu.crm.customer.dto;

import java.util.List;

public record DeptVO(
    Long id,
    String deptName,
    Long parentId,
    Long leaderUserId,
    List<DeptVO> children
) {}
