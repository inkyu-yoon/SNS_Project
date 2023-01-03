package likelion.sns.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Post extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    private String title;

    private String body;

    private Timestamp deletedAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Post(String title, String body,User user) {
        this.title = title;
        this.body = body;
        this.user = user;
    }

    public void modifyPost(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public void deletePost() {
        this.deletedAt = Timestamp.valueOf(LocalDateTime.now());
    }
}
