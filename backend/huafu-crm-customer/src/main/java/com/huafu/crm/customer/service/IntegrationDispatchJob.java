package com.huafu.crm.customer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IntegrationDispatchJob {
    private final IntegrationPlatformService integrationPlatformService;
    private final boolean enabled;
    private final int batchSize;

    public IntegrationDispatchJob(
            IntegrationPlatformService integrationPlatformService,
            @Value("${crm.integration.dispatcher.enabled:true}") boolean enabled,
            @Value("${crm.integration.dispatcher.batch-size:10}") int batchSize) {
        this.integrationPlatformService = integrationPlatformService;
        this.enabled = enabled;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${crm.integration.dispatcher.fixed-delay-ms:30000}")
    public void dispatchPendingLogs() {
        if (!enabled) return;
        integrationPlatformService.executePendingLogs(batchSize);
    }
}
