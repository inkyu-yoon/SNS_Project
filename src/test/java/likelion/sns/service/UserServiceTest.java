package likelion.sns.service;

import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.user.changeRole.UserRoleChangeRequestDto;
import likelion.sns.domain.dto.user.join.UserJoinRequestDto;
import likelion.sns.domain.dto.user.login.UserLoginRequestDto;
import likelion.sns.domain.entity.User;
import likelion.sns.jwt.JwtTokenUtil;
import likelion.sns.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mockStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceTest {

    UserService userService;

    UserRepository userRepository = mock(UserRepository.class);

    BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);

    User mockUser = mock(User.class);

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, encoder);
    }

    /**
     * 회원 가입 테스트
     */
    @Nested
    @DisplayName("회원 가입 테스트")
    class UserJoinTest {

        UserJoinRequestDto mockUserJoinRequestDto = mock(UserJoinRequestDto.class);

        /**
         * 회원 가입 성공
         */
        @Test
        @DisplayName("회원 가입 성공")
        void userJoinSuccess() {

            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.empty());
            given(encoder.encode(any()))
                    .willReturn("encodedPassword");
            given(userRepository.save(any()))
                    .willReturn(mockUser);


            assertDoesNotThrow(() -> userService.createUser(new UserJoinRequestDto("userName", "encodedPassword")));
        }

        /**
         * 회원가입 실패 ( db에 이미 가입 요청한 아이디가 존재할 시 (중복 아이디 에러)
         */
        @Test
        @DisplayName("회원가입 실패 ( db에 이미 가입 요청한 아이디가 존재할 시 (중복 아이디 에러)")
        void userJoinError1() {

            given(mockUserJoinRequestDto.getUserName())
                    .willReturn("userName");


            when(userRepository.findByUserName("userName"))
                    .thenThrow(new SNSAppException(ErrorCode.DUPLICATED_USER_NAME));


            SNSAppException snsAppException = assertThrows(SNSAppException.class, () -> userService.createUser(mockUserJoinRequestDto));

            assertThat(snsAppException.getErrorCode().getHttpStatus())
                    .isEqualTo(HttpStatus.CONFLICT);
            assertThat(snsAppException.getErrorCode().getMessage())
                    .isEqualTo("UserName이 중복됩니다.");
        }

        /**
         * 회원가입 실패 ( db 연결 에러)
         */
        @Test
        @DisplayName("회원가입 실패 ( db 연결 에러)")
        void userJoinError2() {

            given(mockUserJoinRequestDto.getUserName())
                    .willReturn("userName");


            when(userRepository.findByUserName("userName"))
                    .thenThrow(new SNSAppException(ErrorCode.DATABASE_ERROR));


            SNSAppException snsAppException = assertThrows(SNSAppException.class, () -> userService.createUser(mockUserJoinRequestDto));

            assertThat(snsAppException.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(snsAppException.getErrorCode().getMessage()).isEqualTo("DB 에러");
        }
    }

    /**
     * 회원 로그인 테스트
     */
    @Nested
    @DisplayName("회원 로그인 테스트")
    class UserLoginTest {

        UserLoginRequestDto mockUserLoginRequestDto = mock(UserLoginRequestDto.class);

        /**
         * 회원 로그인 성공
         */
        @Test
        @DisplayName("회원 로그인 성공")
        void userLoginSuccess() {
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(encoder.matches(any(), any()))
                    .willReturn(true);
            given(userRepository.save(any()))
                    .willReturn(mockUser);


            MockedStatic<JwtTokenUtil> jwtTokenUtilMockedStatic = mockStatic(JwtTokenUtil.class);
            jwtTokenUtilMockedStatic
                    .when(() -> JwtTokenUtil.createToken("userName", "secretKey"))
                    .thenReturn("token");

            assertDoesNotThrow(() -> userService.loginUser(new UserLoginRequestDto("userName", "password")));
        }


        /**
         * 회원 로그인 실패 (가입된 적이 없는 경우)
         */
        @Test
        @DisplayName("회원 로그인 실패 (가입된 적이 없는 경우)")
        void userLoginError1() {


            given(mockUserLoginRequestDto.getUserName())
                    .willReturn("userName");
            given(mockUserLoginRequestDto.getPassword())
                    .willReturn("password");


            when(userRepository.findByUserName("userName"))
                    .thenThrow(new SNSAppException(ErrorCode.USERNAME_NOT_FOUND));


            SNSAppException snsAppException = assertThrows(SNSAppException.class, () -> userService.loginUser(mockUserLoginRequestDto));

            assertThat(snsAppException.getErrorCode().getHttpStatus())
                    .isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(snsAppException.getErrorCode().getMessage())
                    .isEqualTo("해당하는 유저를 찾을 수 없습니다.");
        }

        /**
         * 회원 로그인 실패 (가입된 회원이지만, 비밀번호가 일치하지 않는 경우)
         */
        @Test
        @DisplayName("회원 로그인 실패 (가입된 회원이지만, 비밀번호가 일치하지 않는 경우)")
        void userLoginError2() {


            given(mockUserLoginRequestDto.getUserName())
                    .willReturn("userName");
            given(mockUserLoginRequestDto.getPassword())
                    .willReturn("password");
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));


            when(encoder.matches("password", mockUser.getPassword()))
                    .thenReturn(false);


            SNSAppException snsAppException = assertThrows(SNSAppException.class, () -> userService.loginUser(mockUserLoginRequestDto));

            assertThat(snsAppException.getErrorCode().getHttpStatus())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(snsAppException.getErrorCode().getMessage())
                    .isEqualTo("패스워드가 잘못되었습니다.");
        }


        /**
         * 회원 로그인 실패 (DB에러)
         */
        @Test
        @DisplayName("회원 로그인 실패 (DB에러)")
        void userLoginError3() {


            given(mockUserLoginRequestDto.getUserName())
                    .willReturn("userName");
            given(mockUserLoginRequestDto.getPassword())
                    .willReturn("password");

            when(userRepository.findByUserName("userName"))
                    .thenThrow(new SNSAppException(ErrorCode.DATABASE_ERROR));


            SNSAppException snsAppException = assertThrows(SNSAppException.class, () -> userService.loginUser(mockUserLoginRequestDto));

            assertThat(snsAppException.getErrorCode().getHttpStatus())
                    .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(snsAppException.getErrorCode().getMessage())
                    .isEqualTo("DB 에러");
        }
    }

    /**
     * 회원 등급 변경 테스트
     */
    @Nested
    @DisplayName("회원 등급 변경 테스트")
    class UserChangeRoleTest {

        UserRoleChangeRequestDto userRoleChangeRequestDto = mock(UserRoleChangeRequestDto.class);

        UserLoginRequestDto mockUserLoginRequestDto = mock(UserLoginRequestDto.class);

        String mockRole = mock(String.class);
        /**
         * 회원 등급 변경 성공
         */
        @Test
        @DisplayName("회원 등급 변경 성공")
        void userChangeRoleSuccess() {
            given(userRoleChangeRequestDto.getRole()).willReturn("admin");

            given(userRepository.findById(any()))
                    .willReturn(Optional.of(mockUser));

            assertDoesNotThrow(() -> userService.changeRole(1L,userRoleChangeRequestDto));
        }

    }
}