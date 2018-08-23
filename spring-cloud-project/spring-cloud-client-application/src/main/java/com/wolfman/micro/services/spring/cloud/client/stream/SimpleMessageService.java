package com.wolfman.micro.services.spring.cloud.client.stream;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface SimpleMessageService {

    @Output("gupao2018") //Channel name
    MessageChannel gupao(); // destination = test2018



}
