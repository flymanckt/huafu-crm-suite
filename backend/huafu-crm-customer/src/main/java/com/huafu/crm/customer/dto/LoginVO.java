package com.huafu.crm.customer.dto;

public record LoginVO(
    String token,
    UserInfoVO user
) {
    public record UserInfoVO(Long id, String username, String realName, Integer status) {}
}
