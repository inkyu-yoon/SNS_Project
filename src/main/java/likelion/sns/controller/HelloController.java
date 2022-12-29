package likelion.sns.controller;

import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.ExceptionManager;
import likelion.sns.Exception.SNSAppException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/hello")
@ApiIgnore
public class HelloController {

    @GetMapping("")
    public String hello() {
        return "윤인규";
    }

    @GetMapping("/{num}")
    public String hello(@PathVariable(name = "num") String num) {
        int sum = 0;

        try {
            sum = Arrays.stream(num.split("")).mapToInt(s -> Integer.valueOf(s)).sum();
        } catch (NumberFormatException e) {
            throw new SNSAppException(ErrorCode.BAD_REQUEST_SUM);
        }

        return String.valueOf(sum);
    }
}
