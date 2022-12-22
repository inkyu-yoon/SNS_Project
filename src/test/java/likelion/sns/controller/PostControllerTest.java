package likelion.sns.controller;

import com.google.gson.Gson;
import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.modify.PostModifyResponseDto;
import likelion.sns.domain.dto.read.PostDetailDto;
import likelion.sns.domain.dto.read.PostListDto;
import likelion.sns.domain.dto.write.PostWriteRequestDto;
import likelion.sns.domain.dto.write.PostWriteResponseDto;
import likelion.sns.domain.entity.UserRole;
import likelion.sns.jwt.ExceptionHandlerFilter;
import likelion.sns.jwt.JwtTokenFilter;
import likelion.sns.jwt.JwtTokenUtil;
import likelion.sns.service.PostService;
import likelion.sns.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WebApplicationContext wac;

    @MockBean
    PostService postService;

    @MockBean
    UserService userService;

    @Test
    @DisplayName("포스트 리스트 조회 성공 테스트")
    @WithMockUser
    void postListTest() throws Exception {
        // new post 가 앞에 배치되어야 한다.
        Timestamp oldPost = new Timestamp(System.currentTimeMillis());
        Timestamp newPost = new Timestamp(System.currentTimeMillis() + 100);

        List<PostListDto> posts = new ArrayList<>();
        PostListDto postListDtoOld = new PostListDto(1L, "첫번째 게시글","내용1", "윤인규", oldPost.toString(), oldPost.toString());
        PostListDto postListDtoNew = new PostListDto(2L, "두번째 게시글","내용2", "윤인규", newPost.toString(),newPost.toString());
        posts.add(postListDtoOld);
        posts.add(postListDtoNew);


        //createdAt 을 기준으로 내림차순 으로 정렬한다.
        Collections.sort(posts, (a, b) -> {
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        // Page를 구성한다.
        Page<PostListDto> postPage = new PageImpl<>(posts);

        BDDMockito.given(postService.getPostList(ArgumentMatchers.any())).willReturn(postPage);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/posts"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id").value(1));

    }

    @Test
    @DisplayName("게시글 단건 조회 테스트")
    @WithMockUser
    void postDetailTest() throws Exception {

        Long id = 1L;
        PostDetailDto post = new PostDetailDto(id, "title", "body", "userName", "2022-12-21 17:54:33", "2022-12-21 17:54:33");

        BDDMockito.given(postService.getPostById(ArgumentMatchers.any())).willReturn(post);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/posts/" + id))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userName").exists());

    }

    @Test
    @DisplayName("게시글 작성 테스트")
    @WithMockCustomUser
    void postWriteSuccess() throws Exception {
        PostWriteRequestDto postWriteRequestDto = new PostWriteRequestDto("title", "body");
        Mockito.when(postService.writePost(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(new PostWriteResponseDto("포스트 등록 완료", 1L));

        Gson gson = new Gson();
        String content = gson.toJson(postWriteRequestDto);

        //UsernamePasswordAuthenticationToken 권한 인증 상황
        //WithMockCustomerUser 어노테이션으로 Jwt 토큰을 인증 받았음을 가정

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/posts")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.message").value("포스트 등록 완료"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.postId").value(1));
    }

    @Test
    @DisplayName("토큰 없이 요청 시 게시글 작성 에러 테스트")
    void postWriteErrorNonToken() throws Exception {
        PostWriteRequestDto postWriteRequestDto = new PostWriteRequestDto("title", "body");

        Mockito.when(postService.writePost(ArgumentMatchers.any(), ArgumentMatchers.any())).
                thenThrow(new SNSAppException(ErrorCode.INVALID_PERMISSION));


        Gson gson = new Gson();
        String content = gson.toJson(postWriteRequestDto);
        String secretKey = "secret";

        //정의한 필터를 거치게끔 설정
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .addFilters(new ExceptionHandlerFilter(), new JwtTokenFilter(userService, secretKey), new UsernamePasswordAuthenticationFilter()).build();

        //토큰을 담지 않고 요청을 보냄
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/posts")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("ERROR"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.errorCode").value("INVALID_PERMISSION"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.message").value("사용자가 권한이 없습니다."));

    }

    @Test
    @DisplayName("토큰 유효하지 않을 시 게시글 작성 에러 테스트")
    void postWriteErrorInvalidToken() throws Exception {
        PostWriteRequestDto postWriteRequestDto = new PostWriteRequestDto("title", "body");
        Mockito.when(postService.writePost(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenThrow(new SNSAppException(ErrorCode.INVALID_TOKEN));

        Mockito.when(userService.findRoleByUserName(ArgumentMatchers.any())).thenReturn(UserRole.USER);

        Gson gson = new Gson();
        String content = gson.toJson(postWriteRequestDto);
        String secretKey = "secret";

        //토큰 정보가 잘못됨
        String token = JwtTokenUtil.createToken("inkyu", secretKey);
        token = token.replace("A", "C");

        //정의한 필터를 거치게끔 설정
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .addFilters(new ExceptionHandlerFilter(), new JwtTokenFilter(userService, secretKey), new UsernamePasswordAuthenticationFilter()).build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/posts")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("ERROR"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.errorCode").value("INVALID_TOKEN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.message").value("Token is expired or unauthorized."));

    }


    @Test
    @DisplayName("수정 성공")
    @WithMockUser
    void modifySuccess() throws Exception {
        Long postId = 1L;
        PostModifyResponseDto postModifyResponseDto = new PostModifyResponseDto("수정 내용",postId);

        Gson gson = new Gson();
        String content = gson.toJson(postModifyResponseDto);

        //UsernamePasswordAuthenticationToken 권한 인증 상황
        //WithMockCustomerUser 어노테이션으로 Jwt 토큰을 인증 받았음을 가정

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/posts/"+postId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("SUCCESS"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.message").value("포스트 수정 완료"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.postId").value(postId));
    }

    @Test
    @DisplayName("수정 에러 (인증 실패 시)")
    void modifyErrorUnAuth() throws Exception {
        Long postId = 1L;
        PostModifyResponseDto postModifyResponseDto = new PostModifyResponseDto("수정 내용",postId);

        Gson gson = new Gson();
        String content = gson.toJson(postModifyResponseDto);
        String secretKey = "secret";


        //정의한 필터를 거치게끔 설정
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .addFilters(new ExceptionHandlerFilter(), new JwtTokenFilter(userService, secretKey), new UsernamePasswordAuthenticationFilter()).build();

        //토큰 제공하지 않았을 때, 에러 발생해야함
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/posts/"+postId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("ERROR"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.errorCode").value("INVALID_PERMISSION"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.message").value("사용자가 권한이 없습니다."));
    }

    @Test
    @DisplayName("수정 에러 (토큰 검증은 통과했지만 수정요청자와 작성자 불일치)")
    @WithMockUser
    void modifyErrorInValid() throws Exception {
        Long postId = 1L;
        PostModifyResponseDto postModifyResponseDto = new PostModifyResponseDto("수정 내용",postId);

        Gson gson = new Gson();
        String content = gson.toJson(postModifyResponseDto);

        Mockito.doThrow(new SNSAppException(ErrorCode.INVALID_PERMISSION, "작성자와 수정 요청자가 일치하지 않습니다."))
                .when(postService).modifyPost(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());


        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/posts/"+postId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("ERROR"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.errorCode").value("INVALID_PERMISSION"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.message").value("작성자와 수정 요청자가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("수정 에러 (토큰 검증은 통과했지만 수정요청자와 작성자 불일치)")
    @WithMockUser
    void modifyErrorDBError() throws Exception {
        Long postId = 1L;
        PostModifyResponseDto postModifyResponseDto = new PostModifyResponseDto("수정 내용",postId);

        Gson gson = new Gson();
        String content = gson.toJson(postModifyResponseDto);

        Mockito.doThrow(new SQLException())
                .when(postService).modifyPost(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());


        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/posts/"+postId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("ERROR"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.errorCode").value("DATABASE_ERROR"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.message").value("DB 에러"));
    }
}