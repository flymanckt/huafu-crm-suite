package com.huafu.crm.customer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.entity.CrmCustomerYarnUsage;
import java.util.List;

public interface CrmCustomerYarnUsageService {

    List<CrmCustomerYarnUsage> getByCustomerId(Long customerId);

    PageResult<CrmCustomerYarnUsage> pageByCustomerId(Long customerId, long current, long size);

    CrmCustomerYarnUsage create(Long customerId, CrmCustomerYarnUsage yarnUsage);

    CrmCustomerYarnUsage update(Long id, CrmCustomerYarnUsage yarnUsage);

    void delete(Long id);
}
