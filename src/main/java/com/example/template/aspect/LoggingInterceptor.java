package com.example.template.aspect;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CDI Interceptor untuk logging entry/exit/durasi semua method yang ditandai @Logged.
 *
 * Menggantikan Spring AOP LoggingAspect (@Aspect, @Around).
 */
@Logged
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class LoggingInterceptor {

    @AroundInvoke
    public Object logAround(InvocationContext context) throws Exception {
        Logger log = LoggerFactory.getLogger(context.getTarget().getClass());

        String methodName = context.getMethod().getName();
        log.debug("[ENTRY] {}", methodName);

        long startTime = System.currentTimeMillis();
        try {
            Object result = context.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.debug("[EXIT] {} | durasi: {}ms", methodName, duration);
            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[ERROR] {} | durasi: {}ms | exception: {}", methodName, duration, ex.getMessage());
            throw ex;
        }
    }
}
