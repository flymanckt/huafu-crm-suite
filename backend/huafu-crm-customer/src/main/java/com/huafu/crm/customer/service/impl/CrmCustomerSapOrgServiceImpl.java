package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.entity.CrmCustomerSapOrg;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.common.util.InputSanitizer;
import com.huafu.crm.customer.mapper.CrmCustomerSapOrgMapper;
import com.huafu.crm.customer.service.CrmCustomerSapOrgService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CrmCustomerSapOrgServiceImpl implements CrmCustomerSapOrgService {

    private final CrmCustomerSapOrgMapper mapper;

    public CrmCustomerSapOrgServiceImpl(CrmCustomerSapOrgMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrmCustomerSapOrg> getByCustomerId(Long customerId) {
        return mapper.selectList(new LambdaQueryWrapper<CrmCustomerSapOrg>()
                .eq(CrmCustomerSapOrg::getCustomerId, customerId)
                .eq(CrmCustomerSapOrg::getDeleted, 0)
                .orderByDesc(CrmCustomerSapOrg::getCreatedTime));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CrmCustomerSapOrg> pageByCustomerId(Long customerId, long current, long size) {
        Page<CrmCustomerSapOrg> page = new Page<>(current, size);
        LambdaQueryWrapper<CrmCustomerSapOrg> qw = new LambdaQueryWrapper<>();
        qw.eq(CrmCustomerSapOrg::getCustomerId, customerId);
        qw.eq(CrmCustomerSapOrg::getDeleted, 0);
        qw.orderByDesc(CrmCustomerSapOrg::getCreatedTime);
        Page<CrmCustomerSapOrg> result = mapper.selectPage(page, qw);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional
    public CrmCustomerSapOrg create(Long customerId, CrmCustomerSapOrg sapOrg) {
        normalize(customerId, sapOrg);
        mapper.insert(sapOrg);
        return sapOrg;
    }

    @Override
    @Transactional
    public CrmCustomerSapOrg update(Long id, CrmCustomerSapOrg sapOrg) {
        CrmCustomerSapOrg existing = mapper.selectById(id);
        if (existing == null) throw new BizException(1001, "SAP组织信息不存在");
        normalize(existing.getCustomerId(), sapOrg);
        sapOrg.setId(id);
        sapOrg.setVersion(existing.getVersion());
        mapper.updateById(sapOrg);
        return mapper.selectById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    private void normalize(Long customerId, CrmCustomerSapOrg sapOrg) {
        sapOrg.setCustomerId(customerId);
        sapOrg.setSapCode(safe(sapOrg.getSapCode()));
        sapOrg.setCompanyCode(safe(sapOrg.getCompanyCode()));
        sapOrg.setSalesOrg(safe(sapOrg.getSalesOrg()));
        sapOrg.setSalesOffice(safe(sapOrg.getSalesOffice()));
        sapOrg.setSalesGroup(safe(sapOrg.getSalesGroup()));
        sapOrg.setPriceList(safe(sapOrg.getPriceList()));
        sapOrg.setCurrency(safe(sapOrg.getCurrency()));
        sapOrg.setDeliveryPlant(safe(sapOrg.getDeliveryPlant()));
        sapOrg.setPaymentTerms(safe(sapOrg.getPaymentTerms()));
        sapOrg.setTaxClassification(safe(sapOrg.getTaxClassification()));
    }

    private String safe(String value) {
        return InputSanitizer.safeText(value);
    }
}
