package likelion.sns.controller;

import likelion.sns.domain.Response;
import likelion.sns.domain.dto.join.UserJoinRequestDto;
import likelion.sns.domain.dto.join.UserJoinResponseDto;
import likelion.sns.domain.dto.login.UserLoginRequestDto;
import likelion.sns.domain.dto.login.UserLoginResponseDto;
import likelion.sns.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
@Slf4j
public class UserController {
    private final UserService userService;

    /**
     회원 가입
     **/
    @PostMapping("/join")
    public Response join(@RequestBody UserJoinRequestDto requestDto) throws SQLException {
        UserJoinResponseDto user = userService.createUser(requestDto);
        log.info("{}",user);

        return Response.success(user);
    }

    /**
     회원 로그인
     **/
    @PostMapping("/login")
    public Response login(@RequestBody UserLoginRequestDto requestDto) throws SQLException {
        log.info("{}",requestDto);

        UserLoginResponseDto responseDto = userService.loginUser(requestDto);
        log.info("{}",responseDto);

        return Response.success(responseDto);
    }
}
