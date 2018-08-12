## Ⅳ - Spring Cloud 服务熔断

### 1.Spring Cloud Hystrix Client



> 注意：方法签名
>
> - 访问限定符
> - 方法返回类型
> - 方法名称
> - 方法参数
>   - 方法数量
>   - 方法类型 + 顺序
>   - ~~方法名称（编译时预留，IDE，Debug）~~



### 2.实现服务熔断（Future）

#### 2.1 低级版本（无容错实现）

~~~java
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * 简易版本
     * @param message
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/say")
    public String say(@RequestParam String message) throws Exception {
        Future<String> future = executorService.submit(()->{
            return doSay(message);

        });
        //100 毫秒超时
        String returnValue =  future.get(100, TimeUnit.MILLISECONDS);

        return returnValue;
    }

    private String doSay(String message) throws InterruptedException {
        int value = random.nextInt(200);
        System.out.println("say() costs " + value + "ms.");
        Thread.sleep(value);
        String returnValue = "Say："+message;
        return returnValue;
    }
~~~



#### 2.2 低级版本 + （有容错实现）

~~~java
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @GetMapping("/say")
    public String say(@RequestParam String message) throws Exception {
        Future<String> future = executorService.submit(()->{
            return doSay(message);
        });
        //100 毫秒超时
        String returnValue = null;
        try {
            returnValue =  future.get(100, TimeUnit.MILLISECONDS);
        }catch (InterruptedException | ExecutionException | TimeoutException e){
            //超级容错 = 执行错误 或
            returnValue = errorContent(message);
        }
        return returnValue;
    }

    private String doSay(String message) throws InterruptedException {
        int value = random.nextInt(200);
        System.out.println("say() costs " + value + "ms.");
        Thread.sleep(value);
        String returnValue = "Say："+message;
        return returnValue;
    }

    public String errorContent(String message){
        return "Fault";
    }
~~~

#### 2.3 中级版本

~~~java
@RestControllerAdvice(assignableTypes = HystrixServerEndController.class)
public class CircuitBreakerControllerAdvice {

    @ExceptionHandler
    public void onTimeoutException(TimeoutException timeoutExcepiton, Writer writer) throws IOException {
        writer.write(errorContent("")); //网络 I/O 是被容器管理的

    }

    public String errorContent(String message){
        return "Fault";
    }

}
~~~

~~~java
	@GetMapping("/say")
    public String say(@RequestParam String message) throws Exception {
        Future<String> future = executorService.submit(()->{
            return doSay(message);
        });
        //100 毫秒 超时
        String returnValue = null;
        try{
            returnValue = future.get(100, TimeUnit.MILLISECONDS);
        }catch (TimeoutException e){
            future.cancel(true);
            throw e;
        }
        return returnValue;
    }	
~~~



#### 2.4 高级版本（无注解实现）

~~~java
@RestController
@RequestMapping("/hystrix/advanced")
public class HystrixServerAdvancedController {

    private final static Random random = new Random();

    @GetMapping("/say")
    public String say(@RequestParam String message) throws Exception {
        return doSay(message);
    }

    private String doSay(String message) throws InterruptedException {
        int value = random.nextInt(200);
        System.out.println("say() costs " + value + "ms.");
        Thread.sleep(value);
        String returnValue = "Say："+message;
        System.out.println(returnValue);
        return returnValue;
    }
    
}

~~~

~~~java
@Aspect
@Component
public class ServerControllerAspect {

    private ExecutorService executorService = Executors.newFixedThreadPool(20);

    @Around("execution(* com.wolfman.micro.services.spring.cloud.server.controller.HystrixServerAdvancedController.say(..)) && " +
            "args(message)")
    public Object advancedSayInTimeout(ProceedingJoinPoint point, String message) throws Throwable {

        Future<Object> future = executorService.submit(()->{
            Object returnValue = null;
            try{
                returnValue = point.proceed(new String[]{message});
            }catch (Throwable throwable){
            }
            return returnValue;
        });

        //100 毫秒 超时
        Object returnValue = null;
        try{
            returnValue = future.get(100, TimeUnit.MILLISECONDS);
        }catch (TimeoutException e){
            future.cancel(true);//取消执行
            returnValue = errorContent("");
            //throw e;
        }
        return returnValue;
    }

    public String errorContent(String message){
        return "Fault";
    }

    @PreDestroy
    public void destory(){
        executorService.shutdown();
    }

}
~~~

#### 2.5 高级版本 + 有注解实现

```
import com.wolfman.micro.services.spring.cloud.server.annotation.CircuitBreaker;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.concurrent.*;

@Aspect
@Component
public class ServerAdvancedAnnotationControllerAspect {

    private ExecutorService executorService = Executors.newFixedThreadPool(20);

    private Object doInvoke(ProceedingJoinPoint point, String message, long timeout) throws Throwable {

        Future<Object> future = executorService.submit(()->{
            Object returnValue = null;
            try{
                returnValue = point.proceed(new String[]{message});
            }catch (Throwable throwable){
            }
            return returnValue;
        });

        //100 毫秒 超时
        Object returnValue = null;
        try{
            returnValue = future.get(timeout, TimeUnit.MILLISECONDS);
        }catch (TimeoutException e){
            future.cancel(true);//取消执行
            returnValue = errorContent("");
            //throw e;
        }
        return returnValue;
    }



//    @Around("execution(* com.wolfman.micro.services.spring.cloud.server.controller.HystrixServerAdvancedAnnotationController.say(..)) && " +
//            "args(message) && @annotation(circuitBreaker)")
//    public Object advancedAnnotationSayInTimeout(ProceedingJoinPoint point, String message, CircuitBreaker circuitBreaker) throws Throwable {
//        long timeout = circuitBreaker.timeout();
//        return doInvoke(point,message,timeout);
//    }

    /**
     * 利用反射来做
     * @param point
     * @param message
     * @return
     * @throws Throwable
     */
    @Around("execution(* com.wolfman.micro.services.spring.cloud.server.controller.HystrixServerAdvancedAnnotationController.say(..)) && " +
            "args(message)")
    public Object advancedAnnotationSayInTimeout(ProceedingJoinPoint point, String message) throws Throwable {
        long timeout = -1;
        if (point instanceof MethodInvocationProceedingJoinPoint){
            MethodInvocationProceedingJoinPoint methodPoint = (MethodInvocationProceedingJoinPoint) point;
            MethodSignature signature = (MethodSignature) methodPoint.getSignature();
            Method method = signature.getMethod();
            CircuitBreaker circuitBreaker = method.getAnnotation(CircuitBreaker.class);
            timeout = circuitBreaker.timeout();
        }
        return doInvoke(point,message,timeout);
    }

    public String errorContent(String message){
        return "Fault";
    }

    @PreDestroy
    public void destory(){
        executorService.shutdown();
    }

}
```

#### 2.6 高级版本 + 注解信号量方式

~~~java
import java.lang.annotation.*;

@Target(ElementType.METHOD)//标注在方法
@Retention(RetentionPolicy.RUNTIME) //运行时保存注解信息
@Documented
public @interface SemaphoreCircuitBreaker {

    /**
     * 信号量
     * @return 设置超时时间
     */
    int value();

}
~~~

~~~java
import com.wolfman.micro.services.spring.cloud.server.annotation.CircuitBreaker;
import com.wolfman.micro.services.spring.cloud.server.annotation.SemaphoreCircuitBreaker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 *  高级版本 + 注解
 */
@RestController
@RequestMapping("/hystrix/advanced/annotation/semaphore")
public class HystrixServerAdvancedAnnotationSemaphoreController {

    private final static Random random = new Random();

    /**
     * 高级版本 + 注解（信号量）
     * @param message
     * @return
     * @throws Exception
     */
    @GetMapping("/say")
    @SemaphoreCircuitBreaker(1)
    public String say(@RequestParam String message) throws Exception {
        return doSay(message);
    }

    private String doSay(String message) throws InterruptedException {
        int value = random.nextInt(200);
        System.out.println("say() costs " + value + "ms.");
        Thread.sleep(value);
        String returnValue = "Say："+message;
        System.out.println(returnValue);
        return returnValue;
    }

}

~~~

~~~java
import com.wolfman.micro.services.spring.cloud.server.annotation.CircuitBreaker;
import com.wolfman.micro.services.spring.cloud.server.annotation.SemaphoreCircuitBreaker;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.concurrent.*;

@Aspect
@Component
public class ServerAdvancedAnnotationSemaphoreControllerAspect {

    private ExecutorService executorService = Executors.newFixedThreadPool(20);

    private volatile Semaphore semaphore = null;

    @Around("execution(* com.wolfman.micro.services.spring.cloud.server.controller.HystrixServerAdvancedAnnotationSemaphoreController.say(..)) && " +
            "args(message) && @annotation(circuitBreaker)")
    public Object advancedAnnotationSayInTimeout(ProceedingJoinPoint point,
                                                 String message,
                                                 SemaphoreCircuitBreaker circuitBreaker) throws Throwable {
        int value = circuitBreaker.value();
        if (semaphore == null){
            semaphore = new Semaphore(value);
        }
        Object returnValue = null;
        try{
            if (semaphore.tryAcquire()){
                returnValue = point.proceed(new Object[]{message});
                Thread.sleep(1000);
            }else {
                returnValue = errorContent(message);
            }
        }finally {
            semaphore.release();
        }
        return returnValue;
    }

    public String errorContent(String message){
        return "Fault";
    }

    @PreDestroy
    public void destory(){
        executorService.shutdown();
    }

}
~~~