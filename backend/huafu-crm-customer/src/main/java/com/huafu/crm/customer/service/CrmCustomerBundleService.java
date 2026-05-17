package com.huafu.crm.customer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.entity.CrmCustomerBundle;
import java.util.List;

public interface CrmCustomerBundleService {

    List<CrmCustomerBundle> getByCustomerId(Long customerId);

    PageResult<CrmCustomerBundle> pageByCustomerId(Long customerId, long current, long size);

    List<CrmCustomerBundle> getBundleOf(Long customerId);

    CrmCustomerBundle create(Long parentCustomerId, Long childCustomerId, CrmCustomerBundle bundle);

    void delete(Long id);
}
