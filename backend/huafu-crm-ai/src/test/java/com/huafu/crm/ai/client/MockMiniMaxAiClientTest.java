package com.huafu.crm.ai.client;

import com.huafu.crm.ai.model.DailyReportAiResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockMiniMaxAiClientTest {
    private final MockMiniMaxAiClient client = new MockMiniMaxAiClient();

    @Test
    void parseDailyReportShouldReturnStableCountsAndHighlights() {
        String text = "今日新增商机：客户A询价\n市场情报：竞品降价\n丢单：客户B转向竞品\n普通拜访记录";

        DailyReportAiResult result = client.parseDailyReport(text);

        assertThat(result.originalText()).isEqualTo(text);
        assertThat(result.opportunityCount()).isEqualTo(1);
        assertThat(result.marketIntelligenceCount()).isEqualTo(1);
        assertThat(result.lostOrderCount()).isEqualTo(1);
        assertThat(result.highlights()).containsExactly(
                "今日新增商机：客户A询价",
                "市场情报：竞品降价",
                "丢单：客户B转向竞品"
        );
    }

    @Test
    void parseDailyReportShouldHandleBlankInputDeterministically() {
        DailyReportAiResult result = client.parseDailyReport("   ");

        assertThat(result.originalText()).isEqualTo("   ");
        assertThat(result.opportunityCount()).isZero();
        assertThat(result.marketIntelligenceCount()).isZero();
        assertThat(result.lostOrderCount()).isZero();
        assertThat(result.highlights()).containsExactly("mock parser: no daily report content");
    }
}
