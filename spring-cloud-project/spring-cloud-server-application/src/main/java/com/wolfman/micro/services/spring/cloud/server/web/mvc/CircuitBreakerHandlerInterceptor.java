package com.wolfman.micro.services.spring.cloud.server.web.mvc;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Writer;
import java.util.concurrent.TimeoutException;

public class CircuitBreakerHandlerInterceptor implements HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {

//        if ("/hystrix/end/say".equals(request.getRequestURI()) && ex instanceof TimeoutException){
//            Writer writer = response.getWriter();
//            System.out.println(request.getParameter("message"));
//            writer.write(errorContent(""));
//        }

    }

    public String errorContent(String message){
        return "Fault";
    }

}
