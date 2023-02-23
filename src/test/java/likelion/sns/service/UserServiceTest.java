package likelion.sns.service;


import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.user.changeRole.UserRoleChangeRequestDto;
import likelion.sns.domain.dto.user.join.UserJoinRequestDto;
import likelion.sns.domain.dto.user.login.UserLoginRequestDto;
import likelion.sns.domain.entity.User;
import likelion.sns.jwt.JwtTokenUtil;
import likelion.sns.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder encoder;

    @Mock
    private User mockUser;

    @InjectMocks
    private UserService userService;


    @Nested
    @DisplayName("회원가입 테스트")
    class joinTest {

        @Mock
        UserJoinRequestDto mockRequestDto;


        /**
         * 회원 가입 성공 테스트
         */
        @Test
        @DisplayName("회원 가입 성공 테스트")
        void joinSuccess() throws SQLException {

            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.empty());
            given(userRepository.save(any()))
                    .willReturn(mockUser);


            assertDoesNotThrow(() -> userService.createUser(new UserJoinRequestDto("userName", "encodedPassword")));
        }

        /**
         * 회원 가입 실패 테스트 (가입 요청한 아이디가 이미 존재할 시)
         */
        @Test
        @DisplayName("회원 가입 실패 테스트 (가입 요청한 아이디가 이미 존재할 시)")
        void joinError1() throws SQLException {

            given(mockRequestDto.getUserName())
                    .willReturn("name");

            when(userRepository.findByUserName(any()))
                    .thenReturn(Optional.of(mockUser));

            SNSAppException exception = assertThrows(SNSAppException.class, () -> userService.createUser(mockRequestDto));

            assertThat(exception.getErrorCode().getMessage())
                    .isEqualTo("UserName이 중복됩니다.");
        }

    }

    @Nested
    @DisplayName("회원 로그인 테스트")
    class LoginTest {

        @Mock
        UserLoginRequestDto requestDto;

        /**
         * 로그인 성공
         */
        @Test
        @DisplayName("로그인 성공 테스트")
        void loginSuccess() {
            MockedStatic<JwtTokenUtil> jwtTokenUtilMockedStatic = mockStatic(JwtTokenUtil.class);

            given(requestDto.getUserName())
                    .willReturn("name");
            given(requestDto.getPassword())
                    .willReturn("password");
            given(userRepository.findByUserName(any()))
                    .willReturn(Optional.of(mockUser));
            given(encoder.matches(any(), any()))
                    .willReturn(true);
            given(JwtTokenUtil.createToken(any(), eq("secret")))
                    .willReturn("jwt token");

            assertDoesNotThrow(() -> userService.loginUser(requestDto));

            jwtTokenUtilMockedStatic.close();
        }

        /**
         * 로그인 실패 테스트 (userName으로 가입된 회원을 찾을 수 없는 경우)
         */

        @Test
        @DisplayName("로그인 실패 테스트 (userName으로 가입된 회원을 찾을 수 없는 경우)")
        void loginError1() {

            when(userRepository.findByUserName(any()))
                    .thenReturn(Optional.empty());

            assertThrows(SNSAppException.class, () -> userService.loginUser(requestDto));

        }

        /**
         * 로그인 실패 테스트 (비밀번호가 일치하지 않는 경우)
         */

        @Test
        @DisplayName("로그인 실패 테스트 (비밀번호가 일치하지 않는 경우)")
        void loginError2() {
            given(requestDto.getUserName())
                    .willReturn("name");
            given(requestDto.getPassword())
                    .willReturn("password");
            given(userRepository.findByUserName(any()))
                    .willReturn(Optional.of(mockUser));

            when(encoder.matches(any(), any()))
                    .thenReturn(false);


            SNSAppException exception = assertThrows(SNSAppException.class, () -> userService.loginUser(requestDto));

            assertThat(exception.getErrorCode().getMessage())
                    .isEqualTo("패스워드가 잘못되었습니다.");

        }
    }

    @Nested
    @DisplayName("권한 변경 테스트")
    class changeRole {

        @Mock
        UserRoleChangeRequestDto requestDto;

        /**
         * 권한 변경 성공 테스트
         */
        @Test
        @DisplayName("권한 변경 성공 테스트")
        void changeRoleSuccess() {

            given(userRepository.findById(1L))
                    .willReturn(Optional.of(mockUser));

            assertDoesNotThrow(() -> userService.changeRole(1L));
        }

        /**
         * 권한 변경 실패 테스트
         */
        @Test
        @DisplayName("권한 변경 실패 테스트")
        void changeRoleError1() {

            when(userRepository.findById(any()))
                    .thenReturn(Optional.empty());

            SNSAppException exception = assertThrows(SNSAppException.class, () -> userService.changeRole(any()));
            assertThat(exception.getErrorCode().getMessage())
                    .isEqualTo("해당하는 유저를 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("JwtTokenFilter용 findRole 메서드 테스트")
    class findRoleTest {
        /**
         * 회원 등급 찾아오기 성공
         */
        @Test
        @DisplayName("회원 등급 찾아오기 성공")
        void findRoleSuccess() {
            given(userRepository.findByUserName(any()))
                    .willReturn(Optional.of(mockUser));

            assertDoesNotThrow(() -> userService.findRoleByUserName(any()));
        }
    }
}