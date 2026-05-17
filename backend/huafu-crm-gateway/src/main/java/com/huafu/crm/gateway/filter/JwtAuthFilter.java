package com.huafu.crm.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huafu.crm.common.utils.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final String SECRET = "HuafuCRM-SecretKey-2026-ForDevOnly-AtLeast32Bytes!";
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 放行登录接口
        if (path.startsWith("/api/auth/")) {
            return chain.filter(exchange);
        }

        // 仅对 /api/admin/** 做鉴权
        if (!path.startsWith("/api/admin/")) {
            return chain.filter(exchange);
        }

        String auth = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return writeUnauthorized(exchange.getResponse(), "缺少认证令牌");
        }

        String token = auth.substring(7);
        if (!jwtUtil.validate(token)) {
            return writeUnauthorized(exchange.getResponse(), "令牌无效或已过期");
        }

        // 提取 userId 传递给下游服务
        Long userId = jwtUtil.parseUserId(token);
        String username = jwtUtil.parse(token).get("username", String.class);

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-Username", username != null ? username : "")
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private Mono<Void> writeUnauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "code", 401,
                    "message", message,
                    "timestamp", System.currentTimeMillis()
            ));
            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            byte[] fallback = "{\"code\":401,\"message\":\"Unauthorized\"}".getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(fallback)));
        }
    }

    @Override
    public int getOrder() {
        return -100; // 高优先级
    }
}
