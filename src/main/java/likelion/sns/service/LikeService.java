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

        //user 유효성 검사하고 찾아오기
        User requestUser = userValid(requestUserName);

        // post 유효성 검사하고 찾아오기
        Post foundPost = postValid(postId);

        Long requestUserId = requestUser.getId();
        log.info("좋아요를 누른 회원 id = {} , 게시글 id = {} ", requestUserId, postId);

        // 이미 좋아요를 누른 경우 2번 입력할 수 없으므로 예외처리
        likeValid(requestUserId,postId);

        likeRepository.save(Like.createLike(requestUser,foundPost));


        // 좋아요 입력 후 , 유효한 알람이라면 알림 엔티티에 데이터 저장
        saveAlarm(foundPost, requestUser);

    }

    /**
     * 좋아요 개수
     */
    public Long getLikesCount(Long postId) {

        // 게시글 유효성 검사
        postValid(postId);

        Long totalCount = likeRepository.countByPost_Id(postId);
        log.info("좋아요 총 갯수 : {} ", totalCount);

        return totalCount;
    }

    /*
    아래 메서드는 유효성 검사 및 중복 메서드 정리
     */

    /**
     * 해당하는 회원이 없을 시, 예외 처리
     */
    public User userValid(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new SNSAppException(ErrorCode.USERNAME_NOT_FOUND));
    }

    /**
     * 해당하는 게시글이 없을 시, 그리고 deletedAt 데이터가 있을 시 예외 처리
     */
    public Post postValid(Long postId) {

        //DB에 저장되어 있는 게시글이 없는 경우
        Post foundPost = postRepository.findById(postId)
                .orElseThrow(() -> new SNSAppException(ErrorCode.POST_NOT_FOUND));

        // deletedAt 에 데이터가 채워져서 삭제 처리가 된 경우
        if(foundPost.getDeletedAt() != null){
            throw new SNSAppException(ErrorCode.POST_NOT_FOUND);
        }

        return foundPost;
    }

    /**
     * 좋아요를 이미 누른 회원인 경우 예외처리
     */
    public void likeValid(Long requestUserId , Long postId) {
        likeRepository.findByUser_IdAndPost_Id(requestUserId, postId)
                .ifPresent((like) -> {
                    throw new SNSAppException(ErrorCode.FORBIDDEN_ADD_LIKE);
                });
    }

    /**
     * 내가 작성하지 않은 게시글에 좋아요 입력 시 알림 저장
     */
    public void saveAlarm(Post foundPost,User requestUser) {
        Long targetId = foundPost.getId();

        // 게시글을 작성한 user
        User postAuthor = foundPost.getUser();

        // 알림을 발생시킨(좋아요를 입력한) user id
        Long fromUserId = requestUser.getId();
        log.info("게시글 id : {} 댓글 작성자 id : {} 알림이 도착할 사용자 id:{}", targetId, fromUserId, postAuthor);

        //만약 게시글 주인과 알림을 발생시킨 아이디가 같다면, 알림을 저장하지 않음
        if (!(postAuthor.getId() == fromUserId)) {
            alarmRepository.save(Alarm.createAlarm(postAuthor, AlarmType.NEW_LIKE_ON_POST, fromUserId, targetId));
        }
    }

}
