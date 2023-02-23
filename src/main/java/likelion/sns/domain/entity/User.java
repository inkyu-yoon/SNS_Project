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

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "user_name", unique = true,nullable = false)
    private String userName;

    @OneToMany(mappedBy = "user")
    private List<Post> posts = new ArrayList<>();

    /**
     * 정적 팩토리 메서드 용 생성자
     */
    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.role = UserRole.ROLE_USER;
    }

    public static User createUser(String userName, String password){
        return new User(userName, password);
    }
    public void changeRole() {
        if (this.role.equals(UserRole.ROLE_USER)) {
            this.role = UserRole.ROLE_ADMIN;
        } else {
            this.role = UserRole.ROLE_USER;
        }
    }
}