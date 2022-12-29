package likelion.sns.controller;

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

        int sum = Arrays.stream(num.split("")).mapToInt(s -> Integer.valueOf(s)).sum();

        return String.valueOf(sum);
    }
}
