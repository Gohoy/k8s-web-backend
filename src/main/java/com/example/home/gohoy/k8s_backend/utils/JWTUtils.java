package com.example.home.gohoy.k8s_backend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTUtils {

    private static final String SECRET_KEY = "TheFurthestDistanceInTheWorldIsNotBetweenLifeAndDeathButWhenIStandInFrontOfYouYetYouDonNotKnowThatILoveYou";

    // 生成JWT令牌
    public static String generateToken(String username,byte isAdmin, long expirationMillis) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMillis);

        Map<String, Object> claims = new HashMap<>();
        // 在claims中添加用户信息或其他声明
        claims.put("username", username);
        claims.put("isAdmin",isAdmin);
        // 可以添加更多的声明，比如用户角色等

        byte[] apiKeySecretBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS256.getJcaName());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    // 验证JWT令牌，如果验证失败将抛出异常，否则返回声明（claims）
    public static Claims verifyToken(String token) {
        byte[] apiKeySecretBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS256.getJcaName());

        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
