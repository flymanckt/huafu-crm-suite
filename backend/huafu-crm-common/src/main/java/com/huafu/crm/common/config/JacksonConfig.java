package com.huafu.crm.common.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;

@Configuration
public class JacksonConfig {

    // 序列化Long为String，防止JS端JSON.parse精度丢失（19位Long超MAX_SAFE_INTEGER）
    private static class LongToStringSerializer extends JsonSerializer<Long> {
        @Override
        public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.toString());
        }
    }

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        SimpleModule longModule = new SimpleModule();
        longModule.addSerializer(Long.class, new LongToStringSerializer());
        longModule.addSerializer(Long.TYPE, new LongToStringSerializer());

        return new Jackson2ObjectMapperBuilder()
                .modules(new JavaTimeModule(), longModule)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
