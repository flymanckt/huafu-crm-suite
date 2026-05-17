package com.huafu.crm.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("crm_customer_ext")
public class CrmCustomerExt {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long customerId;

    private String unifiedCreditCode;

    private String englishName;

    private String assetType;

    private String customerCategory;

    private String customerSource;

    private String sapAccountGroup;

    private String sapCountryCode;

    private String sapRegion;

    private String phone;

    private String fax;

    private String taxRateCategory;

    private String podRelevance;

    private String accountAssignmentGroup;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedTime;

    @TableLogic
    private Integer deleted;
}
