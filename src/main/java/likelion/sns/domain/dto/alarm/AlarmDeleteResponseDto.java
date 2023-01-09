package likelion.sns.domain.dto.alarm;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString(of={"message","id"})
public class AlarmDeleteResponseDto {
    private String message;
    private Long id;

    public AlarmDeleteResponseDto(Long alarmId) {
        this.message = "알림 삭제 완료";
        this.id = alarmId;
    }
}
