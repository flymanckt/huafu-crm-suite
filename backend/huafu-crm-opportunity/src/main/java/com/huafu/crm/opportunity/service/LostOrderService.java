package com.huafu.crm.opportunity.service;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.opportunity.dto.LostOrderCreateDTO;
import com.huafu.crm.opportunity.query.LostOrderQuery;
import com.huafu.crm.opportunity.vo.LostOrderVO;

public interface LostOrderService {
    LostOrderVO create(LostOrderCreateDTO dto);
    PageResult<LostOrderVO> page(LostOrderQuery query);
}
