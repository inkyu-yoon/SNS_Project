package likelion.sns.domain.dto.comment.write;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@ToString(of = {"replyComment"})
public class ReplyCommentWriteRequestDto {
    @NotBlank
    private String replyComment;

}
