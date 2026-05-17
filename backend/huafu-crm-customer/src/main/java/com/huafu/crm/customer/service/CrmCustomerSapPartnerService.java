package com.huafu.crm.customer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.entity.CrmCustomerSapPartner;
import java.util.List;

public interface CrmCustomerSapPartnerService {

    List<CrmCustomerSapPartner> getByCustomerId(Long customerId);

    PageResult<CrmCustomerSapPartner> pageByCustomerId(Long customerId, long current, long size);

    CrmCustomerSapPartner create(Long customerId, CrmCustomerSapPartner partner);

    CrmCustomerSapPartner update(Long id, CrmCustomerSapPartner partner);

    void delete(Long id);
}
