package com.huafu.crm.ai.consumer;

import com.huafu.crm.ai.client.AiClient;
import com.huafu.crm.ai.model.ParseTask;
import org.springframework.stereotype.Component;

@Component
public class AiParseConsumer {
    private final AiClient aiClient;
    public AiParseConsumer(AiClient aiClient) { this.aiClient = aiClient; }
    public void accept(ParseTask task) { aiClient.parseDailyReport(task.content()); }
}
