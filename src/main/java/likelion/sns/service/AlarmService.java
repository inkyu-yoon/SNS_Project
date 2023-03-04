package likelion.sns.service;

import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.alarm.AlarmListDetailsDto;
import likelion.sns.domain.entity.Alarm;
import likelion.sns.domain.entity.Post;
import likelion.sns.domain.entity.User;
import likelion.sns.repository.AlarmCustomRepositoryImpl;
import likelion.sns.repository.AlarmRepository;
import likelion.sns.repository.PostRepository;
import likelion.sns.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;


    /**
     * 알림 목록 확인
     */
    public List<AlarmListDetailsDto> getAlarms(String requestUserName) {
        //user 유효성 검사하고 찾아오기
        User requestUser = userValid(requestUserName);

        List<AlarmListDetailsDto> result = alarmRepository.getAlarmListByUserId(requestUser.getId());
        log.info("🔔알림 조회 끝 userName : {}");
        return result;
    }
    /**
     *
     * 알림 삭제 (헤더에서 ✔확인 체크시)
     */
    @Transactional
    public void deleteAlarm(String requestUserName, Long alarmId) {

        userValid(requestUserName);

        Alarm foundAlarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new SNSAppException(ErrorCode.ALARM_NOT_FOUND));

        alarmRepository.delete(foundAlarm);
    }

    /**
     * 알림 삭제 (게시글이 soft delete되면, 해당 포스트에 대한 알림을 모두 삭제)
     */
    @Transactional
    public void deleteAlarmWithPost(Long postId) {
        alarmRepository.deleteAlarmWithPost(postId);
    }



    /*
    아래 메서드는 유효성 검사 및 중복 메서드 정리
     */

    /**
     * 해당하는 회원이 없을 시, 예외 처리
     */
    public User userValid(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new SNSAppException(ErrorCode.USERNAME_NOT_FOUND));
    }

}
