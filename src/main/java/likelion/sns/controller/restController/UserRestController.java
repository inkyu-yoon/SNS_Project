package likelion.sns.controller.restController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import likelion.sns.domain.Response;
import likelion.sns.domain.dto.changeRole.UserRoleChangeRequestDto;
import likelion.sns.domain.dto.changeRole.UserRoleChangeResponseDto;
import likelion.sns.domain.dto.join.UserJoinRequestDto;
import likelion.sns.domain.dto.join.UserJoinResponseDto;
import likelion.sns.domain.dto.login.UserLoginRequestDto;
import likelion.sns.domain.dto.login.UserLoginResponseDto;
import likelion.sns.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
@Slf4j
@Api(tags = {"User API"})
public class UserRestController {
    private final UserService userService;

    /**
     회원 가입
     **/
    @ApiOperation(value="회원 가입", notes="userName, password로 회원 데이터 저장")
    @PostMapping("/join")
    public Response join(@RequestBody UserJoinRequestDto requestDto) throws SQLException {
        UserJoinResponseDto user = userService.createUser(requestDto);
        log.info("{}",user);

        return Response.success(user);
    }

    /**
     회원 로그인
     **/
    @ApiOperation(value="회원 로그인", notes="userName, password로 저장된 회원 데이터가 있으면 jwt Token 반환")
    @PostMapping("/login")
    public Response login(@RequestBody UserLoginRequestDto requestDto, HttpServletRequest request) throws SQLException {
        log.info("{}",requestDto);

        UserLoginResponseDto responseDto = userService.loginUser(requestDto);
        log.info("{}",responseDto);

        if (responseDto.getJwt() != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute("userName",requestDto.getUserName());

        }

        return Response.success(responseDto);
    }

    /**
     * ADMIN 회원이 일반 회원 등급을 변경하는 기능
     **/
    @ApiOperation(value="회원 권한 변경", notes="(유효한 jwt Token 필요) 회원 권한이 ADMIN 인 사용자만 할 수 있으며, 요청할 때, role은 USER 혹은 ADMIN 만 입력가능")
    @PostMapping("/{userId}/role/change")
    public Response changeRole(@PathVariable(name = "userId") Long userId, @RequestBody UserRoleChangeRequestDto requestDto) {
        log.info("{}",requestDto);

        userService.changeRole(userId, requestDto);

        UserRoleChangeResponseDto responseDto = new UserRoleChangeResponseDto(userId, userId + "번 아이디의 권한을 " + requestDto.getRole().toUpperCase() + "로 변경하였습니다.");

        return Response.success(responseDto);
    }
}
