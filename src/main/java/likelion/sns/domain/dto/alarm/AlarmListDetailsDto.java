package likelion.sns.domain.dto.alarm;

import likelion.sns.domain.entity.AlarmType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString(of = {"fromUserName", "PostName", "text"})
public class AlarmListDetailsDto {
    private String fromUserName;
    private String PostName;
    private String text;

    public AlarmListDetailsDto(AlarmType alarmType, String fromUserName, String PostName) {
        this.fromUserName = fromUserName;
        this.PostName = PostName;

        if (alarmType.equals(AlarmType.NEW_LIKE_ON_POST)) {
            this.text = "좋아요를 눌렀습니다.";
        } else if (alarmType.equals(AlarmType.NEW_COMMENT_ON_POST)) {
            this.text = "댓글을 달았습니다.";
        }
    }
}
