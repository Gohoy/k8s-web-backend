package com.example.home.gohoy.k8s_backend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTUtils {

    private static final String SECRET_KEY = "TheFurthestDistanceInTheWorldIsNotBetweenLifeAndDeathButWhenIStandInFrontOfYouYetYouDonNotKnowThatILoveYou";

    // 生成JWT令牌
    public static String generateToken(String username, long expirationMillis) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMillis);

        Map<String, Object> claims = new HashMap<>();
        // 在claims中添加用户信息或其他声明
        claims.put("username", username);
        // 可以添加更多的声明，比如用户角色等

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // 验证JWT令牌，如果验证失败将抛出异常，否则返回声明（claims）
    public static Claims verifyToken(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }
}
