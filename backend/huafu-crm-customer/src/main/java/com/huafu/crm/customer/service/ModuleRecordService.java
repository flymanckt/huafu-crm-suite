package com.huafu.crm.customer.service;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.customer.dto.ModuleRecordSaveDTO;
import com.huafu.crm.customer.vo.ModuleRecordVO;

public interface ModuleRecordService {
    PageResult<ModuleRecordVO> page(String moduleKey, long current, long size, String keyword, String status);
    ModuleRecordVO create(String moduleKey, ModuleRecordSaveDTO dto);
    ModuleRecordVO update(String moduleKey, Long id, ModuleRecordSaveDTO dto);
    void delete(String moduleKey, Long id);
}
