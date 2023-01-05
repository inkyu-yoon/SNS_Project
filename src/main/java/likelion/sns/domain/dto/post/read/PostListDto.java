package likelion.sns.domain.dto.post.read;

import likelion.sns.domain.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.text.SimpleDateFormat;

@Getter
@Setter
@AllArgsConstructor
@ToString(of = {"id", "title", "body", "userName", "createdAt", "lastModifiedAt"})
public class PostListDto {
    private Long id;
    private String title;
    private String body;
    private String userName;
    private String createdAt;
    private String lastModifiedAt;
    private Long likeNum;

    public PostListDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.body = post.getBody();
        this.userName = post.getUser().getUserName();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

        this.createdAt = sdf.format(post.getCreatedAt());

        if (post.getModifiedAt() != null) {
            this.lastModifiedAt = sdf.format(post.getModifiedAt());
        }
    }
}
