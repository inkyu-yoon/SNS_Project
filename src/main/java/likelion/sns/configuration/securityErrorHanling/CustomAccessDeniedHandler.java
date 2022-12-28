package likelion.sns.configuration.securityErrorHanling;

import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.ExceptionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * "ADMIN" 만 접근할 수 있는 요청에 "ADMIN"이 아닌 사용자가 요청할 시 예외 처리
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {

        log.info("{}", accessDeniedException.getMessage());

        ErrorCode errorCode = ErrorCode.FORBIDDEN_REQUEST;

        ExceptionManager.setErrorResponse(response, errorCode);
    }
}