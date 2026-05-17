package com.huafu.crm.ai.controller;

import com.huafu.crm.ai.client.AiClient;
import com.huafu.crm.ai.model.DailyReportAiResult;
import com.huafu.crm.common.api.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiParseControllerTest {
 @Test
 void parseDailyReportShouldReturnResultWrapper() {
  AiClient aiClient = mock(AiClient.class);
  DailyReportAiResult parsed = new DailyReportAiResult("日报", 1, 2, 3, List.of("摘要"));
  when(aiClient.parseDailyReport("日报")).thenReturn(parsed);
  AiParseController controller = new AiParseController(aiClient, new ObjectMapper());

  Result<DailyReportAiResult> result = controller.parseDailyReport("日报");

  assertThat(result.getCode()).isEqualTo(200);
  assertThat(result.getData()).isSameAs(parsed);
  assertThat(result.getData().opportunityCount()).isEqualTo(1);
  verify(aiClient).parseDailyReport("日报");
 }
}
