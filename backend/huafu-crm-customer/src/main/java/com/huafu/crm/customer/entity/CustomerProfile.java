package com.huafu.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.OffsetDateTime;

@TableName("crm_customer_profile")
public class CustomerProfile {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long customerId;
    private String industryPosition;
    private String mainCustomerGroup;
    private String mainBrands;
    private String yarnVolumeSummary;
    private String competitorSummary;
    private String machineSummary;
    private String otherAssets;
    private String overviewAuto;
    private String overviewManual;
    private Integer overviewEditable;
    // BUG-002 新增字段
    private String tags;        // JSON 字符串，存储标签数组
    private String remark;      // 备注
    private String customerStage; // 客户阶段
    @TableField(fill = FieldFill.INSERT) private String createdBy;
    @TableField(fill = FieldFill.INSERT) private OffsetDateTime createdTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) private OffsetDateTime updatedTime;
    @TableLogic private Integer deleted;
    private Long tenantId;
    @Version private Integer version;

    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public Long getCustomerId() { return customerId; } public void setCustomerId(Long v) { this.customerId = v; }
    public String getIndustryPosition() { return industryPosition; } public void setIndustryPosition(String v) { this.industryPosition = v; }
    public String getMainCustomerGroup() { return mainCustomerGroup; } public void setMainCustomerGroup(String v) { this.mainCustomerGroup = v; }
    public String getMainBrands() { return mainBrands; } public void setMainBrands(String v) { this.mainBrands = v; }
    public String getYarnVolumeSummary() { return yarnVolumeSummary; } public void setYarnVolumeSummary(String v) { this.yarnVolumeSummary = v; }
    public String getCompetitorSummary() { return competitorSummary; } public void setCompetitorSummary(String v) { this.competitorSummary = v; }
    public String getMachineSummary() { return machineSummary; } public void setMachineSummary(String v) { this.machineSummary = v; }
    public String getOtherAssets() { return otherAssets; } public void setOtherAssets(String v) { this.otherAssets = v; }
    public String getOverviewAuto() { return overviewAuto; } public void setOverviewAuto(String v) { this.overviewAuto = v; }
    public String getOverviewManual() { return overviewManual; } public void setOverviewManual(String v) { this.overviewManual = v; }
    public Integer getOverviewEditable() { return overviewEditable; } public void setOverviewEditable(Integer v) { this.overviewEditable = v; }
    public String getTags() { return tags; } public void setTags(String v) { this.tags = v; }
    public String getRemark() { return remark; } public void setRemark(String v) { this.remark = v; }
    public String getCustomerStage() { return customerStage; } public void setCustomerStage(String v) { this.customerStage = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public OffsetDateTime getCreatedTime() { return createdTime; } public void setCreatedTime(OffsetDateTime v) { this.createdTime = v; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String v) { this.updatedBy = v; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; } public void setUpdatedTime(OffsetDateTime v) { this.updatedTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { this.version = v; }
}
