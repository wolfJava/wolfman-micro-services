package com.wolfman.micro.services.spring.cloud.server;

import com.wolfman.micro.services.spring.cloud.server.stream.SimpleMessageReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.SubscribableChannel;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableDiscoveryClient
@EnableHystrix//激活 Hystrix
@EnableAspectJAutoProxy(proxyTargetClass = true)//激活 AOP
@EnableBinding(SimpleMessageReceiver.class) //激活并引入 SimpleMessageReceiver
public class ServerApplication {

    public static void main(String[] args) {

        new SpringApplicationBuilder(ServerApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);

    }


    @Autowired
    private SimpleMessageReceiver simpleMessageReceiver;


    @PostConstruct
    public void init(){
        SubscribableChannel subscribableChannel = simpleMessageReceiver.gupao();
        subscribableChannel.subscribe(message -> {
            MessageHeaders headers = message.getHeaders();
            String encoding = (String) headers.get("charset-encoding");
            String text = (String) headers.get("content-type");
            byte[] content = (byte[]) message.getPayload();
            try {
                System.out.println("接收到消息：" + new String(content,encoding));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
    }



}
