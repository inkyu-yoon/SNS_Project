package likelion.sns.domain.dto.user.login;

import likelion.sns.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@ToString(of={"userName","password"})
public class UserLoginRequestDto {
    private String userName;
    private String password;

}
