package com.huafu.crm.common.api;

import com.huafu.crm.common.exception.BizException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {
    @Test
    void okShouldContainSuccessCodeMessageAndData() {
        Result<String> result = Result.ok("payload");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("success");
        assertThat(result.getData()).isEqualTo("payload");
    }

    @Test
    void okWithoutDataShouldReturnNullData() {
        Result<Void> result = Result.ok();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNull();
    }

    @Test
    void failShouldContainFailureCodeAndMessageWithoutData() {
        Result<Object> result = Result.fail(400, "invalid input");

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).isEqualTo("invalid input");
        assertThat(result.getData()).isNull();
    }

    @Test
    void bizExceptionShouldExposeCodeAndMessage() {
        BizException exception = new BizException(1001, "customer not found");

        assertThat(exception.getCode()).isEqualTo("1001");
        assertThat(exception.getMessage()).isEqualTo("customer not found");
    }
}
