package com.wolfman.spring;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.SpringApplicationEvent;

@EnableAutoConfiguration
public class SpringBootEventDemo {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SpringBootEventDemo.class)
                .listeners(event -> {//添加监听器
                    System.out.println("监听到事件：" + event);
                })
                .run(args)
                .close();
    }
}
