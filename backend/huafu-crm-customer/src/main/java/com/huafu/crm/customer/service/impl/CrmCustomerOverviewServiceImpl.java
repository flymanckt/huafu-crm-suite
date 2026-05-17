package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huafu.crm.common.entity.CrmCustomerOverview;
import com.huafu.crm.customer.mapper.CrmCustomerOverviewMapper;
import com.huafu.crm.customer.service.CrmCustomerOverviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrmCustomerOverviewServiceImpl implements CrmCustomerOverviewService {

    private final CrmCustomerOverviewMapper mapper;

    public CrmCustomerOverviewServiceImpl(CrmCustomerOverviewMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public CrmCustomerOverview getByCustomerId(Long customerId) {
        return mapper.selectOne(new LambdaQueryWrapper<CrmCustomerOverview>()
                .eq(CrmCustomerOverview::getCustomerId, customerId)
                .eq(CrmCustomerOverview::getDeleted, 0));
    }

    @Override
    @Transactional
    public CrmCustomerOverview saveOrUpdate(Long customerId, CrmCustomerOverview overview) {
        LambdaQueryWrapper<CrmCustomerOverview> qw = new LambdaQueryWrapper<CrmCustomerOverview>()
                .eq(CrmCustomerOverview::getCustomerId, customerId)
                .eq(CrmCustomerOverview::getDeleted, 0);
        CrmCustomerOverview existing = mapper.selectOne(qw);

        if (existing != null) {
            overview.setId(existing.getId());
            overview.setCustomerId(customerId);
            overview.setVersion(existing.getVersion());
            mapper.updateById(overview);
            return mapper.selectById(overview.getId());
        } else {
            overview.setCustomerId(customerId);
            mapper.insert(overview);
            return overview;
        }
    }
}
