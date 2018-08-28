## Ⅶ - Spring Cloud Stream 服务流

上节课遗留问题

指导文章 https://docs.oracle.com/javase/tutorial/reflect/member/methodparameterreflection.html

Java 8+ 增加 javac -parameters 参数


大前提：必须是 Java 8


如果不增加参数的话，-parameters 编译参数，Parameter#getName() 方法 “arg0”

为什么 Spring  DefaultParameterNameDiscoverer 返回 null



javap -v sayingRestService.class >> temp.txt  查看字节码方式



### 去年 Spring Cloud Stream 整合 Kafka



### 相关技术 

#### Reactive Streams

publiser

Subscriber

Processor



元编程：基于编程的编程：Reflection，Function、Lambda

~~~java
Stream
	.of(1,2,3,4,5,6) //生产
	.map(String::valueOf) //处理
	.forEach(System.out::println); //消费
~~~

//for-each 必须实现 java.lang.Iterable，例如：[]、Collection



#### Spring Cloud Data Flow



### 主要内容

#### Spring Cloud Stream 整合 RabbitMQ

**代码详见 spring-cloud-client-application 与 spring-cloud-server-applicaiton**





##### 增加依赖

~~~java
<!-- 增加 stream rabbit 依赖 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream-binder-rabbit</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
</dependency>
~~~

##### 分析依赖

- **间接依赖：Spring Integration，相关文档：**https://docs.spring.io/spring-integration/docs/5.0.7.RELEASE/reference/html/amqp.html
- Spring Message
- Spring AMQP
  - RabbitMQ
  - RabbitTemplate 自动装配



##### 配置项

- RabbitBindingProperties





> 小技巧：
>
> Spring中，大多数都是 *Template 实现 * Operations
>
> 例如：jdbcTemplate、RedisTemplate、RabbitTemplate、RestTemplate、KafkaTemplate
>
> 



##### 务必在消息处理中做幂等性处理

~~~java
	@Autowired
    private SimpleMessageReceiver simpleMessageReceiver;

    @PostConstruct
    public void init(){ //接口编程
        SubscribableChannel subscribableChannel = simpleMessageReceiver.gupao();
        subscribableChannel.subscribe(message -> {
            MessageHeaders headers = message.getHeaders();
            String encoding = (String) headers.get("charset-encoding");
            String text = (String) headers.get("content-type");
            byte[] content = (byte[]) message.getPayload();
            try {
                System.out.println("接收到消息：" + new String(content,encoding));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
    }

    @StreamListener("gupao2018")
    public void onMessage(byte[] data){//注解编程
        System.out.println("onMessage(byte[]):"+data);

    }

    @StreamListener("gupao2018")
    public void onMessage(String data){//注解编程

        System.out.println("onMessage(String):"+data);

    }

    @ServiceActivator(inputChannel = "gupao2018")
    public void onServiceActivator(String data){//Spring Integration 注解驱动

        System.out.println("onServiceActivator(String):"+data);

    }

    /**
     * 同一种编程模型，都会收到
     * 不同的编程模型，循环收到
     *
     * @StreamListener 优先于 @ServiceActivator 优先于 注解编程
     *
     */

~~~

**注意：**相同的编程模型重复执行，例如：@StreamListener。不同的编程模型轮流执行