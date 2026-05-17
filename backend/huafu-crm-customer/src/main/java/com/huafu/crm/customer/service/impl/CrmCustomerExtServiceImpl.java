package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huafu.crm.common.entity.CrmCustomerExt;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.customer.mapper.CrmCustomerExtMapper;
import com.huafu.crm.customer.service.CrmCustomerExtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrmCustomerExtServiceImpl implements CrmCustomerExtService {

    private final CrmCustomerExtMapper mapper;

    public CrmCustomerExtServiceImpl(CrmCustomerExtMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public CrmCustomerExt getByCustomerId(Long customerId) {
        return mapper.selectOne(new LambdaQueryWrapper<CrmCustomerExt>()
                .eq(CrmCustomerExt::getCustomerId, customerId)
                .eq(CrmCustomerExt::getDeleted, 0));
    }

    @Override
    @Transactional
    public CrmCustomerExt saveOrUpdate(Long customerId, CrmCustomerExt ext) {
        LambdaQueryWrapper<CrmCustomerExt> qw = new LambdaQueryWrapper<CrmCustomerExt>()
                .eq(CrmCustomerExt::getCustomerId, customerId)
                .eq(CrmCustomerExt::getDeleted, 0);
        CrmCustomerExt existing = mapper.selectOne(qw);

        if (existing != null) {
            ext.setId(existing.getId());
            ext.setCustomerId(customerId);
            ext.setVersion(existing.getVersion());
            mapper.updateById(ext);
            return mapper.selectById(ext.getId());
        } else {
            ext.setCustomerId(customerId);
            mapper.insert(ext);
            return ext;
        }
    }
}
