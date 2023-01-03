package likelion.sns.service;

import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.comment.modify.CommentModifyRequestDto;
import likelion.sns.domain.dto.comment.modify.CommentModifyResponseDto;
import likelion.sns.domain.dto.comment.read.CommentListDto;
import likelion.sns.domain.dto.comment.write.CommentWriteRequestDto;
import likelion.sns.domain.dto.comment.write.CommentWriteResponseDto;
import likelion.sns.domain.entity.Comment;
import likelion.sns.domain.entity.Post;
import likelion.sns.domain.entity.User;
import likelion.sns.domain.entity.UserRole;
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

    /**
     * 댓글 리스트 조회 (특정 포스트의 댓글만)
     */
    public Page<CommentListDto> getCommentList(Long postId,Pageable pageable) throws SQLException {
        return commentRepository.findByPost_IdOrderByCreatedAtDesc(postId,pageable).map(comment -> new CommentListDto(comment));
    }

    /**
     * 댓글 작성 (특정 포스트에)
     */
    @Transactional
    public CommentWriteResponseDto writeComment(CommentWriteRequestDto requestDto, String requestUserName, Long postId) throws SQLException {
        // 해당하는 회원이 없을 시, 예외 처리
        User requestUser = userRepository.findByUserName(requestUserName)
                .orElseThrow(() -> new SNSAppException(ErrorCode.USERNAME_NOT_FOUND));

        // 해당하는 게시글이 없을 시, 예외 처리
        Post foundPost = postRepository.findById(postId)
                .orElseThrow(() -> new SNSAppException(ErrorCode.POST_NOT_FOUND));

        // 댓글 저장
        Comment comment = new Comment(requestDto.getComment(), requestUser, foundPost);
        commentRepository.save(comment);

        return new CommentWriteResponseDto(comment,requestUserName,postId);
    }

    /**
     * 댓글 수정 (특정 포스트의 특정 댓글)
     */
    @Transactional
    public void modifyComment(CommentModifyRequestDto requestDto, Long postId, Long commentId, String requestUserName) throws SQLException {

        // 해당하는 회원이 없을 시, 예외 처리
        User requestUser = userRepository.findByUserName(requestUserName)
                .orElseThrow(() -> new SNSAppException(ErrorCode.USERNAME_NOT_FOUND));

        // 해당하는 게시글이 없을 시, 예외 처리
        postRepository.findById(postId)
                .orElseThrow(() -> new SNSAppException(ErrorCode.POST_NOT_FOUND));

        // 수정 요청하는 댓글이 존재하지 않을 시, 예외 처리
        Comment foundComment = commentRepository.findById(commentId)
                .orElseThrow(()-> new SNSAppException(ErrorCode.COMMENT_NOT_FOUND));

        User foundUser = foundComment.getUser();

        UserRole requestUserRole = requestUser.getRole();
        String userName = foundUser.getUserName();
        log.info("댓글 수정 요청자 ROLE = {}", requestUserRole);
        log.info("댓글 작성자 userName = {}", userName);

        //작성자와 유저가 일치하지 않음 (단 ADMIN이면 수정 가능함)
        if (!requestUserRole.equals(UserRole.ROLE_ADMIN) && !userName.equals(requestUserName)) {
            throw new SNSAppException(ErrorCode.USER_NOT_MATCH);
        }

        String newComment = requestDto.getComment();
        log.info("댓글 수정 요청 내용 = {}",newComment);

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
        // 해당하는 회원이 없을 시, 예외 처리
        User requestUser = userRepository.findByUserName(requestUserName)
                .orElseThrow(() -> new SNSAppException(ErrorCode.USERNAME_NOT_FOUND));

        // 해당하는 게시글이 없을 시, 예외 처리
        postRepository.findById(postId)
                .orElseThrow(() -> new SNSAppException(ErrorCode.POST_NOT_FOUND));

        // 수정 요청하는 댓글이 존재하지 않을 시, 예외 처리
        Comment foundComment = commentRepository.findById(commentId)
                .orElseThrow(()-> new SNSAppException(ErrorCode.COMMENT_NOT_FOUND));

        User foundUser = foundComment.getUser();

        UserRole requestUserRole = requestUser.getRole();
        String userName = foundUser.getUserName();
        log.info("댓글 삭제 요청자 ROLE = {}", requestUserRole);
        log.info("댓글 작성자 userName = {}", userName);

        //작성자와 유저가 일치하지 않음 (단 ADMIN이면 수정 가능함)
        if (!requestUserRole.equals(UserRole.ROLE_ADMIN) && !userName.equals(requestUserName)) {
            throw new SNSAppException(ErrorCode.USER_NOT_MATCH);
        }

        commentRepository.delete(foundComment);
    }
}
