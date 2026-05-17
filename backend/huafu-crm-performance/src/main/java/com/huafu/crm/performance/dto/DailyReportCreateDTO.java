package com.huafu.crm.performance.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record DailyReportCreateDTO(
    @NotNull Long userId, @NotNull LocalDate reportDate, @NotNull String contentText
) {}
