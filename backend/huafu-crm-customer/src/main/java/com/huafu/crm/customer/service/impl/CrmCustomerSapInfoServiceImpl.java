package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.entity.CrmCustomerSapInfo;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.common.util.InputSanitizer;
import com.huafu.crm.customer.mapper.CrmCustomerSapInfoMapper;
import com.huafu.crm.customer.service.CrmCustomerSapInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CrmCustomerSapInfoServiceImpl implements CrmCustomerSapInfoService {

    private final CrmCustomerSapInfoMapper mapper;

    public CrmCustomerSapInfoServiceImpl(CrmCustomerSapInfoMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrmCustomerSapInfo> getByCustomerId(Long customerId) {
        return mapper.selectList(new LambdaQueryWrapper<CrmCustomerSapInfo>()
                .eq(CrmCustomerSapInfo::getCustomerId, customerId)
                .eq(CrmCustomerSapInfo::getDeleted, 0)
                .orderByDesc(CrmCustomerSapInfo::getIsDefault)
                .orderByDesc(CrmCustomerSapInfo::getCreatedTime));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CrmCustomerSapInfo> pageByCustomerId(Long customerId, long current, long size) {
        Page<CrmCustomerSapInfo> page = new Page<>(current, size);
        Page<CrmCustomerSapInfo> result = mapper.selectPage(page, new LambdaQueryWrapper<CrmCustomerSapInfo>()
                .eq(CrmCustomerSapInfo::getCustomerId, customerId)
                .eq(CrmCustomerSapInfo::getDeleted, 0)
                .orderByDesc(CrmCustomerSapInfo::getIsDefault)
                .orderByDesc(CrmCustomerSapInfo::getCreatedTime));
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional
    public CrmCustomerSapInfo create(Long customerId, CrmCustomerSapInfo info) {
        normalize(customerId, info);
        if (Integer.valueOf(1).equals(info.getIsDefault())) clearDefault(customerId, null);
        mapper.insert(info);
        return info;
    }

    @Override
    @Transactional
    public CrmCustomerSapInfo update(Long id, CrmCustomerSapInfo info) {
        CrmCustomerSapInfo existing = mapper.selectById(id);
        if (existing == null) throw new BizException(1001, "SAP信息不存在");
        normalize(existing.getCustomerId(), info);
        if (Integer.valueOf(1).equals(info.getIsDefault())) clearDefault(existing.getCustomerId(), id);
        info.setId(id);
        info.setCustomerId(existing.getCustomerId());
        info.setVersion(existing.getVersion());
        mapper.updateById(info);
        return mapper.selectById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    private void normalize(Long customerId, CrmCustomerSapInfo info) {
        info.setCustomerId(customerId);
        if (info.getIsDefault() == null) info.setIsDefault(0);
        info.setSapCode(safe(info.getSapCode()));
        if (info.getSapCode() == null || info.getSapCode().isBlank()) {
            throw new BizException(1001, "请填写SAP编号");
        }
        info.setAccountGroup(safe(info.getAccountGroup()));
        info.setCountryCode(safe(info.getCountryCode()));
        info.setCompanyCode(safe(info.getCompanyCode()));
        info.setSalesOrg(safe(info.getSalesOrg()));
        info.setDistributionChannel(safe(info.getDistributionChannel()));
        info.setDivision(safe(info.getDivision()));
        info.setDescription(safe(info.getDescription()));
    }

    private String safe(String value) {
        return InputSanitizer.safeText(value);
    }

    private void clearDefault(Long customerId, Long exceptId) {
        LambdaUpdateWrapper<CrmCustomerSapInfo> uw = new LambdaUpdateWrapper<CrmCustomerSapInfo>()
                .eq(CrmCustomerSapInfo::getCustomerId, customerId)
                .eq(CrmCustomerSapInfo::getIsDefault, 1)
                .set(CrmCustomerSapInfo::getIsDefault, 0);
        if (exceptId != null) uw.ne(CrmCustomerSapInfo::getId, exceptId);
        mapper.update(null, uw);
    }
}
