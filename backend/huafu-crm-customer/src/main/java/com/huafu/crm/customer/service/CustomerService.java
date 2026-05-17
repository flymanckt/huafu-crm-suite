package com.huafu.crm.customer.service;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.customer.dto.CustomerCreateDTO;
import com.huafu.crm.customer.dto.CustomerUpdateDTO;
import com.huafu.crm.customer.query.CustomerQuery;
import com.huafu.crm.customer.vo.CustomerVO;

public interface CustomerService {
    CustomerVO create(CustomerCreateDTO dto);
    CustomerVO update(Long id, CustomerUpdateDTO dto);
    CustomerVO get(Long id);
    PageResult<CustomerVO> page(CustomerQuery query);
    void delete(Long id);
    PageResult<CustomerVO> pagePublicPool(CustomerQuery query);
    void claimFromPool(Long id);
    void transfer(Long id, Long toUserId, String reason);
    void freeze(Long id, String frozenReason);
    void loss(Long id, String lossReason);
}
