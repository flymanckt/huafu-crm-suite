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
@TableName("crm_customer_sap_org")
public class CrmCustomerSapOrg {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long customerId;

    private String sapCode;

    private String companyCode;

    private String salesOrg;

    private String salesOffice;

    private String salesGroup;

    private String priceList;

    private String currency;

    private String deliveryPlant;

    private String paymentTerms;

    private String taxClassification;

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
