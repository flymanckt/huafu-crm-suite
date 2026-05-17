package com.huafu.crm.opportunity.service;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.opportunity.dto.OpportunityCreateDTO;
import com.huafu.crm.opportunity.dto.StageUpdateDTO;
import com.huafu.crm.opportunity.query.OpportunityQuery;
import com.huafu.crm.opportunity.vo.OpportunityVO;

public interface OpportunityService {
    OpportunityVO create(OpportunityCreateDTO dto);
    OpportunityVO getById(Long id);
    OpportunityVO update(Long id, OpportunityCreateDTO dto);
    Boolean delete(Long id);
    PageResult<OpportunityVO> page(OpportunityQuery query);
    OpportunityVO updateStage(Long id, Integer stage, StageUpdateDTO dto);
    OpportunityVO updateStageSimple(Long id, Integer stage);
}
