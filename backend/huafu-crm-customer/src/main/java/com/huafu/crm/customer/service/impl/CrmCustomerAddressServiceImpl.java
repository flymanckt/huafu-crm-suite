package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.common.util.InputSanitizer;
import com.huafu.crm.customer.entity.CustomerAddress;
import com.huafu.crm.customer.mapper.CustomerAddressMapper;
import com.huafu.crm.customer.service.CrmCustomerAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class CrmCustomerAddressServiceImpl implements CrmCustomerAddressService {

    private final CustomerAddressMapper mapper;

    public CrmCustomerAddressServiceImpl(CustomerAddressMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerAddress> getByCustomerId(Long customerId) {
        return mapper.selectList(new LambdaQueryWrapper<CustomerAddress>()
                .eq(CustomerAddress::getCustomerId, customerId)
                .orderByDesc(CustomerAddress::getIsDefault)
                .orderByAsc(CustomerAddress::getAddressType));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CustomerAddress> pageByCustomerId(Long customerId, long current, long size) {
        Page<CustomerAddress> page = new Page<>(current, size);
        LambdaQueryWrapper<CustomerAddress> qw = new LambdaQueryWrapper<>();
        qw.eq(CustomerAddress::getCustomerId, customerId);
        qw.orderByDesc(CustomerAddress::getIsDefault);
        qw.orderByAsc(CustomerAddress::getAddressType);
        Page<CustomerAddress> result = mapper.selectPage(page, qw);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional
    public CustomerAddress create(Long customerId, CustomerAddress address) {
        normalize(customerId, address);
        if (Integer.valueOf(1).equals(address.getIsDefault())) clearDefault(customerId, null);
        address.setCustomerId(customerId);
        mapper.insert(address);
        return address;
    }

    @Override
    @Transactional
    public CustomerAddress update(Long id, CustomerAddress address) {
        CustomerAddress existing = mapper.selectById(id);
        if (existing == null) throw new BizException(1001, "地址记录不存在");
        normalize(existing.getCustomerId(), address);
        if (Integer.valueOf(1).equals(address.getIsDefault())) clearDefault(existing.getCustomerId(), id);
        address.setId(id);
        address.setCustomerId(existing.getCustomerId());
        address.setVersion(existing.getVersion());
        mapper.updateById(address);
        return mapper.selectById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    private void normalize(Long customerId, CustomerAddress address) {
        address.setCustomerId(customerId);
        if (address.getIsDefault() == null) address.setIsDefault(0);
        if (address.getLocationVerified() == null) address.setLocationVerified(0);
        if (address.getCheckinRadiusMeters() == null || address.getCheckinRadiusMeters() <= 0) {
            address.setCheckinRadiusMeters(500);
        }
        if (address.getCountry() == null || address.getCountry().isBlank()) address.setCountry("中国");
        if (address.getLocationSource() == null || address.getLocationSource().isBlank()) {
            address.setLocationSource(address.getLongitude() != null && address.getLatitude() != null ? "AMAP" : "MANUAL");
        }
        if (Integer.valueOf(1).equals(address.getLocationVerified()) && address.getLocationVerifiedTime() == null) {
            address.setLocationVerifiedTime(OffsetDateTime.now());
        }
        address.setContactName(safe(address.getContactName()));
        address.setPhone(safe(address.getPhone()));
        address.setCountry(safe(address.getCountry()));
        address.setProvince(safe(address.getProvince()));
        address.setCity(safe(address.getCity()));
        address.setDistrict(safe(address.getDistrict()));
        address.setAddressDetail(safe(address.getAddressDetail()));
        address.setFullAddress(safe(resolveFullAddress(address)));
        address.setAmapPoiId(safe(address.getAmapPoiId()));
        address.setAmapPoiName(safe(address.getAmapPoiName()));
        address.setAmapAdcode(safe(address.getAmapAdcode()));
        address.setAmapLevel(safe(address.getAmapLevel()));
        address.setLocationSource(safe(address.getLocationSource()));
        address.setAddressRemark(safe(address.getAddressRemark()));
    }

    private String resolveFullAddress(CustomerAddress address) {
        if (address.getFullAddress() != null && !address.getFullAddress().isBlank()) return address.getFullAddress();
        StringBuilder sb = new StringBuilder();
        append(sb, address.getProvince());
        append(sb, address.getCity());
        append(sb, address.getDistrict());
        append(sb, address.getAddressDetail());
        return sb.toString();
    }

    private void append(StringBuilder sb, String val) {
        if (val != null && !val.isBlank()) sb.append(val.trim());
    }

    private String safe(String val) {
        return InputSanitizer.safeText(val);
    }

    private void clearDefault(Long customerId, Long exceptId) {
        LambdaUpdateWrapper<CustomerAddress> uw = new LambdaUpdateWrapper<CustomerAddress>()
                .eq(CustomerAddress::getCustomerId, customerId)
                .eq(CustomerAddress::getIsDefault, 1)
                .set(CustomerAddress::getIsDefault, 0);
        if (exceptId != null) uw.ne(CustomerAddress::getId, exceptId);
        mapper.update(null, uw);
    }
}
