package likelion.sns.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.http.HttpRequest;

@Controller
@Slf4j
@RequestMapping("/users")
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
     회원 로그아웃 화면
     **/
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.removeAttribute("userName");

        return "redirect:/posts";
    }
}
