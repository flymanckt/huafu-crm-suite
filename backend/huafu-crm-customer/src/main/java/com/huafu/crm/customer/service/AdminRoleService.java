package com.huafu.crm.customer.service;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.customer.dto.RoleCreateDTO;
import com.huafu.crm.customer.dto.RoleUpdateDTO;
import com.huafu.crm.customer.dto.RoleVO;
import java.util.List;

public interface AdminRoleService {
    RoleVO create(RoleCreateDTO dto);
    RoleVO update(Long id, RoleUpdateDTO dto);
    RoleVO getById(Long id);
    PageResult<RoleVO> page(String roleName, long current, long size);
    void delete(Long id);
    List<Long> getMenuIds(Long roleId);
    void updateMenus(Long roleId, List<Long> menuIds);
}
