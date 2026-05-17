package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.customer.entity.CustomerTransfer;
import com.huafu.crm.customer.mapper.CustomerTransferMapper;
import com.huafu.crm.customer.service.CrmCustomerTransferService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CrmCustomerTransferServiceImpl implements CrmCustomerTransferService {

    private final CustomerTransferMapper mapper;

    public CrmCustomerTransferServiceImpl(CustomerTransferMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerTransfer> getByCustomerId(Long customerId) {
        return mapper.selectList(new LambdaQueryWrapper<CustomerTransfer>()
                .eq(CustomerTransfer::getCustomerId, customerId)
                .orderByDesc(CustomerTransfer::getTransferDate)
                .orderByDesc(CustomerTransfer::getCreatedTime));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CustomerTransfer> pageByCustomerId(Long customerId, long current, long size) {
        Page<CustomerTransfer> page = new Page<>(current, size);
        LambdaQueryWrapper<CustomerTransfer> qw = new LambdaQueryWrapper<>();
        qw.eq(CustomerTransfer::getCustomerId, customerId);
        qw.orderByDesc(CustomerTransfer::getTransferDate);
        qw.orderByDesc(CustomerTransfer::getCreatedTime);
        Page<CustomerTransfer> result = mapper.selectPage(page, qw);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional
    public CustomerTransfer create(Long customerId, CustomerTransfer transfer) {
        transfer.setCustomerId(customerId);
        if (transfer.getStatus() == null) transfer.setStatus(1);
        mapper.insert(transfer);
        return transfer;
    }

    @Override
    @Transactional
    public CustomerTransfer confirm(Long id) {
        CustomerTransfer existing = mapper.selectById(id);
        if (existing == null) throw new BizException(1001, "交接记录不存在");
        existing.setStatus(2);
        mapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
