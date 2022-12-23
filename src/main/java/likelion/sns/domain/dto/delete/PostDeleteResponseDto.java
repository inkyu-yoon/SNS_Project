package likelion.sns.domain.dto.delete;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostDeleteResponseDto {
    private String message;
    private Long postId;

    public PostDeleteResponseDto(Long postId) {
        this.message = "포스트 삭제 완료";
        this.postId = postId;
    }
}
