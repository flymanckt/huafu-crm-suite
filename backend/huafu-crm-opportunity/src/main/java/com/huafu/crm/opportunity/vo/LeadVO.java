package com.huafu.crm.opportunity.vo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record LeadVO(
    Long id, String leadNo, Integer leadType, Long customerId, String customerName,
    String contactName, String contactPhone, String province, String city,
    String productName, String competitorName, BigDecimal competitorPrice,
    BigDecimal competitorDiscount, BigDecimal marginRate, Integer source,
    Long creatorUserId, Long handlerUserId, Integer status, OffsetDateTime convertTime,
    Long convertedOppId, String remark, String createdBy, OffsetDateTime createdTime, Integer version
) {}
