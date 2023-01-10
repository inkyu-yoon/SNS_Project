package likelion.sns.controller.restController;

import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.configuration.SecurityConfig;
import likelion.sns.domain.entity.UserRole;
import likelion.sns.jwt.JwtTokenUtil;
import likelion.sns.service.LikeService;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = LikeRestController.class)
@Import(SecurityConfig.class)
class LikeRestControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    WebApplicationContext wac;
    @MockBean
    UserService userService;

    @MockBean
    LikeService likeService;

    @Value("${jwt.token.secret}")
    String secretKey;

    Long postId = 1L;

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
     * 좋아요 입력 테스트
     */
    @Nested
    @DisplayName("좋아요 입력 테스트")
    class LikeTest {
        String token = JwtTokenUtil.createToken("userName", secretKey);

        /**
         * 좋아요 입력 성공 테스트
         */
        @Test
        @DisplayName("좋아요 입력 성공 테스트")
        public void likeSuccess() throws Exception {

            mockMvc.perform(post("/api/v1/posts/" + postId + "/likes")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result").value("좋아요를 눌렀습니다."));
        }

        /**
         * 좋아요 입력 실패 (로그인 하지 않은 경우)
         */

        @Test
        @DisplayName("좋아요 입력 실패 (로그인 하지 않은 경우)")
        public void likeError1() throws Exception {

            mockMvc.perform(post("/api/v1/posts/" + postId + "/likes"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.errorCode").value("TOKEN_NOT_FOUND"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.message").value("토큰이 존재하지 않습니다."));
        }

        /**
         * 좋아요 입력 실패 (해당 포스트가 없는 경우)
         */

        @Test
        @DisplayName("좋아요 입력 실패 (해당 포스트가 없는 경우)")
        public void likeError2() throws Exception {

            doThrow(new SNSAppException(ErrorCode.POST_NOT_FOUND)).when(likeService).AddLike(any(),any());

            mockMvc.perform(post("/api/v1/posts/" + postId + "/likes")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.errorCode").value("POST_NOT_FOUND"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.message").value("해당 포스트가 없습니다."));
        }

        /**
         * 좋아요 입력 실패 (이미 좋아요를 입력한 경우)
         */

        @Test
        @DisplayName("좋아요 입력 실패 (이미 좋아요를 입력한 경우)")
        public void likeError3() throws Exception {

            doThrow(new SNSAppException(ErrorCode.FORBIDDEN_ADD_LIKE)).when(likeService).AddLike(any(),any());

            mockMvc.perform(post("/api/v1/posts/" + postId + "/likes")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.errorCode").value("FORBIDDEN_ADD_LIKE"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.message").value("좋아요는 한번만 누를 수 있습니다."));
        }
    }
    /**
     * 좋아요 개수 테스트
     */
    @Nested
    @DisplayName("좋아요 입력 테스트")
    class LikeCountTest {

        /**
         * 좋아요 개수 카운트 성공 테스트
         */
        @Test
        @DisplayName("좋아요 개수 카운트 성공 테스트")
        public void likeCountSuccess() throws Exception {

            when(likeService.getLikesCount(postId)).thenReturn(1L);

            mockMvc.perform(get("/api/v1/posts/" + postId + "/likes"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result").value(1));
        }
    }
}