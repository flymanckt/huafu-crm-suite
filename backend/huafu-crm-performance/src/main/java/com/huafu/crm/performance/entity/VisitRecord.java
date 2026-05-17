package com.huafu.crm.performance.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@TableName("crm_visit_record")
public class VisitRecord {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String visitNo;
    private Long userId;
    private Long customerId;
    private String customerName;
    private Long contactId;
    private String contactName;
    private LocalDate visitDate;
    private Integer visitType;
    private String visitPurpose;
    private String visitContent;
    private LocalDate nextVisitPlan;
    private Integer isNewCustomer;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String locationName;
    private Long customerAddressId;
    private BigDecimal checkinLongitude;
    private BigDecimal checkinLatitude;
    private String checkinAddress;
    private OffsetDateTime checkinTime;
    private BigDecimal checkinDistanceMeters;
    private Integer checkinRadiusMeters;
    private Integer checkinMatched;
    private String checkinResult;
    private String remark;
    @TableField(fill = FieldFill.INSERT) private String createdBy;
    @TableField(fill = FieldFill.INSERT) private OffsetDateTime createdTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) private OffsetDateTime updatedTime;
    @TableLogic private Integer deleted;
    private Long tenantId;
    @Version private Integer version;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getVisitNo() { return visitNo; } public void setVisitNo(String v) { this.visitNo = v; }
    public Long getUserId() { return userId; } public void setUserId(Long v) { this.userId = v; }
    public Long getCustomerId() { return customerId; } public void setCustomerId(Long v) { this.customerId = v; }
    public String getCustomerName() { return customerName; } public void setCustomerName(String v) { this.customerName = v; }
    public Long getContactId() { return contactId; } public void setContactId(Long v) { this.contactId = v; }
    public String getContactName() { return contactName; } public void setContactName(String v) { this.contactName = v; }
    public LocalDate getVisitDate() { return visitDate; } public void setVisitDate(LocalDate v) { this.visitDate = v; }
    public Integer getVisitType() { return visitType; } public void setVisitType(Integer v) { this.visitType = v; }
    public String getVisitPurpose() { return visitPurpose; } public void setVisitPurpose(String v) { this.visitPurpose = v; }
    public String getVisitContent() { return visitContent; } public void setVisitContent(String v) { this.visitContent = v; }
    public LocalDate getNextVisitPlan() { return nextVisitPlan; } public void setNextVisitPlan(LocalDate v) { this.nextVisitPlan = v; }
    public Integer getIsNewCustomer() { return isNewCustomer; } public void setIsNewCustomer(Integer v) { this.isNewCustomer = v; }
    public BigDecimal getLongitude() { return longitude; } public void setLongitude(BigDecimal v) { this.longitude = v; }
    public BigDecimal getLatitude() { return latitude; } public void setLatitude(BigDecimal v) { this.latitude = v; }
    public String getLocationName() { return locationName; } public void setLocationName(String v) { this.locationName = v; }
    public Long getCustomerAddressId() { return customerAddressId; } public void setCustomerAddressId(Long v) { this.customerAddressId = v; }
    public BigDecimal getCheckinLongitude() { return checkinLongitude; } public void setCheckinLongitude(BigDecimal v) { this.checkinLongitude = v; }
    public BigDecimal getCheckinLatitude() { return checkinLatitude; } public void setCheckinLatitude(BigDecimal v) { this.checkinLatitude = v; }
    public String getCheckinAddress() { return checkinAddress; } public void setCheckinAddress(String v) { this.checkinAddress = v; }
    public OffsetDateTime getCheckinTime() { return checkinTime; } public void setCheckinTime(OffsetDateTime v) { this.checkinTime = v; }
    public BigDecimal getCheckinDistanceMeters() { return checkinDistanceMeters; } public void setCheckinDistanceMeters(BigDecimal v) { this.checkinDistanceMeters = v; }
    public Integer getCheckinRadiusMeters() { return checkinRadiusMeters; } public void setCheckinRadiusMeters(Integer v) { this.checkinRadiusMeters = v; }
    public Integer getCheckinMatched() { return checkinMatched; } public void setCheckinMatched(Integer v) { this.checkinMatched = v; }
    public String getCheckinResult() { return checkinResult; } public void setCheckinResult(String v) { this.checkinResult = v; }
    public String getRemark() { return remark; } public void setRemark(String v) { this.remark = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public OffsetDateTime getCreatedTime() { return createdTime; } public void setCreatedTime(OffsetDateTime v) { this.createdTime = v; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String v) { this.updatedBy = v; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; } public void setUpdatedTime(OffsetDateTime v) { this.updatedTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { this.version = v; }
}
