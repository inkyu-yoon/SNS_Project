package likelion.sns.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SNSAppException extends RuntimeException{

    private ErrorCode errorCode;
    private String message;

}