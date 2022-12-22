package likelion.sns.domain.dto.modify;

import likelion.sns.domain.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PostModifyResponseDto {
    private String message;
    private Long postId;

    public PostModifyResponseDto(Post post) {
        this.message = "포스트 수정 완료";
        this.postId = post.getId();
    }
}
