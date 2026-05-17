package com.huafu.crm.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("crm_customer_contact")
public class CrmCustomerContact {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long customerId;

    @TableField("contact_name")
    private String contactName;

    private String position;

    private String phone;

    @TableField("telephone")
    private String telephone;

    private String wechat;

    private String email;

    private String fax;

    @TableField("role_type")
    private Short roleType;

    @TableField("decision_level")
    private Short decisionLevel;

    @TableField("is_main")
    private Short isMain;

    private LocalDate birthday;

    private String remark;

    @TableField("is_active")
    private Short isActive;

    private Long tenantId;

    @TableField("parent_contact_id")
    private Long parentContactId;

    @TableField(exist = false)
    private List<CrmCustomerContact> children;

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
    private Short deleted;
}
