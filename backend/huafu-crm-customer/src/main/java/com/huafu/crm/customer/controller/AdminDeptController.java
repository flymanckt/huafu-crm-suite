package com.huafu.crm.customer.controller;

import com.huafu.crm.common.api.Result;
import com.huafu.crm.customer.dto.DeptCreateDTO;
import com.huafu.crm.customer.dto.DeptUpdateDTO;
import com.huafu.crm.customer.dto.DeptVO;
import com.huafu.crm.customer.service.AdminDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dept")
@Tag(name = "部门管理", description = "部门树形CRUD")
public class AdminDeptController {
    private final AdminDeptService service;
    public AdminDeptController(AdminDeptService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "新建部门")
    public Result<DeptVO> create(@Valid @RequestBody DeptCreateDTO dto) {
        return Result.ok(service.create(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取部门详情")
    public Result<DeptVO> getById(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @GetMapping("/tree")
    @Operation(summary = "获取部门树形结构")
    public Result<List<DeptVO>> tree() {
        return Result.ok(service.getTree());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新部门")
    public Result<DeptVO> update(@PathVariable Long id, @Valid @RequestBody DeptUpdateDTO dto) {
        return Result.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }
}
