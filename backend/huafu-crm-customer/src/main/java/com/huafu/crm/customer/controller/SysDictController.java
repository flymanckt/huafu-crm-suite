package com.huafu.crm.customer.controller;

import com.huafu.crm.common.api.Result;
import com.huafu.crm.common.entity.SysDictType;
import com.huafu.crm.customer.service.SysDictService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/crm/v1/dict-types")
public class SysDictController {

    private final SysDictService dictService;

    public SysDictController(SysDictService dictService) {
        this.dictService = dictService;
    }

    @GetMapping("/all")
    public Result<List<SysDictType>> getAllDictTypes() {
        return Result.ok(dictService.getAllDictTypes());
    }

    @GetMapping
    public Result<?> pageDictTypes(
            @RequestParam(required=false, defaultValue="1") Integer current,
            @RequestParam(required=false, defaultValue="20") Integer size,
            @RequestParam(required=false) String dictCode,
            @RequestParam(required=false) String dictName,
            @RequestParam(required=false) String dictType,
            @RequestParam(required=false) Integer status) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysDictType> page =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysDictType> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(SysDictType::getDeleted, 0);
        if (StringUtils.hasText(dictCode)) {
            wrapper.like(SysDictType::getDictCode, dictCode.trim());
        }
        if (StringUtils.hasText(dictName)) {
            wrapper.like(SysDictType::getDictName, dictName.trim());
        }
        if (StringUtils.hasText(dictType)) {
            wrapper.eq(SysDictType::getDictType, dictType.trim());
        }
        if (status != null) {
            wrapper.eq(SysDictType::getStatus, status);
        }
        wrapper.orderByAsc(SysDictType::getSortOrder).orderByAsc(SysDictType::getDictCode);
        page = dictService.getBaseMapper().selectPage(page, wrapper);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result<SysDictType> getDictTypeById(@PathVariable("id") Long id) {
        return Result.ok(dictService.getById(id));
    }

    @GetMapping("/{dictCode}/items")
    public Result<?> getDictItemsByDictCode(@PathVariable String dictCode) {
        return Result.ok(dictService.getDictItemsByDictCode(dictCode));
    }

    @PostMapping
    public Result<Boolean> saveDictType(@RequestBody SysDictType dictType) {
        return Result.ok(dictService.saveDictType(dictType));
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateDictType(@PathVariable Long id, @RequestBody SysDictType dictType) {
        dictType.setId(id);
        return Result.ok(dictService.updateDictType(dictType));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteDictType(@PathVariable Long id) {
        return Result.ok(dictService.deleteDictType(id));
    }
}
