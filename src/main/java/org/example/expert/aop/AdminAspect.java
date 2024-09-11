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
    @Pointcut("execution(* org.example.expert.domain.comment.controller.CommentAdminController..*(..))")
    private void adminDeleteComment() {}

    @Pointcut("execution(* org.example.expert.domain.user.controller.UserAdminController..*(..))")
    private void adminChangeUserRole() {}

    @Around("adminDeleteComment() || adminChangeUserRole()")
    public void execute(ProceedingJoinPoint joinPoint) throws Throwable {
        // 접근 로그
        log.trace("[{}] {} 접근",new Date(), joinPoint.getStaticPart());

        try {
            joinPoint.proceed();
        } finally {
            log.trace("{} : {} 접근 해제", new Date(), joinPoint.getStaticPart());
        }
    }
}
