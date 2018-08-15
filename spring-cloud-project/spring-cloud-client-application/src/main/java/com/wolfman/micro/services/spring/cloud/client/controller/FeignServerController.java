package com.wolfman.micro.services.spring.cloud.client.controller;

import com.wolfman.micro.services.spring.cloud.client.service.feign.clients.SayingService;
import com.wolfman.micro.services.spring.cloud.client.service.rest.clients.FeignSayingRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 客户端访问服务端
 */
@RestController
public class FeignServerController {


    @Autowired
    private SayingService sayingService;

    @GetMapping("/feign/say")
    public String feignSay(@RequestParam String message){
        return sayingService.say(message);
    }

    @Autowired
    private FeignSayingRestService feignSayingRestService;

    @GetMapping("/myself/feign/say")
    public String myselfFeignSay(@RequestParam String message){
        return feignSayingRestService.say(message);
    }

}
