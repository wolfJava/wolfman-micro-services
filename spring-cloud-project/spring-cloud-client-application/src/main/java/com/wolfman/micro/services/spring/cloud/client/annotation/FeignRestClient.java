package com.wolfman.micro.services.spring.cloud.client.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * feign rest client 注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FeignRestClient {

    /**
     * REST 服务应用名称
     * @return
     */
    String name();

}
