package com.wolfman.micro.services.spring.cloud.server.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface SimpleMessageReceiver {

    @Input("gupao2018") //Channel name
    SubscribableChannel gupao(); // destination = test2018

}
