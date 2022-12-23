package likelion.sns.controller;

import com.google.gson.Gson;
import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.join.UserJoinRequestDto;
import likelion.sns.domain.dto.join.UserJoinResponseDto;
import likelion.sns.domain.dto.login.UserLoginRequestDto;
import likelion.sns.domain.dto.login.UserLoginResponseDto;
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

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        Mockito.when(userService.createUser(any()))
                .thenReturn(new UserJoinResponseDto(1L, "윤인규"));

        Gson gson = new Gson();
        String content = gson.toJson(user);

        mockMvc.perform(post("/api/v1/users/join")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.result.userId").value(1))
                .andExpect(jsonPath("$.result.userName").value("윤인규"));
    }

    @Test
    @DisplayName("회원가입 중복 에러 테스트")
    @WithMockUser
    void joinError() throws Exception {
        UserJoinRequestDto user = new UserJoinRequestDto("윤인규", "password");

        Mockito.when(userService.createUser(any()))
                .thenThrow(new SNSAppException(ErrorCode.DUPLICATED_USER_NAME, user.getUserName() + "는(은) 이미 존재합니다."));

        Gson gson = new Gson();
        String content = gson.toJson(user);

        mockMvc.perform(post("/api/v1/users/join")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.resultCode").value("ERROR"))
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.result.errorCode").value("DUPLICATED_USER_NAME"))
                .andExpect(jsonPath("$.result.message").value("윤인규는(은) 이미 존재합니다."));
    }

    @Test
    @DisplayName("회원 로그인 성공 테스트")
    @WithMockUser
    void login() throws Exception {
        UserLoginRequestDto user = new UserLoginRequestDto("윤인규", "1234");

        String token = "randomToken";

        Mockito.when(userService.loginUser(any()))
                .thenReturn(new UserLoginResponseDto(token));

        Gson gson = new Gson();
        String content = gson.toJson(user);

        mockMvc.perform(post("/api/v1/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andDo(print())
                .andExpect(jsonPath("$.resultCode").exists())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(jsonPath("$.result.jwt").exists())
                .andExpect(jsonPath("$.result.jwt").value(token));

    }


    @Test
    @DisplayName("회원 로그인 실패 테스트 (user not found)")
    @WithMockUser
    void LoginNotFound() throws Exception {
        UserLoginRequestDto user = new UserLoginRequestDto("윤인규", "1234");

        Mockito.when(userService.loginUser(any()))
                .thenThrow(new SNSAppException(ErrorCode.USERNAME_NOT_FOUND, user.getUserName() + "에 해당하는 회원을 찾을 수 없습니다."));

        Gson gson = new Gson();
        String content = gson.toJson(user);

        mockMvc.perform(post("/api/v1/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("ERROR"))
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.result.errorCode").value("USERNAME_NOT_FOUND"))
                .andExpect(jsonPath("$.result.message").value("윤인규에 해당하는 회원을 찾을 수 없습니다."));

    }

    @Test
    @DisplayName("회원 로그인 실패 테스트 (Invalid password)")
    @WithMockUser
    void LoginInValid() throws Exception {
        UserLoginRequestDto user = new UserLoginRequestDto("윤인규", "1234");

        Mockito.when(userService.loginUser(any()))
                .thenThrow(new SNSAppException(ErrorCode.INVALID_PASSWORD, "잘못된 비밀번호 입니다"));

        Gson gson = new Gson();
        String content = gson.toJson(user);

        mockMvc.perform(post("/api/v1/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("ERROR"))
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.result.errorCode").value("INVALID_PASSWORD"))
                .andExpect(jsonPath("$.result.message").value("잘못된 비밀번호 입니다"));

    }
}