package com.huafu.crm.customer.controller;

import com.huafu.crm.common.api.Result;
import com.huafu.crm.common.entity.SysDictItem;
import com.huafu.crm.common.mapper.SysDictItemMapper;
import com.huafu.crm.customer.service.SysDictService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/crm/v1/items")
public class SysDictItemController {

    private final SysDictService dictService;
    private final SysDictItemMapper dictItemMapper;

    public SysDictItemController(SysDictService dictService, SysDictItemMapper dictItemMapper) {
        this.dictService = dictService;
        this.dictItemMapper = dictItemMapper;
    }

    @GetMapping("/{id}")
    public Result<SysDictItem> getDictItemById(@PathVariable Long id) {
        return Result.ok(dictItemMapper.selectById(id));
    }

    @PostMapping
    public Result<Boolean> saveDictItem(@RequestBody SysDictItem dictItem) {
        return Result.ok(dictService.saveDictItem(dictItem));
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateDictItem(@PathVariable Long id, @RequestBody SysDictItem dictItem) {
        dictItem.setId(id);
        return Result.ok(dictService.updateDictItem(dictItem));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteDictItem(@PathVariable Long id) {
        return Result.ok(dictService.deleteDictItem(id));
    }

    @PutMapping("/{id}/status")
    public Result<Boolean> updateDictItemStatus(@PathVariable Long id, @RequestParam Integer status) {
        return Result.ok(dictService.updateDictItemStatus(id, status));
    }
}
