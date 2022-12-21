package likelion.sns.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "user_name",unique = true)
    private String userName;

    @OneToMany(mappedBy = "user")
    private List<Post> posts = new ArrayList<>();

    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.role = UserRole.USER;
    }
}