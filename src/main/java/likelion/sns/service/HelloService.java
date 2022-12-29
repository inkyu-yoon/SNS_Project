package likelion.sns.service;

import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class HelloService {
    public String getSum(String num) {
        int sum = 0;

        try {
            sum = Arrays.stream(num.split("")).mapToInt(s -> Integer.valueOf(s)).sum();
        } catch (NumberFormatException e) {
            throw new SNSAppException(ErrorCode.BAD_REQUEST_SUM);
        }

        return String.valueOf(sum);
    }
}
