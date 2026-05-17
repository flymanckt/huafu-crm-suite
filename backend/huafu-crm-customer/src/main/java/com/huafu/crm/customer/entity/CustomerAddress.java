package com.huafu.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@TableName("crm_customer_address")
public class CustomerAddress {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long customerId;
    private Integer addressType;
    private String contactName;
    private String phone;
    private String country;
    private String province;
    private String city;
    private String district;
    private String addressDetail;
    private String fullAddress;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String amapPoiId;
    private String amapPoiName;
    private String amapAdcode;
    private String amapLevel;
    private String locationSource;
    private Integer locationVerified;
    private OffsetDateTime locationVerifiedTime;
    private Integer checkinRadiusMeters;
    private String addressRemark;
    private Integer isDefault;
    @TableField(fill = FieldFill.INSERT) private String createdBy;
    @TableField(fill = FieldFill.INSERT) private OffsetDateTime createdTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) private OffsetDateTime updatedTime;
    @TableLogic private Integer deleted;
    private Long tenantId;
    @Version private Integer version;

    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public Long getCustomerId() { return customerId; } public void setCustomerId(Long v) { this.customerId = v; }
    public Integer getAddressType() { return addressType; } public void setAddressType(Integer v) { this.addressType = v; }
    public String getContactName() { return contactName; } public void setContactName(String v) { this.contactName = v; }
    public String getPhone() { return phone; } public void setPhone(String v) { this.phone = v; }
    public String getCountry() { return country; } public void setCountry(String v) { this.country = v; }
    public String getProvince() { return province; } public void setProvince(String v) { this.province = v; }
    public String getCity() { return city; } public void setCity(String v) { this.city = v; }
    public String getDistrict() { return district; } public void setDistrict(String v) { this.district = v; }
    public String getAddressDetail() { return addressDetail; } public void setAddressDetail(String v) { this.addressDetail = v; }
    public String getFullAddress() { return fullAddress; } public void setFullAddress(String v) { this.fullAddress = v; }
    public BigDecimal getLongitude() { return longitude; } public void setLongitude(BigDecimal v) { this.longitude = v; }
    public BigDecimal getLatitude() { return latitude; } public void setLatitude(BigDecimal v) { this.latitude = v; }
    public String getAmapPoiId() { return amapPoiId; } public void setAmapPoiId(String v) { this.amapPoiId = v; }
    public String getAmapPoiName() { return amapPoiName; } public void setAmapPoiName(String v) { this.amapPoiName = v; }
    public String getAmapAdcode() { return amapAdcode; } public void setAmapAdcode(String v) { this.amapAdcode = v; }
    public String getAmapLevel() { return amapLevel; } public void setAmapLevel(String v) { this.amapLevel = v; }
    public String getLocationSource() { return locationSource; } public void setLocationSource(String v) { this.locationSource = v; }
    public Integer getLocationVerified() { return locationVerified; } public void setLocationVerified(Integer v) { this.locationVerified = v; }
    public OffsetDateTime getLocationVerifiedTime() { return locationVerifiedTime; } public void setLocationVerifiedTime(OffsetDateTime v) { this.locationVerifiedTime = v; }
    public Integer getCheckinRadiusMeters() { return checkinRadiusMeters; } public void setCheckinRadiusMeters(Integer v) { this.checkinRadiusMeters = v; }
    public String getAddressRemark() { return addressRemark; } public void setAddressRemark(String v) { this.addressRemark = v; }
    public Integer getIsDefault() { return isDefault; } public void setIsDefault(Integer v) { this.isDefault = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public OffsetDateTime getCreatedTime() { return createdTime; } public void setCreatedTime(OffsetDateTime v) { this.createdTime = v; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String v) { this.updatedBy = v; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; } public void setUpdatedTime(OffsetDateTime v) { this.updatedTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { this.version = v; }
}
