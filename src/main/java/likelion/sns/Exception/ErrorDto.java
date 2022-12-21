package likelion.sns.Exception;

import lombok.Getter;

@Getter
public class ErrorDto {
    private String errorCode;
    private String message;

    public ErrorDto(SNSAppException e) {
        this.errorCode = e.getErrorCode().toString();
        this.message = e.getErrorCode().getMessage();
    }
}
