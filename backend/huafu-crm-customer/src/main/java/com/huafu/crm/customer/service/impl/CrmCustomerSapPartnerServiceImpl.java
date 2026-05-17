package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.entity.CrmCustomerSapPartner;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.customer.mapper.CrmCustomerSapPartnerMapper;
import com.huafu.crm.customer.service.CrmCustomerSapPartnerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CrmCustomerSapPartnerServiceImpl implements CrmCustomerSapPartnerService {

    private final CrmCustomerSapPartnerMapper mapper;

    public CrmCustomerSapPartnerServiceImpl(CrmCustomerSapPartnerMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrmCustomerSapPartner> getByCustomerId(Long customerId) {
        return mapper.selectList(new LambdaQueryWrapper<CrmCustomerSapPartner>()
                .eq(CrmCustomerSapPartner::getCustomerId, customerId)
                .eq(CrmCustomerSapPartner::getDeleted, 0)
                .orderByDesc(CrmCustomerSapPartner::getCreatedTime));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CrmCustomerSapPartner> pageByCustomerId(Long customerId, long current, long size) {
        Page<CrmCustomerSapPartner> page = new Page<>(current, size);
        LambdaQueryWrapper<CrmCustomerSapPartner> qw = new LambdaQueryWrapper<>();
        qw.eq(CrmCustomerSapPartner::getCustomerId, customerId);
        qw.eq(CrmCustomerSapPartner::getDeleted, 0);
        qw.orderByDesc(CrmCustomerSapPartner::getCreatedTime);
        Page<CrmCustomerSapPartner> result = mapper.selectPage(page, qw);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional
    public CrmCustomerSapPartner create(Long customerId, CrmCustomerSapPartner partner) {
        partner.setCustomerId(customerId);
        mapper.insert(partner);
        return partner;
    }

    @Override
    @Transactional
    public CrmCustomerSapPartner update(Long id, CrmCustomerSapPartner partner) {
        CrmCustomerSapPartner existing = mapper.selectById(id);
        if (existing == null) throw new BizException(1001, "SAP合作伙伴不存在");
        partner.setId(id);
        partner.setCustomerId(existing.getCustomerId());
        partner.setVersion(existing.getVersion());
        mapper.updateById(partner);
        return mapper.selectById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
