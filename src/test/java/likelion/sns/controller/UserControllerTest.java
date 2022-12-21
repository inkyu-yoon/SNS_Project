package likelion.sns.controller;

import com.google.gson.Gson;
import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.join.UserJoinRequestDto;
import likelion.sns.domain.dto.join.UserJoinResponseDto;
import likelion.sns.domain.dto.login.UserLoginRequestDto;
import likelion.sns.domain.dto.login.UserLoginResponseDto;
import likelion.sns.jwt.JwtTokenUtil;
import likelion.sns.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @Test
    @DisplayName("회원가입 (join) 성공 테스트")
    @WithMockUser
    void join() throws Exception {
        UserJoinRequestDto user = new UserJoinRequestDto("윤인규", "password");

        Mockito.when(userService.createUser(ArgumentMatchers.any())).thenReturn(new UserJoinResponseDto(1L, "윤인규"));

        Gson gson = new Gson();
        String content = gson.toJson(user);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/join")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.userId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.userName").value("윤인규"));
    }

    @Test
    @DisplayName("회원가입 중복 에러 테스트")
    @WithMockUser
    void joinError() throws Exception {
        UserJoinRequestDto user = new UserJoinRequestDto("윤인규", "password");

        Mockito.when(userService.createUser(ArgumentMatchers.any())).thenThrow(new SNSAppException(ErrorCode.DUPLICATED_USER_NAME, user.getUserName() + "는(은) 이미 존재합니다."));

        Gson gson = new Gson();
        String content = gson.toJson(user);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/join")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("ERROR"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.errorCode").value("DUPLICATED_USER_NAME"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.message").value("윤인규는(은) 이미 존재합니다."));
    }

    @Test
    @DisplayName("회원 로그인 성공 테스트")
    @WithMockUser
    void login() throws Exception {
        UserLoginRequestDto user = new UserLoginRequestDto("윤인규", "1234");

        String token = "randomToken";

        Mockito.when(userService.loginUser(ArgumentMatchers.any())).thenReturn(new UserLoginResponseDto(token));

        Gson gson = new Gson();
        String content = gson.toJson(user);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.jwt").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.jwt").value(token));

    }


    @Test
    @DisplayName("회원 로그인 실패 테스트 (user not found)")
    @WithMockUser
    void LoginNotFound() throws Exception {
        UserLoginRequestDto user = new UserLoginRequestDto("윤인규", "1234");

        Mockito.when(userService.loginUser(ArgumentMatchers.any())).thenThrow(new SNSAppException(ErrorCode.USERNAME_NOT_FOUND, user.getUserName() + "에 해당하는 회원을 찾을 수 없습니다."));

        Gson gson = new Gson();
        String content = gson.toJson(user);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("ERROR"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.errorCode").value("USERNAME_NOT_FOUND"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.message").value("윤인규에 해당하는 회원을 찾을 수 없습니다."));

    }

    @Test
    @DisplayName("회원 로그인 실패 테스트 (Invalid password)")
    @WithMockUser
    void LoginInValid() throws Exception {
        UserLoginRequestDto user = new UserLoginRequestDto("윤인규", "1234");

        Mockito.when(userService.loginUser(ArgumentMatchers.any())).thenThrow(new SNSAppException(ErrorCode.INVALID_PASSWORD, "잘못된 비밀번호 입니다"));

        Gson gson = new Gson();
        String content = gson.toJson(user);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("ERROR"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.errorCode").value("INVALID_PASSWORD"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.message").value("잘못된 비밀번호 입니다"));

    }
}