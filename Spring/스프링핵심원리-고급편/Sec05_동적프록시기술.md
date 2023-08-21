## 리플렉션

프록시를 사용해서 기존 코드 변경 없이 로그 추적기라는 부가 기능 적용했다. 근데 이 방식은 대상 클래스 수 만큼 프록시 클래스를 만들어야 한다는 점.

JDK 동적 프록시나 CGLIB 같은 기술을 사용해서 프록시 객체를 동적으로 만들 수 있음.



**리플렉션**

- 클래스나 메서드의 메타 정보를 사용해서 동적으로 호출하는 메서드를 변경할 수 있음



```java
@Test
void reflection1() throws Exception {
  //클래스 정보
  Class classHello = Class.forName("hello.proxy.jdkdynamic.ReflectionTest$Hello");
  Hello target = new Hello();

  //callA 메서드 정보
  Method methodCallA = classHello.getMethod("callA");
  Object result1 = methodCallA.invoke(target);
  log.info("result1={}", result1);

  //callB 메서드 정보
  Method methodCallB = classHello.getMethod("callB");
  Object result2 = methodCallB.invoke(target);
  log.info("result2={}", result2);
}

@Test
void reflection2() throws Exception {
  Class classHello = Class.forName("hello.proxy.jdkdynamic.ReflectionTest$Hello");
  Hello target = new Hello();

  Method methodCallA = classHello.getMethod("callA");
  dynamicCall(methodCallA, target);

  Method methodCallB = classHello.getMethod("callB");
  dynamicCall(methodCallB, target);
}

private void dynamicCall(Method method, Object target) throws Exception {
  log.info("start");
  Object result = method.invoke(target);
  log.info("result={}", result);
}
```

- `Class.forName("hello.proxy.jdkdynamic.ReflectionTest$Hello")`: 클래스 메타 정보를 획득. 내부 클래스는 구분을 위해서 `$` 사용한다.
- `classHello.getMethod("call")`: 해당 클래스의 `call` 메서드 메타 정보 획득
- `methodCallA.invoke(target)`: 획득한 메서드 메타 정보로 실제 인스턴스의 메서드를 호출한다.
- 클래스나 메서드 정보를 동적으로 변경할 수 있다!

- `dynamicCall(Method, Object)`: 공통 로직을 모두 처리할 수 있는 공통 처리 로직. 호출할 메서드 정보와 실행할 인스턴스 정보를 넘기면 된다.



**주의**

- 리플렉션 기술은 런타임에 동작하므로 컴파일 시점에 오류를 잡을 수 없다.
- 따라서 일반적으로 리플렉션 기술은 사용하지 않는 것이 좋다.



## JDK 동적 프록시

보통 프록시의 로직은 같고, 적용 대상의 차이 정도만 있다. 일일이 프록시 클래스를 작성하는 것은 너무 번거롭다.

동적 프록시 기술을 사용하면 개발자가 직접 프록시 클래스를 작성하지 않아도 된다. 런타임에 동적으로 만들어줌.

참고) JDK 동적 프록시는 인터페이스 기반으로 프록시를 동적으로 생성하므로 인터페이스 필수



### 예제

JDK 동적 프록시에 적용할 로직은 `InvocationHandler` 인터페이스를 구현해서 작성하면 된다.



``` java
package java.lang.reflect;

public interface InvocationHandler {
  public Object invoke(Object proxy, Method method, Object[] args)
    throws Throwable;
}
```

- `Object proxy`: 프록시 자신
- `Method method`: 호출한 메서드
- `Object[] args`: 메서드를 호출할 때 전달한 인수



```java
package hello.proxy.jdkdynamic.code;

@Slf4j
@RequiredArgsConstructor
public class TimeInvocationHandler implements InvocationHandler {
  
  private final Object target;
  
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    log.info("TimeProxy 실행");
    long startTime = System.currentTimeMillis();
    
    Object result = method.invoke(target, args);
    
    long endTime = System.currentTimeMillis();
    long resultTime = endTime - startTime;
    log.info("TimeProxy 종료 resultTime = {}", resultTime);
    
    return result;
  }
}
```

- `Object target`: 동적 프록시가 호출할 대상
- `method.invoke(target, args)`: 리플렉션을 사용해서 `target` 인스턴스의 메서드를 실행



```java
package hello.proxy.jdkdynamic;

@Slf4j
public class JdkDynamicProxyTest {

  @Test
  void dynamicA() {
    AInterface target = new AImpl();
    TimeInvocationHandler handler = new TimeInvocationHandler(target);
    AInterface proxy = (AInterface) Proxy.newProxyInstance(AInterface.class.getClassLoader(), new Class[]{AInterface.class}, handler);
    proxy.call();
    log.info("targetClass={}", target.getClass());
    log.info("proxyClass={}", proxy.getClass());
  }

}
```

- `Proxy.newProxyInstance(AInterface.class.getClassLoader(), new Class[]{AInterface.class}, handler);`: 클래스 로더 정보, 인터페이스, 핸들러 로직 넣어주면 프록시 생성 가능.

- 프록시의 `call()` 호출하면 프록시가 우리가 지정해준 `InvocationHandler`의 `invoke()`를 호출한다.



**실행 결과**



**실행 결과**

```
[Test worker] INFO hello.proxy.jdkdynamic.code.TimeInvocationHandler - TimeProxy 실행
[Test worker] INFO hello.proxy.jdkdynamic.code.AImpl - A
[Test worker] INFO hello.proxy.jdkdynamic.code.TimeInvocationHandler - TimeProxy 종료 resultTime = 1
[Test worker] INFO hello.proxy.jdkdynamic.JdkDynamicProxyTest - targetClass=class hello.proxy.jdkdynamic.code.AImpl
[Test worker] INFO hello.proxy.jdkdynamic.JdkDynamicProxyTest - proxyClass=class com.sun.proxy.$Proxy12
```



### 적용

**`LogTraceBasicHandler.java`**

``` java
package hello.proxy.config.v2_dynamicproxy.handler;

import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LogTraceBasicHandler implements InvocationHandler {

  private final Object target;
  private final LogTrace logTrace;

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    TraceStatus status = null;
    try {
      String msg = method.getDeclaringClass().getSimpleName() + "." + method.getName() + "()";
      status = logTrace.begin(msg);

      //target 호출
      Object result = method.invoke(target, args);
      logTrace.end(status);
      return result;
    } catch (Exception e) {
      logTrace.exception(status, e);
      throw e;
    }
  }
}

```

- `Object target`: 프록시가 호출할 대상
- 파라미터로 넘어오는 `Method` 객체로 호출되는 메서드 정보, 클래스 정보를 동적으로 획득 가능



**`config`**

```java
package hello.proxy.config.v2_dynamicproxy;

import hello.proxy.app.v1.OrderControllerV1;
import hello.proxy.app.v1.OrderControllerV1Impl;
import hello.proxy.app.v1.OrderRepositoryV1;
import hello.proxy.app.v1.OrderRepositoryV1Impl;
import hello.proxy.app.v1.OrderServiceV1;
import hello.proxy.app.v1.OrderServiceV1Impl;
import hello.proxy.config.v2_dynamicproxy.handler.LogTraceBasicHandler;
import hello.proxy.trace.logtrace.LogTrace;
import java.lang.reflect.Proxy;
import lombok.extern.java.Log;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamicProxyBasicConfig {

  @Bean
  public OrderControllerV1 orderControllerV1(LogTrace logTrace) {
    OrderControllerV1 orderController = new OrderControllerV1Impl(orderServiceV1(logTrace));
    OrderControllerV1 proxy = (OrderControllerV1) Proxy.newProxyInstance(OrderControllerV1.class.getClassLoader(),
        new Class[]{OrderControllerV1.class},
        new LogTraceBasicHandler(orderController, logTrace));

    return proxy;
  }
  @Bean
  public OrderServiceV1 orderServiceV1(LogTrace logTrace) {
    OrderServiceV1 orderService = new OrderServiceV1Impl(orderRepositoryV1(logTrace));
    OrderServiceV1 proxy = (OrderServiceV1) Proxy.newProxyInstance(OrderServiceV1.class.getClassLoader(),
        new Class[]{OrderServiceV1.class},
        new LogTraceBasicHandler(orderService, logTrace));
    
    return proxy;
  }

  @Bean
  public OrderRepositoryV1 orderRepositoryV1(LogTrace logTrace) {
    OrderRepositoryV1 orderRepository = new OrderRepositoryV1Impl();

    OrderRepositoryV1 proxy = (OrderRepositoryV1) Proxy.newProxyInstance(OrderRepositoryV1.class.getClassLoader(),
        new Class[]{OrderRepositoryV1.class}, new LogTraceBasicHandler(orderRepository, logTrace));

    return proxy;
  }

}
```

- JDK 동적 프록시 기술을 사용해서 `Controller`, `Service`, `Repository` 클래스의 동적 프록시 생성



**남은 문제**

- `no-log`를 실행해도 동적 프록시가 적용되는 문제



```java
package hello.proxy.config.v2_dynamicproxy.handler;

@RequiredArgsConstructor
public class LogTraceFilterHandler implements InvocationHandler {

  private final Object target;
  private final LogTrace logTrace;
  private final String[] patterns;

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // 메서드명 필터링
    String methodName = method.getName();
    if (!PatternMatchUtils.simpleMatch(patterns, methodName)) {
      return method.invoke(target, args);
    }

    ...
  }
}
```

- 메서드명을 획득할 수 있으므로 패턴 매칭을 통해 필터링



**`config`**

```java
package hello.proxy.config.v2_dynamicproxy;

@Configuration
public class DynamicProxyFilterConfig {
  
  private static final String[] PATTERNS = {"request*", "order*", "save*"};

  @Bean
  public OrderControllerV1 orderControllerV1(LogTrace logTrace) {
    OrderControllerV1 orderController = new OrderControllerV1Impl(orderServiceV1(logTrace));
    OrderControllerV1 proxy = (OrderControllerV1) Proxy.newProxyInstance(OrderControllerV1.class.getClassLoader(),
        new Class[]{OrderControllerV1.class},
        new LogTraceFilterHandler(orderController, logTrace, PATTERNS));

    return proxy;
  }
  
  ...
}
```



**JDK 동적 프록시의 한계**

- JDK 동적 프록시는 인터페이스가 필수이다.
- V2 애플리케이션처럼 인터페이스없이 클래스만 있는 경우에는 **CGLIB**라는 바이트 코드를 조작하는 라이브러리를 사용해야 한다.



## CGLIB

CGLIB: Code Generator LIBrary

- 바이트 코드 조작해서 동적으로 클래스를 생성하는 라이브러리
- 인터페이스없이 구체 클래스만으로 동적 프록시 생성 가능
- 스프링은 `ProxyFactory`라는 클래스를 제공해주기 때문에 직접 CGLIB를 사용할 일은 거의 없다.

- JDK 동적 프록시의 `InvocationHandler` == CGLIB의 `MethodInterceptor`



```java
package org.springframework.cglib.proxy;

public interface MethodInterceptor extends Callback {

  Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable;
}
```

- `obj`: CGLIB가 적용된 객체
- `method`: 호출된 메서드
- `args`: 메서드를 호출하면서 전달된 인수
- `proxy`: 메서드 호출에 사용





### 예제

```java
package hello.proxy.cglib.code;

import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

@Slf4j
@RequiredArgsConstructor
public class TimeMethodInterceptor implements MethodInterceptor {

  private final Object target;

  @Override
  public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
    log.info("TimeProxy 실행");
    long startTime = System.currentTimeMillis();
    
    Object result = methodProxy.invoke(target, args);

    long endTime = System.currentTimeMillis();
    long resultTime = endTime - startTime;
    log.info("TimeProxy 종료 resultTime = {}", resultTime);

    return result;
  }
}
```

- `MethodInterceptor`를 구현해서 CGLIB 프록시 로직 정의
- 참고) `method`를 사용해도 되지만, 성능상 CGLIB는 `MethodProxy methodProxy`를 사용하는 것을 권장



```java
package hello.proxy.cglib;

@Slf4j
public class CglibTest {

  @Test
  void cglib() {
    ConcreteService target = new ConcreteService();
    
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(ConcreteService.class);
    enhancer.setCallback(new TimeMethodInterceptor(target));
    
    ConcreteService proxy = (ConcreteService) enhancer.create();
    
    log.info("targetClass={}", target.getClass());
    log.info("proxyClass={}", proxy.getClass());
    proxy.call();
  }
}
```

- `Enhancer`: CGLIB는 `Enhancer`를 사용해서 프록시를 생성
- `enhancer.setSuperClass(ConcreteService.class)`: 어떤 구체 클래스를 상속받을지를 지정
- `enhancer.setCallback(new TimeMethodInterceptor(target))`: 프록시에 적용할 실행 로직을 할당
- `enhancer.create()`: 프록시 인스턴스 생성



**실행 결과**

```
[Test worker] INFO hello.proxy.cglib.CglibTest - targetClass=class hello.proxy.common.service.ConcreteService
[Test worker] INFO hello.proxy.cglib.CglibTest - proxyClass=class hello.proxy.common.service.ConcreteService$$EnhancerByCGLIB$$25d6b0e3
[Test worker] INFO hello.proxy.cglib.code.TimeMethodInterceptor - TimeProxy 실행
[Test worker] INFO hello.proxy.common.service.ConcreteService - concreteService 호출
[Test worker] INFO hello.proxy.cglib.code.TimeMethodInterceptor - TimeProxy 종료 resultTime = 10
```



**CGLIB 제약**

클래스 기반 프록시는 상속을 사용하기 때문에 몇가지 제약이 있다.

- 부모 클래스의 생성자 체크 필요 -> 자식 클래스를 동적으로 생성하므로 기본 생성자 필요함
- 클래스나 메서드에 `final` 키워드 있으면 상속/오버라이딩 불가능



**남은 문제**

- 인터페이스가 있는 경우에는 JDK 동적 프록시를 적용하고, 그렇지 않은 경우에는 CGLIB를 적용하려면 어떻게 해야할까? 
- 두 기술을 함께 사용할 때 부가 기능을 제공하기 위해서 JDK 동적 프록시가 제공하는 InvocationHandler 와 CGLIB가 제공하는 MethodInterceptor 를 각각 중복으로 만들어서 관리해야 할까? 
- 특정 조건에 맞을 때 프록시 로직을 적용하는 기능도 공통으로 제공되었으면?