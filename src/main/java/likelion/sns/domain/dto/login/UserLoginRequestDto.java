package likelion.sns.domain.dto.login;

import likelion.sns.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class UserLoginRequestDto {
    private String userName;
    private String password;

}
