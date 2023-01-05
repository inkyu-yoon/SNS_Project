package likelion.sns.controller.restController;

import com.google.gson.Gson;
import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.configuration.SecurityConfig;
import likelion.sns.domain.dto.post.modify.PostModifyRequestDto;
import likelion.sns.domain.dto.post.modify.PostModifyResponseDto;
import likelion.sns.domain.dto.post.read.PostDetailDto;
import likelion.sns.domain.dto.post.read.PostListDto;
import likelion.sns.domain.dto.post.write.PostWriteRequestDto;
import likelion.sns.domain.dto.post.write.PostWriteResponseDto;
import likelion.sns.domain.entity.UserRole;
import likelion.sns.jwt.JwtTokenUtil;
import likelion.sns.service.PostService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = PostRestController.class)
@Import(SecurityConfig.class)
class PostRestControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired WebApplicationContext wac;
    @Autowired Gson gson;
    @MockBean PostService postService;
    @MockBean UserService userService;
    @Value("${jwt.token.secret}") String secretKey;

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
        Mockito.when(userService.findRoleByUserName(any())).thenReturn(UserRole.ROLE_USER);
    }


    /**
     * 포스트 조회 테스트
     */

    @Nested
    @DisplayName("포스트 조회 테스트")
    class PostReadTest {

        /**
         * 포스트 리스트 조회 성공 테스트
         * 게시글 리스트로 조회할 시, 늦게 등록된 게시글이 위치해 있어야 한다. (날짜순 내림차순)
         */

        @Test
        @DisplayName("포스트 리스트 조회 성공 테스트")
        void postListSuccess() throws Exception {

            // 시간을 임의로 설정한다. new Post 가 더 최신글이 될 것이다.
            Timestamp oldPost = new Timestamp(currentTimeMillis());
            Timestamp newPost = new Timestamp(currentTimeMillis() + 100);

            //일단 리스트에 담는다.
            List<PostListDto> posts = new ArrayList<>();
            PostListDto postListDtoOld = new PostListDto(1L, "첫번째 게시글", "내용1", "윤인규", oldPost.toString(), oldPost.toString());
            PostListDto postListDtoNew = new PostListDto(2L, "두번째 게시글", "내용2", "윤인규", newPost.toString(), newPost.toString());
            posts.add(postListDtoOld);
            posts.add(postListDtoNew);


            //createdAt 을 기준으로 내림차순 으로 정렬한다.
            Collections.sort(posts, (a, b) -> {
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });

            // 실제 컨트롤러에서는 Page클래스에 담을 것이므로 Page로 구성한다.
            Page<PostListDto> postPage = new PageImpl<>(posts);

            //postService.
            given(postService.getPostList(any())).willReturn(postPage);

            mockMvc.perform(get("/api/v1/posts"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.content").exists())
                    .andExpect(jsonPath("$.result.content[0].id").value(2))
                    .andExpect(jsonPath("$.result.content[1].id").value(1));

        }

        /**
         * 포스트 단건 조회 성공 테스트
         */

        @Test
        @DisplayName("게시글 단건 조회 테스트")
        @WithMockUser
        void postDetailSuccess() throws Exception {

            Long id = 1L;
            PostDetailDto post = new PostDetailDto(id, "title", "body", "userName", "2022-12-21 17:54:33", "2022-12-21 17:54:33", null);

            given(postService.getPostById(any())).willReturn(post);

            mockMvc.perform(get("/api/v1/posts/" + id))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.id").value(1));

        }
    }

    /**
     * 포스트 작성 테스트
     */

    @Nested
    @DisplayName("포스트 작성 테스트")
    class PostWriteTest {
        PostWriteRequestDto postWriteRequestDto = new PostWriteRequestDto("title", "body");

        String token = JwtTokenUtil.createToken("userName", secretKey);
        String content = gson.toJson(postWriteRequestDto);

        /**
         *  포스트 작성 성공 테스트
         */
        @Test
        @DisplayName("게시글 작성 테스트")
        void postWriteSuccess() throws Exception {

            Mockito.when(postService.writePost(any(), any())).thenReturn(new PostWriteResponseDto("포스트 등록 완료", 1L));

            mockMvc.perform(post("/api/v1/posts")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .characterEncoding("utf-8")
                            .content(content)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.message").value("포스트 등록 완료"))
                    .andExpect(jsonPath("$.result.postId").value(1));
        }

        /**
         *  포스트 작성 에러 테스트 (토큰이 Bearer 형식이 아닌 경우)
         */
        @Test
        @DisplayName("포스트 작성 에러 (토큰이 Bearer 형식이 아닌 경우)")
        void postWriteError1() throws Exception {

            mockMvc.perform(post("/api/v1/posts")
                            .header(HttpHeaders.AUTHORIZATION, "not Bearer Token")
                            .content(content)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.errorCode").value("INVALID_TOKEN"))
                    .andExpect(jsonPath("$.result.message").value("유효하지 않은 토큰입니다."));
        }

        /**
         *  포스트 작성 에러 테스트 (토큰이 없이 요청하는 경우)
         */
        @Test
        @DisplayName("포스트 작성 에러 (토큰 없이 요청할 경우)")
        void postWriteError2() throws Exception {

            mockMvc.perform(post("/api/v1/posts")
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

    /**
     * 포스트 수정 테스트
     */

    @Nested
    @DisplayName("포스트 수정 테스트")
    class PostModifyTest {

        PostModifyRequestDto PostModifyRequestDto = new PostModifyRequestDto("수정 제목","수정 내용");

        String token = JwtTokenUtil.createToken("userName", secretKey);

        String content = gson.toJson(PostModifyRequestDto);
        Long postId = 1L;

        /**
         * 포스트 수정 성공 테스트
         */

        @Test
        @DisplayName("수정 성공")
        void postModifySuccess() throws Exception {

            mockMvc.perform(put("/api/v1/posts/" + postId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .characterEncoding("utf-8")
                            .content(content)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.message").value("포스트 수정 완료"))
                    .andExpect(jsonPath("$.result.postId").value(postId));
        }

        /**
         *  포스트 수정 에러 테스트 (토큰이 Bearer 형식이 아닌 경우)
         */

        @Test
        @DisplayName("포스트 수정 에러 (토큰이 Bearer 형식이 아닌 경우)")
        void postModifyError1() throws Exception {

            mockMvc.perform(put("/api/v1/posts/" + postId)
                            .header(HttpHeaders.AUTHORIZATION, "not Bearer Token")
                            .content(content)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.errorCode").value("INVALID_TOKEN"))
                    .andExpect(jsonPath("$.result.message").value("유효하지 않은 토큰입니다."));
        }

        /**
         *  포스트 수정 에러 테스트 (토큰이 없이 요청하는 경우)
         */

        @Test
        @DisplayName("포스트 수정 에러 (토큰 없이 요청할 경우)")
        void postModifyError2() throws Exception {

            mockMvc.perform(put("/api/v1/posts/" + postId)
                            .content(content)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.errorCode").value("TOKEN_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.message").value("토큰이 존재하지 않습니다."));
        }

        /**
         *  포스트 수정 에러 테스트 (토큰 검증은 통과했지만 수정요청자와 작성자 불일치)
         */

        @Test
        @DisplayName("수정 에러 (토큰 검증은 통과했지만 수정요청자와 작성자 불일치)")
        void postModifyError3() throws Exception {

            Mockito.doThrow(new SNSAppException(ErrorCode.USER_NOT_MATCH))
                    .when(postService).modifyPost(any(), any(), any());


            mockMvc.perform(put("/api/v1/posts/" + postId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .characterEncoding("utf-8")
                            .content(content)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.errorCode").value("USER_NOT_MATCH"))
                    .andExpect(jsonPath("$.result.message").value("작성자와 요청자가 일치하지 않습니다."));
        }

        /**
         * DB 에 문제가 생긴 경우 (SQLException)
         */

        @Test
        @DisplayName("수정 에러 (DB 에러)")
        void modifyErrorDBError() throws Exception {

            Mockito.doThrow(new SQLException())
                    .when(postService).modifyPost(any(), any(), any());

            mockMvc.perform(put("/api/v1/posts/" + postId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .characterEncoding("utf-8")
                            .content(content)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.errorCode").value("DATABASE_ERROR"))
                    .andExpect(jsonPath("$.result.message").value("DB 에러"));
        }
    }

    @Nested
    @DisplayName("포스트 삭제 테스트")
    class PostDeleteTest {

        String token = JwtTokenUtil.createToken("userName", secretKey);
        Long postId = 1L;

        /**
         * 포스트 삭제 성공 테스트
         */

        @Test
        @DisplayName("삭제 성공")
        void postDeleteSuccess() throws Exception {

            mockMvc.perform(delete("/api/v1/posts/" + postId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.message").value("포스트 삭제 완료"))
                    .andExpect(jsonPath("$.result.postId").value(postId));
        }

        /**
         *  포스트 삭제 에러 테스트 (토큰이 Bearer 형식이 아닌 경우)
         */

        @Test
        @DisplayName("포스트 삭제 에러 (토큰이 Bearer 형식이 아닌 경우)")
        void postDeleteError1() throws Exception {

            mockMvc.perform(delete("/api/v1/posts/" + postId)
                            .header(HttpHeaders.AUTHORIZATION, "not Bearer token")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.errorCode").value("INVALID_TOKEN"))
                    .andExpect(jsonPath("$.result.message").value("유효하지 않은 토큰입니다."));
        }

        /**
         *  포스트 삭제 에러 테스트 (토큰이 없이 요청하는 경우)
         */

        @Test
        @DisplayName("포스트 삭제 에러 (토큰 없이 요청할 경우)")
        void postDeleteError2() throws Exception {

            mockMvc.perform(delete("/api/v1/posts/" + postId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.errorCode").value("TOKEN_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.message").value("토큰이 존재하지 않습니다."));
        }

        /**
         *  포스트 삭제 에러 테스트 (토큰 검증은 통과했지만 삭제 요청자와 작성자 불일치)
         */

        @Test
        @DisplayName("삭제 에러 (토큰 검증은 통과했지만 삭제 요청자와 작성자 불일치)")
        void postDeleteError3() throws Exception {

            Mockito.doThrow(new SNSAppException(ErrorCode.USER_NOT_MATCH))
                    .when(postService).deletePost(any(), any());


            mockMvc.perform(delete("/api/v1/posts/" + postId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.errorCode").value("USER_NOT_MATCH"))
                    .andExpect(jsonPath("$.result.message").value("작성자와 요청자가 일치하지 않습니다."));
        }

        /**
         * DB 에 문제가 생긴 경우 (SQLException)
         */

        @Test
        @DisplayName("삭제 에러 (DB 에러)")
        void deleteErrorDBError() throws Exception {

            Mockito.doThrow(new SQLException())
                    .when(postService).deletePost(any(), any());

            mockMvc.perform(delete("/api/v1/posts/" + postId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.errorCode").value("DATABASE_ERROR"))
                    .andExpect(jsonPath("$.result.message").value("DB 에러"));
        }
    }

    /**
     * 마이피드 테스트
     */
    @Nested
    @DisplayName("포스트 마이피드 테스트")
    class PostMyFeedTest {

        String token = JwtTokenUtil.createToken("userName", secretKey);

        /**
         * 마이 피드 조회 성공 테스트
         */
        @Test
        @DisplayName("마이피드 조회 성공 테스트")
        public void myFeedSuccess() throws Exception {

            mockMvc.perform(get("/api/v1/posts/my")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"));

        }

        /**
         *  마이 피드 조회 실패 테스트, 로그인 하지 않은 경우
         */
        @Test
        @DisplayName("마이피드 실패 테스트 (로그인 하지 않은 경우 = 토큰 없이 요청하는 경우)")
        public void myFeedError() throws Exception {

            mockMvc.perform(get("/api/v1/posts/my"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").exists())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.errorCode").value("TOKEN_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.message").value("토큰이 존재하지 않습니다."));
        }
    }
}