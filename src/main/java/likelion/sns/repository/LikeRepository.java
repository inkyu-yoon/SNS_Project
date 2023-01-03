package likelion.sns.repository;

import likelion.sns.domain.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUser_IdAndPost_Id(Long userId, Long postId);

    Long countByPost_Id(Long PostId);
}
