package likelion.sns.controller.restController;

import likelion.sns.configuration.SecurityConfig;
import likelion.sns.domain.entity.UserRole;
import likelion.sns.jwt.JwtTokenUtil;
import likelion.sns.service.AlarmService;
import likelion.sns.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AlarmRestController.class)
@Import(SecurityConfig.class)
class AlarmRestControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    WebApplicationContext wac;
    @MockBean
    UserService userService;

    @MockBean
    AlarmService alarmService;

    @Value("${jwt.token.secret}")
    String secretKey;


    /**
     * SecurityConfig 로 설정한 Filter Chain 적용
     */

    @BeforeEach
    public void setUpMockMvc() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // 요청자가 ROLE_USER 임을 가정
        when(userService.findRoleByUserName(any())).thenReturn(UserRole.ROLE_USER);
    }

    /**
     * 알림 테스트
     */

    @Nested
    @DisplayName("알림 테스트")
    class AlarmTest {

        String token = JwtTokenUtil.createToken("userName",1L, secretKey);

        /**
         * 알림 목록 조회 성공 테스트
         */
        @Test
        @DisplayName("알림 목록 조회 성공 테스트")
        public void alarmReadSuccess() throws Exception {

            mockMvc.perform(get("/api/v1/alarms")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("SUCCESS"));
        }

        /**
         * 알림 목록 조회 에러 테스트 (로그인 하지 않은 경우 = 토큰 없이 요청하는 경우)
         */
        @Test
        @DisplayName("알림 목록 조회 에러 테스트 (로그인 하지 않은 경우)")
        public void alarmReadError() throws Exception {

            mockMvc.perform(get("/api/v1/alarms"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.errorCode").value("TOKEN_NOT_FOUND"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.message").value("토큰이 존재하지 않습니다."));
        }
    }

    /**
     * 알림 삭제 테스트
     */

    @Nested
    @DisplayName("알림 삭제 테스트")
    class AlarmDeleteTest {

        String token = JwtTokenUtil.createToken("userName",1L, secretKey);

        Long alarmId = 1L;
        /**
         * 알림 삭제 성공 테스트
         */
        @Test
        @DisplayName("알림 목록 조회 성공 테스트")
        public void alarmDeleteSuccess() throws Exception {

            mockMvc.perform(delete("/api/v1/alarms/"+alarmId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.message").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.message").value("알림 삭제 완료"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.id").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.id").value(1L));
        }

        /**
         * 알림 삭제 에러 테스트 (로그인 하지 않은 경우)
         */
        @Test
        @DisplayName("알림 삭제 에러 테스트 (로그인 하지 않은 경우)")
        public void alarmDeleteError() throws Exception {

            mockMvc.perform(delete("/api/v1/alarms/"+alarmId))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.errorCode").value("TOKEN_NOT_FOUND"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.message").value("토큰이 존재하지 않습니다."));
        }
    }
}