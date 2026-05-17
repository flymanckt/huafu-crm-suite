package com.huafu.crm.customer.dto;

import java.util.List;
import java.util.Map;

public record BatchUpdateDTO(
    List<Object> ids,
    Map<String, Object> fields
) {}
