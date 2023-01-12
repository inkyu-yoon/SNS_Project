package likelion.sns.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "deleted_at is null")
@SQLDelete(sql = "UPDATE comment SET deleted_at = now() WHERE comment_id = ?")
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    private String comment;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();


    /**
     * 정적 팩토리 메서드용 생성자
     */
    public Comment(String comment, User user, Post post) {
        this.comment = comment;
        this.user = user;
        this.post = post;
    }

    public Comment(String comment, User user, Post post, Comment parent) {
        this.comment = comment;
        this.user = user;
        this.post = post;
        this.parent = parent;
    }

    public static Comment createComment(String comment, User user, Post post) {
        return new Comment(comment, user, post);
    }

    public static Comment createReplyComment(String comment, User user, Post post, Comment parent) {
        return new Comment(comment, user, post, parent);
    }

    public void modifyComment(String newComment) {
        this.comment = newComment;
    }
}
