package com.huafu.crm.opportunity.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@TableName("crm_quote_item")
public class QuoteItem {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long quoteId;
    private String productName;
    private String productCode;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String leadTime;
    private String remark;
    @TableField(fill = FieldFill.INSERT) private String createdBy;
    @TableField(fill = FieldFill.INSERT) private OffsetDateTime createdTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) private OffsetDateTime updatedTime;
    @TableLogic private Integer deleted;
    private Long tenantId;
    @Version private Integer version;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getQuoteId() { return quoteId; } public void setQuoteId(Long v) { this.quoteId = v; }
    public String getProductName() { return productName; } public void setProductName(String v) { this.productName = v; }
    public String getProductCode() { return productCode; } public void setProductCode(String v) { this.productCode = v; }
    public BigDecimal getQuantity() { return quantity; } public void setQuantity(BigDecimal v) { this.quantity = v; }
    public String getUnit() { return unit; } public void setUnit(String v) { this.unit = v; }
    public BigDecimal getUnitPrice() { return unitPrice; } public void setUnitPrice(BigDecimal v) { this.unitPrice = v; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal v) { this.amount = v; }
    public String getLeadTime() { return leadTime; } public void setLeadTime(String v) { this.leadTime = v; }
    public String getRemark() { return remark; } public void setRemark(String v) { this.remark = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public OffsetDateTime getCreatedTime() { return createdTime; } public void setCreatedTime(OffsetDateTime v) { this.createdTime = v; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String v) { this.updatedBy = v; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; } public void setUpdatedTime(OffsetDateTime v) { this.updatedTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { this.version = v; }
}