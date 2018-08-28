package com.wolfman.micro.services.spring.cloud.client.service.rest.clients;

import com.wolfman.micro.services.spring.cloud.client.annotation.FeignRestClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.stream.Stream;

//spring-cloud-server-application
@FeignRestClient(name = "${saying.rest.service.name}")
public interface FeignSayingRestService {

    @GetMapping("/say")
    public String say(@RequestParam("message") String message);

    public static void main(String[] args) throws Exception {
        //java方式获得参数名
        Method methodOne = FeignSayingRestService.class.getMethod("say",String.class);
        Parameter parameter = methodOne.getParameters()[0];
        System.out.println(parameter.getName());

        //spring方式来获得
        Method method = ReflectionUtils.findMethod(FeignSayingRestService.class,"say",String.class);
        ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
        Stream.of(discoverer.getParameterNames(method))
                .forEach(System.out::println);


    }


}
