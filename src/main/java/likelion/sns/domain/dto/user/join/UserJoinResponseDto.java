package likelion.sns.domain.dto.user.join;

import likelion.sns.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString(of={"userId","userName"})
public class UserJoinResponseDto {
    private Long userId;
    private String userName;

    public UserJoinResponseDto(User user) {
        this.userId = user.getId();
        this.userName = user.getUserName();
    }
}
