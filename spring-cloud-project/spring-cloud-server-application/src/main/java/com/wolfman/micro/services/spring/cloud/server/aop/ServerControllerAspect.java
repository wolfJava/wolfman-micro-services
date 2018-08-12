package com.wolfman.micro.services.spring.cloud.server.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;

@Aspect
@Component
public class ServerControllerAspect {

    private ExecutorService executorService = Executors.newFixedThreadPool(20);

    @Around("execution(* com.wolfman.micro.services.spring.cloud.server.controller.HystrixServerAdvancedController.say(..)) && " +
            "args(message)")
    public Object advancedSayInTimeout(ProceedingJoinPoint point, String message) throws Throwable {

        Future<Object> future = executorService.submit(()->{
            Object returnValue = null;
            try{
                returnValue = point.proceed(new String[]{message});
            }catch (Throwable throwable){
            }
            return returnValue;
        });

        //100 毫秒 超时
        Object returnValue = null;
        try{
            returnValue = future.get(100, TimeUnit.MILLISECONDS);
        }catch (TimeoutException e){
            future.cancel(true);//取消执行
            returnValue = errorContent("");
            //throw e;
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
