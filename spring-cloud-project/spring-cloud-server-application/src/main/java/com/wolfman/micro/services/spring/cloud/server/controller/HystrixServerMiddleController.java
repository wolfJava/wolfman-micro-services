package com.wolfman.micro.services.spring.cloud.server.controller;


import org.jboss.netty.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.concurrent.*;

/**
 *  中级版本
 */
@RestController
@RequestMapping("/hystrix/middle")
public class HystrixServerMiddleController {

    private final static Random random = new Random();

    @Value("${spring.application.name}")
    private String currentServiceName;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @GetMapping("/say")
    public String say(@RequestParam String message) throws Exception {
        Future<String> future = executorService.submit(()->{
            return doSay(message);
        });
        //100 毫秒 超时
        String returnValue = null;
        try{
            returnValue = future.get(100, TimeUnit.MILLISECONDS);
        }catch (TimeoutException e){
            future.cancel(true);
            throw e;
        }
        return returnValue;
    }

    private String doSay(String message) throws InterruptedException {
        int value = random.nextInt(200);
        System.out.println("say() costs " + value + "ms.");
        Thread.sleep(value);
        String returnValue = "Say："+message;
        System.out.println(returnValue);
        return returnValue;
    }



}
