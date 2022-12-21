package likelion.sns.Exception;

import io.jsonwebtoken.SignatureException;
import likelion.sns.domain.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionManager {

    @ExceptionHandler(SNSAppException.class)
    public ResponseEntity<?> SNSAppExceptionHandler(SNSAppException e){
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(Response.error(new ErrorDto(e)));
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<?> SignatureExceptionHandler(SNSAppException e){
        return ResponseEntity.status(ErrorCode.INVALID_TOKEN.getHttpStatus())
                .body(Response.error(ErrorCode.INVALID_TOKEN));
    }
}

