package com.huafu.crm.customer.service.sap;

import com.huafu.crm.customer.entity.IntegrationFieldMapping;
import com.huafu.crm.customer.entity.IntegrationInterface;
import com.huafu.crm.customer.entity.IntegrationLog;
import com.huafu.crm.customer.entity.SapRfcConfig;
import java.util.List;

public interface SapJcoService {
    boolean isAvailable();

    String availabilityMessage();

    String testConnection(SapRfcConfig config);

    SapJcoResult execute(
        SapRfcConfig config,
        IntegrationInterface integrationInterface,
        IntegrationLog log,
        List<IntegrationFieldMapping> mappings
    );
}
