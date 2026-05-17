package com.huafu.crm.customer.service;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.customer.entity.CustomerAddress;
import java.util.List;

public interface CrmCustomerAddressService {

    List<CustomerAddress> getByCustomerId(Long customerId);

    PageResult<CustomerAddress> pageByCustomerId(Long customerId, long current, long size);

    CustomerAddress create(Long customerId, CustomerAddress address);

    CustomerAddress update(Long id, CustomerAddress address);

    void delete(Long id);
}
