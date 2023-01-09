package likelion.sns.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class TimeTraceAop {
    /**
     * 매서드 실행 시, 동작 시간 표시
     */
//    @Around("execution(* likelion.sns..*(..))")
//    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
//        long start = System.currentTimeMillis();
//        System.out.println("🟢START: " + joinPoint.toString());
//        try {
//            return joinPoint.proceed();
//        } finally {
//            long finish = System.currentTimeMillis();
//            long timeMs = finish - start;
//            System.out.println("🔴END: " + joinPoint.toString()+ " " + timeMs +
//                    "ms");
//        }
//    }
}
