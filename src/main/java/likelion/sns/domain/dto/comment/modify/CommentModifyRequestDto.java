package likelion.sns.domain.dto.comment.modify;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(of={"comment"})
public class CommentModifyRequestDto {
    private String comment;

}
