package likelion.sns.domain.dto.changeRole;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString(of={"role"})
public class UserRoleChangeRequestDto {
    private String role;
}
