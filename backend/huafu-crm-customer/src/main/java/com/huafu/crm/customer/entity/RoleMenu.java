package com.huafu.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.OffsetDateTime;

@TableName("crm_role_menu")
public class RoleMenu {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long roleId;
    private Long menuId;
    @TableField(fill = FieldFill.INSERT) private String createdBy;
    @TableField(fill = FieldFill.INSERT) private OffsetDateTime createdTime;
    @TableLogic private Integer deleted;
    private Long tenantId;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getRoleId() { return roleId; } public void setRoleId(Long v) { this.roleId = v; }
    public Long getMenuId() { return menuId; } public void setMenuId(Long v) { this.menuId = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public OffsetDateTime getCreatedTime() { return createdTime; } public void setCreatedTime(OffsetDateTime v) { this.createdTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
}
