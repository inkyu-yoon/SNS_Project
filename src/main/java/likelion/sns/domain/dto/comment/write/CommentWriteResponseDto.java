package likelion.sns.domain.dto.comment.write;

import likelion.sns.domain.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.text.SimpleDateFormat;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@ToString(of = {"id","comment","userName","postId","createdAt"})
public class CommentWriteResponseDto {
    private Long id;
    private String comment;
    private String userName;
    private Long postId;
    private String createdAt;

    public CommentWriteResponseDto(Comment comment,String userName, Long postId) {
        this.id = comment.getId();
        this.comment = comment.getComment();
        this.userName = userName;
        this.postId = postId;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        this.createdAt = sdf.format(comment.getCreatedAt());
    }

}

