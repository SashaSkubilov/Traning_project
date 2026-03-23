package com.example.training_project.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceExecutionLoggingAspect {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceExecutionLoggingAspect.class);

    @Around("within(com.example.training_project.service..*)")
    public Object logExecutionTime(final ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            LOG.info("Service method {} executed in {} ms",
                    joinPoint.getSignature().toShortString(),
                    durationMs);
        }
    }
}
