package likelion.sns.service;

import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.entity.Like;
import likelion.sns.domain.entity.Post;
import likelion.sns.domain.entity.User;
import likelion.sns.repository.AlarmRepository;
import likelion.sns.repository.LikeRepository;
import likelion.sns.repository.PostRepository;
import likelion.sns.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.when;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private AlarmRepository alarmRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private LikeService likeService;

    @Mock
    private User mockUser;
    @Mock
    private User otherMockUser;
    @Mock
    private Post mockPost;
    @Mock
    private Like mockLike;

    /**
     * 좋아요 입력하기 테스트
     */
    @Nested
    @DisplayName("좋아요 입력하기 테스트")
    class AddLike {

        /**
         * 좋아요 입력하기 성공 테스트
         */
        @Test
        @DisplayName("좋아요 입력하기 테스트 성공")
        void addLikeSuccess(){
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));
            given(mockUser.getId())
                    .willReturn(1L);
            given(mockPost.getUser())
                    .willReturn(mockUser);
            given(likeRepository.findByUser_IdAndPost_Id(1L, 1L))
                    .willReturn(Optional.empty());

            assertDoesNotThrow(() -> likeService.AddLike(1L, "userName"));
        }

        /**
         * 좋아요 입력하기 성공 테스트 (다른 사용자가 누른경우 알림 발생)
         */
        @Test
        @DisplayName("좋아요 입력하기 테스트 성공")
        void addLikeSuccess2(){
            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));
            given(mockUser.getId())
                    .willReturn(1L);
            given(mockPost.getUser())
                    .willReturn(otherMockUser);
            given(otherMockUser.getId())
                    .willReturn(2L);
            given(likeRepository.findByUser_IdAndPost_Id(1L, 1L))
                    .willReturn(Optional.empty());

            assertDoesNotThrow(() -> likeService.AddLike(1L, "userName"));
        }

        /**
         * 좋아요 입력 실패 (요청한 회원이 존재하지 않는 경우)
         */
        @Test
        @DisplayName("좋아요 입력 실패 테스트 (요청한 회원이 존재하지 않는 경우)")
        void AddLikeError1(){

            when(userRepository.findByUserName("userName"))
                    .thenReturn(Optional.empty());

            SNSAppException exception = assertThrows(SNSAppException.class, () -> likeService.AddLike(1L, "userName"));

            assertThat(exception.getErrorCode().getMessage())
                    .isEqualTo("해당하는 유저를 찾을 수 없습니다.");
        }

        /**
         * 좋아요 입력 실패 (요청한 게시글이 존재하지 않는 경우)
         */
        @Test
        @DisplayName("좋아요 입력 실패 테스트 (요청한 게시글이 존재하지 않는 경우)")
        void AddLikeError2(){

            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));

            when(postRepository.findById(1L))
                    .thenReturn(Optional.empty());

            SNSAppException exception = assertThrows(SNSAppException.class, () -> likeService.AddLike(1L, "userName"));

            assertThat(exception.getErrorCode().getMessage())
                    .isEqualTo("해당 포스트가 없습니다.");
        }

        /**
         * 좋아요 입력 실패 (요청한 게시글이 존재하지 않는 경우)
         */
        @Test
        @DisplayName("좋아요 입력 실패 테스트 (요청한 게시글이 존재하지 않는 경우)")
        void AddLikeError3(){

            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));

            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));

            when(mockPost.getDeletedAt())
                    .thenReturn(Timestamp.valueOf(LocalDateTime.now()));

            SNSAppException exception = assertThrows(SNSAppException.class, () -> likeService.AddLike(1L, "userName"));

            assertThat(exception.getErrorCode().getMessage())
                    .isEqualTo("해당 포스트가 없습니다.");
        }

        /**
         * 좋아요 입력 실패 (이미 좋아요를 누른 경우)
         */
        @Test
        @DisplayName("좋아요 입력 실패 테스트 (이미 좋아요를 누른 경우)")
        void AddLikeError4(){

            given(userRepository.findByUserName("userName"))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));
            given(mockUser.getId())
                    .willReturn(1L);

            when(likeRepository.findByUser_IdAndPost_Id(1L,1L))
                    .thenReturn(Optional.of(mockLike));

            SNSAppException exception = assertThrows(SNSAppException.class, () -> likeService.AddLike(1L, "userName"));

            assertThat(exception.getErrorCode().getMessage())
                    .isEqualTo("좋아요는 한번만 누를 수 있습니다.");
        }
    }

    /**
     * 좋아요 갯수 구하기 테스트
     */
    @Nested
    @DisplayName("좋아요 갯수 구하기 테스트")
    class GetLikesCount {
        /**
         * 좋아요 갯수 구하기 성공 테스트
         */
        @Test
        @DisplayName("좋아요 갯수 구하기 성공 테스트")
        void getLikesCountSuccess(){
            given(postRepository.findById(1L))
                    .willReturn(Optional.of(mockPost));

            assertDoesNotThrow(() -> likeService.getLikesCount(1L));

        }
        /**
         * 좋아요 갯수 구하기 실패 테스트 (게시글이 존재하지 않는 경우)
         */
        @Test
        @DisplayName("좋아요 갯수 구하기 실패 테스트 (게시글이 존재하지 않는 경우)")
        void getLikesCountError(){
            when(postRepository.findById(1L))
                    .thenReturn(Optional.empty());

            SNSAppException exception = assertThrows(SNSAppException.class, () -> likeService.getLikesCount(1L));

            assertThat(exception.getErrorCode().getMessage())
                    .isEqualTo("해당 포스트가 없습니다.");

        }
    }

}