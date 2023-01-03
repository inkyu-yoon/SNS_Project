package likelion.sns.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    DUPLICATED_USER_NAME(HttpStatus.CONFLICT, "UserName이 중복됩니다."),
    USERNAME_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 유저를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "패스워드가 잘못되었습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다."),
    FORBIDDEN_REQUEST(HttpStatus.FORBIDDEN, "ADMIN 회원만 접근할 수 있습니다."),
    FORBIDDEN_ADD_LIKE(HttpStatus.FORBIDDEN, "좋아요는 한번만 누를 수 있습니다."),
    USER_NOT_MATCH(HttpStatus.UNAUTHORIZED, "작성자와 요청자가 일치하지 않습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 포스트가 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 댓글이 없습니다."),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB 에러"),
    BAD_REQUEST(HttpStatus.FORBIDDEN, "권한은 ADMIN 또는 USER 로 변경할 수 있습니다." ),

    // hello Controller 전용
    BAD_REQUEST_SUM(HttpStatus.FORBIDDEN, "숫자만 입력해주세요" );

    private HttpStatus httpStatus;
    private String message;


}