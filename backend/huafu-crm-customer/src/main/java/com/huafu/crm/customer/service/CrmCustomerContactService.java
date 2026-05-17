package com.huafu.crm.customer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.customer.entity.CustomerContact;
import java.util.List;

public interface CrmCustomerContactService {

    List<CustomerContact> getByCustomerId(Long customerId);

    PageResult<CustomerContact> pageByCustomerId(Long customerId, long current, long size);

    CustomerContact create(Long customerId, CustomerContact contact);

    CustomerContact update(Long id, CustomerContact contact);

    void delete(Long id);

    List<CustomerContact> getTreeByCustomerId(Long customerId);
}
