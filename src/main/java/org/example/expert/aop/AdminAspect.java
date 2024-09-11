package org.example.expert.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j(topic = "AdminAspect")
@Aspect
@Component
public class AdminAspect {
    @Pointcut("@annotation(org.example.expert.annotation.AccessLog)")
    private void accessLogAnnotation() {}

    @Around("accessLogAnnotation()")
    public void adviceAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        // 접근 로그
        log.trace("[{}] {} 접근",new Date(), joinPoint.getStaticPart());

        try {
            joinPoint.proceed();
        } finally {
            log.trace("{} : {} 접근 해제", new Date(), joinPoint.getStaticPart());
        }
    }
}
