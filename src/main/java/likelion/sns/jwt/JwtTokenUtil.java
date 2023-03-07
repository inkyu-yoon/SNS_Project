package likelion.sns.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtTokenUtil {

    private static long expireTimeMs = 1000 * 60 * 60; //1시간

    public static String createToken(String userName,Long userId, String key) {
        Claims claims = Jwts.claims();
        claims.put("userName", userName);
        claims.put("userId", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expireTimeMs)) //현재 시간 +종료 시간 = 토큰 유효 시간
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    /**
     * 토큰이 만료되었는지 확인
     */
    public static boolean isExpired(String token, String secretKey) {
        Date expiredDate = extractClaims(token, secretKey).getExpiration();
        return expiredDate.before(new Date());
    }

    /**
     * 암호화된 토큰을 해독하는 역할
     */
    public static Claims extractClaims(String token, String secretKey) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    public static String getId(String token, String key) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody()
                .get("userId", String.class);
    }

}
