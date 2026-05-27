package com.huafu.crm.customer.dto;

public record LoginVO(
    String token,
    UserInfoVO user
) {
    public record UserInfoVO(
        Long id,
        String username,
        String realName,
        Long deptId,
        Integer status,
        java.util.List<Long> roleIds,
        java.util.List<String> roleKeys,
        java.util.List<String> roleNames,
        java.util.List<Long> menuIds,
        java.util.List<String> permissions
    ) {}
}
