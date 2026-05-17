package com.huafu.crm.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huafu.crm.common.api.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Gateway WebFlux 全局异常处理
 * 当请求路由失败或发生其他异常时返回友好的JSON错误信息
 */
@Component
public class WebFluxExceptionHandler implements ErrorWebExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(WebFluxExceptionHandler.class);
    private final ObjectMapper objectMapper;

    public WebFluxExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.setStatusCode(determineStatus(ex));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String path = exchange.getRequest().getURI().getPath();
        String message = buildMessage(ex, path);

        log.error("[Gateway异常] path={} type={} message={}", path, ex.getClass().getSimpleName(), ex.getMessage(), ex);

        Result<Void> result = Result.fail(
                response.getStatusCode().value(),
                message
        );

        try {
            String json = objectMapper.writeValueAsString(result);
            DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            byte[] fallback = "{\"code\":500,\"message\":\"系统内部错误\"}".getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(fallback)));
        }
    }

    private HttpStatus determineStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException rse) {
            int status = rse.getStatusCode().value();
            if (status == 404) return HttpStatus.NOT_FOUND;
            if (status == 401 || status == 403) return HttpStatus.UNAUTHORIZED;
            if (status >= 400 && status < 500) return HttpStatus.BAD_REQUEST;
        }
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Connection refused")) return HttpStatus.BAD_GATEWAY;
            if (ex.getMessage().contains("504")) return HttpStatus.GATEWAY_TIMEOUT;
            if (ex.getMessage().contains("502")) return HttpStatus.BAD_GATEWAY;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String buildMessage(Throwable ex, String path) {
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) {
            return "系统内部错误，请稍后重试";
        }
        if (msg.contains("Connection refused")) {
            return "后端服务连接失败，请稍后重试";
        }
        if (msg.contains("504") || msg.contains("Gateway Timeout")) {
            return "请求超时，请稍后重试";
        }
        if (msg.contains("502") || msg.contains("Bad Gateway")) {
            return "网关异常，请稍后重试";
        }
        if (msg.contains("No static resource") || msg.contains("404")) {
            return "请求的接口不存在，请检查URL是否正确";
        }
        if (msg.contains("Access Denied") || msg.contains("Access is Denied")) {
            return "无权访问该资源";
        }
        if (msg.length() > 100) {
            return msg.substring(0, 100) + "...（详情见服务器日志）";
        }
        return msg;
    }
}
