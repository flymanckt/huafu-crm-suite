package com.huafu.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.OffsetDateTime;

@TableName("crm_user")
public class User {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private Long deptId;
    private String post;
    private Integer status;
    private OffsetDateTime lastLoginTime;
    private String lastLoginIp;
    private Integer loginCount;
    @TableField(fill = FieldFill.INSERT) private String createdBy;
    @TableField(fill = FieldFill.INSERT) private OffsetDateTime createdTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) private OffsetDateTime updatedTime;
    @TableLogic private Integer deleted;
    private Long tenantId;
    @Version private Integer version;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; } public void setUsername(String v) { this.username = v; }
    public String getPassword() { return password; } public void setPassword(String v) { this.password = v; }
    public String getRealName() { return realName; } public void setRealName(String v) { this.realName = v; }
    public String getPhone() { return phone; } public void setPhone(String v) { this.phone = v; }
    public String getEmail() { return email; } public void setEmail(String v) { this.email = v; }
    public Long getDeptId() { return deptId; } public void setDeptId(Long v) { this.deptId = v; }
    public String getPost() { return post; } public void setPost(String v) { this.post = v; }
    public Integer getStatus() { return status; } public void setStatus(Integer v) { this.status = v; }
    public OffsetDateTime getLastLoginTime() { return lastLoginTime; } public void setLastLoginTime(OffsetDateTime v) { this.lastLoginTime = v; }
    public String getLastLoginIp() { return lastLoginIp; } public void setLastLoginIp(String v) { this.lastLoginIp = v; }
    public Integer getLoginCount() { return loginCount; } public void setLoginCount(Integer v) { this.loginCount = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public OffsetDateTime getCreatedTime() { return createdTime; } public void setCreatedTime(OffsetDateTime v) { this.createdTime = v; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String v) { this.updatedBy = v; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; } public void setUpdatedTime(OffsetDateTime v) { this.updatedTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { this.version = v; }
}
