package com.huafu.crm.customer.service;

import com.huafu.crm.common.entity.SysUserFilterConfig;
import java.util.List;

public interface SysUserConfigService {
    String getColumnConfig(Long userId, String pageCode);
    boolean saveColumnConfig(Long userId, String pageCode, String columnConfigs, String configName);
    boolean resetColumnConfig(Long userId, String pageCode);
    String getFilterConfig(Long userId, String pageCode);
    boolean saveFilterConfig(Long userId, String pageCode, String filterConfigs, String configName);
    List<SysUserFilterConfig> getFilterConfigList(Long userId, String pageCode);
    boolean deleteFilterConfig(Long userId, String pageCode, String configName);
}
