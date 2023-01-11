package likelion.sns.service;


import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.user.changeRole.UserRoleChangeRequestDto;
import likelion.sns.domain.dto.user.join.UserJoinRequestDto;
import likelion.sns.domain.dto.user.join.UserJoinResponseDto;
import likelion.sns.domain.dto.user.login.UserLoginRequestDto;
import likelion.sns.domain.dto.user.login.UserLoginResponseDto;
import likelion.sns.domain.entity.User;
import likelion.sns.domain.entity.UserRole;
import likelion.sns.jwt.JwtTokenUtil;
import likelion.sns.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @Value("${jwt.token.secret}")
    private String secretKey;

    /**
     * 회원 가입
     */
    @Transactional
    public UserJoinResponseDto createUser(UserJoinRequestDto joinRequest) throws SQLException  {

        String requestUserName = joinRequest.getUserName();

        //만약 db에 가입 요청한 아이디가 이미 존재할 시, 에러를 발생시킴
        userJoinValid(requestUserName);

        // 회원 가입으로 입력한 raw한 비밀번호를 암호화 한다.
        String encodedPassword = encoder.encode(joinRequest.getPassword());

        // 회원 가입 요청을 입려한 userName 과 암호화한 password를 db에 저장
        User saved = userRepository.save(User.createUser(requestUserName, encodedPassword));

        return new UserJoinResponseDto(saved);
    }

    /**
     * 회원 로그인
     */

    public UserLoginResponseDto loginUser(UserLoginRequestDto loginRequest) throws SQLException {
        String requestUserName = loginRequest.getUserName();
        String requestPassword = loginRequest.getPassword();

        // 로큰 요청한 userName이 가입된 적 없으면 에러를 발생시킴
        User found = userValid(requestUserName);

        // 가입된 회원이지만, 입력한 비밀번호화 DB에 입력된 비밀번호가 다를 경우, 에러를 발생시킴
        if (!encoder.matches(requestPassword, found.getPassword())) {
            throw new SNSAppException(ErrorCode.INVALID_PASSWORD, "잘못된 비밀번호 입니다");
        }

        // 로그인에 성공할 시, token을 create 하고 반환
        return new UserLoginResponseDto(JwtTokenUtil.createToken(requestUserName, secretKey));
    }



    /**
     * UserRole(권한) 변경
     */
    @Transactional
    public void changeRole(Long userId, UserRoleChangeRequestDto requestDto) {

        String requestRole = requestDto.getRole();

        // request 로 받은 데이터가 유효한 데이터(admin 혹은 user)가 아닌 경우 에러 처리
        if (!requestRole.equalsIgnoreCase("admin") || !requestRole.equalsIgnoreCase("user")) {
            throw new SNSAppException(ErrorCode.BAD_REQUEST);
        }

        // UserRole(권한) 을 변경할 회원이 DB에 없는 경우 에러 처리
        User found = userRepository.findById(userId)
                .orElseThrow(() -> new SNSAppException(ErrorCode.USERNAME_NOT_FOUND));

        log.info("{}", found);

        found.changeRole(requestRole);
    }

    /**
     * JwtTokenFilter 에서 사용하기 위해 만든 메서드
     */
    public UserRole findRoleByUserName(String userName) {
        return userRepository.findByUserName(userName).get().getRole();
    }

    /**
     * 해당하는 회원이 존재할 시, 예외 처리
     */
    public void userJoinValid(String userName) {
        userRepository.findByUserName(userName)
                .ifPresent(user -> {throw new SNSAppException(ErrorCode.DUPLICATED_USER_NAME);});

    }

    /**
     * 해당하는 회원이 없을 시, 예외 처리
     */
    public User userValid(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new SNSAppException(ErrorCode.USERNAME_NOT_FOUND));
    }
}
