package com.huafu.crm.customer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.entity.CrmCustomerSapOrg;
import java.util.List;

public interface CrmCustomerSapOrgService {

    List<CrmCustomerSapOrg> getByCustomerId(Long customerId);

    PageResult<CrmCustomerSapOrg> pageByCustomerId(Long customerId, long current, long size);

    CrmCustomerSapOrg create(Long customerId, CrmCustomerSapOrg sapOrg);

    CrmCustomerSapOrg update(Long id, CrmCustomerSapOrg sapOrg);

    void delete(Long id);
}
