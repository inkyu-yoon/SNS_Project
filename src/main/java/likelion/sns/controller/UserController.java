package likelion.sns.controller;

import likelion.sns.domain.Response;
import likelion.sns.domain.dto.join.UserJoinRequestDto;
import likelion.sns.domain.dto.join.UserJoinResponseDto;
import likelion.sns.domain.dto.login.UserLoginRequestDto;
import likelion.sns.domain.dto.login.UserLoginResponseDto;
import likelion.sns.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/join")
    public Response join(@RequestBody UserJoinRequestDto userJoinRequestDto) throws SQLException {
        UserJoinResponseDto user = userService.createUser(userJoinRequestDto);
        return Response.success(user);
    }

    @PostMapping("/login")
    public Response login(@RequestBody UserLoginRequestDto userLoginRequestDto) throws SQLException {
        return Response.success(userService.loginUser(userLoginRequestDto));
    }
}
