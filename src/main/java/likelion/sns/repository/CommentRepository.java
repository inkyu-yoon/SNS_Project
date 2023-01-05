package likelion.sns.repository;

import likelion.sns.domain.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByPost_IdOrderByCreatedAtDesc(Long postId, Pageable pageable);
    Page<Comment> findByPost_Id(Long postId, Pageable pageable);

}
