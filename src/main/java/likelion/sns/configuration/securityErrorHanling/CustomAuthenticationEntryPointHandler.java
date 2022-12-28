package likelion.sns.configuration.securityErrorHanling;

import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.ExceptionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class CustomAuthenticationEntryPointHandler implements AuthenticationEntryPoint {

    /**
     *  토큰 없이 Security Chain 에서 autenticated된 url 접근 시, 에러 핸들링
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null) {
            log.error("토큰이 존재하지 않습니다.");
            ErrorCode errorCode = ErrorCode.TOKEN_NOT_FOUND;

            ExceptionManager.setErrorResponse(response, errorCode);
        }
    }
}