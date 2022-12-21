package likelion.sns.domain.dto.join;

import likelion.sns.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserJoinResponseDto {
    private Long userId;
    private String userName;

    public UserJoinResponseDto(User user) {
        this.userId = user.getId();
        this.userName = user.getUserName();
    }
}
