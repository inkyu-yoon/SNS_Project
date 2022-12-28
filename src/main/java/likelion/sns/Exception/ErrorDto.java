package likelion.sns.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.SQLException;

@Getter
@AllArgsConstructor
public class ErrorDto {
    private String errorCode;
    private String message;

    public ErrorDto(SNSAppException e) {
        this.errorCode = e.getErrorCode().toString();
        this.message = e.getErrorCode().getMessage();
    }

    public ErrorDto(SQLException e) {
        this.errorCode = ErrorCode.DATABASE_ERROR.toString();
        this.message = ErrorCode.DATABASE_ERROR.getMessage();
    }
}
