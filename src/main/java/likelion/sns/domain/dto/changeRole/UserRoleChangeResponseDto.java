package likelion.sns.domain.dto.changeRole;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString(of={"userId","message"})
public class UserRoleChangeResponseDto {
    private Long userId;
    private String message;
}
