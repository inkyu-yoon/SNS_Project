package likelion.sns.domain.dto.join;

import likelion.sns.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@ToString(of={"userName","password"})
public class UserJoinRequestDto {
    private String userName;
    private String password;

    public User toEntity(String password){
        return new User(this.userName, password);
    }
}
