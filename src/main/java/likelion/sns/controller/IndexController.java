package likelion.sns.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/")
@ApiIgnore
public class IndexController {


    @GetMapping("")
    public String index(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        if (session.getAttribute("userName") != null) {
            model.addAttribute("loginUserName", session.getAttribute("userName"));
        }
        return "index";
    }

}
