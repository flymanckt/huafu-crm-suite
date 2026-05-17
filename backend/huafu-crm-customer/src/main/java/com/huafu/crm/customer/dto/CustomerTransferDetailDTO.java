package com.huafu.crm.customer.dto;

import java.time.LocalDate;

public record CustomerTransferDetailDTO(
    Long id,
    Long customerId,
    Long fromUserId,
    Long toUserId,
    Long supervisorUserId,
    LocalDate transferDate,
    String reason,
    String businessSummary,
    String contactInfoSnapshot,
    String receivableInfo,
    String cooperationIssues,
    String futureOpportunities,
    String handoverAttachments,
    Integer status
) {}
