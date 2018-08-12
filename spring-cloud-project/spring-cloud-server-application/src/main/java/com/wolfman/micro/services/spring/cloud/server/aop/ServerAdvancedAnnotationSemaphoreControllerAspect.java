package com.wolfman.micro.services.spring.cloud.server.aop;

import com.wolfman.micro.services.spring.cloud.server.annotation.CircuitBreaker;
import com.wolfman.micro.services.spring.cloud.server.annotation.SemaphoreCircuitBreaker;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.concurrent.*;

@Aspect
@Component
public class ServerAdvancedAnnotationSemaphoreControllerAspect {

    private ExecutorService executorService = Executors.newFixedThreadPool(20);

    private volatile Semaphore semaphore = null;

    @Around("execution(* com.wolfman.micro.services.spring.cloud.server.controller.HystrixServerAdvancedAnnotationSemaphoreController.say(..)) && " +
            "args(message) && @annotation(circuitBreaker)")
    public Object advancedAnnotationSayInTimeout(ProceedingJoinPoint point,
                                                 String message,
                                                 SemaphoreCircuitBreaker circuitBreaker) throws Throwable {
        int value = circuitBreaker.value();
        if (semaphore == null){
            semaphore = new Semaphore(value);
        }
        Object returnValue = null;
        try{
            if (semaphore.tryAcquire()){
                returnValue = point.proceed(new Object[]{message});
                Thread.sleep(1000);
            }else {
                returnValue = errorContent(message);
            }
        }finally {
            semaphore.release();
        }
        return returnValue;
    }

    public String errorContent(String message){
        return "Fault";
    }

    @PreDestroy
    public void destory(){
        executorService.shutdown();
    }

}
