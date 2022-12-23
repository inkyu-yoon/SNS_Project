package likelion.sns.domain.dto.write;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@ToString(of = {"title","body"})
public class PostWriteRequestDto {

    private String title;
    private String body;

}
