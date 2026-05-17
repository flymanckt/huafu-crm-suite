package com.huafu.crm.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huafu.crm.ai.client.AiClient;
import com.huafu.crm.ai.model.DailyReportAiResult;
import com.huafu.crm.common.api.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/ai/parse", "/ai/parse", "/ai"})
public class AiParseController {
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    public AiParseController(AiClient aiClient, ObjectMapper objectMapper) {
        this.aiClient = aiClient;
        this.objectMapper = objectMapper;
    }

    @PostMapping({"/daily-report", "/parse-daily-report"})
    public Result<DailyReportAiResult> parseDailyReport(@RequestBody Object body) {
        return Result.ok(aiClient.parseDailyReport(extractText(body)));
    }

    private String extractText(Object body) {
        if (body == null) return "";
        if (body instanceof String text) return text;
        Map<?, ?> map = objectMapper.convertValue(body, Map.class);
        Object text = map.get("text");
        return text == null ? "" : String.valueOf(text);
    }
}
