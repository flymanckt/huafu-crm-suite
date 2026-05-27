package com.huafu.crm.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huafu.crm.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
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
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/auth/",
            "/api/wecom/callback"
    );
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        ServerHttpRequest sanitizedRequest = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove("X-User-Id");
                    headers.remove("X-Username");
                })
                .build();
        ServerWebExchange sanitizedExchange = exchange.mutate().request(sanitizedRequest).build();

        if (PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith)) {
            return chain.filter(sanitizedExchange);
        }
        if (!path.startsWith("/api/")) {
            return chain.filter(sanitizedExchange);
        }

        String auth = sanitizedRequest.getHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return writeUnauthorized(sanitizedExchange.getResponse(), "缺少认证令牌");
        }

        String token = auth.substring(7);
        if (!jwtUtil.validate(token)) {
            return writeUnauthorized(sanitizedExchange.getResponse(), "令牌无效或已过期");
        }

        Claims claims = jwtUtil.parse(token);
        Long userId = jwtUtil.parseUserId(token);
        String username = claims.get("username", String.class);
        List<String> permissions = claimList(claims.get("permissions"));
        List<String> roles = claimList(claims.get("roles"));

        String requiredPermission = requiredPermission(path);
        if (requiredPermission != null && !hasPermission(permissions, requiredPermission)) {
            return writeForbidden(sanitizedExchange.getResponse(), "没有访问权限");
        }

        ServerHttpRequest mutatedRequest = sanitizedRequest.mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-Username", username != null ? username : "")
                .header("X-Roles", String.join(",", roles))
                .header("X-Permissions", String.join(",", permissions))
                .build();

        return chain.filter(sanitizedExchange.mutate().request(mutatedRequest).build());
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

    private Mono<Void> writeForbidden(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "code", 403,
                    "message", message,
                    "timestamp", System.currentTimeMillis()
            ));
            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            byte[] fallback = "{\"code\":403,\"message\":\"Forbidden\"}".getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(fallback)));
        }
    }

    private String requiredPermission(String path) {
        if (path.startsWith("/api/admin/user")) return "admin:user";
        if (path.startsWith("/api/admin/role")) return "admin:role";
        if (path.startsWith("/api/admin/dept")) return "admin:dept";
        if (path.startsWith("/api/dict") || path.startsWith("/api/system/dict")) return "system:dict";
        if (path.startsWith("/api/system/config") || path.startsWith("/api/config")) return "system:config";
        if (path.startsWith("/api/integration")) return "system:integration";
        if (path.startsWith("/api/wecom/message")) return "wecom:message";
        return null;
    }

    private boolean hasPermission(List<String> permissions, String requiredPermission) {
        if (requiredPermission == null) return true;
        if (permissions == null || permissions.isEmpty()) return false;
        Set<String> normalized = permissions.stream()
                .filter(item -> item != null && !item.isBlank())
                .collect(Collectors.toSet());
        return normalized.contains("*") || normalized.contains(requiredPermission);
    }

    private List<String> claimList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(item -> item != null)
                    .map(String::valueOf)
                    .filter(item -> !item.isBlank())
                    .toList();
        }
        if (value instanceof String text && !text.isBlank()) {
            return List.of(text.split(",")).stream()
                    .map(String::trim)
                    .filter(item -> !item.isBlank())
                    .toList();
        }
        return List.of();
    }

    @Override
    public int getOrder() {
        return -100; // 高优先级
    }
}
