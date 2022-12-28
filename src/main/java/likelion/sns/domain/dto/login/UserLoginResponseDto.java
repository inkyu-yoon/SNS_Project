package likelion.sns.domain.dto.login;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString(of={"jwt"})
public class UserLoginResponseDto {
    private String jwt;
}
