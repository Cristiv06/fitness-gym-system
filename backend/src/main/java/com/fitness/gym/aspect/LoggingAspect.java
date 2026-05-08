package com.fitness.gym.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* com.fitness.gym.service.impl.*.*(..))")
    private void serviceMethods() {}

    @Around("serviceMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String method = signature.getName();

        log.debug(">> {}() called with args: {}", method, joinPoint.getArgs());
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long elapsed = System.currentTimeMillis() - start;
        log.debug("<< {}() returned in {} ms", method, elapsed);

        return result;
    }

    @AfterThrowing(pointcut = "serviceMethods()", throwing = "ex")
    public void logException(org.aspectj.lang.JoinPoint joinPoint, Exception ex) {
        Logger log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        log.error("Exception in {}() - {}: {}",
                joinPoint.getSignature().getName(),
                ex.getClass().getSimpleName(),
                ex.getMessage());
    }
}
