package com.huafu.crm.customer.dto;

import java.time.LocalDate;

public record ModuleRecordSaveDTO(
    String recordNo,
    String title,
    String status,
    String ownerName,
    LocalDate recordDate,
    String payloadJson,
    String remark
) {}
