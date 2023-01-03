package likelion.sns.domain.dto.comment.modify;

import likelion.sns.domain.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.text.SimpleDateFormat;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(of = {"id","comment","userName","postId","createdAt","modifiedAt"})
public class CommentModifyResponseDto {
    private Long id;
    private String comment;
    private String userName;
    private Long postId;
    private String createdAt;
    private String modifiedAt;

    public CommentModifyResponseDto(Comment comment, String userName, Long postId) {
        this.id = comment.getId();
        this.comment = comment.getComment();
        this.userName = userName;
        this.postId = postId;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        this.createdAt = sdf.format(comment.getCreatedAt());
        this.modifiedAt = sdf.format(comment.getModifiedAt());
    }
}
