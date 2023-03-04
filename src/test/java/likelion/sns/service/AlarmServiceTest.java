package likelion.sns.service;

import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.alarm.AlarmListDetailsDto;
import likelion.sns.domain.entity.Alarm;
import likelion.sns.domain.entity.User;
import likelion.sns.repository.AlarmCustomRepositoryImpl;
import likelion.sns.repository.AlarmRepository;
import likelion.sns.repository.PostRepository;
import likelion.sns.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AlarmServiceTest {

    @Mock
    private AlarmRepository alarmRepository;


    @Mock
    private AlarmCustomRepositoryImpl alarmCustomRepository;
    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private User mockUser;

    @Mock
    private Alarm mockAlarm;

    @Mock
    private AlarmListDetailsDto mockDetailAlarm;

    @InjectMocks
    private AlarmService alarmService;

    @Nested
    @DisplayName("알림 목록 확인 테스트")
    class GetAlarmTest{

        /**
         * 알림 목록 확인 성공 테스트
         */
        @Test
        @DisplayName("알림 목록 확인 성공 테스트")
        void getAlarmSuccess(){
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(alarmRepository.getAlarmListByUserId(any()))
                    .willReturn(List.of(mockDetailAlarm));

            assertDoesNotThrow(()->alarmService.getAlarms("userName"));
        }

        /**
         * 알림 목록 확인 실패 테스트 (알림을 확인한 사용자가 존재하지 않는 경우)
         */
        @Test
        @DisplayName("알림 목록 확인 실패 테스트 (알림을 확인한 사용자가 존재하지 않는 경우)")
        void getAlarmError1(){

            when(userRepository.findByUserName("userName"))
                    .thenReturn(Optional.empty());

            SNSAppException exception = assertThrows(SNSAppException.class, () -> alarmService.getAlarms("userName"));
            assertThat(exception.getErrorCode().getMessage()).isEqualTo("해당하는 유저를 찾을 수 없습니다.");

        }
    }

    @Nested
    @DisplayName("알림 삭제 테스트")
    class DeleteAlarmTest{
        /**
         * 알림 단건 삭제 성공 테스트
         */
        @Test
        @DisplayName("알림 단건 삭제 성공 테스트")
        void deleteAlarmSuccess(){
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(alarmRepository.findById(any()))
                    .willReturn(Optional.of(mockAlarm));

            assertDoesNotThrow(() -> alarmService.deleteAlarm("userName", any()));

        }

        /**
         * 알림 단건 삭제 실패 테스트 (알림 삭제 요청한 사용자가 존재하지 않는 경우)
         */
        @Test
        @DisplayName("알림 단건 실패 테스트 (알림 삭제 요청한 사용자가 존재하지 않는 경우)")
        void deleteAlarmError1(){

            when(userRepository.findByUserName("userName"))
                    .thenReturn(Optional.empty());

            SNSAppException exception = assertThrows(SNSAppException.class, () -> alarmService.deleteAlarm("userName",any()));
            assertThat(exception.getErrorCode().getMessage()).isEqualTo("해당하는 유저를 찾을 수 없습니다.");

        }

        /**
         * 알림 단건 삭제 실패 테스트 (알림 삭제 요청한 알림이 존재하지 않는 경우)
         */
        @Test
        @DisplayName("알림 단건 실패 테스트 (알림 삭제 요청한 알림이 존재하지 않는 경우)")
        void deleteAlarmError2(){

            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));

            when(alarmRepository.findById(any()))
                    .thenReturn(Optional.empty());

            SNSAppException exception = assertThrows(SNSAppException.class, () -> alarmService.deleteAlarm("userName",any()));
            assertThat(exception.getErrorCode().getMessage()).isEqualTo("해당 알림이 없습니다.");

        }

        /**
         * 알림 전체 삭제 성공 테스트
         */
        @Test
        @DisplayName("알림 전체 삭제 성공 테스트")
        void deleteAllAlarmSuccess(){
            assertDoesNotThrow(() -> alarmService.deleteAlarmWithPost(any()));
        }
    }
}