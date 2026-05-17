package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huafu.crm.common.context.UserContext;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.customer.dto.SysConfigDTO;
import com.huafu.crm.customer.entity.SysConfig;
import com.huafu.crm.customer.mapper.SysConfigMapper;
import com.huafu.crm.customer.service.SysConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    private final UserContext userContext;

    public SysConfigServiceImpl(UserContext userContext) {
        this.userContext = userContext;
    }

    @Override
    public List<SysConfig> listByGroup(String group) {
        return lambdaQuery()
            .eq(SysConfig::getConfigGroup, group)
            .eq(SysConfig::getDeleted, 0)
            .eq(SysConfig::getVisible, true)
            .list();
    }

    @Override
    public List<SysConfig> listVisible() {
        return lambdaQuery()
            .eq(SysConfig::getDeleted, 0)
            .eq(SysConfig::getVisible, true)
            .list();
    }

    @Override
    public SysConfig getByConfigKey(String configKey) {
        return lambdaQuery()
            .eq(SysConfig::getConfigKey, configKey)
            .eq(SysConfig::getDeleted, 0)
            .one();
    }

    @Override
    public String getConfigValue(String configKey) {
        SysConfig cfg = getByConfigKey(configKey);
        return cfg != null ? cfg.getConfigValue() : null;
    }

    @Override
    @Transactional
    public boolean saveConfig(SysConfigDTO dto) {
        if (existsConfigKey(dto.configKey())) {
            throw new BizException(1001, "配置键已存在：" + dto.configKey());
        }
        SysConfig entity = toEntity(dto);
        entity.setCreatedBy(userContext.getCurrentUserId().orElse(1L));
        entity.setCreatedTime(LocalDateTime.now());
        return save(entity);
    }

    @Override
    @Transactional
    public boolean updateConfig(SysConfigDTO dto) {
        if (dto.id() == null) throw new BizException(1001, "更新缺少ID");
        SysConfig existing = getById(dto.id());
        if (existing == null) throw new BizException(1001, "配置不存在");
        if (!existing.getEditable()) throw new BizException(1001, "该配置不可编辑");

        SysConfig entity = toEntity(dto);
        entity.setId(dto.id());
        entity.setConfigKey(existing.getConfigKey());
        entity.setCreatedBy(existing.getCreatedBy());
        entity.setCreatedTime(existing.getCreatedTime());
        entity.setUpdatedBy(userContext.getCurrentUserId().orElse(1L));
        entity.setUpdatedTime(LocalDateTime.now());
        return updateById(entity);
    }

    @Override
    @Transactional
    public boolean deleteConfig(Long id) {
        SysConfig cfg = getById(id);
        if (cfg == null) throw new BizException(1001, "配置不存在");
        if (!cfg.getEditable()) throw new BizException(1001, "该配置不可删除");
        return removeById(id);
    }

    @Override
    public List<String> listGroups() {
        return baseMapper.selectList(null).stream()
            .map(SysConfig::getConfigGroup)
            .distinct()
            .sorted()
            .toList();
    }

    private boolean existsConfigKey(String key) {
        return lambdaQuery().eq(SysConfig::getConfigKey, key).eq(SysConfig::getDeleted, 0).count() > 0;
    }

    private SysConfig toEntity(SysConfigDTO dto) {
        SysConfig e = new SysConfig();
        e.setConfigKey(dto.configKey());
        e.setConfigValue(dto.configValue());
        e.setConfigName(dto.configName());
        e.setConfigGroup(dto.configGroup());
        e.setDescription(dto.description());
        e.setType(dto.type() != null ? dto.type() : "STRING");
        e.setEditable(dto.editable() != null ? dto.editable() : true);
        e.setVisible(dto.visible() != null ? dto.visible() : true);
        return e;
    }
}
