package likelion.sns.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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

    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.role = UserRole.USER;
    }
}