package likelion.sns.repository;

import likelion.sns.domain.entity.Alarm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    Page<Alarm> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Modifying
    @Query("update Alarm a set a.deletedAt = CURRENT_DATE where a.targetId = :postId")
    void deleteAlarmWithPost(@Param("postId") Long postId);
}
