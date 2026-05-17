package com.huafu.crm.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    // 单点密钥，生产环境应从配置中心读取
    private static final String SECRET = "HuafuCRM-SecretKey-2026-ForDevOnly-AtLeast32Bytes!";
    private static final long EXPIRE_MS = 7 * 24 * 60 * 60 * 1000L; // 7天
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /**
     * 生成 token
     */
    public String generate(Long userId, String username, Map<String, Object> extra) {
        JwtBuilder builder = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_MS))
                .signWith(KEY);
        if (extra != null) {
            extra.forEach(builder::claim);
        }
        return builder.compact();
    }

    /**
     * 解析 token，返回 userId
     */
    public Long parseUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * 解析 token，返回完整 claims
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证 token 是否有效
     */
    public boolean validate(String token) {
        try {
            Jwts.parser().verifyWith(KEY).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
