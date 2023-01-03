package likelion.sns.service;

import likelion.sns.domain.dto.comment.read.CommentListDto;
import likelion.sns.domain.dto.post.read.PostListDto;
import likelion.sns.repository.CommentRepository;
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

    /**
     * 댓글 리스트 조회 (특정 포스트의 댓글만)
     */
    public Page<CommentListDto> getCommentList(Long postId,Pageable pageable) throws SQLException {
        return commentRepository.findByPost_IdOrderByCreatedAtDesc(postId,pageable).map(comment -> new CommentListDto(comment));
    }

}
