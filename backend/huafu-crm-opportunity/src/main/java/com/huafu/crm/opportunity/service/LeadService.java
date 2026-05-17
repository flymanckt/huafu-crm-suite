package com.huafu.crm.opportunity.service;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.opportunity.dto.LeadCreateDTO;
import com.huafu.crm.opportunity.query.LeadQuery;
import com.huafu.crm.opportunity.vo.LeadVO;

public interface LeadService {
    LeadVO create(LeadCreateDTO dto);
    LeadVO getById(Long id);
    PageResult<LeadVO> page(LeadQuery query);
}
