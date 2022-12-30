package likelion.sns.controller.restController;

import com.google.gson.Gson;
import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.configuration.SecurityConfig;
import likelion.sns.domain.dto.join.UserJoinRequestDto;
import likelion.sns.domain.dto.join.UserJoinResponseDto;
import likelion.sns.domain.dto.login.UserLoginRequestDto;
import likelion.sns.domain.dto.login.UserLoginResponseDto;
import likelion.sns.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserRestController.class)
@Import(SecurityConfig.class)
class UserRestControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired WebApplicationContext wac;
    @Autowired Gson gson;

    @MockBean UserService userService;

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
    }



    /**
     * 회원가입 테스트
     */

    @Nested
    @DisplayName("회원가입 테스트")
    public class JoinTest {
        String content = gson.toJson(userJoinRequestDto);

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
}