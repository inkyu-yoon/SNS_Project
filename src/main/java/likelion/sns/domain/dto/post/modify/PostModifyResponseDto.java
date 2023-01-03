package likelion.sns.domain.dto.post.modify;

import likelion.sns.domain.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(of ={"message","postId"})
public class PostModifyResponseDto {
    private String message;
    private Long postId;

    public PostModifyResponseDto(Long postId) {
        this.message = "포스트 수정 완료";
        this.postId = postId;
    }
}
