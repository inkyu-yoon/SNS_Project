package likelion.sns.domain.dto.alarm;

import likelion.sns.domain.entity.Alarm;
import likelion.sns.domain.entity.AlarmType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.text.SimpleDateFormat;

@Getter
@AllArgsConstructor
@ToString(of = {"id", "title", "body", "userName", "createdAt", "lastModifiedAt"})
public class AlarmListDto {
    private Long id;
    private AlarmType alarmType;
    private Long fromUserId;
    private Long targetId;
    private String text;
    private String createdAt;

    public AlarmListDto(Alarm alarm) {
        this.id = alarm.getId();
        this.alarmType = alarm.getAlarmType();
        this.fromUserId = alarm.getFromUserId();
        this.targetId = alarm.getTargetId();
        if (alarm.getAlarmType().equals(AlarmType.NEW_COMMENT_ON_POST)) {
            this.text = "new comment!";
        } else if (alarm.getAlarmType().equals(AlarmType.NEW_LIKE_ON_POST)) {
            this.text = "new like!";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        this.createdAt = sdf.format(alarm.getCreatedAt());

    }


}
