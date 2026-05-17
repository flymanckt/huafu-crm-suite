package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.customer.dto.DeptCreateDTO;
import com.huafu.crm.customer.dto.DeptUpdateDTO;
import com.huafu.crm.customer.dto.DeptVO;
import com.huafu.crm.customer.entity.Dept;
import com.huafu.crm.customer.mapper.DeptMapper;
import com.huafu.crm.customer.service.AdminDeptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminDeptServiceImpl implements AdminDeptService {
    private final DeptMapper deptMapper;

    public AdminDeptServiceImpl(DeptMapper deptMapper) {
        this.deptMapper = deptMapper;
    }

    @Override
    @Transactional
    public DeptVO create(DeptCreateDTO dto) {
        Dept d = new Dept();
        d.setDeptName(dto.deptName());
        d.setParentId(dto.parentId() != null ? dto.parentId() : 0L);
        d.setLeaderUserId(dto.leaderUserId());
        if (d.getTenantId() == null) d.setTenantId(1L);
        deptMapper.insert(d);
        return toVO(d);
    }

    @Override
    @Transactional
    public DeptVO update(Long id, DeptUpdateDTO dto) {
        Dept d = deptMapper.selectById(id);
        if (d == null) throw new BizException(1003, "部门不存在");
        if (dto.deptName() != null) d.setDeptName(dto.deptName());
        if (dto.parentId() != null) d.setParentId(dto.parentId());
        if (dto.leaderUserId() != null) d.setLeaderUserId(dto.leaderUserId());
        deptMapper.updateById(d);
        return toVO(deptMapper.selectById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public DeptVO getById(Long id) {
        Dept d = deptMapper.selectById(id);
        if (d == null) throw new BizException(1003, "部门不存在");
        return toVO(d);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeptVO> getTree() {
        List<Dept> all = deptMapper.selectList(new LambdaQueryWrapper<Dept>()
            .orderByAsc(Dept::getCreatedTime));
        Map<Long, List<Dept>> childrenMap = all.stream()
            .filter(d -> d.getParentId() != null && d.getParentId() != 0)
            .collect(Collectors.groupingBy(Dept::getParentId));
        List<Dept> roots = all.stream()
            .filter(d -> d.getParentId() == null || d.getParentId() == 0)
            .toList();
        return roots.stream().map(r -> toTree(r, childrenMap)).toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Dept d = deptMapper.selectById(id);
        if (d == null) throw new BizException(1003, "部门不存在");
        long childCount = deptMapper.selectCount(
            new LambdaQueryWrapper<Dept>().eq(Dept::getParentId, id));
        if (childCount > 0) throw new BizException(1003, "该部门有子部门，禁止删除");
        deptMapper.deleteById(id);
    }

    private DeptVO toTree(Dept dept, Map<Long, List<Dept>> childrenMap) {
        List<DeptVO> children = childrenMap.getOrDefault(dept.getId(), List.of())
            .stream().map(c -> toTree(c, childrenMap)).toList();
        return new DeptVO(dept.getId(), dept.getDeptName(), dept.getParentId(), dept.getLeaderUserId(), children);
    }

    private DeptVO toVO(Dept d) {
        return new DeptVO(d.getId(), d.getDeptName(), d.getParentId(), d.getLeaderUserId(), List.of());
    }
}
