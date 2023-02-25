package likelion.sns.repository.custom;

import likelion.sns.domain.dto.alarm.AlarmListDetailsDto;
import likelion.sns.domain.entity.Alarm;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface AlarmCustomRepository {
    List<AlarmListDetailsDto> getAlarmListByUserId(Long userId);
}
