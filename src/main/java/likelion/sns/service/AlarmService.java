package likelion.sns.service;

import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.alarm.AlarmListDto;
import likelion.sns.domain.entity.User;
import likelion.sns.repository.AlarmRepository;
import likelion.sns.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;

    /**
     * 알람 목록 확인
     */
    public Page<AlarmListDto> getAlarms(String requestUserName, Pageable pageable) {
        //user 유효성 검사하고 찾아오기
        User requestUser = userValid(requestUserName);

        Long requestUserId = requestUser.getId();

        return alarmRepository.findByUser_IdOrderByCreatedAtDesc(requestUserId, pageable).map(alarm -> new AlarmListDto(alarm));
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
