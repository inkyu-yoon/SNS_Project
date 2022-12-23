package likelion.sns.Exception;

import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import likelion.sns.domain.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

}

