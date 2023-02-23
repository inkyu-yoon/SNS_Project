package likelion.sns.service;

import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.comment.modify.CommentModifyRequestDto;
import likelion.sns.domain.dto.comment.write.CommentWriteRequestDto;
import likelion.sns.domain.dto.comment.write.ReplyCommentWriteRequestDto;
import likelion.sns.domain.entity.Comment;
import likelion.sns.domain.entity.Post;
import likelion.sns.domain.entity.User;
import likelion.sns.domain.entity.UserRole;
import likelion.sns.repository.AlarmRepository;
import likelion.sns.repository.CommentRepository;
import likelion.sns.repository.PostRepository;
import likelion.sns.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private AlarmRepository alarmRepository;

    @Mock
    private Comment mockComment;
    @Mock
    private Comment mockParentComment;
    @Mock
    private User mockUser;
    @Mock
    private User otherMockUser;
    @Mock
    private Post mockPost;
    @InjectMocks
    private CommentService commentService;

    @Nested
    @DisplayName("댓글 조회 테스트")
    class GetCommentList {

        PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC, "createdAt");
        PageRequest pageableAsc = PageRequest.of(0, 20, Sort.Direction.ASC, "createdAt");

        /**
         * 댓글 리스트 조회 성공 테스트 (최신순으로 조회)
         */
        @Test
        @DisplayName("댓글 리스트 조회 성공 테스트 (최신순으로 조회)")
        void getCommentListSuccess1() {
            given(commentRepository.findByPost_IdAndParentIsNullOrderByCreatedAtDesc(1L, pageable))
                    .willReturn(new PageImpl<>(List.of(mockComment)));
            given(mockComment.getUser())
                    .willReturn(mockUser);
            given(mockComment.getPost())
                    .willReturn(mockPost);
            given(mockComment.getCreatedAt())
                    .willReturn(Timestamp.valueOf(LocalDateTime.now()));
            given(mockComment.getModifiedAt())
                    .willReturn(Timestamp.valueOf(LocalDateTime.now()));

            assertDoesNotThrow(() -> commentService.getCommentList(1L, pageable));
        }

        /**
         * 댓글 리스트 조회 성공 테스트
         */
        @Test
        @DisplayName("댓글 리스트 조회 성공 테스트")
        void getCommentListSuccess2() {
            given(commentRepository.findByPost_IdAndParentIsNull(1L, pageableAsc))
                    .willReturn(new PageImpl<>(List.of(mockComment)));
            given(mockComment.getUser())
                    .willReturn(mockUser);
            given(mockComment.getPost())
                    .willReturn(mockPost);
            given(mockComment.getCreatedAt())
                    .willReturn(Timestamp.valueOf(LocalDateTime.now()));
            given(mockComment.getModifiedAt())
                    .willReturn(Timestamp.valueOf(LocalDateTime.now()));

            assertDoesNotThrow(() -> commentService.getCommentListAsc(1L, pageableAsc));
        }

        /**
         * 대댓글 리스트 조회 성공 테스트
         */
        @Test
        @DisplayName("대댓글 리스트 조회 성공 테스트")
        void getCommentListSuccess3() {
            given(commentRepository.findByPost_IdAndParent_Id(1L, 1L, pageableAsc))
                    .willReturn(new PageImpl<>(List.of(mockComment)));
            given(mockComment.getUser())
                    .willReturn(mockUser);
            given(mockComment.getPost())
                    .willReturn(mockPost);
            given(mockComment.getCreatedAt())
                    .willReturn(Timestamp.valueOf(LocalDateTime.now()));
            given(mockComment.getModifiedAt())
                    .willReturn(Timestamp.valueOf(LocalDateTime.now()));

            assertDoesNotThrow(() -> commentService.getReplyCommentListAsc(1L, 1L, pageableAsc));
        }
    }

    /**
     * 댓글 작성 테스트
     */
    @Nested
    @DisplayName("댓글 작성 테스트")
    class WriteComment {
        @Mock
        CommentWriteRequestDto requestDto;

        @Mock
        ReplyCommentWriteRequestDto replyRequestDto;

        MockedStatic<Comment> commentMockedStatic = mockStatic(Comment.class);

        @AfterEach
        void closeStatic() {
            commentMockedStatic.close();
        }

        /**
         * 댓글 작성 성공 테스트
         */
        @Test
        @DisplayName("댓글 작성 성공 테스트")
        void writeCommentSuccess() {
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));
            given(requestDto.getComment())
                    .willReturn("comment");
            given(Comment.createComment("comment", mockUser, mockPost))
                    .willReturn(mockComment);
            given(mockComment.getCreatedAt())
                    .willReturn(Timestamp.valueOf(LocalDateTime.now()));
            given(mockPost.getUser())
                    .willReturn(otherMockUser);

            assertDoesNotThrow(() -> commentService.writeComment(requestDto, "userName", 1L));
        }

        /**
         * 댓글 작성 성공 테스트 (post 작성자와 다른 사람이 단 경우)
         */
        @Test
        @DisplayName("댓글 작성 성공 테스트")
        void writeCommentSuccess2() {
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));
            given(requestDto.getComment())
                    .willReturn("comment");
            given(Comment.createComment("comment", mockUser, mockPost))
                    .willReturn(mockComment);
            given(mockComment.getCreatedAt())
                    .willReturn(Timestamp.valueOf(LocalDateTime.now()));
            given(mockPost.getUser())
                    .willReturn(otherMockUser);

            given(mockUser.getId())
                    .willReturn(1L);
            given(otherMockUser.getId())
                    .willReturn(2L);

            assertDoesNotThrow(() -> commentService.writeComment(requestDto, "userName", 1L));
        }

        /**
         * 댓글 작성 실패 테스트 (요청한 회원이 존재하지 않는 경우)
         */
        @Test
        @DisplayName("댓글 작성 실패 테스트 (요청한 회원이 존재하지 않는 경우)")
        void writeCommentError1() {

            when(userRepository.findByUserName("userName"))
                    .thenReturn(Optional.empty());

            SNSAppException exception = assertThrows(SNSAppException.class, () -> commentService.writeComment(requestDto, "userName", 1L));
            assertThat(exception.getErrorCode().getMessage()).isEqualTo("해당하는 유저를 찾을 수 없습니다.");

        }

        /**
         * 댓글 작성 실패 테스트 (요청한 포스트가 존재하지 않는 경우)
         */
        @Test
        @DisplayName("댓글 작성 실패 테스트 (요청한 포스트가 존재하지 않는 경우)")
        void writeCommentError2() {
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));

            when(postRepository.findById(1L))
                    .thenReturn(Optional.empty());

            SNSAppException exception = assertThrows(SNSAppException.class, () -> commentService.writeComment(requestDto, "userName", 1L));
            assertThat(exception.getErrorCode().getMessage()).isEqualTo("해당 포스트가 없습니다.");

        }

        /**
         * 댓글 작성 실패 테스트 (요청한 포스트가 존재하지 않는 경우)
         */
        @Test
        @DisplayName("댓글 작성 실패 테스트 (요청한 포스트가 존재하지 않는 경우)")
        void writeCommentError3() {
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));

            when(mockPost.getDeletedAt())
                    .thenReturn(Timestamp.valueOf(LocalDateTime.now()));

            SNSAppException exception = assertThrows(SNSAppException.class, () -> commentService.writeComment(requestDto, "userName", 1L));
            assertThat(exception.getErrorCode().getMessage()).isEqualTo("해당 포스트가 없습니다.");

        }

        /**
         * 대댓글 작성 성공 테스트
         */
        @Test
        @DisplayName("대댓글 작성 성공 테스트")
        void writeReplyCommentSuccess() {
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));
            given(commentRepository.findById(1L))
                    .willReturn(Optional.of(mockParentComment));
            given(replyRequestDto.getReplyComment())
                    .willReturn("comment");
            given(Comment.createReplyComment("comment", mockUser, mockPost, mockParentComment))
                    .willReturn(mockComment);
            given(mockComment.getId())
                    .willReturn(1L);
            given(mockComment.getCreatedAt())
                    .willReturn(Timestamp.valueOf(LocalDateTime.now()));
            given(mockPost.getUser())
                    .willReturn(otherMockUser);

            assertDoesNotThrow(() -> commentService.writeReplyComment(replyRequestDto, "userName", 1L, 1L));
        }

        /**
         * 대댓글 작성 실패 테스트 (요청한 회원이 존재하지 않는 경우)
         */
        @Test
        @DisplayName("대댓글 작성 실패 테스트 (요청한 회원이 존재하지 않는 경우)")
        void writeReplyCommentError1() {

            when(userRepository.findByUserName("userName"))
                    .thenReturn(Optional.empty());

            SNSAppException exception = assertThrows(SNSAppException.class, () -> commentService.writeReplyComment(replyRequestDto, "userName", 1L, 1L));
            assertThat(exception.getErrorCode().getMessage()).isEqualTo("해당하는 유저를 찾을 수 없습니다.");

        }

        /**
         * 대댓글 작성 실패 테스트 (요청한 포스트가 존재하지 않는 경우)
         */
        @Test
        @DisplayName("대댓글 작성 실패 테스트 (요청한 포스트가 존재하지 않는 경우)")
        void writeReplyCommentError2() {
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));

            when(postRepository.findById(1L))
                    .thenReturn(Optional.empty());

            SNSAppException exception = assertThrows(SNSAppException.class, () -> commentService.writeReplyComment(replyRequestDto, "userName", 1L, 1L));

            assertThat(exception.getErrorCode().getMessage()).isEqualTo("해당 포스트가 없습니다.");

        }

        /**
         * 대댓글 작성 실패 테스트 (요청한 포스트가 존재하지 않는 경우)
         */
        @Test
        @DisplayName("대댓글 작성 실패 테스트 (요청한 포스트가 존재하지 않는 경우)")
        void writeReplyCommentError3() {
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));

            when(mockPost.getDeletedAt())
                    .thenReturn(Timestamp.valueOf(LocalDateTime.now()));

            SNSAppException exception = assertThrows(SNSAppException.class, () -> commentService.writeReplyComment(replyRequestDto, "userName", 1L, 1L));

            assertThat(exception.getErrorCode().getMessage()).isEqualTo("해당 포스트가 없습니다.");

        }

        /**
         * 대댓글 작성 실패 테스트 (요청한 부모 댓글이 존재하지 않는 경우)
         */
        @Test
        @DisplayName("대댓글 작성 실패 테스트 (요청한 부모 댓글이 존재하지 않는 경우)")
        void writeReplyCommentError4() {
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));

            when(commentRepository.findById(1L))
                    .thenReturn(Optional.empty());

            SNSAppException exception = assertThrows(SNSAppException.class, () -> commentService.writeReplyComment(replyRequestDto, "userName", 1L, 1L));

            assertThat(exception.getErrorCode().getMessage()).isEqualTo("해당 댓글이 없습니다.");

        }
    }

    @Nested
    @DisplayName("댓글 수정 테스트")
    class ModifyComment {

        @Mock
        CommentModifyRequestDto requestDto;

        /**
         * 댓글 수정 성공 테스트 (ADMIN 사용자가 요청한 경우)
         */
        @Test
        @DisplayName("댓글 수정 성공 테스트 (ADMIN 사용자가 요청한 경우)")
        void modifyCommentSuccess() {
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));
            given(commentRepository.findById(1L))
                    .willReturn(Optional.of(mockComment));
            given(mockUser.getRole())
                    .willReturn(UserRole.ROLE_ADMIN);
            given(mockComment.getUser())
                    .willReturn(otherMockUser);
            given(otherMockUser.getUserName())
                    .willReturn("userName2");
            given(commentRepository.saveAndFlush(mockComment))
                    .willReturn(mockComment);
            given(mockComment.getCreatedAt())
                    .willReturn(Timestamp.valueOf(LocalDateTime.now()));
            given(mockComment.getModifiedAt())
                    .willReturn(Timestamp.valueOf(LocalDateTime.now()));

            assertDoesNotThrow(() -> commentService.modifyComment(requestDto, 1L, 1L, "userName"));
        }

        /**
         * 댓글 수정 성공 테스트 (USER 사용자가 요청했지만, 작성자와 일치하는 경우)
         */
        @Test
        @DisplayName("댓글 수정 성공 테스트 (USER 사용자가 요청했지만, 작성자와 일치하는 경우)")
        void modifyCommentSuccess2() {
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));
            given(commentRepository.findById(1L))
                    .willReturn(Optional.of(mockComment));
            given(mockUser.getRole())
                    .willReturn(UserRole.ROLE_USER);
            given(mockUser.getUserName())
                    .willReturn("userName");
            given(mockComment.getUser())
                    .willReturn(mockUser);
            given(commentRepository.saveAndFlush(mockComment))
                    .willReturn(mockComment);
            given(mockComment.getCreatedAt())
                    .willReturn(Timestamp.valueOf(LocalDateTime.now()));
            given(mockComment.getModifiedAt())
                    .willReturn(Timestamp.valueOf(LocalDateTime.now()));

            assertDoesNotThrow(() -> commentService.modifyComment(requestDto, 1L, 1L, "userName"));
        }

        /**
         * 댓글 수정 실패 테스트 (요청한 사용자가 존재하지 않는 경우)
         */
        @Test
        @DisplayName("댓글 수정 실패 테스트 (요청한 사용자가 존재하지 않는 경우)")
        void modifyCommentError1() {

            when(userRepository.findByUserName("userName"))
                    .thenReturn(Optional.empty());

            SNSAppException exception = assertThrows(SNSAppException.class, () -> commentService.modifyComment(requestDto, 1L, 1L, "userName"));

            assertThat(exception.getErrorCode().getMessage()).isEqualTo("해당하는 유저를 찾을 수 없습니다.");

        }

        /**
         * 댓글 수정 실패 테스트 (요청한 포스트가 존재하지 않는 경우)
         */
        @Test
        @DisplayName("댓글 수정 실패 테스트 (요청한 포스트가 존재하지 않는 경우)")
        void modifyCommentError2() {

            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));

            when(postRepository.findById(1L))
                    .thenReturn(Optional.empty());

            SNSAppException exception = assertThrows(SNSAppException.class, () -> commentService.modifyComment(requestDto, 1L, 1L, "userName"));

            assertThat(exception.getErrorCode().getMessage()).isEqualTo("해당 포스트가 없습니다.");

        }

        /**
         * 댓글 수정 실패 테스트 (요청한 포스트가 존재하지 않는 경우)
         */
        @Test
        @DisplayName("댓글 수정 실패 테스트 (요청한 포스트가 존재하지 않는 경우)")
        void modifyCommentError3() {

            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));

            when(mockPost.getDeletedAt())
                    .thenReturn(Timestamp.valueOf(LocalDateTime.now()));

            SNSAppException exception = assertThrows(SNSAppException.class, () -> commentService.modifyComment(requestDto, 1L, 1L, "userName"));

            assertThat(exception.getErrorCode().getMessage()).isEqualTo("해당 포스트가 없습니다.");

        }

        /**
         * 댓글 수정 실패 테스트 (요청한 댓글이 존재하지 않는 경우)
         */
        @Test
        @DisplayName("댓글 수정 실패 테스트 (요청한 댓글이 존재하지 않는 경우)")
        void modifyCommentError4() {

            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));

            when(commentRepository.findById(1L))
                    .thenReturn(Optional.empty());

            SNSAppException exception = assertThrows(SNSAppException.class, () -> commentService.modifyComment(requestDto, 1L, 1L, "userName"));

            assertThat(exception.getErrorCode().getMessage()).isEqualTo("해당 댓글이 없습니다.");

        }

        /**
         * 댓글 수정 실패 테스트 (작성자와 요청자가 일치하지 않는 경우)
         */
        @Test
        @DisplayName("댓글 수정 실패 테스트 (작성자와 요청자가 일치하지 않는 경우)")
        void modifyCommentError5() {

            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));
            given(commentRepository.findById(1L))
                    .willReturn(Optional.of(mockComment));
            given(mockUser.getRole())
                    .willReturn(UserRole.ROLE_USER);
            given(mockComment.getUser())
                    .willReturn(otherMockUser);
            given(otherMockUser.getUserName())
                    .willReturn("userName2");

            SNSAppException exception = assertThrows(SNSAppException.class, () -> commentService.modifyComment(requestDto, 1L, 1L, "userName"));

            assertThat(exception.getErrorCode().getMessage()).isEqualTo("작성자와 요청자가 일치하지 않습니다.");

        }
    }

    /**
     * 댓글 삭제 테스트
     */
    @Nested
    @DisplayName("댓글 삭제 테스트")
    class DeleteComment {
        /**
         * 댓글 삭제 성공 테스트 (요청자가 ADMIN 사용자인 경우)
         */
        @Test
        @DisplayName("댓글 삭제 성공 테스트 (요청자가 ADMIN 사용자인 경우)")
        void deleteCommentSuccess1() {
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));
            given(commentRepository.findById(1L))
                    .willReturn(Optional.of(mockComment));
            given(mockUser.getRole())
                    .willReturn(UserRole.ROLE_ADMIN);
            given(mockComment.getUser())
                    .willReturn(otherMockUser);
            given(otherMockUser.getUserName())
                    .willReturn("userName2");

            assertDoesNotThrow(() -> commentService.deleteComment(1L, 1L, "userName"));
        }

        /**
         * 댓글 삭제 성공 테스트 (작성자와 요청자가 일치하는 경우)
         */
        @Test
        @DisplayName("댓글 삭제 성공 테스트(작성자와 요청자가 일치하는 경우)")
        void deleteCommentSuccess2() {
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));
            given(commentRepository.findById(1L))
                    .willReturn(Optional.of(mockComment));
            given(mockUser.getRole())
                    .willReturn(UserRole.ROLE_USER);
            given(mockUser.getUserName())
                    .willReturn("userName");
            given(mockComment.getUser())
                    .willReturn(mockUser);

            assertDoesNotThrow(() -> commentService.deleteComment(1L, 1L, "userName"));
        }

        /**
         * 댓글 삭제 실패 테스트 (요청한 사용자가 존재하지 않는 경우)
         */
        @Test
        @DisplayName("댓글 수정 실패 테스트 (요청한 사용자가 존재하지 않는 경우)")
        void deleteCommentError1() {

            when(userRepository.findByUserName("userName"))
                    .thenReturn(Optional.empty());

            SNSAppException exception = assertThrows(SNSAppException.class, () -> commentService.deleteComment(1L, 1L, "userName"));

            assertThat(exception.getErrorCode().getMessage()).isEqualTo("해당하는 유저를 찾을 수 없습니다.");

        }

        /**
         * 댓글 삭제 실패 테스트 (요청한 포스트가 존재하지 않는 경우)
         */
        @Test
        @DisplayName("댓글 삭제 실패 테스트 (요청한 포스트가 존재하지 않는 경우)")
        void deleteCommentError2() {

            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));

            when(postRepository.findById(1L))
                    .thenReturn(Optional.empty());

            SNSAppException exception = assertThrows(SNSAppException.class, () -> commentService.deleteComment(1L, 1L, "userName"));


            assertThat(exception.getErrorCode().getMessage()).isEqualTo("해당 포스트가 없습니다.");

        }

        /**
         * 댓글 삭제 실패 테스트 (요청한 포스트가 존재하지 않는 경우)
         */
        @Test
        @DisplayName("댓글 수정 실패 테스트 (요청한 포스트가 존재하지 않는 경우)")
        void deleteCommentError3() {

            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));

            when(mockPost.getDeletedAt())
                    .thenReturn(Timestamp.valueOf(LocalDateTime.now()));

            SNSAppException exception = assertThrows(SNSAppException.class, () -> commentService.deleteComment(1L, 1L, "userName"));


            assertThat(exception.getErrorCode().getMessage()).isEqualTo("해당 포스트가 없습니다.");

        }

        /**
         * 댓글 삭제 실패 테스트 (요청한 댓글이 존재하지 않는 경우)
         */
        @Test
        @DisplayName("댓글 삭제 실패 테스트 (요청한 댓글이 존재하지 않는 경우)")
        void deleteCommentError4() {

            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));

            when(commentRepository.findById(1L))
                    .thenReturn(Optional.empty());

            SNSAppException exception = assertThrows(SNSAppException.class, () -> commentService.deleteComment(1L, 1L, "userName"));


            assertThat(exception.getErrorCode().getMessage()).isEqualTo("해당 댓글이 없습니다.");

        }

        /**
         * 댓글 삭제 실패 테스트 (작성자와 요청자가 일치하지 않는 경우)
         */
        @Test
        @DisplayName("댓글 삭제 실패 테스트 (작성자와 요청자가 일치하지 않는 경우)")
        void deleteCommentError5() {

            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));
            given(commentRepository.findById(1L))
                    .willReturn(Optional.of(mockComment));
            given(mockUser.getRole())
                    .willReturn(UserRole.ROLE_USER);
            given(mockComment.getUser())
                    .willReturn(otherMockUser);
            given(otherMockUser.getUserName())
                    .willReturn("userName2");

            SNSAppException exception = assertThrows(SNSAppException.class, () -> commentService.deleteComment(1L, 1L, "userName"));

            assertThat(exception.getErrorCode().getMessage()).isEqualTo("작성자와 요청자가 일치하지 않습니다.");

        }

    }
}