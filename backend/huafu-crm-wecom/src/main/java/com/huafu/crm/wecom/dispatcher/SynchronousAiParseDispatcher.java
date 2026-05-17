package com.huafu.crm.wecom.dispatcher;

import com.huafu.crm.wecom.processor.DailyReportAiParseProcessor;
import org.springframework.stereotype.Component;

@Component
public class SynchronousAiParseDispatcher implements AiParseDispatcher {
    private final DailyReportAiParseProcessor processor;

    public SynchronousAiParseDispatcher(DailyReportAiParseProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void dispatch(Long dailyReportId) {
        processor.parse(dailyReportId);
    }
}
