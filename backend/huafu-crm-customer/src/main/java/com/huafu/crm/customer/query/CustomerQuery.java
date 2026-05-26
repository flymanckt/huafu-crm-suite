package com.huafu.crm.customer.query;

public record CustomerQuery(
    Integer current,
    Integer size,
    String customerName,
    String customerCode,
    Integer type,
    Integer level,
    Integer status,
    Long ownerUserId,
    Integer businessType,
    String customerCategory,
    String customerSource,
    String mainCustomerGroup,
    Integer customerStage,
    Integer riskLevel,
    String salesMerchandiser
) {
    public CustomerQuery {
        if (current == null) current = 1;
        if (size == null) size = 20;
    }
}
