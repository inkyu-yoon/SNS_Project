package likelion.sns.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@Slf4j
@RequestMapping("/users")
@ApiIgnore
public class UserController {

    /**
     회원 가입 화면
     **/
    @GetMapping("/join")
    public String join() {
        return "users/join";
    }

    /**
     회원 로그인 화면
     **/
    @GetMapping("/login")
    public String login() {
        return "users/login";
    }

    /**
     회원 로그아웃
     세션에 저장되어 있는 회원명 제거
     토큰은 웹에서(mustache 파일에서) 제거
     **/
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.removeAttribute("userName");
        return "redirect:/";
    }
}
