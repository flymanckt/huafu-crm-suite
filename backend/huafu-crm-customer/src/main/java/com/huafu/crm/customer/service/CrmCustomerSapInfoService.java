package com.huafu.crm.customer.service;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.entity.CrmCustomerSapInfo;

import java.util.List;

public interface CrmCustomerSapInfoService {
    List<CrmCustomerSapInfo> getByCustomerId(Long customerId);
    PageResult<CrmCustomerSapInfo> pageByCustomerId(Long customerId, long current, long size);
    CrmCustomerSapInfo create(Long customerId, CrmCustomerSapInfo info);
    CrmCustomerSapInfo update(Long id, CrmCustomerSapInfo info);
    void delete(Long id);
}
