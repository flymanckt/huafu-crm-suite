package com.huafu.crm.performance.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@TableName("crm_customer")
public class Customer {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String customerCode;
    private String customerName;
    private Long ownerUserId;
    @TableField(fill = FieldFill.INSERT) private String createdBy;
    @TableField(fill = FieldFill.INSERT) private OffsetDateTime createdTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) private OffsetDateTime updatedTime;
    private OffsetDateTime lastVisitTime;
    @TableLogic private Integer deleted;
    private Long tenantId;
    @Version private Integer version;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getCustomerCode() { return customerCode; } public void setCustomerCode(String v) { this.customerCode = v; }
    public String getCustomerName() { return customerName; } public void setCustomerName(String v) { this.customerName = v; }
    public Long getOwnerUserId() { return ownerUserId; } public void setOwnerUserId(Long v) { this.ownerUserId = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public OffsetDateTime getCreatedTime() { return createdTime; } public void setCreatedTime(OffsetDateTime v) { this.createdTime = v; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String v) { this.updatedBy = v; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; } public void setUpdatedTime(OffsetDateTime v) { this.updatedTime = v; }
    public OffsetDateTime getLastVisitTime() { return lastVisitTime; } public void setLastVisitTime(OffsetDateTime v) { this.lastVisitTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { this.version = v; }
}
