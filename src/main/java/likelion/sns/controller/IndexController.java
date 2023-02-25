package likelion.sns.controller;


import likelion.sns.domain.dto.alarm.AlarmListDetailsDto;
import likelion.sns.service.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

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

    /**
     * 로그인 되어 있을 시, 상단 바에(nav bar)에 사용자 명과, 알람 목록 전달
     * 로그인 여부는 세션에 회원명이 저장되어 있는지로 확인
     * 서비스는 토큰 존재 여부, 만료 여부 등 유효성으로 체크
     */
    public void showLoginUserNameAndAlarm(HttpServletRequest request, Model model,Pageable pageable) {
        HttpSession session = request.getSession(true);

        if (session.getAttribute("userName") != null) {
            Object loginUserName = session.getAttribute("userName");
            model.addAttribute("loginUserName", loginUserName);

            List<AlarmListDetailsDto> alarms = alarmService.getAlarms((String) loginUserName, pageable);
            model.addAttribute("alarms", alarms);
        }
    }
}
