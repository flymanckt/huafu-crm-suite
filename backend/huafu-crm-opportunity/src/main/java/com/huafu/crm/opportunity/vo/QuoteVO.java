package com.huafu.crm.opportunity.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record QuoteVO(
    Long id,
    String quoteNo,
    Long customerId,
    Long opportunityId,
    Long contactId,
    String contactName,
    String contactPhone,
    Long salesUserId,
    LocalDate quoteDate,
    LocalDate validDate,
    BigDecimal totalAmount,
    BigDecimal discountRate,
    BigDecimal finalAmount,
    String paymentTerms,
    String deliveryTerms,
    Integer status,
    OffsetDateTime sentTime,
    OffsetDateTime confirmedTime,
    String remark,
    List<QuoteItemVO> items,
    OffsetDateTime createdTime
) {}
