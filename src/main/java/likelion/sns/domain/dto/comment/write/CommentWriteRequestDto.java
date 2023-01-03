package likelion.sns.domain.dto.comment.write;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.text.SimpleDateFormat;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@ToString(of = {"comment"})
public class CommentWriteRequestDto {
    private String comment;
}
