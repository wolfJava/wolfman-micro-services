package com.wolfman.micro.services.spring.cloud.server.controller;


import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.ribbon.proxy.annotation.Hystrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ServerController {

    private final static Random random = new Random();

    @Value("${spring.application.name}")
    private String currentServiceName;


    @HystrixCommand(
            fallbackMethod = "errorContent",
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",
                    value = "100")
            }
    )
    @GetMapping("/say")
    public String say(@RequestParam String message) throws InterruptedException {

        int value = random.nextInt(200);
        System.out.println("say() costs " + value + "ms.");
        Thread.sleep(value);

        System.out.println("serverController接收到消息 - say：" + message);
        return "Hello,"+message;
    }

    public String errorContent(String message){
        return "Fault";
    }


}
