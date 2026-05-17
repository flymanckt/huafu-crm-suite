package com.huafu.crm.wecom.service.impl;

import com.huafu.crm.wecom.consumer.WeComMessageConsumer;
import com.huafu.crm.wecom.service.WeComService;
import org.springframework.stereotype.Service;

@Service
public class WeComServiceImpl implements WeComService {
    private final WeComMessageConsumer consumer;
    public WeComServiceImpl(WeComMessageConsumer consumer) { this.consumer = consumer; }
    @Override public void enqueueIncomingMessage(String encryptedXml) {
        // Phase 0: in-memory handoff placeholder. Replace with RocketMQ producer in Phase 1.
        consumer.accept(encryptedXml);
    }
}
