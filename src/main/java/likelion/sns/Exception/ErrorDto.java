package likelion.sns.Exception;

import lombok.Getter;

@Getter
public class ErrorDto {
    private String errorCode;
    private String message;

    public ErrorDto(SNSAppException e) {
        this.errorCode = e.getErrorCode().toString();
        if (e.getMessage() == null) {
            this.message = e.getErrorCode().getMessage();
        } else {
            this.message = e.getMessage();
        }
    }
}
