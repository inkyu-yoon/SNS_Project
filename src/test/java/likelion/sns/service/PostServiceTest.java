package likelion.sns.service;

import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.modify.PostModifyRequestDto;
import likelion.sns.domain.dto.write.PostWriteRequestDto;
import likelion.sns.domain.entity.Post;
import likelion.sns.domain.entity.User;
import likelion.sns.domain.entity.UserRole;
import likelion.sns.repository.PostRepository;
import likelion.sns.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static java.util.Optional.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PostServiceTest {

    PostService postService;

    PostRepository postRepository = mock(PostRepository.class);
    UserRepository userRepository = mock(UserRepository.class);


    @BeforeEach
    void setUp() {
        postService = new PostService(postRepository, userRepository);
    }

    @Test
    @DisplayName("포스트 등록 성공")
    void postWriteSuccess() {

        Post mockPost = mock(Post.class);
        User mockUser = mock(User.class);

        when(userRepository.findByUserName(any()))
                .thenReturn(of(mockUser));

        when(postRepository.save(any()))
                .thenReturn(mockPost);

        Assertions.assertDoesNotThrow(() -> postService.writePost(new PostWriteRequestDto("title", "body"), "userName"));
    }

    @Test
    @DisplayName("포스트 등록 실패 (존재하는 회원이 없는 경우)")
    void postWriteError() {

        //db에서 회원이 없다면 SNSAppException을 일으킬 것
        when(userRepository.findByUserName(any()))
                .thenThrow(new SNSAppException(ErrorCode.USERNAME_NOT_FOUND));

        SNSAppException snsAppException = Assertions.assertThrows(SNSAppException.class, () -> postService.writePost(any(), "userName"));

        assertThat(snsAppException.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(snsAppException.getErrorCode().getMessage()).isEqualTo("해당하는 유저를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("포스트 수정 에러 (포스트가 존재하지 않음)")
    void postModifyError1() {

        User mockUser = mock(User.class);

        when(userRepository.findByUserName(any()))
                .thenReturn(of(mockUser));

        when(postRepository.findById(any()))
                .thenThrow(new SNSAppException(ErrorCode.POST_NOT_FOUND));

        SNSAppException snsAppException = Assertions.assertThrows(SNSAppException.class, () -> postService.modifyPost(any(), 1L, "userName"));

        assertThat(snsAppException.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(snsAppException.getErrorCode().getMessage()).isEqualTo("해당 포스트가 없습니다.");
    }

    @Test
    @DisplayName("포스트 수정 에러 (유저가 존재하지 않음)")
    void postModifyError2() {

        when(userRepository.findByUserName("userName"))
                .thenThrow(new SNSAppException(ErrorCode.USERNAME_NOT_FOUND));

        SNSAppException snsAppException = Assertions.assertThrows(SNSAppException.class, () -> postService.modifyPost(any(), 1L, "userName"));

        assertThat(snsAppException.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(snsAppException.getErrorCode().getMessage()).isEqualTo("해당하는 유저를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("포스트 수정 에러 (요청한 유저와 작성자가 일치하지 않음)")
    void postModifyError3() {

        User mockUser = mock(User.class);
        Post mockPost = mock(Post.class);


        when(userRepository.findByUserName(any()))
                .thenReturn(of(mockUser));

        when(postRepository.findById(any()))
                .thenReturn(of(mockPost));

        doReturn(mockUser)
                .when(mockPost).getUser();

        doReturn(UserRole.USER)
                .when(mockUser).getRole();

        doReturn("userName1")
                .when(mockUser).getUserName();


        SNSAppException snsAppException = Assertions.assertThrows(SNSAppException.class, () -> postService.modifyPost(new PostModifyRequestDto("title","body"), 1L, "userName"));

        assertThat(snsAppException.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(snsAppException.getErrorCode().getMessage()).isEqualTo("작성자와 요청자가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("포스트 수정 에러 (포스트가 존재하지 않음)")
    void postDeleteError1() {

        User mockUser = mock(User.class);

        when(userRepository.findByUserName(any()))
                .thenReturn(of(mockUser));

        when(postRepository.findById(any()))
                .thenThrow(new SNSAppException(ErrorCode.POST_NOT_FOUND));

        SNSAppException snsAppException = Assertions.assertThrows(SNSAppException.class, () -> postService.deletePost( 1L, "userName"));

        assertThat(snsAppException.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(snsAppException.getErrorCode().getMessage()).isEqualTo("해당 포스트가 없습니다.");
    }

    @Test
    @DisplayName("포스트 수정 에러 (유저가 존재하지 않음)")
    void postDeleteError2() {

        when(userRepository.findByUserName("userName"))
                .thenThrow(new SNSAppException(ErrorCode.USERNAME_NOT_FOUND));

        SNSAppException snsAppException = Assertions.assertThrows(SNSAppException.class, () -> postService.deletePost( 1L, "userName"));

        assertThat(snsAppException.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(snsAppException.getErrorCode().getMessage()).isEqualTo("해당하는 유저를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("포스트 수정 에러 (요청한 유저와 작성자가 일치하지 않음)")
    void postDeleteError3() {

        User mockUser = mock(User.class);
        Post mockPost = mock(Post.class);

        when(userRepository.findByUserName(any()))
                .thenReturn(of(mockUser));
        when(postRepository.findById(any()))
                .thenReturn(of(mockPost));

        doReturn(mockUser)
                .when(mockPost).getUser();
        doReturn(UserRole.USER)
                .when(mockUser).getRole();

        String requestName = "request";
        String writerName = "writer";

        doReturn(requestName)
                .when(mockUser).getUserName();

        SNSAppException snsAppException = Assertions.assertThrows(SNSAppException.class, () -> postService.deletePost( 1L, writerName));

        assertThat(snsAppException.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(snsAppException.getErrorCode().getMessage()).isEqualTo("작성자와 요청자가 일치하지 않습니다.");
    }


}