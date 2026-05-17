package com.huafu.crm.wecom.consumer;

import com.huafu.crm.wecom.parser.WeComXmlParser;
import com.huafu.crm.wecom.service.DailyReportService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class WeComMessageConsumer {
    private final WeComXmlParser parser; private final DailyReportService dailyReportService;
    public WeComMessageConsumer(WeComXmlParser parser, DailyReportService dailyReportService) { this.parser = parser; this.dailyReportService = dailyReportService; }
    @Async public void accept(String xml) { WeComXmlParser.ParsedMessage msg = parser.parse(xml); dailyReportService.handleDailyReportText(msg.fromUser(), msg.content()); }
}
