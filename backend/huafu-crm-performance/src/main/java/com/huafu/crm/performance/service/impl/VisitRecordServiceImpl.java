package com.huafu.crm.performance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.util.InputSanitizer;
import com.huafu.crm.performance.dto.VisitRecordCreateDTO;
import com.huafu.crm.performance.entity.CustomerAddress;
import com.huafu.crm.performance.entity.VisitRecord;
import com.huafu.crm.performance.entity.CustomerContact;
import com.huafu.crm.performance.entity.Customer;
import com.huafu.crm.performance.mapper.CustomerAddressMapper;
import com.huafu.crm.performance.mapper.VisitRecordMapper;
import com.huafu.crm.performance.mapper.CustomerContactMapper;
import com.huafu.crm.performance.mapper.CustomerMapper;
import com.huafu.crm.performance.query.VisitRecordQuery;
import com.huafu.crm.performance.service.VisitRecordService;
import com.huafu.crm.performance.vo.VisitRecordVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class VisitRecordServiceImpl implements VisitRecordService {
    private final VisitRecordMapper mapper;
    private final CustomerContactMapper contactMapper;
    private final CustomerMapper customerMapper;
    private final CustomerAddressMapper addressMapper;

    public VisitRecordServiceImpl(VisitRecordMapper mapper,
                                 CustomerContactMapper contactMapper,
                                 CustomerMapper customerMapper,
                                 CustomerAddressMapper addressMapper) {
        this.mapper = mapper;
        this.contactMapper = contactMapper;
        this.customerMapper = customerMapper;
        this.addressMapper = addressMapper;
    }

    @Override
    @Transactional
    public VisitRecordVO create(VisitRecordCreateDTO dto) {
        VisitRecord e = new VisitRecord();
        e.setUserId(dto.userId());
        e.setCustomerId(dto.customerId());
        e.setCustomerName(safe(dto.customerName()));
        e.setContactId(dto.contactId());
        e.setContactName(safe(dto.contactName()));
        e.setVisitDate(dto.visitDate());
        e.setVisitType(dto.visitType());
        e.setVisitPurpose(safe(dto.visitPurpose()));
        e.setVisitContent(safe(dto.visitContent()));
        e.setNextVisitPlan(dto.nextVisitPlan());
        e.setIsNewCustomer(dto.isNewCustomer() != null ? dto.isNewCustomer() : 0);
        e.setLongitude(dto.longitude());
        e.setLatitude(dto.latitude());
        e.setLocationName(safe(dto.locationName()));
        e.setCustomerAddressId(dto.customerAddressId());
        e.setCheckinLongitude(dto.checkinLongitude() != null ? dto.checkinLongitude() : dto.longitude());
        e.setCheckinLatitude(dto.checkinLatitude() != null ? dto.checkinLatitude() : dto.latitude());
        e.setCheckinAddress(safe(dto.checkinAddress() != null ? dto.checkinAddress() : dto.locationName()));
        e.setCheckinTime(dto.checkinTime() != null ? dto.checkinTime() : OffsetDateTime.now());
        applyAddressCheck(e);
        e.setRemark(safe(dto.remark()));
        mapper.insert(e);

        // 自动更新联系人最近联系时间
        if (dto.contactId() != null) {
            CustomerContact contact = contactMapper.selectById(dto.contactId());
            if (contact != null) {
                contact.setUpdatedTime(OffsetDateTime.now());
                contactMapper.updateById(contact);
            }
        } else if (dto.customerId() != null) {
            // 如果没指定contactId，更新该客户主联系人
            CustomerContact mainContact = contactMapper.selectOne(
                new LambdaQueryWrapper<CustomerContact>()
                    .eq(CustomerContact::getCustomerId, dto.customerId())
                    .orderBy(true, true, CustomerContact::getId)
                    .last("LIMIT 1"));
            if (mainContact != null) {
                mainContact.setUpdatedTime(OffsetDateTime.now());
                contactMapper.updateById(mainContact);
            }
        }

        // 更新客户最近拜访时间
        if (dto.customerId() != null) {
            Customer c = customerMapper.selectById(dto.customerId());
            if (c != null) {
                c.setLastVisitTime(OffsetDateTime.now());
                customerMapper.updateById(c);
            }
        }

        return toVO(e);
    }

    @Override @Transactional(readOnly = true)
    public PageResult<VisitRecordVO> page(VisitRecordQuery query) {
        Page<VisitRecord> page = new Page<>(query.current(), query.size());
        LambdaQueryWrapper<VisitRecord> qw = new LambdaQueryWrapper<>();
        if (query.userId() != null) qw.eq(VisitRecord::getUserId, query.userId());
        qw.orderByDesc(VisitRecord::getVisitDate);
        Page<VisitRecord> result = mapper.selectPage(page, qw);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(),
            result.getRecords().stream().map(this::toVO).toList());
    }

    private VisitRecordVO toVO(VisitRecord e) {
        return new VisitRecordVO(e.getId(), e.getVisitNo(), e.getUserId(), e.getCustomerId(),
            e.getCustomerName(), e.getContactId(), e.getContactName(), e.getVisitDate(),
            e.getVisitType(), e.getVisitPurpose(), e.getVisitContent(), e.getNextVisitPlan(),
            e.getIsNewCustomer(), e.getLongitude(), e.getLatitude(), e.getLocationName(),
            e.getCustomerAddressId(), e.getCheckinLongitude(), e.getCheckinLatitude(),
            e.getCheckinAddress(), e.getCheckinTime(), e.getCheckinDistanceMeters(),
            e.getCheckinRadiusMeters(), e.getCheckinMatched(), e.getCheckinResult(),
            e.getRemark(), e.getCreatedBy(), e.getCreatedTime(), e.getVersion());
    }

    private void applyAddressCheck(VisitRecord e) {
        if (e.getCheckinLongitude() == null || e.getCheckinLatitude() == null || e.getCustomerId() == null) {
            e.setCheckinMatched(0);
            e.setCheckinResult("UNCHECKED");
            return;
        }
        CustomerAddress target = resolveAddress(e);
        if (target == null || target.getLongitude() == null || target.getLatitude() == null) {
            e.setCheckinMatched(0);
            e.setCheckinResult("NO_ADDRESS");
            return;
        }
        e.setCustomerAddressId(target.getId());
        int radius = target.getCheckinRadiusMeters() != null && target.getCheckinRadiusMeters() > 0
                ? target.getCheckinRadiusMeters()
                : 500;
        BigDecimal distance = distanceMeters(
                target.getLongitude(), target.getLatitude(),
                e.getCheckinLongitude(), e.getCheckinLatitude());
        e.setCheckinDistanceMeters(distance);
        e.setCheckinRadiusMeters(radius);
        boolean matched = distance.compareTo(BigDecimal.valueOf(radius)) <= 0;
        e.setCheckinMatched(matched ? 1 : 0);
        e.setCheckinResult(matched ? "MATCHED" : "MISMATCHED");
    }

    private CustomerAddress resolveAddress(VisitRecord e) {
        if (e.getCustomerAddressId() != null) {
            CustomerAddress selected = addressMapper.selectById(e.getCustomerAddressId());
            if (selected != null && e.getCustomerId().equals(selected.getCustomerId())) return selected;
        }
        List<CustomerAddress> addresses = addressMapper.selectList(new LambdaQueryWrapper<CustomerAddress>()
                .eq(CustomerAddress::getCustomerId, e.getCustomerId())
                .eq(CustomerAddress::getLocationVerified, 1)
                .isNotNull(CustomerAddress::getLongitude)
                .isNotNull(CustomerAddress::getLatitude)
                .orderByDesc(CustomerAddress::getAddressType));
        CustomerAddress nearest = null;
        BigDecimal nearestDistance = null;
        for (CustomerAddress address : addresses) {
            BigDecimal distance = distanceMeters(
                    address.getLongitude(), address.getLatitude(),
                    e.getCheckinLongitude(), e.getCheckinLatitude());
            if (nearestDistance == null || distance.compareTo(nearestDistance) < 0) {
                nearest = address;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private BigDecimal distanceMeters(BigDecimal lng1, BigDecimal lat1, BigDecimal lng2, BigDecimal lat2) {
        double earthRadius = 6371008.8d;
        double radLat1 = Math.toRadians(lat1.doubleValue());
        double radLat2 = Math.toRadians(lat2.doubleValue());
        double deltaLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double deltaLng = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BigDecimal.valueOf(earthRadius * c).setScale(2, RoundingMode.HALF_UP);
    }

    private String safe(String val) {
        return InputSanitizer.safeText(val);
    }
}
