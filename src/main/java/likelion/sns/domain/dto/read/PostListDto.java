package likelion.sns.domain.dto.read;

import likelion.sns.domain.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Getter
@AllArgsConstructor
public class PostListDto {
    private Long id;
    private String title;
    private String userName;
    private String createdAt;
    private String lastModifiedAt;

    public PostListDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.userName = post.getUser().getUserName();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        if (post.getModifiedAt() != null) {
            this.createdAt = sdf.format(post.getCreatedAt());
        }
        if (post.getModifiedAt() != null) {
            this.lastModifiedAt = sdf.format(post.getModifiedAt());
        }
    }
}
