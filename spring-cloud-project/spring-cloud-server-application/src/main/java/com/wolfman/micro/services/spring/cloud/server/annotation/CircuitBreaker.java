package com.wolfman.micro.services.spring.cloud.server.annotation;

import java.lang.annotation.*;
import java.lang.reflect.Method;

@Target(ElementType.METHOD)//标注在方法
@Retention(RetentionPolicy.RUNTIME) //运行时保存注解信息
@Documented
public @interface CircuitBreaker {

    /**
     * 超时时间
     * @return 设置超时时间
     */
    long timeout();

}
