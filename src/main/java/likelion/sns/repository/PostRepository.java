package likelion.sns.repository;

import likelion.sns.domain.entity.Post;
import likelion.sns.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);
    Page<Post> findByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId,Pageable pageable);

}
