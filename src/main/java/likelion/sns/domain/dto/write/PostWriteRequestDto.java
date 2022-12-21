package likelion.sns.domain.dto.write;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class PostWriteRequestDto {

    private String title;
    private String body;

}
