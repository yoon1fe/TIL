## 프로젝트

**예제 상황**

- v1 - 인터페이스와 구현 클래스 - 스프링 빈으로 수동 등록
- v2 - 인터페이스 없는 구체 클래스 - 스프링 빈으로 수동 등록
- v3 - 컴포넌트 스캔으로 스프링 빈 자동 등록



위처럼 다양한 케이스에서 프록시를 적용하는 방법 알아보자.



### v1 - 인터페이스와 구현 클래스 - 스프링 빈으로 수동 등록

`Repository`, `Service`, `Controller` 모두 인터페이스 및 구현 클래스 생성.

- 참고: 스프링은 @Controller 또는 @RequestMapping 이 있어야 스프링 컨트롤러로 인식한다. 스프링 부트 3.0 (스프링 6.0)부터는 오직 `@Controller` 애너테이션이 붙은 클래스만 스프링 컨트롤러로 인식한다!!



**`AppV1Config.java`**

```java
package hello.proxy.config;

import hello.proxy.app.v1.OrderControllerV1;
import hello.proxy.app.v1.OrderControllerV1Impl;
import hello.proxy.app.v1.OrderRepositoryV1;
import hello.proxy.app.v1.OrderRepositoryV1Impl;
import hello.proxy.app.v1.OrderServiceV1;
import hello.proxy.app.v1.OrderServiceV1Impl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppV1Config {

  @Bean
  public OrderControllerV1 orderControllerV1() {
    return new OrderControllerV1Impl(orderServiceV1());
  }

  @Bean
  public OrderServiceV1 orderServiceV1() {
    return new OrderServiceV1Impl(orderRepositoryV1());
  }

  @Bean
  public OrderRepositoryV1 orderRepositoryV1() {
    return new OrderRepositoryV1Impl();
  }

}
```



**`ProxyApplication.java`**

```java
package hello.proxy;

import hello.proxy.config.AppV1Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(AppV1Config.class)
@SpringBootApplication(scanBasePackages = "hello.proxy.app") //주의
public class ProxyApplication {

 public static void main(String[] args) {
  SpringApplication.run(ProxyApplication.class, args);
 }

}
```

- `@Import(AppV1Config.class)`: 클래스를 스프링 빈으로 등록한다. 일반적으로 `@Configuration` 같은 설정 파일을 등록할 때 사용하지만 스프링 빈을 등록할 때도 사용 가능
- `@SpringBootApplication(scanBasePackages = "hello.proxy.app")`: `@ComponentScan` 과 기능 같다. 컴포넌트 스캔 시작할 위치 지정. 해당 패키지와 그 하위 패키지를 스캔한다. 디폴트 값은 해당 애너테이션이 있는 클래스의 패키지와 그 하위 패키지.



### v2 - 인터페이스 없는 구체 클래스 - 스프링 빈으로 수동 등록



**`AppV2Config.java`**

```java
package hello.proxy.config;

import hello.proxy.app.v2.OrderControllerV2;
import hello.proxy.app.v2.OrderRepositoryV2;
import hello.proxy.app.v2.OrderServiceV2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppV2Config {

  @Bean
  public OrderControllerV2 orderControllerV2() {
    return new OrderControllerV2(orderServiceV2());
  }

  @Bean
  public OrderServiceV2 orderServiceV2() {
    return new OrderServiceV2(orderRepositoryV2());
  }

  @Bean
  public OrderRepositoryV2 orderRepositoryV2() {
    return new OrderRepositoryV2();
  }

}
```

- 수동 빈 등록



### v3 - 컴포넌트 스캔으로 스프링 빈 자동 등록

컴포넌트 스캔의 대상이 되도록 `@Repository`, `@Service`, `@RestController` 를 붙이면, `ProxyApplication`에서 `@SpringBootApplication(scanBasePackages = "hello.proxy.app")`을 사용하고 있기 때문에 자동으로 스프링 빈으로 등록이 된다!



### 요구사항 추가

저번에 만든 로그 추적기를 사용하려면 어쨌든 기존 원본 코드를 변경해야 한다..



**요구사항 추가**

- **원본 코드 수정하지 않고 로그 추적기 적용**
  - **프록시!!**
- 특정 메서드는 로그 출력 X
- 다양한 케이스에 모두 적용



## 프록시, 프록시 패턴, 데코레이터 패턴

**프록시란??**

- 클라이언트 - 서버 사이에서 클라이언트의 요청을 대신 받아서 서버에 다시 요청하는 **대리자**.
- 클라이언트는 대리자를 통해 요청했기 때문에 클라이언트는 그 이후의  과정은 알 수 없다.
- 서버와 프록시는 같은 인터페이스를 사용해야 한다. DI를 사용해서 대체 가능하도록.



**프록시의 주요 기능**

- 접근 제어
  - 권한에 따른 접근 차단
  - 캐싱
  - 지연 로딩
- 부가 기능 추가



**GOF 디자인 패턴에서의 프록시**

- 프록시 패턴: 접근 제어가 주 목적
- 데코레이터 패턴: 새로운 기능 추가가 주 목적



### 프록시 패턴

캐시로서의 프록시

``` java
package hello.proxy.pureproxy.proxy.code;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheProxy implements Subject {

  private Subject target;
  private String cacheValue;

  @Override
  public String operation() {
    log.info("프록시 객체 호출");
    if (cacheValue == null) {
      cacheValue = target.operation();
    }
    return cacheValue;
  }
}
```

- 최종적으로 실제 객체를 호출해야 하므로 내부에 실제 객체의 참조를 갖고 있어야 한다(`target`).
- `client -> cacheProxy -> realSubject` 런타임 의존 관계 형성



``` 
15:57:14.056 INFO hello.proxy.pureproxy.proxy.code.CacheProxy - 프록시 객체 호출
15:57:14.060 INFO hello.proxy.pureproxy.proxy.code.RealSubject - 실제 객체 호출
15:57:15.065 INFO hello.proxy.pureproxy.proxy.code.CacheProxy - 프록시 객체 호출
15:57:15.065 INFO hello.proxy.pureproxy.proxy.code.CacheProxy - 프록시 객체 호출
```



### 데코레이터 패턴

데코레이터 패턴: 프록시를 활용해서 **부가 기능** 추가

- 요청 값이나, 응답 값을 중간에 변형
- 실행 시간을 측정해서 추가 로그



**응답값 변형**

``` java
package hello.proxy.pureproxy.decorator.code;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageDecorator implements Component {

  private Component component;

  public MessageDecorator(Component component) {
    this.component = component;
  }

  @Override
  public String operation() {
    log.info("MessageDecorator 실행");
    String result = component.operation();
    String decoResult = "*****" + result + "*****";
    log.info("MessageDecorator 꾸미기 적용 전={}, 적용 후={}", result,
        decoResult);
    return decoResult;
  }
}
```



``` 
16:11:45.599 [Test worker] INFO hello.proxy.pureproxy.decorator.code.MessageDecorator - MessageDecorator 실행
16:11:45.601 [Test worker] INFO hello.proxy.pureproxy.decorator.code.RealComponent - RealComponent 실행
16:11:45.605 [Test worker] INFO hello.proxy.pureproxy.decorator.code.MessageDecorator - MessageDecorator 꾸미기 적용 전=data, 적용 후=*****data*****
16:11:45.606 [Test worker] INFO hello.proxy.pureproxy.decorator.code.DecoratorPatternClient - result=*****data*****

```



**실행 시간 측정 기능**

``` java
package hello.proxy.pureproxy.decorator.code;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeDecorator implements Component {

  private Component component;

  public TimeDecorator(Component component) {
    this.component = component;
  }

  @Override
  public String operation() {
    log.info("TimeDecorator 실행");
    long startTime = System.currentTimeMillis();
    String result = component.operation();
    long endTime = System.currentTimeMillis();
    long resultTime = endTime - startTime;
    log.info("TimeDecorator 종료 resultTime={}ms", resultTime);
    return result;
  }
}
```

- `client -> timeDecorator -> messageDecorator -> realComponent` 의 의존관계 설정 가능



**프록시 패턴 vs 데코레이터 패턴**

둘은 **의도(intent)**에 따라 구분 가능하다.

- 프록시 패턴의 의도: 다른 객체에 대한 **접근을 제어**하기 위해 대리자를 제공
- 데코레이턴 패턴의 의도: **객체에 추가 책임(기능)을 동적으로 추가**하고, 기능 확장을 위한 유연한 대안 제공



## 인터페이스 기반 프록시

프록시를 사용하면 기존 코드를 수정하지 않고 로그 추적 기능 도입 가능!! (데코레이터 패턴)



**`OrderRepositoryInterfaceProxy.java`**

``` java
package hello.proxy.config.v1_proxy.interface_proxy;

import hello.proxy.app.v1.OrderRepositoryV1;
import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderRepositoryInterfaceProxy implements OrderRepositoryV1 {

  private final OrderRepositoryV1 target;
  private final LogTrace logTrace;

  @Override
  public void save(String itemId) {
    TraceStatus status = null;
    try {
      status = logTrace.begin("OrderRepository.save()");
      //target 호출
      target.save(itemId);
      logTrace.end(status);
    } catch (Exception e) {
      logTrace.exception(status, e);
      throw e;
    }
  }
}

```

- `OrderRepository` 인터페이스를 구현하는 프록시 생성. 구현한 메서드 앞뒤로 `LogTrace` 사용하는 로직 추가



**`OrderServiceInterfaceProxy.java`**

``` java
package hello.proxy.config.v1_proxy.interface_proxy;

import hello.proxy.app.v1.OrderServiceV1;
import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderServiceInterfaceProxy implements OrderServiceV1 {

  private final OrderServiceV1 target;
  private final LogTrace logTrace;

  @Override
  public void orderItem(String itemId) {
    TraceStatus status = null;
    try {
      status = logTrace.begin("OrderService.orderItem()");
      //target 호출
      target.orderItem(itemId);
      logTrace.end(status);
    } catch (Exception e) {
      logTrace.exception(status, e);
      throw e;
    }
  }
}
```



**`OrderControllerInterfaceProxy.java`**

``` java
package hello.proxy.config.v1_proxy.interface_proxy;

import hello.proxy.app.v1.OrderControllerV1;
import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderControllerInterfaceProxy implements OrderControllerV1 {

  private final OrderControllerV1 target;
  private final LogTrace logTrace;

  @Override
  public String request(String itemId) {
    TraceStatus status = null;
    try {
      status = logTrace.begin("OrderController.request()");
      //target 호출
      String result = target.request(itemId);
      logTrace.end(status);
      return result;
    } catch (Exception e) {
      logTrace.exception(status, e);
      throw e;
    }
  }

  @Override
  public String noLog() {
    return target.noLog();
  }
}
```



**`InterfaceProxyConfig.java`**

``` java
package hello.proxy.config.v1_proxy;

import hello.proxy.app.v1.*;
import
    hello.proxy.config.v1_proxy.interface_proxy.OrderControllerInterfaceProxy;
import
    hello.proxy.config.v1_proxy.interface_proxy.OrderRepositoryInterfaceProxy;
import hello.proxy.config.v1_proxy.interface_proxy.OrderServiceInterfaceProxy;
import hello.proxy.trace.logtrace.LogTrace;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InterfaceProxyConfig {

  @Bean
  public OrderControllerV1 orderController(LogTrace logTrace) {
    OrderControllerV1Impl controllerImpl = new
        OrderControllerV1Impl(orderService(logTrace));
    return new OrderControllerInterfaceProxy(controllerImpl, logTrace);
  }

  @Bean
  public OrderServiceV1 orderService(LogTrace logTrace) {
    OrderServiceV1Impl serviceImpl = new
        OrderServiceV1Impl(orderRepository(logTrace));
    return new OrderServiceInterfaceProxy(serviceImpl, logTrace);
  }

  @Bean
  public OrderRepositoryV1 orderRepository(LogTrace logTrace) {
    OrderRepositoryV1Impl repositoryImpl = new OrderRepositoryV1Impl();
    return new OrderRepositoryInterfaceProxy(repositoryImpl, logTrace);
  }
}
```

- 프록시를 생성하고 **프록시를 스프링 빈으로 등록한다. 실제 객체는 빈으로 등록하지 않음**



## 구체 클래스 기반 프록시

자바의 다향성은 인터페이스를 구현하든 클래스를 상속하든 상위 타입만 맞으면 다형성이 적용되기 때문에 인터페이스 없이도 프록시를 만들 수 있다.



**`OrderRepositoryConcreteProxy.java`**

``` java
package hello.proxy.config.v1_proxy.concrete_proxy;

import hello.proxy.app.v2.OrderRepositoryV2;
import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;

public class OrderRepositoryConcreteProxy extends OrderRepositoryV2 {

  private final OrderRepositoryV2 target;
  private final LogTrace logTrace;

  public OrderRepositoryConcreteProxy(OrderRepositoryV2 target, LogTrace
      logTrace) {
    this.target = target;
    this.logTrace = logTrace;
  }

  @Override
  public void save(String itemId) {
    TraceStatus status = null;
    try {
      status = logTrace.begin("OrderRepository.save()");
      //target 호출
      target.save(itemId);
      logTrace.end(status);
    } catch (Exception e) {
      logTrace.exception(status, e);
      throw e;
    }
  }
}
```

- 인터페이스가 아닌 `OrderRepositoryV2` 클래스를 상속받아 프록시를 생성



**`OrderServiceConcreteProxy.java`**

``` java
package hello.proxy.config.v1_proxy.concrete_proxy;

import hello.proxy.app.v2.OrderServiceV2;
import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;

public class OrderServiceConcreteProxy extends OrderServiceV2 {

  private final OrderServiceV2 target;
  private final LogTrace logTrace;

  public OrderServiceConcreteProxy(OrderServiceV2 target, LogTrace logTrace) {
    super(null);
    this.target = target;
    this.logTrace = logTrace;
  }

  @Override
  public void orderItem(String itemId) {
    TraceStatus status = null;
    try {
      status = logTrace.begin("OrderService.orderItem()");
      //target 호출
      target.orderItem(itemId);
      logTrace.end(status);
    } catch (Exception e) {
      logTrace.exception(status, e);
      throw e;
    }
  }
}
```



**클래스 기반 프록시의 단점**

- `super(null)`: 자바 기본 문법에 의해 자식 클래스를 생성할 때는 항상 `super()` 로 부모 클래스의 생성자를 호출해야 한다. 이 부분을 생략하면 기본 생성자가 호출된다. 그런데 부모 클래스인 `OrderServiceV2` 는 기본 생성자가 없고, 생성자에서 파라미터 1개를 필수로 받는다. 따라서 파라미터를 넣어서 `super(..)` 를 호출해야 한다.



## 인터페이스 기반 프록시와 클래스 기반 프록시

- 클래스 기반 프록시는 해당 클래스에만 적용할 수 있다. 인터페이스 기반 프록시는 인터페이스만 같으면 모든 곳에 적용할 수 있다.
- 클래스 기반 프록시는 상속을 사용하기 때문에 몇가지 제약이 있다. 
  - 부모 클래스의 생성자를 호출해야 한다.(앞서 본 예제) 
  - 클래스에 final 키워드가 붙으면 상속이 불가능하다. 
  - 메서드에 final 키워드가 붙으면 해당 메서드를 오버라이딩 할 수 없다.



인터페이스 기반의 프록시는 상속이라는 제약에서 자유롭다.

인터페이스 기반 프록시의 단점은 인터페이스가 필요하다는 것 그 자체.



**너무 많은 프록시 클래스**

프록시 클래스를 너무 많이 만들어야 한다는 단점.

대상 클래스만 다를 뿐, 로직은 모두 똑같다. 프록시 클래스를 하나만 만들어서 모든 곳에 적용하는 방법은 없을까? 바로 다음에 설명할 동적 프록시 기술이 이 문제를 해결해준다.
