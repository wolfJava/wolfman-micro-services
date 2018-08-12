package com.wolfman.micro.services.spring.cloud.server.web.mvc;

import com.wolfman.micro.services.spring.cloud.server.controller.HystrixServerMiddleController;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.TimeoutException;

@RestControllerAdvice(assignableTypes = HystrixServerMiddleController.class)
public class CircuitBreakerControllerAdvice {

    @ExceptionHandler
    public void onTimeoutException(TimeoutException timeoutExcepiton, Writer writer) throws IOException {
        writer.write(errorContent("")); //网络 I/O 是被容器管理的

    }

    public String errorContent(String message){
        return "Fault";
    }

}
