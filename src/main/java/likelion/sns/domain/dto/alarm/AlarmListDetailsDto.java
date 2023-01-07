package likelion.sns.domain.dto.alarm;

import likelion.sns.domain.entity.Alarm;
import likelion.sns.domain.entity.AlarmType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.text.SimpleDateFormat;

@Getter
@AllArgsConstructor
@ToString(of = {"fromUserName", "PostName", "text"})
public class AlarmListDetailsDto {
    private String fromUserName;
    private String PostName;
    private String text;

    private String createdAt;

    public AlarmListDetailsDto(Alarm alarm, String fromUserName, String PostName) {
        this.fromUserName = fromUserName;
        this.PostName = PostName;

        if (alarm.getAlarmType().equals(AlarmType.NEW_LIKE_ON_POST)) {
            this.text = "좋아요를 눌렀습니다.";
        } else if (alarm.getAlarmType().equals(AlarmType.NEW_COMMENT_ON_POST)) {
            this.text = "댓글을 달았습니다.";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        this.createdAt = sdf.format(alarm.getCreatedAt());
    }
}
