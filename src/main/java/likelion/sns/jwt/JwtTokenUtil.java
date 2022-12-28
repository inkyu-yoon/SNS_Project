package likelion.sns.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtTokenUtil {

    private static long expireTimeMs = 1000 * 60 * 60; //1시간

    public static String createToken(String userName, String key) {
        Claims claims = Jwts.claims();
        claims.put("userName", userName);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expireTimeMs)) //현재 시간 +종료 시간 = 토큰 유효 시간
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }
}
