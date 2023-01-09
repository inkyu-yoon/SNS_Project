package likelion.sns.domain.dto.comment.read;

import com.fasterxml.jackson.annotation.JsonIgnore;
import likelion.sns.domain.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CommentListDto {

    private Long id;
    private String comment;
    private String userName;
    private Long postId;
    private String createdAt;
    private String lastModifiedAt;

    private List<CommentListDto> replys ;

    private int replysSize =0;

    @JsonIgnore
    private String isModified;

    public CommentListDto(Comment comment) {
        this.id = comment.getId();
        this.comment = comment.getComment();
        this.userName = comment.getUser().getUserName();
        this.postId = comment.getPost().getId();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        this.createdAt = sdf.format(comment.getCreatedAt());
        this.lastModifiedAt = sdf.format(comment.getModifiedAt());
        if (!createdAt.equals(lastModifiedAt)) {
            this.isModified = "(수정됨)";
        }
    }
}
