package likelion.sns.controller;


import likelion.sns.domain.dto.alarm.AlarmListDetailsDto;
import likelion.sns.domain.dto.alarm.AlarmListDto;
import likelion.sns.service.AlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@ApiIgnore
public class IndexController {

    private final AlarmService alarmService;

    @GetMapping("")
    public String index(Model model, HttpServletRequest request, Pageable pageable) {

        // 로그인 시, 화면에 로그인 회원명, 알림 표시
        showLoginUserNameAndAlarm(request, model, pageable);
        return "index";
    }

    public void showLoginUserNameAndAlarm(HttpServletRequest request, Model model,Pageable pageable) {
        HttpSession session = request.getSession(true);

        if (session.getAttribute("userName") != null) {
            Object loginUserName = session.getAttribute("userName");
            model.addAttribute("loginUserName", loginUserName);


            Page<AlarmListDetailsDto> alarms = alarmService.getDetailAlarms((String) loginUserName, pageable);
            model.addAttribute("alarms", alarms);
        }
    }
}
