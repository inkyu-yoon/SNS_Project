package likelion.sns.controller.restController;

import com.google.gson.Gson;
import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.configuration.SecurityConfig;
import likelion.sns.domain.dto.user.changeRole.UserRoleChangeRequestDto;
import likelion.sns.domain.dto.user.join.UserJoinRequestDto;
import likelion.sns.domain.dto.user.join.UserJoinResponseDto;
import likelion.sns.domain.dto.user.login.UserLoginRequestDto;
import likelion.sns.domain.dto.user.login.UserLoginResponseDto;
import likelion.sns.domain.entity.UserRole;
import likelion.sns.jwt.JwtTokenUtil;
import likelion.sns.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserRestController.class)
@Import({SecurityConfig.class})
class UserRestControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired WebApplicationContext wac;
    @Autowired Gson gson;

    @Value("${jwt.token.secret}")
    String secretKey;
    @MockBean UserService userService;

    @MockBean BindingResult br;

    UserJoinRequestDto userJoinRequestDto = new UserJoinRequestDto("윤인규", "password");
    UserLoginRequestDto userLoginRequestDto = new UserLoginRequestDto("윤인규", "1234");


    /**
     * SecurityConfig 로 설정한 Filter Chain 적용
     */

    @BeforeEach
    public void setUpMockMvc() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // 등급 변경 기능 테스트

    }



    /**
     * 회원가입 테스트
     */

    @Nested
    @DisplayName("회원가입 테스트")
    public class JoinTest {
        String content = gson.toJson(userJoinRequestDto);
        UserJoinRequestDto nullUserJoinRequestDto = new UserJoinRequestDto("", "password");

        /**
         * 회원가입 성공 테스트
         */
        @Test
        @DisplayName("회원가입 (join) 성공 테스트")
        void joinSuccess() throws Exception {


            Mockito.when(userService.createUser(any()))
                    .thenReturn(new UserJoinResponseDto(1L, userLoginRequestDto.getUserName()));

            mockMvc.perform(post("/api/v1/users/join")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.userId").value(1))
                    .andExpect(jsonPath("$.result.userName").value(userLoginRequestDto.getUserName()));
        }

        /**
         * 회원가입 에러 테스트 (중복 회원이 존재하는 경우)
         */

        @Test
        @DisplayName("회원가입 중복 에러 테스트")
        void joinError1() throws Exception {

            Mockito.when(userService.createUser(any()))
                    .thenThrow(new SNSAppException(ErrorCode.DUPLICATED_USER_NAME));

            mockMvc.perform(post("/api/v1/users/join")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("utf-8")
                            .content(content))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.errorCode").value("DUPLICATED_USER_NAME"))
                    .andExpect(jsonPath("$.result.message").value("UserName이 중복됩니다."));
        }

    }

    /**
     * 로그인 테스트
     */

    @Nested
    @DisplayName("로그인 테스트")
    public class LoginTest {

        String content = gson.toJson(userLoginRequestDto);

        /**
         * 로그인 성공 테스트
         */
        @Test
        @DisplayName("회원 로그인 성공 테스트")
        void loginSuccess() throws Exception {

            String token = "randomToken";

            Mockito.when(userService.loginUser(any()))
                    .thenReturn(new UserLoginResponseDto(token));

            mockMvc.perform(post("/api/v1/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content))
                    .andDo(print())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.jwt").exists())
                    .andExpect(jsonPath("$.result.jwt").value(token));

        }


        /**
         * 로그인 실패 테스트 (저장되어 있는 UserName 없음)
         */
        @Test
        @DisplayName("회원 로그인 실패 테스트 (저장되어 있는 UserName 없음)")
        void LoginNotFound() throws Exception {

            Mockito.when(userService.loginUser(any()))
                    .thenThrow(new SNSAppException(ErrorCode.USERNAME_NOT_FOUND));

            mockMvc.perform(post("/api/v1/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.errorCode").value("USERNAME_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.message").value("해당하는 유저를 찾을 수 없습니다."));

        }

        /**
         * 로그인 실패 테스트 (비밀번호가 일치하지 않음)
         */
        @Test
        @DisplayName("회원 로그인 실패 테스트 (Invalid password)")
        void LoginInValid() throws Exception {

            Mockito.when(userService.loginUser(any()))
                    .thenThrow(new SNSAppException(ErrorCode.INVALID_PASSWORD));

            mockMvc.perform(post("/api/v1/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.errorCode").value("INVALID_PASSWORD"))
                    .andExpect(jsonPath("$.result.message").value("패스워드가 잘못되었습니다."));

        }
    }


    /**
     *  관리자 회원 등급 변경 기능 테스트
     */

    @Nested
    @DisplayName("로그인 테스트")
    public class ChangeRoleTest {

        UserRoleChangeRequestDto userRoleChangeRequestDto = new UserRoleChangeRequestDto("ADMIN");
        String content = gson.toJson(userRoleChangeRequestDto);

        String token = JwtTokenUtil.createToken("userName", secretKey);


        /**
         * 등급 변경 성공 테스트
         */
        @Test
        @DisplayName("등급 변경 성공 테스트")
        void changeRoleSuccess() throws Exception {

            Mockito.when(userService.findRoleByUserName(any())).thenReturn(UserRole.ROLE_ADMIN);

            mockMvc.perform(post("/api/v1/users/1/role/change")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content))
                    .andDo(print())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.userId").exists())
                    .andExpect(jsonPath("$.result.userId").value(1))
                    .andExpect(jsonPath("$.result.message").exists())
                    .andExpect(jsonPath("$.result.message").value("1번 아이디의 권한을 ADMIN로 변경하였습니다."));

        }

        /**
         * 등급 변경 실패 테스트 ( USER 등급인 회원이 요청하는 경우)
         */
        @Test
        @DisplayName("등급 변경 실패 테스트 ( USER 등급인 회원이 요청하는 경우)")
        void changeRoleError1() throws Exception {

            Mockito.when(userService.findRoleByUserName(any())).thenReturn(UserRole.ROLE_USER);

            mockMvc.perform(post("/api/v1/users/1/role/change")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content))
                    .andDo(print())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").exists())
                    .andExpect(jsonPath("$.result.errorCode").value("FORBIDDEN_REQUEST"))
                    .andExpect(jsonPath("$.result.message").exists())
                    .andExpect(jsonPath("$.result.message").value("ADMIN 회원만 접근할 수 있습니다."));

        }


        /**
         * 등급 변경 실패 테스트 (토큰 없이 요청하는 경우)
         */
        @Test
        @DisplayName("회원 로그인 실패 테스트 (저장되어 있는 UserName 없음)")
        void changeRoleError2() throws Exception {

            mockMvc.perform(post("/api/v1/users/1/role/change")
                            .content(content)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.errorCode").value("TOKEN_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.message").value("토큰이 존재하지 않습니다."));
        }

    }
}