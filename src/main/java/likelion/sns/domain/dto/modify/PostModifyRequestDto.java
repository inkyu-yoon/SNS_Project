package likelion.sns.domain.dto.modify;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PostModifyRequestDto {
    private String title;
    private String body;

}
