package likelion.sns.configuration;

import com.google.gson.Gson;
import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.ErrorDto;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class CustomAuthenticationEntryPointHandler implements AuthenticationEntryPoint {


    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null ) {
            log.error("토큰이 존재하지 않습니다.");
            setResponse(response, ErrorCode.TOKEN_NOT_FOUND);
        }


    }
    private void setResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {

        Gson gson = new Gson();
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(gson.toJson(Response.error(new ErrorDto(new SNSAppException(errorCode)))));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}