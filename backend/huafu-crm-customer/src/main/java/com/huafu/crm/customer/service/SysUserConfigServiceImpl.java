package com.huafu.crm.customer.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huafu.crm.common.entity.SysUserColumnConfig;
import com.huafu.crm.common.entity.SysUserFilterConfig;
import com.huafu.crm.common.mapper.SysUserColumnConfigMapper;
import com.huafu.crm.common.mapper.SysUserFilterConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SysUserConfigServiceImpl extends ServiceImpl<SysUserColumnConfigMapper, SysUserColumnConfig> implements SysUserConfigService {

    private final SysUserFilterConfigMapper filterConfigMapper;

    public SysUserConfigServiceImpl(SysUserFilterConfigMapper filterConfigMapper) {
        this.filterConfigMapper = filterConfigMapper;
    }

    @Override
    public String getColumnConfig(Long userId, String pageCode) {
        LambdaQueryWrapper<SysUserColumnConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserColumnConfig::getUserId, userId)
               .eq(SysUserColumnConfig::getPageCode, pageCode)
               .isNull(SysUserColumnConfig::getConfigName)
               .orderByDesc(SysUserColumnConfig::getUpdatedAt)
               .last("LIMIT 1");
        SysUserColumnConfig config = this.getOne(wrapper);
        return config != null ? config.getColumnConfigs() : null;
    }

    @Override
    @Transactional
    public boolean saveColumnConfig(Long userId, String pageCode, String columnConfigs, String configName) {
        LambdaQueryWrapper<SysUserColumnConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserColumnConfig::getUserId, userId)
               .eq(SysUserColumnConfig::getPageCode, pageCode);
        
        if (StringUtils.hasText(configName)) {
            wrapper.eq(SysUserColumnConfig::getConfigName, configName);
        } else {
            wrapper.isNull(SysUserColumnConfig::getConfigName);
        }
        wrapper.orderByDesc(SysUserColumnConfig::getUpdatedAt).last("LIMIT 1");
        
        SysUserColumnConfig existing = this.getOne(wrapper);
        
        if (existing != null) {
            existing.setColumnConfigs(columnConfigs);
            return this.updateById(existing);
        } else {
            SysUserColumnConfig newConfig = new SysUserColumnConfig();
            newConfig.setUserId(userId);
            newConfig.setPageCode(pageCode);
            newConfig.setColumnConfigs(columnConfigs);
            newConfig.setConfigName(configName);
            return this.save(newConfig);
        }
    }

    @Override
    @Transactional
    public boolean resetColumnConfig(Long userId, String pageCode) {
        LambdaQueryWrapper<SysUserColumnConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserColumnConfig::getUserId, userId)
               .eq(SysUserColumnConfig::getPageCode, pageCode)
               .isNull(SysUserColumnConfig::getConfigName);
        return this.remove(wrapper);
    }

    @Override
    public String getFilterConfig(Long userId, String pageCode) {
        LambdaQueryWrapper<SysUserFilterConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserFilterConfig::getUserId, userId)
               .eq(SysUserFilterConfig::getPageCode, pageCode)
               .isNull(SysUserFilterConfig::getConfigName)
               .orderByDesc(SysUserFilterConfig::getUpdatedAt)
               .last("LIMIT 1");
        SysUserFilterConfig config = filterConfigMapper.selectOne(wrapper);
        return config != null ? config.getFilterConfigs() : null;
    }

    @Override
    @Transactional
    public boolean saveFilterConfig(Long userId, String pageCode, String filterConfigs, String configName) {
        LambdaQueryWrapper<SysUserFilterConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserFilterConfig::getUserId, userId)
               .eq(SysUserFilterConfig::getPageCode, pageCode);
        
        if (StringUtils.hasText(configName)) {
            wrapper.eq(SysUserFilterConfig::getConfigName, configName);
        } else {
            wrapper.isNull(SysUserFilterConfig::getConfigName);
        }
        wrapper.orderByDesc(SysUserFilterConfig::getUpdatedAt).last("LIMIT 1");
        
        SysUserFilterConfig existing = filterConfigMapper.selectOne(wrapper);
        
        if (existing != null) {
            existing.setFilterConfigs(filterConfigs);
            return filterConfigMapper.updateById(existing) > 0;
        } else {
            SysUserFilterConfig newConfig = new SysUserFilterConfig();
            newConfig.setUserId(userId);
            newConfig.setPageCode(pageCode);
            newConfig.setFilterConfigs(filterConfigs);
            newConfig.setConfigName(configName);
            return filterConfigMapper.insert(newConfig) > 0;
        }
    }

    @Override
    public List<SysUserFilterConfig> getFilterConfigList(Long userId, String pageCode) {
        LambdaQueryWrapper<SysUserFilterConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserFilterConfig::getUserId, userId)
               .eq(SysUserFilterConfig::getPageCode, pageCode)
               .isNotNull(SysUserFilterConfig::getConfigName)
               .orderByDesc(SysUserFilterConfig::getCreatedAt);
        return filterConfigMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public boolean deleteFilterConfig(Long userId, String pageCode, String configName) {
        LambdaQueryWrapper<SysUserFilterConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserFilterConfig::getUserId, userId)
               .eq(SysUserFilterConfig::getPageCode, pageCode)
               .eq(SysUserFilterConfig::getConfigName, configName);
        return filterConfigMapper.delete(wrapper) > 0;
    }
}
