package com.huafu.crm.customer.dto;

public record CustomerTransferDTO(
    Long toUserId,
    String reason
) {}
