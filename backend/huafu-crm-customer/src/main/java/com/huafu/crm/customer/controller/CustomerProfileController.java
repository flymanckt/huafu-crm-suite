package com.huafu.crm.customer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.api.Result;
import com.huafu.crm.customer.entity.CustomerProfile;
import com.huafu.crm.customer.entity.CustomerProfileHistory;
import com.huafu.crm.customer.mapper.CustomerProfileHistoryMapper;
import com.huafu.crm.customer.mapper.CustomerProfileMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/customer/{customerId}/profile")
@Tag(name = "客户画像", description = "客户画像读写及历史版本")
public class CustomerProfileController {
    private final CustomerProfileMapper profileMapper;
    private final CustomerProfileHistoryMapper historyMapper;

    public CustomerProfileController(CustomerProfileMapper profileMapper,
                                     CustomerProfileHistoryMapper historyMapper) {
        this.profileMapper = profileMapper;
        this.historyMapper = historyMapper;
    }

    @GetMapping
    @Operation(summary = "获取客户画像")
    public Result<CustomerProfile> get(@PathVariable Long customerId) {
        CustomerProfile p = profileMapper.selectOne(new LambdaQueryWrapper<CustomerProfile>()
            .eq(CustomerProfile::getCustomerId, customerId));
        if (p == null) {
            p = new CustomerProfile();
            p.setCustomerId(customerId);
            profileMapper.insert(p);
        }
        return Result.ok(p);
    }

    @PutMapping
    @Operation(summary = "更新客户画像（自动记录历史版本）")
    public Result<CustomerProfile> update(@PathVariable Long customerId, @RequestBody CustomerProfile profile) {
        profile.setCustomerId(customerId);
        CustomerProfile existing = profileMapper.selectOne(new LambdaQueryWrapper<CustomerProfile>()
            .eq(CustomerProfile::getCustomerId, customerId));

        if (existing == null) {
            profileMapper.insert(profile);
        } else {
            // P0-3: 更新前先保存历史快照
            saveProfileHistory(existing);
            profile.setId(existing.getId());
            profileMapper.updateById(profile);
        }
        return Result.ok(profile);
    }

    @GetMapping("/history")
    @Operation(summary = "查询画像历史版本")
    public Result<PageResult<CustomerProfileHistory>> history(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        // 先查到 profileId
        CustomerProfile p = profileMapper.selectOne(new LambdaQueryWrapper<CustomerProfile>()
            .eq(CustomerProfile::getCustomerId, customerId));
        if (p == null) {
            return Result.ok(new PageResult<>(current, size, 0, List.of()));
        }
        Page<CustomerProfileHistory> page = new Page<>(current, size);
        LambdaQueryWrapper<CustomerProfileHistory> qw = new LambdaQueryWrapper<>();
        qw.eq(CustomerProfileHistory::getProfileId, p.getId());
        qw.orderByDesc(CustomerProfileHistory::getVersion);
        Page<CustomerProfileHistory> result = historyMapper.selectPage(page, qw);
        return Result.ok(new PageResult<>(
            result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords()));
    }

    private void saveProfileHistory(CustomerProfile current) {
        CustomerProfileHistory hist = new CustomerProfileHistory();
        BeanUtils.copyProperties(current, hist);
        hist.setId(null);
        hist.setProfileId(current.getId());
        // 取最大版本号 +1
        Long historyCount = historyMapper.selectCount(
            new LambdaQueryWrapper<CustomerProfileHistory>()
                .eq(CustomerProfileHistory::getProfileId, current.getId())
        );
        hist.setVersion(historyCount.intValue() + 1);
        hist.setOperatedAt(OffsetDateTime.now());
        hist.setOperatedBy("system"); // TODO: 替换为真实用户上下文
        historyMapper.insert(hist);
    }
}
