package online.faramita.bbs.util;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    /**
     * 生成jwt
     * Hs256
     * @param secret
     * @param expire
     * @param calims
     * @return
     */
    public static String createJwt(String secret, Long expire, Map<String, Object> calims) {
        // jwt过期时间
        Long expMills = System.currentTimeMillis() + expire;
        Date exp = new Date(expMills);
        // 生成HMAC密钥
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        // 设置body
        JwtBuilder builder = Jwts.builder()
                        .signWith(key)      // 声明签名算法和密钥
                        .claims(calims)     // 私有声明
                        .expiration(exp);   // 过期时间
        return builder.compact();
    }

    /**
     * Token解密
     * @param secret
     * @param header
     * @return
     */
    public static Claims parseJWT(String secret, String header) {
        // 生成HMAC密钥
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        // 得到DefaultJwtParse
        JwtParser jwtParser = Jwts.parser()
                        .verifyWith(key)    // 设置签名方法
                        .build();
        Jws<Claims> jws = jwtParser.parseSignedClaims(header);
        Claims claims = jws.getPayload();
        return claims;
    }
}
