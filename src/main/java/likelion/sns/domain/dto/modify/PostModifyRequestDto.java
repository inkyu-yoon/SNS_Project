package likelion.sns.domain.dto.modify;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(of={"title","body"})
public class PostModifyRequestDto {
    private String title;
    private String body;

}
