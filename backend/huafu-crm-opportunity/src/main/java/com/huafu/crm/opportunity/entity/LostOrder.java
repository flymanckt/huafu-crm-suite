package com.huafu.crm.opportunity.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@TableName("crm_lost_order")
public class LostOrder {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String lostNo;
    private Long opportunityId;
    private Long customerId;
    private Integer lostType;
    private String competitorName;
    private BigDecimal competitorPrice;
    private BigDecimal ourPrice;
    private BigDecimal marginDiff;
    private BigDecimal competitorDiscount;
    private BigDecimal ourDiscount;
    private String reasonDetail;
    private Integer recoveryPossible;
    private LocalDate followUpDate;
    private Long handlerUserId;
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
    public String getLostNo() { return lostNo; } public void setLostNo(String v) { this.lostNo = v; }
    public Long getOpportunityId() { return opportunityId; } public void setOpportunityId(Long v) { this.opportunityId = v; }
    public Long getCustomerId() { return customerId; } public void setCustomerId(Long v) { this.customerId = v; }
    public Integer getLostType() { return lostType; } public void setLostType(Integer v) { this.lostType = v; }
    public String getCompetitorName() { return competitorName; } public void setCompetitorName(String v) { this.competitorName = v; }
    public BigDecimal getCompetitorPrice() { return competitorPrice; } public void setCompetitorPrice(BigDecimal v) { this.competitorPrice = v; }
    public BigDecimal getOurPrice() { return ourPrice; } public void setOurPrice(BigDecimal v) { this.ourPrice = v; }
    public BigDecimal getMarginDiff() { return marginDiff; } public void setMarginDiff(BigDecimal v) { this.marginDiff = v; }
    public BigDecimal getCompetitorDiscount() { return competitorDiscount; } public void setCompetitorDiscount(BigDecimal v) { this.competitorDiscount = v; }
    public BigDecimal getOurDiscount() { return ourDiscount; } public void setOurDiscount(BigDecimal v) { this.ourDiscount = v; }
    public String getReasonDetail() { return reasonDetail; } public void setReasonDetail(String v) { this.reasonDetail = v; }
    public Integer getRecoveryPossible() { return recoveryPossible; } public void setRecoveryPossible(Integer v) { this.recoveryPossible = v; }
    public LocalDate getFollowUpDate() { return followUpDate; } public void setFollowUpDate(LocalDate v) { this.followUpDate = v; }
    public Long getHandlerUserId() { return handlerUserId; } public void setHandlerUserId(Long v) { this.handlerUserId = v; }
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