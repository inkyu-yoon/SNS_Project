package likelion.sns.service;

import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.comment.read.CommentListDto;
import likelion.sns.domain.dto.comment.write.CommentWriteRequestDto;
import likelion.sns.domain.dto.comment.write.CommentWriteResponseDto;
import likelion.sns.domain.dto.post.read.PostListDto;
import likelion.sns.domain.dto.post.write.PostWriteRequestDto;
import likelion.sns.domain.dto.post.write.PostWriteResponseDto;
import likelion.sns.domain.entity.Comment;
import likelion.sns.domain.entity.Post;
import likelion.sns.domain.entity.User;
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
        User foundUser = userRepository.findByUserName(requestUserName)
                .orElseThrow(() -> new SNSAppException(ErrorCode.USERNAME_NOT_FOUND));

        // 해당하는 게시글이 없을 시, 예외 처리
        Post foundPost = postRepository.findById(postId).orElseThrow(() -> new SNSAppException(ErrorCode.POST_NOT_FOUND));

        // 댓글 저장
        Comment comment = new Comment(requestDto.getComment(), foundUser, foundPost);
        commentRepository.save(comment);

        return new CommentWriteResponseDto(comment,requestUserName,postId);
    }
}
