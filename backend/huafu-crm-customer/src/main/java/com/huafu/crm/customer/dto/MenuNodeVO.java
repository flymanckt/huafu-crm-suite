package com.huafu.crm.customer.dto;

import java.util.List;

public record MenuNodeVO(
    Long id,
    String menuName,
    String permission,
    String path,
    List<MenuNodeVO> children
) {}
