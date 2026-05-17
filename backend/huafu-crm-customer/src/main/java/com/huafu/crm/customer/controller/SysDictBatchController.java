package com.huafu.crm.customer.controller;

import com.huafu.crm.common.api.Result;
import com.huafu.crm.common.entity.SysDictItem;
import com.huafu.crm.customer.service.SysDictService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/crm/v1/dicts")
public class SysDictBatchController {

    private final SysDictService dictService;

    public SysDictBatchController(SysDictService dictService) {
        this.dictService = dictService;
    }

    @GetMapping("/batch-codes")
    public Result<Map<String, List<SysDictItem>>> getBatchDictItems(@RequestParam String codes) {
        List<String> codeList = Arrays.asList(codes.split(","));
        return Result.ok(dictService.getBatchDictItems(codeList));
    }

    @GetMapping("/codes/{dictCode}")
    public Result<List<SysDictItem>> getDictItemsByCode(@PathVariable String dictCode) {
        return Result.ok(dictService.getDictItemsByDictCode(dictCode));
    }
}
