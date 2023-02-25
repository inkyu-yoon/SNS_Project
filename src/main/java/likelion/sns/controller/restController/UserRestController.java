package likelion.sns.controller.restController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import likelion.sns.Exception.ExceptionManager;
import likelion.sns.domain.Response;
import likelion.sns.domain.dto.user.changeRole.UserRoleChangeResponseDto;
import likelion.sns.domain.dto.user.join.UserJoinRequestDto;
import likelion.sns.domain.dto.user.join.UserJoinResponseDto;
import likelion.sns.domain.dto.user.login.UserLoginRequestDto;
import likelion.sns.domain.dto.user.login.UserLoginResponseDto;
import likelion.sns.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
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
     * 회원 가입
     **/
    @ApiOperation(value = "회원 가입", notes = "userName, password로 회원 데이터 저장")
    @PostMapping("/join")
    public ResponseEntity join(@Validated @RequestBody UserJoinRequestDto requestDto, BindingResult br) throws SQLException {
        log.info("🎉 회원 가입 요청 requestDto : {}", requestDto);

        //바인딩 에러 처리
        if (br.hasErrors()) {
            ExceptionManager.ifNullAndBlank();
        }

        UserJoinResponseDto responseDto = userService.createUser(requestDto);

        return ResponseEntity.ok(Response.success(responseDto));
    }

    /**
     * 회원 로그인
     **/
    @ApiOperation(value = "회원 로그인", notes = "userName, password로 저장된 회원 데이터가 있으면 jwt Token 반환")
    @PostMapping("/login")
    public ResponseEntity login(@Validated @RequestBody UserLoginRequestDto requestDto, BindingResult br, HttpServletRequest request) throws SQLException {
        log.info("🎉 로그인 요청 requestDto : {}", requestDto);

        //바인딩 에러 처리
        if (br.hasErrors()) {
            ExceptionManager.ifNullAndBlank();
        }

        UserLoginResponseDto responseDto = userService.loginUser(requestDto);

        HttpSession session = request.getSession(true);
        session.setAttribute("userName", requestDto.getUserName());


        return ResponseEntity.ok(Response.success(responseDto));
    }

    /**
     * ADMIN 회원이 일반 회원 등급을 변경하는 기능
     **/
    @ApiOperation(value = "회원 권한 변경", notes = "(유효한 jwt Token 필요) 회원 권한이 ADMIN 인 사용자만 할 수 있으며, 요청할 때, role은 USER 혹은 ADMIN 만 입력가능")
    @PostMapping("/{userId}/role/change")
    public ResponseEntity changeRole(@PathVariable(name = "userId") Long userId) {
        log.info("🎉 관리자가 등급을 변경할 회원 id : {} ", userId);


        //회원 등급 변경
        userService.changeRole(userId);

        UserRoleChangeResponseDto responseDto = new UserRoleChangeResponseDto(userId, userId + "번 아이디의 권한을 변경하였습니다.");

        return ResponseEntity.ok(Response.success(responseDto));
    }
}
