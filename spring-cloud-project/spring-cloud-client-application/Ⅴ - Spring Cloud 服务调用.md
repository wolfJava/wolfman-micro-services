## Ⅴ - Spring Cloud 服务调用

### 1-预备知识

prosesson上的图



### 2-分析服务调用引入背景

@LoadBalanced RestTemplate 限制

- 面向 URL 组件，必须依赖于 主机 + 端口 + URI
- 并非接口编程（Spring Cloud 中，需要理解应用名称 + 服务 URI）

RestTemplate 不依赖于服务接口，仅关注 REST 响应内容。



**举例：**

>~~~java
>lbRestTemplate.getForObject("http://" + serviceName+"/say?message="+message,String.class);
>~~~



### 3-Spring Cloud Feign 基本用法

#### 3.1-Spring Cloud Feign 客户端注解 @FeignClient

服务（应用）定位

> @FeignClient("${serviceName}")	// 服务提供方的应用名称

服务 URI 定位

> **注意：**Spring Cloud Feign 和 OpenFeign 区别
>
> 



##### 3.1.1 服务端框架纵向比较

Spring Cloud Feign： 是 OpenFeign 扩展，并且使用 SpringMVC 注解来做 URI 映射，比如 @RequestMapping 或 @GetMapping 之类

OpenFeign ：灵感来自于 JAX-RS（Java REST 标准），重复发明轮子。

JAX-RS：Java REST 标准（https://github.com/mercyblitz/jsr/tree/master/REST），可一致性高，Jersey(Servlet容器)、Weblogic

> JSR参考链接：https://github.com/mercyblitz/jsr

- JAX-RS
  - HTTP 请求方法
- Spring Web MVC
- OpenFeign

| 技术栈             | HTTP 请求方法表达       | 变量路径      | 请求参数      | 自描述消息                                          | 内容协商 |
| ------------------ | ----------------------- | ------------- | ------------- | --------------------------------------------------- | -------- |
| JAX-RS             | @GET                    | @PathParam    | @FormParam    | @Produces("application/widgets+xml")                |          |
| Spring Web MVC     | @GetMapping             | @PathVariable | @RequestParam | @RequestMapping(produces="application/widgets+xml") |          |
| OpenFeign          | @RequestLine（GET ...） | @Param        | @Param        |                                                     |          |
| Spring Cloud Feign | @GetMapping             | @PathVariable | @RequestParam |                                                     |          |



##### 3.1.2-REST 核心概念（Java 技术描述）—— 非常重要

###### 请求映射（@RequestMapping）

自己补充内容

###### 请求参数处理（@RequestParam）

自己补充内容

###### 请求主体处理（@RequestBody）

自己补充内容

###### 响应处理（@ResponseBody，@ResponseStatus）

自己补充内容

@ResponseBody+@ResponseStatus<=@ResponseEntity

###### 自描述消息（@RequestMapping(produces="application/widgets+xml")）

自己补充内容

###### 内容协商（ContentNegotiationManager）

理论知识：https://developer.mozilla.org/en-US/docs/Web/HTTP/Content-negotiation

自己补充内容



#### 3.2-整合 Spring Cloud Feign

**1-增加 Spring Cloud Feign 依赖**

~~~java
<!-- 增加 feign 依赖 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
~~~

**2-整合 @EnableFeignClients**

~~~java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication  //标准 Spring boot 应用
@EnableDiscoveryClient//激活服务发现客户端
@EnableScheduling
@EnableFeignClients //激活服务调用
public class ClientApplication {

    public static void main(String[] args) {

        new SpringApplicationBuilder(ClientApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
~~~

**3-整合@FeignClient**

之前实现：

~~~java
	@GetMapping("/loadBalance/invoke/{serviceName}/say")
    public String lbInvokeSay(@PathVariable String serviceName,
                            @RequestParam String message){

        return lbRestTemplate.getForObject("http://" + serviceName+"/say?message="+message,String.class);
    }
~~~

整合 @FeignClient 实现：

~~~java
@FeignClient(name = "spring-cloud-server-application")
public interface SayingService {

    @GetMapping("/say")
    public String say(@RequestParam("message") String message);

}
~~~

注入 SayingService

~~~java
@Autowired
private SayingService sayingService;
~~~

调用 SayingService

~~~java
@GetMapping("/feign/say")
public String feignSay(@RequestParam String message){
    return sayingService.say(message);
}
~~~

启动 ZK 服务器

启动 spring-cloud-server-application

启动 spring-cloud-client-application



#### 3.3-实现自定义 RestClient（模拟@FeignClient）

##### 3.3.1-Spring Cloud Feign 编程模型特征

- @Enable 模块驱动
- @*Client 绑定客户端接口，指定应用名称
- 客户端接口指定请求映射 @RequestMapping
- 客户端接口指定请求参数 @RequestParam
  - 必须指定 @RequestParamValue();
- @Autowired 客户端接口是一个代理

##### 3.3.2-实现 @FeignRestClient

~~~java
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
~~~

##### 3.3.3-实现 @FeignRestClient 服务接口

~~~java
@FeignRestClient(name = "spring-cloud-server-application")
public interface FeignSayingRestService {

    @GetMapping("/say")
    public String say(@RequestParam("message") String message);

}
~~~

##### 3.3.4实现 @FeignEnableRestClient 模块

~~~java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(FeignRestClientsRegistrar.class)
public @interface FeignEnableRestClient {

    /**
     * 指定 @RestClient 接口
     * @return
     */
    Class<?>[] clients() default {};

}
~~~



##### 3.3.5-实现 FeignRestClientsRegistrar

- 指定 @FeignRestClient 服务接口
  - 识别 @FeignRestClient
  - 过滤所有 @RequestMapping 方法
- 将 @FeignRestClient 服务接口注册Bean
  - @FeignRestClient 服务接口想成代理实现
    - say 方法执行 REST 请求

**实现详见代码**



> Zuul : IP:Port/serviceName/uri
