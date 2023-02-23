package likelion.sns.service;

import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.post.modify.PostModifyRequestDto;
import likelion.sns.domain.dto.post.write.PostWriteRequestDto;
import likelion.sns.domain.entity.Post;
import likelion.sns.domain.entity.User;
import likelion.sns.domain.entity.UserRole;
import likelion.sns.repository.PostRepository;
import likelion.sns.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Post mockPost;
    @Mock
    private User mockUser;

    @Mock
    private User foundMockUser;

    @InjectMocks
    private PostService postService;

    /**
     * 게시글 리스트 조회 테스트
     */
    @Nested
    @DisplayName("게시글 리스트 조회 테스트")
    class GetPost {

        PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC, "createdAt");

        /**
         * 리스트 조회 성공 테스트
         */
        @Test
        @DisplayName("리스트 조회 성공 테스트")
        void getPostSuccess() {
            given(postRepository.findByOrderByCreatedAtDesc(pageable))
                    .willReturn(new PageImpl<>(List.of(mockPost)));
            given(mockPost.getUser())
                    .willReturn(mockUser);
            given(mockPost.getCreatedAt())
                    .willReturn(Timestamp.valueOf(LocalDateTime.now()));
            given(mockUser.getUserName())
                    .willReturn("username");

            assertDoesNotThrow(() -> postService.getPostList(pageable));
        }
    }

    /**
     * 게시글 단건 조회 테스트
     */
    @Nested
    @DisplayName("게시글 단건 조회 테스트")
    class GetOnePost {

        /**
         * 단건 조회 성공 테스트
         */
        @Test
        @DisplayName("단건 조회 성공 테스트")
        void getPostSuccess() {

            given(postRepository.findById(any()))
                    .willReturn(Optional.of(mockPost));
            given(mockPost.getDeletedAt())
                    .willReturn(null);
            given(mockPost.getUser())
                    .willReturn(mockUser);
            given(mockPost.getCreatedAt())
                    .willReturn(Timestamp.valueOf(LocalDateTime.now()));
            given(mockPost.getModifiedAt())
                    .willReturn(Timestamp.valueOf(LocalDateTime.now()));


            assertDoesNotThrow(() -> postService.getPostById(any()));
        }

        /**
         * 단건 조회 실패 (해당하는 게시글이 없는 경우)
         */
        @Test
        @DisplayName("게시글 단건 조회 실패 (해당하는 게시글이 없는 경우)")
        void getPostError1() {

            when(postRepository.findById(any()))
                    .thenReturn(Optional.empty());

            SNSAppException exception = assertThrows(SNSAppException.class, () -> postService.getPostById(any()));
            assertThat(exception.getErrorCode().getMessage())
                    .isEqualTo("해당 포스트가 없습니다.");
        }

        /**
         * 단건 조회 실패 (해당하는 게시글의 deletedAt에 데이터가 채워져 있는 경우)
         */
        @Test
        @DisplayName("게시글 단건 조회 실패 (해당하는 게시글의 deletedAt에 데이터가 채워져 있는 경우)")
        void getPostError2() {

            given(postRepository.findById(any()))
                    .willReturn(Optional.of(mockPost));

            when(mockPost.getDeletedAt())
                    .thenReturn(Timestamp.valueOf(LocalDateTime.now()));

            SNSAppException exception = assertThrows(SNSAppException.class, () -> postService.getPostById(any()));
            assertThat(exception.getErrorCode().getMessage())
                    .isEqualTo("해당 포스트가 없습니다.");
        }
    }

    /**
     * 포스트 등록 테스트
     */
    @Nested
    @DisplayName("포스트 등록")
    class postWriteTest {

        @Mock
        PostWriteRequestDto requestDto;

        /**
         * 포스트 등록 성공
         */
        @Test
        @DisplayName("포스트 등록 성공")
        void postWriteSuccess() {

            given(userRepository.findByUserName(any()))
                    .willReturn(Optional.of(mockUser));

            given(postRepository.save(any()))
                    .willReturn(mockPost);

            assertDoesNotThrow(() -> postService.writePost(requestDto, any()));
        }

        /**
         * 포스트 등록 실패 (존재하는 회원이 없는 경우)
         */
        @Test
        @DisplayName("포스트 등록 실패 (존재하는 회원이 없는 경우)")
        void postWriteError() {


            when(userRepository.findByUserName(any()))
                    .thenReturn(Optional.empty());

            SNSAppException snsAppException = assertThrows(SNSAppException.class, () -> postService.writePost(requestDto, any()));

            assertThat(snsAppException.getErrorCode().getMessage()).isEqualTo("해당하는 유저를 찾을 수 없습니다.");
        }
    }

    /**
     * 포스트 수정 테스트
     */
    @Nested
    @DisplayName("포스트 수정 테스트")
    class postModifyTest {

        @Mock
        PostModifyRequestDto requestDto;

        /**
         * 포스트 수정 성공 테스트 (관리자인 경우)
         */
        @Test
        @DisplayName("포스트 수정 성공 (관리자인 경우)")
        void postModifySuccess1() {
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(any()))
                    .willReturn(Optional.of(mockPost));
            given(mockUser.getRole())
                    .willReturn(UserRole.ROLE_ADMIN);

            given(mockPost.getUser())
                    .willReturn(foundMockUser);
            given(foundMockUser.getUserName())
                    .willReturn("userName2");

            assertDoesNotThrow(() -> postService.modifyPost(requestDto, any(), "userName"));

        }

        /**
         * 포스트 수정 성공 테스트 (요청자와 작성자가 일치하는 경우)
         */
        @Test
        @DisplayName("포스트 수정 성공 (요청자와 작성자가 일치하는 경우)")
        void postModifySuccess2() {
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(any()))
                    .willReturn(Optional.of(mockPost));
            given(mockUser.getRole())
                    .willReturn(UserRole.ROLE_USER);

            given(mockPost.getUser())
                    .willReturn(foundMockUser);
            given(foundMockUser.getUserName())
                    .willReturn("userName");

            assertDoesNotThrow(() -> postService.modifyPost(requestDto, any(), "userName"));

        }

        /**
         * 포스트 수정 에러 (유저가 존재하지 않음)
         */
        @Test
        @DisplayName("포스트 수정 에러 (유저가 존재하지 않음)")
        void postModifyError2() {

            when(userRepository.findByUserName(any()))
                    .thenReturn(Optional.empty());

            SNSAppException snsAppException = assertThrows(SNSAppException.class, () -> postService.modifyPost(requestDto, any(), "userName"));

            assertThat(snsAppException.getErrorCode().getMessage()).isEqualTo("해당하는 유저를 찾을 수 없습니다.");
        }


        /**
         * 포스트 수정 에러 (포스트가 존재하지 않음)
         */
        @Test
        @DisplayName("포스트 수정 에러 (포스트가 존재하지 않음)")
        void postModifyError1() {

            given(userRepository.findByUserName(any()))
                    .willReturn(Optional.of(mockUser));

            when(postRepository.findById(any()))
                    .thenReturn(Optional.empty());

            SNSAppException snsAppException = assertThrows(SNSAppException.class, () -> postService.modifyPost(any(), 1L, "userName"));

            assertThat(snsAppException.getErrorCode().getMessage()).isEqualTo("해당 포스트가 없습니다.");
        }


        /**
         * 포스트 수정 에러 (요청한 유저와 작성자가 일치하지 않음)
         */
        @Test
        @DisplayName("포스트 수정 에러 (요청한 유저와 작성자가 일치하지 않음)")
        void postModifyError3() {

            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(any()))
                    .willReturn(Optional.of(mockPost));
            given(mockUser.getRole())
                    .willReturn(UserRole.ROLE_USER);

            given(mockPost.getUser())
                    .willReturn(foundMockUser);
            given(foundMockUser.getUserName())
                    .willReturn("userName2");

            SNSAppException snsAppException = assertThrows(SNSAppException.class, () -> postService.modifyPost(requestDto, any(), "userName"));

            assertThat(snsAppException.getErrorCode().getMessage()).isEqualTo("작성자와 요청자가 일치하지 않습니다.");
        }
    }

    /**
     * 포스트 삭제 테스트
     */
    @Nested
    @DisplayName("포스트 삭제 테스트")
    class postDeleteTest {

        /**
         * 포스트 삭제 성공
         */
        @Test
        @DisplayName("포스트 삭제 성공 (삭제 요청자와 작성자가 일치하는 경우)")
        void postDeleteSuccess1() {

            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(any()))
                    .willReturn(Optional.of(mockPost));
            given(mockUser.getRole())
                    .willReturn(UserRole.ROLE_USER);

            given(mockPost.getUser())
                    .willReturn(foundMockUser);
            given(foundMockUser.getUserName())
                    .willReturn("userName");

            assertDoesNotThrow(() -> postService.deletePost(any(), "userName"));
        }

        /**
         * 포스트 삭제 성공
         */
        @Test
        @DisplayName("포스트 삭제 성공 (삭제 요청자가 ADMIN인 경우)")
        void postDeleteSuccess2() {

            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(any()))
                    .willReturn(Optional.of(mockPost));
            given(mockUser.getRole())
                    .willReturn(UserRole.ROLE_ADMIN);

            given(mockPost.getUser())
                    .willReturn(foundMockUser);
            given(foundMockUser.getUserName())
                    .willReturn("userName2");

            assertDoesNotThrow(() -> postService.deletePost(any(), "userName"));
        }

        /**
         * 포스트 삭제 에러 (유저가 존재하지 않음)
         */
        @Test
        @DisplayName("포스트 삭제 에러 (유저가 존재하지 않음)")
        void postDeleteError1() {

            when(userRepository.findByUserName("userName"))
                    .thenReturn(Optional.empty());

            SNSAppException snsAppException = assertThrows(SNSAppException.class, () -> postService.deletePost(1L, "userName"));

            assertThat(snsAppException.getErrorCode().getMessage()).isEqualTo("해당하는 유저를 찾을 수 없습니다.");
        }


        /**
         * 포스트 삭제 에러 (포스트가 존재하지 않음)
         */
        @Test
        @DisplayName("포스트 삭제 에러 (포스트가 존재하지 않음)")
        void postDeleteError2() {


            given(userRepository.findByUserName(any()))
                    .willReturn(Optional.of(mockUser));

            when(postRepository.findById(any()))
                    .thenReturn(Optional.empty());

            SNSAppException snsAppException = assertThrows(SNSAppException.class, () -> postService.deletePost(1L, "userName"));

            assertThat(snsAppException.getErrorCode().getMessage()).isEqualTo("해당 포스트가 없습니다.");
        }


        /**
         * 포스트 수정 에러 (요청한 유저와 작성자가 일치하지 않음)
         */
        @Test
        @DisplayName("포스트 수정 에러 (요청한 유저와 작성자가 일치하지 않음)")
        void postDeleteError3() {

            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(any()))
                    .willReturn(Optional.of(mockPost));
            given(mockUser.getRole())
                    .willReturn(UserRole.ROLE_USER);

            given(mockPost.getUser())
                    .willReturn(foundMockUser);
            given(foundMockUser.getUserName())
                    .willReturn("userName2");

            SNSAppException snsAppException = assertThrows(SNSAppException.class, () -> postService.deletePost(any(), "userName"));

            assertThat(snsAppException.getErrorCode().getMessage()).isEqualTo("작성자와 요청자가 일치하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("마이피드 기능 테스트")
    class GetMyPost {

        PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC, "createdAt");

        /**
         * 마이피드 성공 테스트
         */
        @Test
        @DisplayName("마이피드 성공 테스트")
        void getMyPostSuccess() {

            given(userRepository.findByUserName(any()))
                    .willReturn(Optional.of(mockUser));
            given(mockUser.getId())
                    .willReturn(any());
            given(postRepository.findByUser_IdOrderByCreatedAtDesc(any(), pageable))
                    .willReturn(new PageImpl<>(List.of(mockPost)));
            given(mockPost.getUser())
                    .willReturn(mockUser);
            given(mockPost.getCreatedAt())
                    .willReturn(Timestamp.valueOf(LocalDateTime.now()));

            assertDoesNotThrow(() -> postService.getMyPosts("userName", pageable));

        }


        /**
         * 마이피드 실패 테스트
         */
        @Test
        @DisplayName("마이피드 실패 테스트 (회원이 존재하지 않음)")
        void getMyPostError() {

            when(userRepository.findByUserName(any()))
                    .thenReturn(Optional.empty());

            SNSAppException exception = assertThrows(SNSAppException.class, () -> postService.getMyPosts("userName", pageable));

            assertThat(exception.getErrorCode().getMessage()).isEqualTo("해당하는 유저를 찾을 수 없습니다.");

        }
    }

    @Nested
    @DisplayName("게시글 제목으로 검색 시 리스트 조회 테스트")
    class GetPostsByTitle {

        PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC, "createdAt");

        /**
         * 성공 테스트
         */
        @Test
        @DisplayName("게시글 제목으로 검색 시 리스트 조회 성공 테스트")
        void getPostsByTitleSuccess() {
            given(postRepository.findByTitleContainingOrderByCreatedAtDesc("title", pageable))
                    .willReturn(new PageImpl<>(List.of(mockPost)));
            given(mockPost.getUser())
                    .willReturn(mockUser);
            given(mockPost.getCreatedAt())
                    .willReturn(Timestamp.valueOf(LocalDateTime.now()));

            assertDoesNotThrow(() -> postService.getPostsByTitle("title", pageable));

        }
    }

    @Nested
    @DisplayName("회원명 제목으로 검색 시 리스트 조회 테스트")
    class GetPostsByUserName {

        PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC, "createdAt");

        /**
         * 성공 테스트
         */
        @Test
        @DisplayName("회원명 제목으로 검색 시 리스트 조회 성공 테스트")
        void getPostsByUserNameSuccess1() {
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(mockUser.getId())
                    .willReturn(any());
            given(postRepository.findByUser_IdOrderByCreatedAtDesc(any(), pageable))
                    .willReturn(new PageImpl<>(List.of(mockPost)));
            given(mockPost.getUser())
                    .willReturn(mockUser);
            given(mockPost.getCreatedAt())
                    .willReturn(Timestamp.valueOf(LocalDateTime.now()));

            assertDoesNotThrow(() -> postService.getPostsByUserName("userName", pageable));

        }

        /**
         * 성공 테스트
         */
        @Test
        @DisplayName("회원명 제목으로 검색 시 리스트 조회 성공 테스트")
        void getPostsByUserNameSuccess2() {
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.empty());

            assertDoesNotThrow(() -> postService.getPostsByUserName("userName", pageable));

        }
    }
}