package likelion.sns.domain.dto.write;

import likelion.sns.domain.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString(of = {"message","postId"})
public class PostWriteResponseDto {
    private String message;
    private Long postId;

    public PostWriteResponseDto(Post post) {
        this.message = "포스트 등록 완료";
        this.postId = post.getId();
    }
}
