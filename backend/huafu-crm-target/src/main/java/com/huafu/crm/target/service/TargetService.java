package com.huafu.crm.target.service;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.target.dto.TargetCreateDTO;
import com.huafu.crm.target.query.TargetQuery;
import com.huafu.crm.target.vo.TargetVO;
import com.huafu.crm.target.vo.TargetAchieveVO;

public interface TargetService {
    TargetVO create(TargetCreateDTO dto);
    TargetVO getById(Long id);
    TargetVO update(Long id, TargetCreateDTO dto);
    Boolean delete(Long id);
    PageResult<TargetVO> page(TargetQuery query);
    PageResult<TargetAchieveVO> achievePage(Long targetId, Integer current, Integer size);
}
