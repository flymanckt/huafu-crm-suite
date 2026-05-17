package com.huafu.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.OffsetDateTime;

@TableName("crm_dept")
public class Dept {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String deptName;
    private Long parentId;
    private Long leaderUserId;
    @TableField(fill = FieldFill.INSERT) private String createdBy;
    @TableField(fill = FieldFill.INSERT) private OffsetDateTime createdTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) private OffsetDateTime updatedTime;
    @TableLogic private Integer deleted;
    private Long tenantId;
    @Version private Integer version;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getDeptName() { return deptName; } public void setDeptName(String v) { this.deptName = v; }
    public Long getParentId() { return parentId; } public void setParentId(Long v) { this.parentId = v; }
    public Long getLeaderUserId() { return leaderUserId; } public void setLeaderUserId(Long v) { this.leaderUserId = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public OffsetDateTime getCreatedTime() { return createdTime; } public void setCreatedTime(OffsetDateTime v) { this.createdTime = v; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String v) { this.updatedBy = v; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; } public void setUpdatedTime(OffsetDateTime v) { this.updatedTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { this.version = v; }
}
