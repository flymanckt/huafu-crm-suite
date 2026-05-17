package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.entity.CrmCustomerBundle;
import com.huafu.crm.customer.mapper.CrmCustomerBundleMapper;
import com.huafu.crm.customer.service.CrmCustomerBundleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CrmCustomerBundleServiceImpl implements CrmCustomerBundleService {

    private final CrmCustomerBundleMapper mapper;

    public CrmCustomerBundleServiceImpl(CrmCustomerBundleMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrmCustomerBundle> getByCustomerId(Long customerId) {
        return mapper.selectList(new LambdaQueryWrapper<CrmCustomerBundle>()
                .eq(CrmCustomerBundle::getParentCustomerId, customerId)
                .eq(CrmCustomerBundle::getDeleted, 0)
                .orderByDesc(CrmCustomerBundle::getCreatedTime));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CrmCustomerBundle> pageByCustomerId(Long customerId, long current, long size) {
        Page<CrmCustomerBundle> page = new Page<>(current, size);
        LambdaQueryWrapper<CrmCustomerBundle> qw = new LambdaQueryWrapper<>();
        qw.eq(CrmCustomerBundle::getParentCustomerId, customerId);
        qw.eq(CrmCustomerBundle::getDeleted, 0);
        qw.orderByDesc(CrmCustomerBundle::getCreatedTime);
        Page<CrmCustomerBundle> result = mapper.selectPage(page, qw);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrmCustomerBundle> getBundleOf(Long customerId) {
        return mapper.selectList(new LambdaQueryWrapper<CrmCustomerBundle>()
                .eq(CrmCustomerBundle::getChildCustomerId, customerId)
                .eq(CrmCustomerBundle::getDeleted, 0));
    }

    @Override
    @Transactional
    public CrmCustomerBundle create(Long parentCustomerId, Long childCustomerId, CrmCustomerBundle bundle) {
        bundle.setParentCustomerId(parentCustomerId);
        bundle.setChildCustomerId(childCustomerId);
        mapper.insert(bundle);
        return bundle;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
