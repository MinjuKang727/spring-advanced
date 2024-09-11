package org.example.expert.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        StringBuffer requestURL = request.getRequestURL();
        Long userId = (Long) request.getAttribute("userId");
        log.info("[{}] Access User : {} , RequestURL : {}",new Date(), userId, requestURL);

        try {
            joinPoint.proceed();
        } finally {
            log.info("[{}] 사용자 접근 해제",new Date());
        }
    }
}
