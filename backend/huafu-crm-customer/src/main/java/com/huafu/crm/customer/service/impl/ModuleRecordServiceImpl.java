package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.common.util.InputSanitizer;
import com.huafu.crm.customer.dto.ModuleRecordSaveDTO;
import com.huafu.crm.customer.entity.ModuleRecord;
import com.huafu.crm.customer.mapper.ModuleRecordMapper;
import com.huafu.crm.customer.service.ModuleRecordService;
import com.huafu.crm.customer.vo.ModuleRecordVO;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ModuleRecordServiceImpl implements ModuleRecordService {
    private final ModuleRecordMapper mapper;

    public ModuleRecordServiceImpl(ModuleRecordMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ModuleRecordVO> page(String moduleKey, long current, long size, String keyword, String status) {
        LambdaQueryWrapper<ModuleRecord> wrapper = new LambdaQueryWrapper<ModuleRecord>()
            .eq(ModuleRecord::getModuleKey, moduleKey)
            .eq(ModuleRecord::getDeleted, 0)
            .orderByDesc(ModuleRecord::getUpdatedTime)
            .orderByDesc(ModuleRecord::getCreatedTime);
        if (StringUtils.hasText(status)) {
            wrapper.eq(ModuleRecord::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ModuleRecord::getTitle, keyword)
                .or().like(ModuleRecord::getRecordNo, keyword)
                .or().like(ModuleRecord::getOwnerName, keyword)
                .or().like(ModuleRecord::getPayloadJson, keyword));
        }
        Page<ModuleRecord> page = mapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords().stream().map(this::toVO).toList());
    }

    @Override
    @Transactional
    public ModuleRecordVO create(String moduleKey, ModuleRecordSaveDTO dto) {
        OffsetDateTime now = OffsetDateTime.now();
        ModuleRecord record = new ModuleRecord();
        record.setModuleKey(moduleKey);
        apply(dto, record);
        record.setDeleted(0);
        record.setTenantId(1L);
        record.setCreatedTime(now);
        record.setUpdatedTime(now);
        mapper.insert(record);
        return toVO(record);
    }

    @Override
    @Transactional
    public ModuleRecordVO update(String moduleKey, Long id, ModuleRecordSaveDTO dto) {
        ModuleRecord record = mapper.selectById(id);
        if (record == null || record.getDeleted() != null && record.getDeleted() == 1 || !moduleKey.equals(record.getModuleKey())) {
            throw new BizException(1001, "记录不存在");
        }
        apply(dto, record);
        record.setUpdatedTime(OffsetDateTime.now());
        mapper.updateById(record);
        return toVO(mapper.selectById(id));
    }

    @Override
    @Transactional
    public void delete(String moduleKey, Long id) {
        ModuleRecord record = mapper.selectById(id);
        if (record == null || !moduleKey.equals(record.getModuleKey())) return;
        record.setDeleted(1);
        record.setUpdatedTime(OffsetDateTime.now());
        mapper.updateById(record);
    }

    private void apply(ModuleRecordSaveDTO dto, ModuleRecord record) {
        record.setRecordNo(InputSanitizer.safeText(dto.recordNo()));
        record.setTitle(InputSanitizer.safeText(dto.title()));
        record.setStatus(InputSanitizer.safeText(dto.status()));
        record.setOwnerName(InputSanitizer.safeText(dto.ownerName()));
        record.setRecordDate(dto.recordDate());
        record.setPayloadJson(InputSanitizer.rejectUnsafeHtml(dto.payloadJson()));
        record.setRemark(InputSanitizer.safeText(dto.remark()));
    }

    private ModuleRecordVO toVO(ModuleRecord record) {
        return new ModuleRecordVO(
            record.getId(),
            record.getModuleKey(),
            record.getRecordNo(),
            record.getTitle(),
            record.getStatus(),
            record.getOwnerName(),
            record.getRecordDate(),
            record.getPayloadJson(),
            record.getRemark(),
            record.getCreatedTime(),
            record.getUpdatedTime()
        );
    }
}
