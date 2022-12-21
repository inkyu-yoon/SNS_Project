package likelion.sns.controller;

import likelion.sns.domain.dto.read.PostDetailDto;
import likelion.sns.domain.dto.read.PostListDto;
import likelion.sns.service.PostService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PostService postService;

    @Test
    @DisplayName("포스트 리스트 조회 성공 테스트")
    @WithMockUser
    void postListTest() throws Exception {
        // new post 가 앞에 배치되어야 한다.
        Timestamp oldPost = new Timestamp(System.currentTimeMillis());
        Timestamp newPost = new Timestamp(System.currentTimeMillis()+100);

        List<PostListDto> posts = new ArrayList<>();
        PostListDto postListDtoOld = new PostListDto(1L, "첫번째 게시글", "윤인규", oldPost);
        PostListDto postListDtoNew = new PostListDto(2L, "두번째 게시글", "윤인규", newPost);
        posts.add(postListDtoOld);
        posts.add(postListDtoNew);


        //createdAt 을 기준으로 내림차순 으로 정렬한다.
        Collections.sort(posts,(a,b)->{
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
}