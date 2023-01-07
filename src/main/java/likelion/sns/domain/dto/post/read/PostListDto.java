package likelion.sns.domain.dto.post.read;

import likelion.sns.domain.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.text.SimpleDateFormat;

@Getter
@AllArgsConstructor
@ToString(of = {"id", "title", "body", "userName", "createdAt", "lastModifiedAt","likeNum","commentNum"})
public class PostListDto {
    private Long id;
    private String title;
    private String body;
    private String userName;
    private String createdAt;
    private String lastModifiedAt;
    private int likeNum;
    private int commentNum;
    public PostListDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.body = post.getBody();
        this.userName = post.getUser().getUserName();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        this.createdAt = sdf.format(post.getCreatedAt());

        if (post.getModifiedAt() != null) {
            this.lastModifiedAt = sdf.format(post.getModifiedAt());
        }
        this.commentNum = post.getComments().size();
        this.likeNum = post.getLikes().size();
    }
}
