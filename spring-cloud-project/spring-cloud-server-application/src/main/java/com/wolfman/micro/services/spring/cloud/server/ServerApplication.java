package com.wolfman.micro.services.spring.cloud.server;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@EnableDiscoveryClient
@EnableHystrix//激活 Hystrix
@EnableAspectJAutoProxy(proxyTargetClass = true)//激活 AOP
public class ServerApplication {

    public static void main(String[] args) {

        new SpringApplicationBuilder(ServerApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);

    }


}
