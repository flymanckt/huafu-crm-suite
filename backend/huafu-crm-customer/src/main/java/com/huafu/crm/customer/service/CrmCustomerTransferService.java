package com.huafu.crm.customer.service;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.customer.entity.CustomerTransfer;
import java.util.List;

public interface CrmCustomerTransferService {

    List<CustomerTransfer> getByCustomerId(Long customerId);

    PageResult<CustomerTransfer> pageByCustomerId(Long customerId, long current, long size);

    CustomerTransfer create(Long customerId, CustomerTransfer transfer);

    CustomerTransfer confirm(Long id);

    void delete(Long id);
}
