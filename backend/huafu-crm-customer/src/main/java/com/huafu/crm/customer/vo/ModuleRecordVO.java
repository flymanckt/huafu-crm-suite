package com.huafu.crm.customer.vo;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ModuleRecordVO(
    Long id,
    String moduleKey,
    String recordNo,
    String title,
    String status,
    String ownerName,
    LocalDate recordDate,
    String payloadJson,
    String remark,
    OffsetDateTime createdTime,
    OffsetDateTime updatedTime
) {}
