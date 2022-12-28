package likelion.sns.configuration.securityErrorHanling;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import likelion.sns.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final String secretKey;

    /**
     * request 에서 전달받은 Jwt 토큰을 확인하는 과정
     */

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        String token;

        // [ Bearer token ] 형태로 입력될 것므로, split을 이용해서 토큰 정보만 추출
        try {
            token = authorization.split(" ")[1];
        } catch (Exception e) {
            log.info("추출할 토큰이 존재하지 않습니다.");
            filterChain.doFilter(request, response);
            return;
        }

        // 만료된 토큰인지 확인하고, 만료되었다면 권한을 부여하지 않는다.
        if (JwtTokenFilter.isExpired(token, secretKey)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰을 생성할 때, 입력해둔 userName 정보를 추출한다.
        String userName = extractClaims(token, secretKey).get("userName").toString();

        // 권한을 줄지 안줄지 결정하는 메서드
        String userRole = userService.findRoleByUserName(userName).name();
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userName, null, List.of(new SimpleGrantedAuthority(userRole)));

        log.info("{}",authenticationToken);

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // SecurityContext 에 authenticationToken 정보를 등록한다.
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
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
    private static Claims extractClaims(String token, String secretKey) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }


}
