package likelion.sns.service;

import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.alarm.AlarmListDetailsDto;
import likelion.sns.domain.dto.alarm.AlarmListDto;
import likelion.sns.domain.entity.Alarm;
import likelion.sns.domain.entity.Post;
import likelion.sns.domain.entity.User;
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
    public Page<AlarmListDto> getAlarms(String requestUserName, Pageable pageable) {
        //user 유효성 검사하고 찾아오기
        User requestUser = userValid(requestUserName);

        return alarmRepository.findByUser_IdOrderByCreatedAtDesc(requestUser.getId(), pageable).map(alarm -> new AlarmListDto(alarm));
    }

    /**
     * UI 용 메서드
     * 알림 목록 확인(상세 아이디, 게시글 제목, 알림 형태)
     */
    public Page<AlarmListDetailsDto> getDetailAlarms(String requestUserName, Pageable pageable) {
        //user 유효성 검사하고 찾아오기
        User requestUser = userValid(requestUserName);

        Page<Alarm> alarms = alarmRepository.findByUser_IdOrderByCreatedAtDesc(requestUser.getId(), pageable);
        List<AlarmListDetailsDto> alarmsDto = new ArrayList<>();


        //post id와 user id가 아닌 post title 과 userName 을 넣기 위해..for Each 사용
        for (Alarm alarm : alarms) {
            Long fromUserId = alarm.getFromUserId();
            Long postId = alarm.getTargetId();

            String fromUserName = userRepository.findById(fromUserId)
                    .orElseThrow(() -> new SNSAppException(ErrorCode.USERNAME_NOT_FOUND)).getUserName();

            String title = postValid(postId).getTitle();

            alarmsDto.add(new AlarmListDetailsDto(alarm, fromUserName, title));

        }

        return new PageImpl<>(alarmsDto);
    }


    /**
     * UI 용 메서드
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

    /**
     * 해당하는 게시글이 없을 시, 그리고 deletedAt 데이터가 있을 시(삭제 처리 된 것으로 볼 수 있으므로) 예외 처리
     */
    public Post postValid(Long postId) {

        //DB에 저장되어 있는 게시글이 없는 경우
        Post foundPost = postRepository.findById(postId)
                .orElseThrow(() -> new SNSAppException(ErrorCode.POST_NOT_FOUND));

        // deletedAt 에 데이터가 채워져서 삭제 처리가 된 경우
        if (foundPost.getDeletedAt() != null) {
            throw new SNSAppException(ErrorCode.POST_NOT_FOUND);
        }

        return foundPost;
    }

}
