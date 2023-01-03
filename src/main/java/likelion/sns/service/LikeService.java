package likelion.sns.service;

import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.entity.Like;
import likelion.sns.domain.entity.Post;
import likelion.sns.domain.entity.User;
import likelion.sns.repository.LikeRepository;
import likelion.sns.repository.PostRepository;
import likelion.sns.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * 좋아요 입력하기, 한 계정당 하나만 가능
     */

    @Transactional
    public void AddLike(Long postId, String requestUserName) throws SQLException {

        // 해당하는 회원이 없을 시, 예외 처리
        User requestUser = userRepository.findByUserName(requestUserName)
                .orElseThrow(() -> new SNSAppException(ErrorCode.USERNAME_NOT_FOUND));

        // 해당하는 게시글이 없을 시, 예외 처리
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new SNSAppException(ErrorCode.POST_NOT_FOUND));

        Long requestUserId = requestUser.getId();
        log.info("좋아요를 누른 회원 id = {} , 게시글 id = {} ",requestUserId,postId);

        // 이미 좋아요를 누른 경우 2번 입력할 수 없으므로 예외처리
        likeRepository.findByUser_IdAndPost_Id(requestUserId,postId)
                .ifPresent((like) -> {throw new SNSAppException(ErrorCode.FORBIDDEN_ADD_LIKE);});

        likeRepository.save(new Like(requestUser, post));

    }

    /**
     * 좋아요 개수
     */
    public Long getLikesCount(Long postId) {

        // 해당하는 게시글이 없을 시, 예외 처리
        postRepository.findById(postId)
                .orElseThrow(() -> new SNSAppException(ErrorCode.POST_NOT_FOUND));

        Long totalCount = likeRepository.countByPost_Id(postId);
        log.info("좋아요 총 갯수 : {} ", totalCount);

        return totalCount;
    }
}
