package com.huafu.crm.opportunity.service;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.opportunity.dto.LostOrderCreateDTO;
import com.huafu.crm.opportunity.query.LostOrderQuery;
import com.huafu.crm.opportunity.vo.LostOrderVO;

public interface LostOrderService {
    LostOrderVO create(LostOrderCreateDTO dto);
    LostOrderVO getById(Long id);
    LostOrderVO update(Long id, LostOrderCreateDTO dto);
    boolean delete(Long id);
    PageResult<LostOrderVO> page(LostOrderQuery query);
}
