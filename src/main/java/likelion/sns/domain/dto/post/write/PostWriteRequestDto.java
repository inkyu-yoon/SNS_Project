package likelion.sns.domain.dto.post.write;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@ToString(of = {"title","body"})
public class PostWriteRequestDto {
    @NotBlank
    private String title;
    @NotBlank
    private String body;

}
