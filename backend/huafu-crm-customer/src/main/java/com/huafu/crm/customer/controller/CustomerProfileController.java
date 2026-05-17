package com.huafu.crm.customer.controller;

import com.huafu.crm.common.api.Result;
import com.huafu.crm.customer.entity.CustomerProfile;
import com.huafu.crm.customer.mapper.CustomerProfileMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer/{customerId}/profile")
@Tag(name = "客户画像", description = "客户画像读写")
public class CustomerProfileController {
    private final CustomerProfileMapper mapper;
    public CustomerProfileController(CustomerProfileMapper mapper) { this.mapper = mapper; }

    @GetMapping
    @Operation(summary = "获取客户画像")
    public Result<CustomerProfile> get(@PathVariable Long customerId) {
        CustomerProfile p = mapper.selectOne(new LambdaQueryWrapper<CustomerProfile>()
            .eq(CustomerProfile::getCustomerId, customerId));
        if (p == null) {
            p = new CustomerProfile();
            p.setCustomerId(customerId);
            mapper.insert(p);
        }
        return Result.ok(p);
    }

    @PutMapping
    @Operation(summary = "更新客户画像")
    public Result<CustomerProfile> update(@PathVariable Long customerId, @RequestBody CustomerProfile profile) {
        profile.setCustomerId(customerId);
        CustomerProfile existing = mapper.selectOne(new LambdaQueryWrapper<CustomerProfile>()
            .eq(CustomerProfile::getCustomerId, customerId));
        if (existing == null) {
            mapper.insert(profile);
        } else {
            profile.setId(existing.getId());
            mapper.updateById(profile);
        }
        return Result.ok(profile);
    }
}
