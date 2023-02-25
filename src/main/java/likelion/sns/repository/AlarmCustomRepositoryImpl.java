package likelion.sns.repository;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import likelion.sns.domain.dto.alarm.AlarmListDetailsDto;
import likelion.sns.repository.custom.AlarmCustomRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import static likelion.sns.domain.entity.QAlarm.alarm;
import static likelion.sns.domain.entity.QPost.post;
import static likelion.sns.domain.entity.QUser.user;

@Repository
public class AlarmCustomRepositoryImpl implements AlarmCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public AlarmCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }


    @Override
    public List<AlarmListDetailsDto> getAlarmListByUserId(Long userId) {
        List<AlarmListDetailsDto> result = jpaQueryFactory.from(alarm)
                .where(alarm.user.id.eq(userId))
                .join(user).on(user.id.eq(alarm.fromUserId))
                .join(post).on(post.id.eq(alarm.targetId))
                .orderBy(alarm.createdAt.desc())
                .transform(GroupBy
                        .groupBy(alarm.id).list(
                                Projections.constructor(AlarmListDetailsDto.class,
                                        alarm, user.userName, post.title)));

            return result;
    }
}
