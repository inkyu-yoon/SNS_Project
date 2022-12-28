package likelion.sns.controller;

import likelion.sns.service.PostService;
import likelion.sns.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class HelloControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PostService postService;

    @MockBean
    UserService userService;

    @Test
    @DisplayName("Hello Controller GET Mapping 테스트")
    @WithMockUser
    void helloTest() throws Exception {
        mockMvc.perform(get("/api/v1/hello"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("hello"));
    }
}