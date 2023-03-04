package likelion.sns.repository;

import likelion.sns.domain.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>,PostCustomRepository {

    Page<Post> findByOrderByCreatedAtDesc(Pageable pageable);
    Page<Post> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Post> findByTitleContainingOrderByCreatedAtDesc(String title, Pageable pageable);

}
