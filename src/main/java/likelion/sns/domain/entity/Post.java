package likelion.sns.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Where(clause = "deleted_at is null")
@SQLDelete(sql = "UPDATE post SET deleted_at = now() WHERE post_id = ?")
public class Post extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    private String title;

    private String body;




    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "post")
    List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    List<Like> likes = new ArrayList<>();


    /**
     * 정적 팩토리 메서드 용 생성자
     */
    public Post(String title, String body, User user) {
        this.title = title;
        this.body = body;
        this.user = user;
    }

    public static Post writePost(String title, String body, User user) {
        return new Post(title, body, user);
    }

    public void modifyPost(String title, String body) {
        this.title = title;
        this.body = body;
    }

}
