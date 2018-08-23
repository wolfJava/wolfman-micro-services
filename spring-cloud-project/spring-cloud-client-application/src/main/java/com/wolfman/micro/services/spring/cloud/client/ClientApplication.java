package com.wolfman.micro.services.spring.cloud.client;

import com.wolfman.micro.services.spring.cloud.client.annotation.FeignEnableRestClient;
import com.wolfman.micro.services.spring.cloud.client.service.feign.clients.SayingService;
import com.wolfman.micro.services.spring.cloud.client.service.rest.clients.FeignSayingRestService;
import com.wolfman.micro.services.spring.cloud.client.stream.SimpleMessageService;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication  //标准 Spring boot 应用
@EnableDiscoveryClient//激活服务发现客户端
@EnableScheduling
@EnableFeignClients(clients = SayingService.class) //激活服务调用并引入FeignClient
@FeignEnableRestClient(clients = FeignSayingRestService.class)  //引入 @FeignRestClient
@EnableBinding(SimpleMessageService.class) //激活并引入 SimpleMessageService
public class ClientApplication {

    public static void main(String[] args) {

        new SpringApplicationBuilder(ClientApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
