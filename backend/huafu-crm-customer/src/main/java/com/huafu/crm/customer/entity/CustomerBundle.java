package com.huafu.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.OffsetDateTime;

@TableName("crm_customer_bundle")
public class CustomerBundle {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long customerId;
    private Integer bundleType;
    private Long bundleCustomerId;
    private String relationRemark;
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
    @Version
    private Integer version;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; } public void setCustomerId(Long v) { this.customerId = v; }
    public Integer getBundleType() { return bundleType; } public void setBundleType(Integer v) { this.bundleType = v; }
    public Long getBundleCustomerId() { return bundleCustomerId; } public void setBundleCustomerId(Long v) { this.bundleCustomerId = v; }
    public String getRelationRemark() { return relationRemark; } public void setRelationRemark(String v) { this.relationRemark = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public OffsetDateTime getCreatedTime() { return createdTime; } public void setCreatedTime(OffsetDateTime v) { this.createdTime = v; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String v) { this.updatedBy = v; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; } public void setUpdatedTime(OffsetDateTime v) { this.updatedTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { this.version = v; }
}
