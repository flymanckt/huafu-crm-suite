package com.huafu.crm.customer.service;

import com.huafu.crm.common.entity.CrmCustomerOverview;

public interface CrmCustomerOverviewService {

    CrmCustomerOverview getByCustomerId(Long customerId);

    CrmCustomerOverview saveOrUpdate(Long customerId, CrmCustomerOverview overview);
}
