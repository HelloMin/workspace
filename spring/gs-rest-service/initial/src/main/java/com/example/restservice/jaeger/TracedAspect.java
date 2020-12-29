package com.example.restservice.jaeger;

import io.opentracing.Span;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TracedAspect {
    @Around("@annotation(Traced)")
    public Object aroundTraced(ProceedingJoinPoint jp) throws Throwable {
        String class_name = jp.getTarget().getClass().getName();
        String method_name = jp.getSignature().getName();

        Span span = JaegerTracing.buildSpan(class_name + "." + method_name);
        JaegerTracing.getTracer().scopeManager().activate(span);
        Object result = jp.proceed();
        span.finish();
        return result;
    }
}
