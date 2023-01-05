package likelion.sns.domain.dto.comment.modify;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(of={"comment"})
public class CommentModifyRequestDto {
    @NotBlank
    private String comment;

}
