package likelion.sns.service;

import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.comment.modify.CommentModifyRequestDto;
import likelion.sns.domain.dto.comment.modify.CommentModifyResponseDto;
import likelion.sns.domain.dto.comment.read.CommentListDto;
import likelion.sns.domain.dto.comment.write.CommentWriteRequestDto;
import likelion.sns.domain.dto.comment.write.CommentWriteResponseDto;
import likelion.sns.domain.entity.*;
import likelion.sns.repository.AlarmRepository;
import likelion.sns.repository.CommentRepository;
import likelion.sns.repository.PostRepository;
import likelion.sns.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final AlarmRepository alarmRepository;

    /**
     * 댓글 리스트 조회 (특정 포스트의 댓글만 최신순으로)
     */
    public Page<CommentListDto> getCommentList(Long postId, Pageable pageable) throws SQLException {
        return commentRepository.findByPost_IdOrderByCreatedAtDesc(postId, pageable).map(comment -> new CommentListDto(comment));
    }

    /**
     * 댓글 리스트 조회 (특정 포스트의 댓글만)
     */
    public Page<CommentListDto> getCommentListAsc(Long postId, Pageable pageable) throws SQLException {
        return commentRepository.findByPost_Id(postId, pageable).map(comment -> new CommentListDto(comment));
    }

    /**
     * 댓글 작성 (특정 포스트에)
     */
    @Transactional
    public CommentWriteResponseDto writeComment(CommentWriteRequestDto requestDto, String requestUserName, Long postId) throws SQLException {
        //user 유효성 검사하고 찾아오기
        User requestUser = userValid(requestUserName);

        // post 유효성 검사하고 찾아오기
        Post foundPost = postValid(postId);

        String commentBody = requestDto.getComment();
        log.info("댓글 내용 = {}", commentBody);

        // 댓글 저장
        Comment comment = Comment.createComment(commentBody, requestUser, foundPost);
        commentRepository.save(comment);

        // 댓글 저장 시 알림 저장
        saveAlarm(foundPost, requestUser);

        return new CommentWriteResponseDto(comment, requestUserName, postId);
    }

    /**
     * 댓글 수정 (특정 포스트의 특정 댓글)
     */
    @Transactional
    public void modifyComment(CommentModifyRequestDto requestDto, Long postId, Long commentId, String requestUserName) throws SQLException {

        //user 유효성 검사하고 찾아오기
        User requestUser = userValid(requestUserName);

        // post 유효성 검사
        postValid(postId);

        // 댓글 유효성 검사
        Comment foundComment = commentValid(commentId);


        User foundUser = foundComment.getUser();

        UserRole requestUserRole = requestUser.getRole();
        String author = foundUser.getUserName();
        log.info("댓글 수정 요청자 ROLE = {} 댓글 작성자 userName = {}", requestUserRole, author);

        // 작성자와 요청자 유효성 검사
        checkAuth(requestUserName, author, requestUserRole);

        String newComment = requestDto.getComment();
        log.info("댓글 수정 요청 내용 = {}", newComment);

        foundComment.modifyComment(newComment);
    }

    /**
     * 댓글 단건 조회 (수정 적용 후, 데이터를 가져오기 위해서 만든 메서드)
     */
    public CommentModifyResponseDto getOneComment(Long postId, Long commentId, String requestUserName) {
        Comment foundComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new SNSAppException(ErrorCode.COMMENT_NOT_FOUND));
        return new CommentModifyResponseDto(foundComment, requestUserName, postId);
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteComment(Long postId, Long commentId, String requestUserName) throws SQLException {
        //user 유효성 검사하고 찾아오기
        User requestUser = userValid(requestUserName);

        //post 유효성 검사
        postValid(postId);

        // 댓글 유효성 검사
        Comment foundComment = commentValid(commentId);

        User foundUser = foundComment.getUser();

        UserRole requestUserRole = requestUser.getRole();
        String author = foundUser.getUserName();
        log.info("댓글 수정 요청자 ROLE = {} 댓글 작성자 userName = {}", requestUserRole, author);

        // 작성자와 요청자 유효성 검사
        checkAuth(requestUserName, author, requestUserRole);

        commentRepository.delete(foundComment);
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
        if (foundPost.getDeletedAt() != null) {
            throw new SNSAppException(ErrorCode.POST_NOT_FOUND);
        }

        return foundPost;
    }

    /**
     * 댓글 유효성 검사 ( 존재하는 댓글인지)
     */
    public Comment commentValid(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new SNSAppException(ErrorCode.COMMENT_NOT_FOUND));
    }


    /**
     * 내가 작성하지 않은 게시글에 댓글 입력 시 알림 저장
     */
    public void saveAlarm(Post foundPost, User requestUser) {
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

    /**
     * 권한이 ADMIN 이 아니면서 요청자와 작성자가 다른 경우
     */
    public void checkAuth(String requestUserName, String author, UserRole requestUserRole) {
        if (!requestUserRole.equals(UserRole.ROLE_ADMIN) && !author.equals(requestUserName)) {
            throw new SNSAppException(ErrorCode.USER_NOT_MATCH);
        }
    }
}
