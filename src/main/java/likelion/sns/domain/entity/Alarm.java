package likelion.sns.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Where(clause = "deleted_at is null")
@SQLDelete(sql = "UPDATE alarm SET deleted_at = now() WHERE alarm_id = ?")
public class Alarm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id")
    private Long id;

    //알림을 받은 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;

    // 알림을 일으킨 사람의 아이디
    private Long fromUserId;

    // 알림이 일어난 포스트 아이디
    private Long targetId;

    /**
     * 정적 팩토리 메서드 용 생성자
     */

    public Alarm(User user, AlarmType alarmType, Long fromUserId, Long targetId) {
        this.user = user;
        this.alarmType = alarmType;
        this.fromUserId = fromUserId;
        this.targetId = targetId;
    }

    public static Alarm createAlarm(User user, AlarmType alarmType, Long fromUserId, Long targetId) {
        return new Alarm(user, alarmType, fromUserId, targetId);

    }
}
