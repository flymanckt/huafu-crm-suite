package com.huafu.crm.customer.service;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.customer.dto.IntegrationFieldMappingDTO;
import com.huafu.crm.customer.dto.IntegrationConnectionConfigDTO;
import com.huafu.crm.customer.dto.IntegrationInterfaceDTO;
import com.huafu.crm.customer.dto.IntegrationLogCreateDTO;
import com.huafu.crm.customer.dto.SapRfcConfigDTO;
import com.huafu.crm.customer.entity.IntegrationConnectionConfig;
import com.huafu.crm.customer.entity.IntegrationFieldMapping;
import com.huafu.crm.customer.entity.IntegrationInterface;
import com.huafu.crm.customer.entity.IntegrationLog;
import com.huafu.crm.customer.entity.SapRfcConfig;
import java.util.List;

public interface IntegrationPlatformService {
    List<IntegrationConnectionConfig> listConnectionConfigs(String connectionType);
    IntegrationConnectionConfig saveConnectionConfig(IntegrationConnectionConfigDTO dto);
    IntegrationConnectionConfig updateConnectionConfig(Long id, IntegrationConnectionConfigDTO dto);
    void deleteConnectionConfig(Long id);
    String testConnectionConfig(Long id);

    List<SapRfcConfig> listSapRfcConfigs();
    SapRfcConfig saveSapRfcConfig(SapRfcConfigDTO dto);
    SapRfcConfig updateSapRfcConfig(Long id, SapRfcConfigDTO dto);
    void deleteSapRfcConfig(Long id);
    String testSapRfcConfig(Long id);

    List<IntegrationInterface> listInterfaces(String keyword, String systemCode, String enabled);
    IntegrationInterface saveInterface(IntegrationInterfaceDTO dto);
    IntegrationInterface updateInterface(Long id, IntegrationInterfaceDTO dto);
    void deleteInterface(Long id);

    List<IntegrationFieldMapping> listMappings(Long interfaceId);
    IntegrationFieldMapping saveMapping(IntegrationFieldMappingDTO dto);
    IntegrationFieldMapping updateMapping(Long id, IntegrationFieldMappingDTO dto);
    void deleteMapping(Long id);

    PageResult<IntegrationLog> pageLogs(long current, long size, String interfaceCode, String status, String keyword);
    IntegrationLog createLog(IntegrationLogCreateDTO dto);
    IntegrationLog repushLog(Long id);
}
