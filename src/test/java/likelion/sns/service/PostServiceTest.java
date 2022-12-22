package likelion.sns.service;

import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.write.PostWriteRequestDto;
import likelion.sns.domain.entity.Post;
import likelion.sns.domain.entity.User;
import likelion.sns.repository.PostRepository;
import likelion.sns.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Optional;

class PostServiceTest {

    PostService postService;

    PostRepository postRepository = Mockito.mock(PostRepository.class);
    UserRepository userRepository = Mockito.mock(UserRepository.class);


    @BeforeEach
    void setUp() {
        postService = new PostService(postRepository, userRepository);
    }

    @Test
    @DisplayName("포스트 등록 성공")
    void postWriteSuccess() {

        Post mockPost = Mockito.mock(Post.class);
        User mockUser = Mockito.mock(User.class);

        Mockito.when(userRepository.findByUserName(ArgumentMatchers.any())).thenReturn(Optional.of(mockUser));
        Mockito.when(postRepository.save(ArgumentMatchers.any())).thenReturn(mockPost);

        Assertions.assertDoesNotThrow(() -> postService.writePost(new PostWriteRequestDto("title", "body"), "userName"));
    }

    @Test
    @DisplayName("포스트 등록 실패 (존재하는 회원이 없는 경우)")
    void postWriteError() {

        Post mockPost = Mockito.mock(Post.class);

        //db에서 회원이 없다면 SNSAppException을 일으킬 것
        Mockito.when(userRepository.findByUserName(ArgumentMatchers.any())).thenThrow(new SNSAppException(ErrorCode.USERNAME_NOT_FOUND));
        Mockito.when(postRepository.save(ArgumentMatchers.any())).thenReturn(mockPost);

        Assertions.assertThrows(SNSAppException.class,() -> postService.writePost(new PostWriteRequestDto("title", "body"), "userName"));
    }

}