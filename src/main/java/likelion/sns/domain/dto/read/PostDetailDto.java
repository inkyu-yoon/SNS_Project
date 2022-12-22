package likelion.sns.domain.dto.read;


import likelion.sns.domain.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.text.SimpleDateFormat;

@AllArgsConstructor
@Getter
public class PostDetailDto {
    private Long id;
    private String title;
    private String body;
    private String userName;
    private String createdAt;
    private String lastModifiedAt;

    public PostDetailDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.body = post.getBody();
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
