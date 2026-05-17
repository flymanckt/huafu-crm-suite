package com.huafu.crm.customer.controller;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.api.Result;
import com.huafu.crm.customer.dto.UserCreateDTO;
import com.huafu.crm.customer.dto.UserUpdateDTO;
import com.huafu.crm.customer.dto.UserVO;
import com.huafu.crm.customer.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/user")
@Tag(name = "用户管理", description = "系统用户CRUD")
public class AdminUserController {
    private final AdminUserService service;
    public AdminUserController(AdminUserService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "新建用户")
    public Result<UserVO> create(@Valid @RequestBody UserCreateDTO dto) {
        return Result.ok(service.create(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public Result<UserVO> getById(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询用户")
    public Result<PageResult<UserVO>> page(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.ok(service.page(username, realName, deptId, status, current, size));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public Result<UserVO> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        return Result.ok(service.update(id, dto));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "启用/禁用用户")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        service.updateStatus(id, status);
        return Result.ok();
    }

    @PutMapping("/{id}/reset-password")
    @Operation(summary = "重置密码")
    public Result<String> resetPassword(@PathVariable Long id) {
        return Result.ok(service.resetPassword(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }
}
