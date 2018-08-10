package com.wolfman.micro.services.spring.cloud.client.controller;


import com.wolfman.micro.services.spring.cloud.client.annotation.CustomizedLoadBalanced;
import com.wolfman.micro.services.spring.cloud.client.loadbalance.LoadBalanceRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 客户端访问服务端
 */
@RestController
public class ClientToInterceptorToServerController {

    @Autowired //注入自定义 RestTemplate bean
    @CustomizedLoadBalanced
    private RestTemplate restTemplate;

    @Autowired //注入 Ribbon RestTemplate bean
    @LoadBalanced
    private RestTemplate lbRestTemplate;

    @GetMapping("/invoke/{serviceName}/say")
    public String invokeSay(@PathVariable String serviceName,
                            @RequestParam String message){

        return restTemplate.getForObject("/" + serviceName+"/say?message="+message,String.class);
    }


    @GetMapping("/loadBalance/invoke/{serviceName}/say")
    public String lbInvokeSay(@PathVariable String serviceName,
                            @RequestParam String message){

        return lbRestTemplate.getForObject("http://" + serviceName+"/say?message="+message,String.class);
    }


    @Bean
    public ClientHttpRequestInterceptor interceptor(){
        return new LoadBalanceRequestInterceptor();
    }

    //Ribbon RestTemplate Bean
    @LoadBalanced
    @Bean
    public RestTemplate loadBalanceRestTemplate(){
        return new RestTemplate();
    }

    //自定义 RestTemplate bean
    @Bean
//    @Qualifier
//    @Autowired
    @CustomizedLoadBalanced
    public RestTemplate restTemplate(){//依赖注入

//        RestTemplate restTemplate = new RestTemplate();
//
//        restTemplate.setInterceptors(Arrays.asList(interceptor));

        return new RestTemplate();
    }


    @Bean
    @Autowired
    public Object putMySelfInterceptor(@CustomizedLoadBalanced Collection<RestTemplate> restTemplates,
                                        ClientHttpRequestInterceptor interceptor){

        restTemplates.forEach(r -> {
            r.setInterceptors(Arrays.asList(interceptor));
        });
        return new Object();
    }


}
