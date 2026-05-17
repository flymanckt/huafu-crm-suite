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
@TableName("crm_customer_bundle")
public class CrmCustomerBundle {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("customer_id")
    private Long parentCustomerId;

    @TableField("bundle_customer_id")
    private Long childCustomerId;

    private Integer bundleType;

    private String relationRemark;

    @TableField(exist = false)
    private String bundleBrand;

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

    private Long tenantId;
}
