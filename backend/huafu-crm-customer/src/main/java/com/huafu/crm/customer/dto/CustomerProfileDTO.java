package com.huafu.crm.customer.dto;

public record CustomerProfileDTO(
    Long id,
    Long customerId,
    String industryPosition,
    String mainCustomerGroup,
    String mainBrands,
    String yarnVolumeSummary,
    String competitorSummary,
    String machineSummary,
    String otherAssets,
    String overviewAuto,
    String overviewManual,
    Integer overviewEditable
) {}
