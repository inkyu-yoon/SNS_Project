package likelion.sns.service;

import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.entity.*;
import likelion.sns.repository.AlarmRepository;
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

    private final AlarmRepository alarmRepository;

    /**
     * 좋아요 입력하기, 한 계정당 하나만 가능
     */

    @Transactional
    public void AddLike(Long postId, String requestUserName) throws SQLException {

        // 해당하는 회원이 없을 시, 예외 처리
        User requestUser = userRepository.findByUserName(requestUserName)
                .orElseThrow(() -> new SNSAppException(ErrorCode.USERNAME_NOT_FOUND));

        // 해당하는 게시글이 없을 시, 예외 처리
        Post foundPost = postRepository.findById(postId)
                .orElseThrow(() -> new SNSAppException(ErrorCode.POST_NOT_FOUND));

        Long requestUserId = requestUser.getId();
        log.info("좋아요를 누른 회원 id = {} , 게시글 id = {} ", requestUserId, postId);

        // 이미 좋아요를 누른 경우 2번 입력할 수 없으므로 예외처리
        likeRepository.findByUser_IdAndPost_Id(requestUserId, postId)
                .ifPresent((like) -> {
                    throw new SNSAppException(ErrorCode.FORBIDDEN_ADD_LIKE);
                });

        likeRepository.save(new Like(requestUser, foundPost));

        // 좋아요 입력 시 , 알림 엔티티에 데이터 저장
        // 좋아요가 달린 게시글 id
        Long targetId = foundPost.getId();

        // 게시글을 작성한 user
        User postAuthor = foundPost.getUser();

        // 알림을 발생시킨(좋아요를 입력한) user id
        Long fromUserId = requestUser.getId();
        log.info("게시글 id : {} 댓글 작성자 id : {} 알림이 도착할 사용자 id:{}", targetId, fromUserId, postAuthor);

        //만약 게시글 주인과 알림을 발생시킨 아이디가 같다면, 알림을 저장하지 않음
        if (!(postAuthor.getId() == fromUserId)) {
            alarmRepository.save(new Alarm(postAuthor, AlarmType.NEW_LIKE_ON_POST, fromUserId, targetId));
        }


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
