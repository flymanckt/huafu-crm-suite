package com.huafu.crm.common.context;

import com.huafu.crm.common.utils.JwtUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class UserContext {

    private final JwtUtil jwtUtil;

    public UserContext(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public Optional<Long> getCurrentUserId() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return Optional.empty();
        HttpServletRequest request = attrs.getRequest();
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return Optional.empty();
        String token = auth.substring(7);
        try {
            Long userId = jwtUtil.parseUserId(token);
            return Optional.ofNullable(userId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<String> getCurrentUsername() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return Optional.empty();
        HttpServletRequest request = attrs.getRequest();
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return Optional.empty();
        String token = auth.substring(7);
        try {
            return Optional.ofNullable(jwtUtil.parse(token).get("username", String.class));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
