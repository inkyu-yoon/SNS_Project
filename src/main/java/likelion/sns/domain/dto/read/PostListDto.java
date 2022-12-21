package likelion.sns.domain.dto.read;

import likelion.sns.domain.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PostListDto {
    private Long id;
    private String title;
    private String userName;
    private Timestamp createdAt;

    public PostListDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.userName = post.getUser().getUserName();
        this.createdAt = post.getCreatedAt();
    }
}
