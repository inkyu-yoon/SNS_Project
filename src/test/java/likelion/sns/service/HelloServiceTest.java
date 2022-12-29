package likelion.sns.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HelloServiceTest {

    HelloService helloService = new HelloService();

    @Test
    @DisplayName("sum of digit service 테스트")
    void sumTest() {
        Assertions.assertThat("15").isEqualTo(helloService.getSum("12345"));
        Assertions.assertThat("6").isEqualTo(helloService.getSum("123"));
        Assertions.assertThat("8").isEqualTo(helloService.getSum("1232"));
    }
}