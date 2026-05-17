package com.huafu.crm.customer.service;

import com.huafu.crm.customer.dto.DeptCreateDTO;
import com.huafu.crm.customer.dto.DeptUpdateDTO;
import com.huafu.crm.customer.dto.DeptVO;
import java.util.List;

public interface AdminDeptService {
    DeptVO create(DeptCreateDTO dto);
    DeptVO update(Long id, DeptUpdateDTO dto);
    DeptVO getById(Long id);
    List<DeptVO> getTree();
    void delete(Long id);
}
