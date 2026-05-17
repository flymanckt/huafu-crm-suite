package com.huafu.crm.opportunity.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@TableName("crm_opportunity")
public class Opportunity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String oppNo;
    private String opportunityName;
    private Long customerId;
    private Long handlerUserId;
    private Integer stage;
    private OffsetDateTime stageUpdateTime;
    private String productName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal estimatedAmount;
    private LocalDate expectedCloseDate;
    private LocalDate actualCloseDate;
    private Integer winProbability;
    private String lostReason;
    private Integer lostType;
    private String competitorName;
    private Long quoteId;
    private String orderNo;
    private String remark;
    private Long sourceLeadId;
    private String rawText;
    private String parsedJson;
    @TableField(fill = FieldFill.INSERT) private String createdBy;
    @TableField(fill = FieldFill.INSERT) private OffsetDateTime createdTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) private OffsetDateTime updatedTime;
    @TableLogic private Integer deleted;
    private Long tenantId;
    @Version private Integer version;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getOppNo() { return oppNo; } public void setOppNo(String v) { this.oppNo = v; }
    public String getOpportunityName() { return opportunityName; } public void setOpportunityName(String v) { this.opportunityName = v; }
    public Long getCustomerId() { return customerId; } public void setCustomerId(Long v) { this.customerId = v; }
    public Long getHandlerUserId() { return handlerUserId; } public void setHandlerUserId(Long v) { this.handlerUserId = v; }
    public Integer getStage() { return stage; } public void setStage(Integer v) { this.stage = v; }
    public OffsetDateTime getStageUpdateTime() { return stageUpdateTime; } public void setStageUpdateTime(OffsetDateTime v) { this.stageUpdateTime = v; }
    public String getProductName() { return productName; } public void setProductName(String v) { this.productName = v; }
    public BigDecimal getQuantity() { return quantity; } public void setQuantity(BigDecimal v) { this.quantity = v; }
    public String getUnit() { return unit; } public void setUnit(String v) { this.unit = v; }
    public BigDecimal getEstimatedAmount() { return estimatedAmount; } public void setEstimatedAmount(BigDecimal v) { this.estimatedAmount = v; }
    public LocalDate getExpectedCloseDate() { return expectedCloseDate; } public void setExpectedCloseDate(LocalDate v) { this.expectedCloseDate = v; }
    public LocalDate getActualCloseDate() { return actualCloseDate; } public void setActualCloseDate(LocalDate v) { this.actualCloseDate = v; }
    public Integer getWinProbability() { return winProbability; } public void setWinProbability(Integer v) { this.winProbability = v; }
    public String getLostReason() { return lostReason; } public void setLostReason(String v) { this.lostReason = v; }
    public Integer getLostType() { return lostType; } public void setLostType(Integer v) { this.lostType = v; }
    public String getCompetitorName() { return competitorName; } public void setCompetitorName(String v) { this.competitorName = v; }
    public Long getQuoteId() { return quoteId; } public void setQuoteId(Long v) { this.quoteId = v; }
    public String getOrderNo() { return orderNo; } public void setOrderNo(String v) { this.orderNo = v; }
    public String getRemark() { return remark; } public void setRemark(String v) { this.remark = v; }
    public Long getSourceLeadId() { return sourceLeadId; } public void setSourceLeadId(Long v) { this.sourceLeadId = v; }
    public String getRawText() { return rawText; } public void setRawText(String v) { this.rawText = v; }
    public String getParsedJson() { return parsedJson; } public void setParsedJson(String v) { this.parsedJson = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public OffsetDateTime getCreatedTime() { return createdTime; } public void setCreatedTime(OffsetDateTime v) { this.createdTime = v; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String v) { this.updatedBy = v; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; } public void setUpdatedTime(OffsetDateTime v) { this.updatedTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { this.version = v; }
}