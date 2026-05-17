package com.huafu.crm.customer.service;

import com.huafu.crm.common.entity.CrmCustomerExt;

public interface CrmCustomerExtService {

    CrmCustomerExt getByCustomerId(Long customerId);

    CrmCustomerExt saveOrUpdate(Long customerId, CrmCustomerExt ext);
}
