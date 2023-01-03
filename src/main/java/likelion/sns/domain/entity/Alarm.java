package likelion.sns.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
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

    public Alarm(User user, AlarmType alarmType, Long fromUserId, Long targetId) {
        this.user = user;
        this.alarmType = alarmType;
        this.fromUserId = fromUserId;
        this.targetId = targetId;
    }
}
