package com.huafu.crm.customer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.entity.CrmCustomerAttachment;
import java.util.List;

public interface CrmCustomerAttachmentService {

    List<CrmCustomerAttachment> getByCustomerId(Long customerId);

    PageResult<CrmCustomerAttachment> pageByCustomerId(Long customerId, long current, long size);

    CrmCustomerAttachment create(Long customerId, CrmCustomerAttachment attachment);

    void delete(Long customerId, Long attachmentId);
}
