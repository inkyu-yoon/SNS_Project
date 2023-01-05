package likelion.sns.domain.dto.user.changeRole;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString(of={"role"})
public class UserRoleChangeRequestDto {
    @NotBlank
    private String role;
}
