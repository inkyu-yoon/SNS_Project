package likelion.sns.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "likes")
@Where(clause = "deleted_at is null")
@SQLDelete(sql = "UPDATE likes SET deleted_at = now() WHERE like_id = ?")
public class Like extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    /**
     * 정적 팩토리 메서드 용 생성자
     */
    public Like(User user, Post post) {
        this.user = user;
        this.post = post;
    }

    public static Like createLike(User user, Post post) {
        return new Like(user, post);
    }
}
