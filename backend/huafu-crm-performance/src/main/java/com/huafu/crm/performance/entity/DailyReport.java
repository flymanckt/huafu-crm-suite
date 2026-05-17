package com.huafu.crm.performance.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@TableName("crm_daily_report")
public class DailyReport {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String reportNo;
    private Long userId;
    private LocalDate reportDate;
    private String contentText;
    private String contentHtml;
    private Integer parseStatus;
    private String parsedJson;
    private Integer opportunityCount;
    private Integer marketIntelligenceCount;
    private Integer lostOrderCount;
    private String parseError;
    private OffsetDateTime parseTime;
    private String wecomMsgId;
    @TableField(fill = FieldFill.INSERT) private String createdBy;
    @TableField(fill = FieldFill.INSERT) private OffsetDateTime createdTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) private OffsetDateTime updatedTime;
    @TableLogic private Integer deleted;
    private Long tenantId;
    @Version private Integer version;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getReportNo() { return reportNo; } public void setReportNo(String v) { this.reportNo = v; }
    public Long getUserId() { return userId; } public void setUserId(Long v) { this.userId = v; }
    public LocalDate getReportDate() { return reportDate; } public void setReportDate(LocalDate v) { this.reportDate = v; }
    public String getContentText() { return contentText; } public void setContentText(String v) { this.contentText = v; }
    public String getContentHtml() { return contentHtml; } public void setContentHtml(String v) { this.contentHtml = v; }
    public Integer getParseStatus() { return parseStatus; } public void setParseStatus(Integer v) { this.parseStatus = v; }
    public String getParsedJson() { return parsedJson; } public void setParsedJson(String v) { this.parsedJson = v; }
    public Integer getOpportunityCount() { return opportunityCount; } public void setOpportunityCount(Integer v) { this.opportunityCount = v; }
    public Integer getMarketIntelligenceCount() { return marketIntelligenceCount; } public void setMarketIntelligenceCount(Integer v) { this.marketIntelligenceCount = v; }
    public Integer getLostOrderCount() { return lostOrderCount; } public void setLostOrderCount(Integer v) { this.lostOrderCount = v; }
    public String getParseError() { return parseError; } public void setParseError(String v) { this.parseError = v; }
    public OffsetDateTime getParseTime() { return parseTime; } public void setParseTime(OffsetDateTime v) { this.parseTime = v; }
    public String getWecomMsgId() { return wecomMsgId; } public void setWecomMsgId(String v) { this.wecomMsgId = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public OffsetDateTime getCreatedTime() { return createdTime; } public void setCreatedTime(OffsetDateTime v) { this.createdTime = v; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String v) { this.updatedBy = v; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; } public void setUpdatedTime(OffsetDateTime v) { this.updatedTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { this.version = v; }
}