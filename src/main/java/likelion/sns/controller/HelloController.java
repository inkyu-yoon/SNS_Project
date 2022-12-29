package likelion.sns.controller;

import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.ExceptionManager;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.service.HelloService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/hello")
@RequiredArgsConstructor
@ApiIgnore
public class HelloController {

    private final HelloService helloService;
    @GetMapping("")
    public String hello() {
        return "윤인규";
    }

    @GetMapping("/{num}")
    public String hello(@PathVariable(name = "num") String num) {
        return helloService.getSum(num);
    }
}
