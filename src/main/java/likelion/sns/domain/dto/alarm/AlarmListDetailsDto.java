package likelion.sns.domain.dto.alarm;

import likelion.sns.domain.entity.Alarm;
import likelion.sns.domain.entity.AlarmType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.text.SimpleDateFormat;

@Getter
@Setter
@AllArgsConstructor
@ToString(of = {"alarmId", "fromUserName", "postName", "postId", "text"})
public class AlarmListDetailsDto {

    private Long alarmId;
    private String fromUserName;
    private String postName;
    private Long postId;
    private String text;

    private String createdAt;

    public AlarmListDetailsDto(Alarm alarm, String fromUserName, String postName) {
        this.alarmId = alarm.getId();
        this.fromUserName = fromUserName;
        this.postName = postName;
        this.postId = alarm.getTargetId();

        if (alarm.getAlarmType().equals(AlarmType.NEW_LIKE_ON_POST)) {
            this.text = "좋아요를 눌렀습니다.";
        } else if (alarm.getAlarmType().equals(AlarmType.NEW_COMMENT_ON_POST)) {
            this.text = "댓글을 달았습니다.";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        this.createdAt = sdf.format(alarm.getCreatedAt());
    }
}
