## Spring Cloud 负载均衡

~~~txt
JSR 305 meta-annotations
注解做编程约束
~~~





### 1.RestTemplate 原理与扩展

Spring 核心 HTTP 消息转换器 HttpMessageConverter

REST 自描述消息：媒体类型（MediaType）：text/html；text/xml；application/json；

HTTP 协议特点：纯文本协议，自我描述



- REST 服务端

- REST 客户端

  反序列化：文本（通讯）->对象（程序使用）

		序列化：对象->文本

####1.1 HttpMessageConverter 分析 

**判断是否可读可写：**

例如clazz = Person.class

~~~java
public interface HttpMessageConverter<T> {
	
	boolean canRead(Class<?> clazz, @Nullable MediaType mediaType);
	
	boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType);
}
~~~

**当前支持的媒体类型：**

例如：MappingJackson2HttpMessageConverter

~~~java
public interface HttpMessageConverter<T> {
	
	List<MediaType> getSupportedMediaTypes();
	
}
~~~

**反序列化：**

~~~java
public interface HttpMessageConverter<T> {
	
	T read(Class<? extends T> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException;
	
}
~~~

特别提醒：Spring Web MVC Servlet

Spring 在早期设计时，它就考虑到了去 Servlet 化。

HttpInputMessage 类似于 HttpServletRequest

~~~java
public interface HttpInputMessage extends HttpMessage {

	InputStream getBody() throws IOException;

    //来自于 HttpMessage
    HttpHeaders getHeaders();
}
~~~

类比

~~~java
public interface HttpServletRequest {
    //来自于 ServletRequest
    ServletInputStream getInputStream() throws IOException;
    
    public Enumeration<String> getHeaders(String name);

    public Enumeration<String> getHeaderNames();
    
}
~~~



**结论：**

​	RestTemplet 利用 HttpMessageConverter 对一定媒体类型（JSON\xml\TEXT）序列化和反序列化；

​	它不依赖于 Servlet API，它系定义实现，对于服务端而言，将 Servlet API 适配成 HttpInputMessage 以及 HttpOutputMessage。



RestTemplate 对应了多个 HttpMessageConverter，那么如何决策正确媒体类型。



####1.2 RestTemplate 在 HttpMessageConverter的设计 

~~~java
public class RestTemplate extends InterceptingHttpAccessor implements RestOperations {

    ...
	// 
    private final List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
	
    public RestTemplate() {
		this.messageConverters.add(new ByteArrayHttpMessageConverter());
		this.messageConverters.add(new StringHttpMessageConverter());
		this.messageConverters.add(new ResourceHttpMessageConverter(false));
		this.messageConverters.add(new SourceHttpMessageConverter<>());
		this.messageConverters.add(new AllEncompassingFormHttpMessageConverter());

		if (romePresent) {
			this.messageConverters.add(new AtomFeedHttpMessageConverter());
			this.messageConverters.add(new RssChannelHttpMessageConverter());
		}

		if (jackson2XmlPresent) {
			this.messageConverters.add(new MappingJackson2XmlHttpMessageConverter());
		}
		else if (jaxb2Present) {
			this.messageConverters.add(new Jaxb2RootElementHttpMessageConverter());
		}

		if (jackson2Present) {
			this.messageConverters.add(new MappingJackson2HttpMessageConverter());
		}
		else if (gsonPresent) {
			this.messageConverters.add(new GsonHttpMessageConverter());
		}
		else if (jsonbPresent) {
			this.messageConverters.add(new JsonbHttpMessageConverter());
		}

		if (jackson2SmilePresent) {
			this.messageConverters.add(new MappingJackson2SmileHttpMessageConverter());
		}
		if (jackson2CborPresent) {
			this.messageConverters.add(new MappingJackson2CborHttpMessageConverter());
		}
	}

    ...

}
~~~

- 添加内建 HttpMessageConverter 实现
- 有条件的添加第三方库 HttpMessageConverter 整合实现

> 问题场景一： http://localhost:8080/person -> XML 而不是 jackson

> Postman，curl 场景最为明显

> 没有传递请求头，无从选择媒体类型

> 假设 Person 既能被 XML 读取，又能被 JSON 读取。哪个 HttpMessageConverter 实现类在前，就先返回哪一个



> Content-Type text/html; charset=utf-8



####1.3 RestTemplate 扩展

**扩展 HTTP 客户端**

- ClientHttpRequestFactory 
  - Spring 实现
    - SimpleClientHttpRequestFactory
  - HttpClient
    - HttpComponentsClientHttpRequestFactory
  - OkHttp
    - OkHttp3ClientHttpRequestFactory
    - OkHttpClientHttpRequestFactory

微服务要使用轻量级的协议，比如REST

Spring Cloud RestTemplate 核心的调用器



####1.4 RestTemplate 整合 Zookeeper

详见git上代码：spring-cloud-client-application



@Controller -> 负载均衡

@Controller -> restTemplate

RestTemplate 管理负载均衡

RestTemplate.getForObject("/${app-name}/uri...");



### 2.Netflix Ribbon

@LoadBalanced 利用注解来过滤，注入方和声明方同时使用

#### 2.1 负载均衡客户端

ServiceInstanceChooser

LoadBalancerClient

#### 2.2 负载均衡上下文

LoadBalancerContext

#### 2.3 负载均衡规则

ILoadBalancer



###3.自我总结

实现流程：

1. 根据注册中心，获取服务器列表
2. 运用请求规则，选择其中一台服务器
3. 利用 restTemplate 发送请求到服务器
4. 输出响应

其中，自定义实现 restTemplate 的过滤器（ClientHttpRequestInterceptor），来过请求，把1、2、3、4步骤放入到了过滤器中实现。

@LoadBalanced 利用注解来过滤，注入方和声明方同时使用，声明使用的restTemplate的类型。



