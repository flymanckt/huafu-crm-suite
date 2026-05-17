package com.huafu.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@TableName("crm_customer_contact")
public class CustomerContact {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long customerId;
    private String contactName;
    private String phone;
    private String telephone;
    private String fax;
    private String wechat;
    private String email;
    private String position;
    private Integer roleType;
    private Integer decisionLevel;
    private Integer isMain;
    private Long parentContactId;
    private LocalDate birthday;
    private String remark;
    private Integer isActive;
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
    public String getContactName() { return contactName; } public void setContactName(String v) { this.contactName = v; }
    public String getPhone() { return phone; } public void setPhone(String v) { this.phone = v; }
    public String getTelephone() { return telephone; } public void setTelephone(String v) { this.telephone = v; }
    public String getFax() { return fax; } public void setFax(String v) { this.fax = v; }
    public String getWechat() { return wechat; } public void setWechat(String v) { this.wechat = v; }
    public String getEmail() { return email; } public void setEmail(String v) { this.email = v; }
    public String getPosition() { return position; } public void setPosition(String v) { this.position = v; }
    public Integer getRoleType() { return roleType; } public void setRoleType(Integer v) { this.roleType = v; }
    public Integer getDecisionLevel() { return decisionLevel; } public void setDecisionLevel(Integer v) { this.decisionLevel = v; }
    public Integer getIsMain() { return isMain; } public void setIsMain(Integer v) { this.isMain = v; }
    public Long getParentContactId() { return parentContactId; } public void setParentContactId(Long v) { this.parentContactId = v; }
    public LocalDate getBirthday() { return birthday; } public void setBirthday(LocalDate v) { this.birthday = v; }
    public String getRemark() { return remark; } public void setRemark(String v) { this.remark = v; }
    public Integer getIsActive() { return isActive; } public void setIsActive(Integer v) { this.isActive = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public OffsetDateTime getCreatedTime() { return createdTime; } public void setCreatedTime(OffsetDateTime v) { this.createdTime = v; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String v) { this.updatedBy = v; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; } public void setUpdatedTime(OffsetDateTime v) { this.updatedTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { this.version = v; }
}
