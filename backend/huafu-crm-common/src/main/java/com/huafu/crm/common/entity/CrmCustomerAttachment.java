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
@TableName("crm_customer_attachment")
public class CrmCustomerAttachment {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long customerId;

    private String attachmentType;

    private String fileName;

    private String fileUrl;

    private Long fileSize;

    private String mimeType;

    private Long uploadedBy;

    private OffsetDateTime uploadedAt;

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
