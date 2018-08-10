package com.wolfman.micro.services.spring.cloud.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication  //标准 Spring boot 应用
@EnableDiscoveryClient//激活服务发现客户端
@EnableScheduling
public class ClientApplication {

    public static void main(String[] args) {

        new SpringApplicationBuilder(ClientApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);


    }


}
