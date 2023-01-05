package likelion.sns.domain.dto.post.modify;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(of={"title","body"})
public class PostModifyRequestDto {
    @NotBlank
    private String title;
    @NotBlank
    private String body;

}
