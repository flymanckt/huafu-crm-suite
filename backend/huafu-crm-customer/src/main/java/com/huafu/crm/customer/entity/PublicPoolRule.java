package com.huafu.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.OffsetDateTime;

@TableName("crm_public_pool_rule")
public class PublicPoolRule {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String ruleName;
    private Integer conditionType;
    private Integer conditionDays;
    private Integer actionType;
    private Long targetUserId;
    private Long deptId;
    private Integer enabled;
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
    public String getRuleName() { return ruleName; } public void setRuleName(String v) { this.ruleName = v; }
    public Integer getConditionType() { return conditionType; } public void setConditionType(Integer v) { this.conditionType = v; }
    public Integer getConditionDays() { return conditionDays; } public void setConditionDays(Integer v) { this.conditionDays = v; }
    public Integer getActionType() { return actionType; } public void setActionType(Integer v) { this.actionType = v; }
    public Long getTargetUserId() { return targetUserId; } public void setTargetUserId(Long v) { this.targetUserId = v; }
    public Long getDeptId() { return deptId; } public void setDeptId(Long v) { this.deptId = v; }
    public Integer getEnabled() { return enabled; } public void setEnabled(Integer v) { this.enabled = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public OffsetDateTime getCreatedTime() { return createdTime; } public void setCreatedTime(OffsetDateTime v) { this.createdTime = v; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String v) { this.updatedBy = v; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; } public void setUpdatedTime(OffsetDateTime v) { this.updatedTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { this.version = v; }
}
