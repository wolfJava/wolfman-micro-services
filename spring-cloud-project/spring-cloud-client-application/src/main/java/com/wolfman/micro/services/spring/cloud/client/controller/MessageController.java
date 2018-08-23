package com.wolfman.micro.services.spring.cloud.client.controller;

import com.wolfman.micro.services.spring.cloud.client.stream.SimpleMessageService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
public class MessageController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SimpleMessageService simpleMessageService;

    @GetMapping
    public String send(@RequestParam String message){
        rabbitTemplate.convertAndSend("huhao");
        return "OK";
    }

    @GetMapping("/stream/send")
    public boolean streamSend(@RequestParam String message){
        //获取 MessageChannel
        MessageChannel messageChannel = simpleMessageService.gupao();
        Map<String,Object> headers = new HashMap<>();
        headers.put("chaset-encoding","UTF-8");
        headers.put("content-type", MediaType.TEXT_PLAIN_VALUE);
        return messageChannel.send(new GenericMessage<String>(message,headers));

    }

}
