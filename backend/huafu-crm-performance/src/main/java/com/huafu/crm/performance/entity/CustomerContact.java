package com.huafu.crm.performance.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.OffsetDateTime;

@TableName("crm_customer_contact")
public class CustomerContact {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long customerId;
    private String contactName;
    private String phone;
    @TableField(fill = FieldFill.INSERT_UPDATE) private OffsetDateTime updatedTime;
    @TableLogic private Integer deleted;
    private Long tenantId;
    @Version private Integer version;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; } public void setCustomerId(Long v) { this.customerId = v; }
    public String getContactName() { return contactName; } public void setContactName(String v) { this.contactName = v; }
    public String getPhone() { return phone; } public void setPhone(String v) { this.phone = v; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; } public void setUpdatedTime(OffsetDateTime v) { this.updatedTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { this.version = v; }
}
