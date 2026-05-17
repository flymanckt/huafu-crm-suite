package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.entity.CrmCustomerYarnUsage;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.customer.mapper.CrmCustomerYarnUsageMapper;
import com.huafu.crm.customer.service.CrmCustomerYarnUsageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CrmCustomerYarnUsageServiceImpl implements CrmCustomerYarnUsageService {

    private final CrmCustomerYarnUsageMapper mapper;

    public CrmCustomerYarnUsageServiceImpl(CrmCustomerYarnUsageMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrmCustomerYarnUsage> getByCustomerId(Long customerId) {
        return mapper.selectList(new LambdaQueryWrapper<CrmCustomerYarnUsage>()
                .eq(CrmCustomerYarnUsage::getCustomerId, customerId)
                .eq(CrmCustomerYarnUsage::getDeleted, 0)
                .orderByDesc(CrmCustomerYarnUsage::getCreatedTime));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CrmCustomerYarnUsage> pageByCustomerId(Long customerId, long current, long size) {
        Page<CrmCustomerYarnUsage> page = new Page<>(current, size);
        LambdaQueryWrapper<CrmCustomerYarnUsage> qw = new LambdaQueryWrapper<>();
        qw.eq(CrmCustomerYarnUsage::getCustomerId, customerId);
        qw.eq(CrmCustomerYarnUsage::getDeleted, 0);
        qw.orderByDesc(CrmCustomerYarnUsage::getCreatedTime);
        Page<CrmCustomerYarnUsage> result = mapper.selectPage(page, qw);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional
    public CrmCustomerYarnUsage create(Long customerId, CrmCustomerYarnUsage yarnUsage) {
        yarnUsage.setCustomerId(customerId);
        mapper.insert(yarnUsage);
        return yarnUsage;
    }

    @Override
    @Transactional
    public CrmCustomerYarnUsage update(Long id, CrmCustomerYarnUsage yarnUsage) {
        CrmCustomerYarnUsage existing = mapper.selectById(id);
        if (existing == null) throw new BizException(1001, "纱线用量记录不存在");
        yarnUsage.setId(id);
        yarnUsage.setCustomerId(existing.getCustomerId());
        yarnUsage.setVersion(existing.getVersion());
        mapper.updateById(yarnUsage);
        return mapper.selectById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
