package com.example.template.quarkus.aspect;

import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interceptor binding annotation untuk logging.
 * Tempelkan @Logged pada class controller agar LoggingInterceptor aktif.
 *
 * Menggantikan Spring AOP @Aspect + Pointcut expression.
 */
@InterceptorBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Logged {
}
