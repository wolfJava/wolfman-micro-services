## Ⅵ - Spring Cloud Gateway 服务网关

### 遗留问题

接口方法参数名称在 ParamterNameDiscoverer 找不到

类方法参数名称在 ParamterNameDiscoverer 可以找到

javac -g：编译时带有debug信息

javac -g：none 编译时不带debug信息



### Spring Cloud Gateway

Spring WebFlux 相像

目的：去 Servlet 化（Java EE Web 技术中心）

技术：Reactor + Netty + Lambda

最新技术：Spring Cloud Function



### 小马哥 Java语言 技术预判

函数式编程（java Lambda、Koltin、Scala、Groovy）

网络编程（Old Java BIO、Java 1.4 NIO（Reactor模式）、Java 1.7 NIO2 和 AIO、Netty）

Reactive：编程模型（非阻塞 + 异步） + 对象设计模式（观察者模式）

**典型技术代表：**

1. 单机版（函数式、并发编程）：Reactive、Rxjava、Java 9 Flow API
2. 网络版（函数式、并发编程、网络编程）
   1. Netty + Reactor -> WebFlux、Spring Cloud Gateway
   2. Vert.x（Netty）



### 主要内容

#### 取代 Zuul 1.x（基于Servlet）

- Resin Servlet 
  - 可以 Nginx 匹敌

- Tomcat Servlet 容器
  - 连接器
    - Java Blocking Connector
    - Java Non Blocking Connector
    - APR/native Connector

- JBoss
- Weblogic



- Netflix Zuul 自己的实现
  - 实现 API 不是非常友好



#### Zuul 实现原理

- @Enable 模块装配
  - @EnableZuulProxy
  - 配合注解：@Import

- 依赖服务发现
  - 我是谁
  - 目的地在哪里
- 依赖服务路由
  - URI映射到目的的服务
- 依赖服务熔断（可选）



#### 服务发现

**举例说明：**

假设 URI：/gateway/spring-cloud-server-application/say

其中 Servlet Path：/gateway

spring-cloud-server-application 是服务器的应用名称

/say是 spring-cloud-server-application 的服务 URI

/gateway/spring-cloud-server-application/say -> http://${rest-api-host}:${rest-api-port}/hello-world?...



**根据 servlet 实现网关**

~~~java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 服务网关路由规则
 *
 * /${service-name}/${service-uri}
 * /gateway/rest-api/hello-world -> http://127.0.0.1:9090/hello-world
 *
 */
@WebServlet(name = "gateway", urlPatterns = "/gateway/*")
public class GatewayServlet extends HttpServlet {

    @Autowired
    private DiscoveryClient discoveryClient;

    private ServiceInstance randomChooseServiceInstance(String serviceName){
        //获取服务实例列表（服务IP ，端口，是否为HTTPS）
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(serviceName);
        //获得服务实例总数
        int size = serviceInstances.size();
        //随机获取数组下标
        int index = new Random().nextInt(size);
        return serviceInstances.get(index);
    }


    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //${service-name}/${service-uri}
        String pathInfo = request.getPathInfo();

        String[] paths = StringUtils.split(pathInfo.substring(1),"/");
        //获取服务名称
        String serviceName = paths[0];
        //获取服务Uri
        String serviceUri = "/" + paths[1];
        //随机选择一台服务实例
        ServiceInstance serviceInstance = randomChooseServiceInstance(serviceName);
        //构建目标服务 URI -> scheme://ip:port/serviceURI
        String targetURL = buildTargetURI(serviceInstance,serviceUri,request);

        // 创建转发客户端
        RestTemplate restTemplate = new RestTemplate();

        // 构造 Request 实体
        RequestEntity<byte[]> requestEntity = null;
        try {
            requestEntity = createRequestEntity(request, targetURL);
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(requestEntity, byte[].class);
            writeHeaders(responseEntity, response);
            writeBody(responseEntity, response);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


    }

    private String buildTargetURI(ServiceInstance serviceInstance, String serviceURI, HttpServletRequest request){
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(serviceInstance.isSecure() ? "https://":"http://")
                .append(serviceInstance.getHost())
                .append(":").append(serviceInstance.getPort())
                .append(serviceURI);
        String queryString = request.getQueryString();
        if (StringUtils.hasText(queryString)){
            urlBuilder.append("?").append(queryString);
        }
        return urlBuilder.toString();
    }

    private RequestEntity<byte[]> createRequestEntity(HttpServletRequest request, String url) throws URISyntaxException, IOException {
        // 获取当前请求方法
        String method = request.getMethod();
        // 装换 HttpMethod
        HttpMethod httpMethod = HttpMethod.resolve(method);
        byte[] body = createRequestBody(request);
        MultiValueMap<String, String> headers = createRequestHeaders(request);
        RequestEntity<byte[]> requestEntity = new RequestEntity<byte[]>(body, headers, httpMethod, new URI(url));
        return requestEntity;
    }

    private MultiValueMap<String, String> createRequestHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        List<String> headerNames = Collections.list(request.getHeaderNames());
        for (String headerName : headerNames) {
            List<String> headerValues = Collections.list(request.getHeaders(headerName));
            for (String headerValue : headerValues) {
                headers.add(headerName, headerValue);
            }
        }
        return headers;
    }

    private byte[] createRequestBody(HttpServletRequest request) throws IOException {
        InputStream inputStream = request.getInputStream();
        return StreamUtils.copyToByteArray(inputStream);
    }


    /**
     * 输出 Body 部分
     *
     * @param responseEntity
     * @param response
     * @throws IOException
     */
    private void writeBody(ResponseEntity<byte[]> responseEntity, HttpServletResponse response) throws IOException {
        if (responseEntity.hasBody()) {
            byte[] body = responseEntity.getBody();
            // 输出二进值
            ServletOutputStream outputStream = response.getOutputStream();
            // 输出 ServletOutputStream
            outputStream.write(body);
            outputStream.flush();
        }
    }

    private void writeHeaders(ResponseEntity<byte[]> responseEntity, HttpServletResponse response) {
        // 获取相应头
        HttpHeaders httpHeaders = responseEntity.getHeaders();
        // 输出转发 Response 头
        for (Map.Entry<String, List<String>> entry : httpHeaders.entrySet()) {
            String headerName = entry.getKey();
            List<String> headerValues = entry.getValue();
            for (String headerValue : headerValues) {
                response.addHeader(headerName, headerValue);
            }
        }
    }
}
~~~



#### 整合负载均衡（Ribbon）

官方实现：看文档

http://cloud.spring.io/spring-cloud-static/Finchley.SR1/single/spring-cloud.html#_ribbon_with_zookeeper



##### 实现 ILoadBalancer



##### 实现 IRule

































































