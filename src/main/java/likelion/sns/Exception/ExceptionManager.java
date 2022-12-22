package likelion.sns.Exception;

import io.jsonwebtoken.SignatureException;
import likelion.sns.domain.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@RestControllerAdvice
public class ExceptionManager {

    @ExceptionHandler(SNSAppException.class)
    public ResponseEntity<?> SNSAppExceptionHandler(SNSAppException e){
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(Response.error(new ErrorDto(e)));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<?> SQLExceptionHandler(SQLException e){
        return ResponseEntity.status(ErrorCode.DATABASE_ERROR.getHttpStatus())
                .body(Response.error(new ErrorDto(e)));
    }

}

