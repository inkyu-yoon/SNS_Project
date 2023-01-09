package likelion.sns.Exception;

import com.google.gson.Gson;
import likelion.sns.domain.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@RestControllerAdvice
@Slf4j
public class ExceptionManager {

    @ExceptionHandler(SNSAppException.class)
    public ResponseEntity<?> SNSAppExceptionHandler(SNSAppException e){
        log.error("에러가 발생했습니다. {}",e.getErrorCode().getMessage());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(Response.error(new ErrorDto(e)));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<?> SQLExceptionHandler(SQLException e){
        log.error("DB 관련 에러가 발생하였습니다.");
        return ResponseEntity.status(ErrorCode.DATABASE_ERROR.getHttpStatus())
                .body(Response.error(new ErrorDto(e)));
    }

    /**
     * requestDto의 값이 null이나 공백인 값으로 요청할 시, BLANK_NOT_ALLOWED 에러 response 반환
     */
    public static ResponseEntity ifNullAndBlank() {
        ErrorCode e = ErrorCode.BLANK_NOT_ALLOWED;
        return ResponseEntity.status(e.getHttpStatus()).body(Response.error(new ErrorDto(e)));
    }

    /**
     * Security Chain 에서 발생하는 에러 응답 구성
     */
    public static void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {


        // 에러 응답코드 설정
        response.setStatus(errorCode.getHttpStatus().value());
        // 응답 body type JSON 타입으로 설정
        response.setContentType("application/json;charset=UTF-8");


        Response<ErrorDto> error = Response.error(new ErrorDto(errorCode.toString(), errorCode.getMessage()));

        //예외 발생 시 Error 내용을 JSON화 한 후 응답 body에 담아서 보낸다.
        Gson gson = new Gson();
        String responseBody = gson.toJson(error);

        response.getWriter().write(responseBody);
    }


}

