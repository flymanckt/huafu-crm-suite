package com.huafu.crm.customer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huafu.crm.customer.dto.SysConfigDTO;
import com.huafu.crm.customer.entity.SysConfig;

import java.util.List;

public interface SysConfigService extends IService<SysConfig> {

    List<SysConfig> listByGroup(String group);

    List<SysConfig> listVisible();

    SysConfig getByConfigKey(String configKey);

    String getConfigValue(String configKey);

    boolean saveConfig(SysConfigDTO dto);

    boolean updateConfig(SysConfigDTO dto);

    boolean deleteConfig(Long id);

    List<String> listGroups();
}
