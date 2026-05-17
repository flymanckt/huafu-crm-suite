package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.entity.CrmCustomerAttachment;
import com.huafu.crm.customer.mapper.CrmCustomerAttachmentMapper;
import com.huafu.crm.customer.service.CrmCustomerAttachmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class CrmCustomerAttachmentServiceImpl implements CrmCustomerAttachmentService {

    private final CrmCustomerAttachmentMapper mapper;

    public CrmCustomerAttachmentServiceImpl(CrmCustomerAttachmentMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrmCustomerAttachment> getByCustomerId(Long customerId) {
        return mapper.selectList(new LambdaQueryWrapper<CrmCustomerAttachment>()
                .eq(CrmCustomerAttachment::getCustomerId, customerId)
                .eq(CrmCustomerAttachment::getDeleted, 0)
                .orderByDesc(CrmCustomerAttachment::getUploadedAt));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CrmCustomerAttachment> pageByCustomerId(Long customerId, long current, long size) {
        Page<CrmCustomerAttachment> page = new Page<>(current, size);
        LambdaQueryWrapper<CrmCustomerAttachment> qw = new LambdaQueryWrapper<>();
        qw.eq(CrmCustomerAttachment::getCustomerId, customerId);
        qw.eq(CrmCustomerAttachment::getDeleted, 0);
        qw.orderByDesc(CrmCustomerAttachment::getUploadedAt);
        Page<CrmCustomerAttachment> result = mapper.selectPage(page, qw);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional
    public CrmCustomerAttachment create(Long customerId, CrmCustomerAttachment attachment) {
        attachment.setCustomerId(customerId);
        attachment.setUploadedAt(OffsetDateTime.now());
        mapper.insert(attachment);
        return attachment;
    }

    @Override
    @Transactional
    public void delete(Long customerId, Long attachmentId) {
        CrmCustomerAttachment attachment = mapper.selectById(attachmentId);
        if (attachment != null && attachment.getCustomerId().equals(customerId)) {
            mapper.deleteById(attachmentId);
        }
    }
}
