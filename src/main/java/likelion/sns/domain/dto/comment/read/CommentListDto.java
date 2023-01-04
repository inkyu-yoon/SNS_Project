package likelion.sns.domain.dto.comment.read;

import likelion.sns.domain.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;

@Getter
@AllArgsConstructor
public class CommentListDto {

    private Long commentId;
    private String comment;
    private String userName;
    private Long postId;
    private String createdAt;
    public CommentListDto(Comment comment) {
        this.commentId = comment.getId();
        this.comment = comment.getComment();
        this.userName = comment.getUser().getUserName();
        this.postId = comment.getPost().getId();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        this.createdAt = sdf.format(comment.getCreatedAt());
    }
}
