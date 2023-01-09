package likelion.sns.domain.dto.user.join;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@ToString(of={"userName","password"})
public class UserJoinRequestDto {
    @NotBlank
    private String userName;
    @NotBlank
    private String password;

}
