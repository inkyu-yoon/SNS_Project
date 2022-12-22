package likelion.sns.service;


import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.join.UserJoinRequestDto;
import likelion.sns.domain.dto.join.UserJoinResponseDto;
import likelion.sns.domain.dto.login.UserLoginRequestDto;
import likelion.sns.domain.dto.login.UserLoginResponseDto;
import likelion.sns.domain.entity.User;
import likelion.sns.domain.entity.UserRole;
import likelion.sns.jwt.JwtTokenUtil;
import likelion.sns.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @Value("${jwt.token.secret}")
    private String secretKey;

    @Transactional
    public UserJoinResponseDto createUser(UserJoinRequestDto joinRequest) throws SQLException  {
        //만약 db에 가입 요청한 아이디가 이미 존재할 시, 에러를 발생시킴
        userRepository.findByUserName(joinRequest.getUserName()).ifPresent(user -> {
            throw new SNSAppException(ErrorCode.DUPLICATED_USER_NAME, joinRequest.getUserName()+"는(은) 이미 존재합니다.");
        });

        // 회원 가입으로 입력한 raw한 비밀번호를 암호화 한다.
        String password = encoder.encode(joinRequest.getPassword());

        // 회원 가입 요청을 입려한 userName 과 암호화한 password를 db에 저장
        User saved = userRepository.save(joinRequest.toEntity(password));

        return new UserJoinResponseDto(saved);
    }

    public UserLoginResponseDto loginUser(UserLoginRequestDto loginRequest) throws SQLException {
        String inputUsername = loginRequest.getUserName();
        String inputPassword = loginRequest.getPassword();

        // 로긘 요청한 userName이 가입된 적 없으면 에러를 발생시킴
        User found = userRepository.findByUserName(inputUsername).orElseThrow(() -> new SNSAppException(ErrorCode.USERNAME_NOT_FOUND, inputUsername+ "에 해당하는 회원을 찾을 수 없습니다."));

        // 가입된 회원이지만, 입력한 비밀번호화 DB에 입력된 비밀번호가 다를 경우, 에러를 발생시킴
        if (!encoder.matches(inputPassword, found.getPassword())) {
            throw new SNSAppException(ErrorCode.INVALID_PASSWORD, "잘못된 비밀번호 입니다");
        }

        // 로그인에 성공할 시, token을 create 하고 반환
        return new UserLoginResponseDto(JwtTokenUtil.createToken(inputUsername, secretKey));
    }

    public UserRole findRoleByUserName(String userName) {
        return userRepository.findByUserName(userName).get().getRole();
    }


}
