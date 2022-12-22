package likelion.sns.Exception;

import lombok.Getter;

import java.sql.SQLException;

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

    public ErrorDto(SQLException e) {
        this.errorCode = ErrorCode.DATABASE_ERROR.toString();
       this.message= ErrorCode.DATABASE_ERROR.getMessage();
    }
}
