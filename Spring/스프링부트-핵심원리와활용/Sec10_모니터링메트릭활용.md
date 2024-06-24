## 메트릭 등록

CPU/메모리 사용량, 톰캣 스레드, DB 커넥션 풀과 같이 공통으로 사용되는 기술 메트릭은 이미 등록되어 있다.

추가로 비즈니스적으로 유의미한 메트릭도 필요할 수 있기 때문에 직접 등록하고 확인해야 함.



### 예제

- 주문수, 취소수
  - 계속 증가하는 수치 == 카운터
- 재고 수량
  - 증가 or 감소 == 게이지



```java
package hello.controller;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderController {

  public final OrderService orderService;

  @GetMapping("/order")
  public String order() {
    log.info("order");
    orderService.order();

    return "order";
  }

  @GetMapping("/cancel")
  public String cancel() {
    log.info("cancel");
    orderService.cancel();

    return "cancel";
  }

  @GetMapping("/stock")
  public int stock() {
    log.info("stock");
    return orderService.getStock().get();
  }

}
```



```java
package hello.order;

public interface OrderService {

  void order();
  void cancel();
  AtomicInteger getStock();

}
```



```java
package hello.order.v0;

@Slf4j
public class OrderServiceV0 implements OrderService {

  private AtomicInteger stock = new AtomicInteger(100);

  @Override
  public void order() {
    log.info("주문");
    stock.decrementAndGet();
  }

  @Override
  public void cancel() {
    log.info("취소");
    stock.incrementAndGet();

  }

  @Override
  public AtomicInteger getStock() {
    return stock;
  }
}
```



### 카운터

마이크로미터를 사용해서 메트릭을 직접 등록해보자. 주문수, 취소수를 대상으로 카운터 메트릭 등록해보자.



**MeterRegistry**

- 마이크로미터 기능을 제공하는 핵심 컴포넌트.
- 스프링을 통해서 주입 받아서 사용하고, 이곳을 통해서 카운터, 게이지 등을 등록



**Counter**

- 단조롭게 증가하는 단일 누적 측정항목
- 보통 하나씩 증가
- 누적이므로 전체 값을 포함(total)
- ex) HTTP 요청수



```java
package hello.order.v1;

import hello.order.OrderService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrderServiceV1 implements OrderService {

  private final MeterRegistry registry;
  private AtomicInteger stock = new AtomicInteger(100);

  @Override
  public void order() {
    log.info("주문");
    stock.decrementAndGet();

    Counter.builder("my.order")
        .tag("class", this.getClass().getName())
        .tag("method", "order")
        .description("order")
        .register(registry).increment();
  }

  @Override
  public void cancel() {
    log.info("취소");
    stock.incrementAndGet();

    Counter.builder("my.order")
        .tag("class", this.getClass().getName())
        .tag("method", "cancel")
        .description("cancel")
        .register(registry).increment();
  }

  @Override
  public AtomicInteger getStock() {
    return stock;
  }
}
```

- `Counter.builder(name)`를 통해서 카운터 생성. `name`에 메트릭 이름 지정.
- `tag`: 프로메테우스에서 필터링할 수 있는 레이블로 사용됨.

- 이제 각각의 메서드를 호출할 때마다 카운터가 증가한다.







### @Counted

OrderServiceV1의 단점은 메트릭을 관리하는 로직이 핵심 비즈니스 로직에 침투해있다는 점. 스프링 AOP 사용하면 깔끔해질 것 같다.

마이크로미터는 이런 상황에 필요한 AOP 구성 요소를 이미 다 만들어두었다!!



```java
package hello.order.v2;

import hello.order.OrderService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrderServiceV2 implements OrderService {

  private final MeterRegistry registry;
  private AtomicInteger stock = new AtomicInteger(100);

  @Counted("my.order")
  @Override
  public void order() {
    log.info("주문");
    stock.decrementAndGet();
  }

  @Counted("my.order")
  @Override
  public void cancel() {
    log.info("취소");
    stock.incrementAndGet();
  }

  @Override
  public AtomicInteger getStock() {
    return stock;
  }
}
```

- 측정을 원하는 메서드에 `@Counted` 애너테이션 지정.
- 이렇게 하면 `tag`에 `method`를 기준으로 분류해서 적용한다.



```java
package hello.order.v2;

import hello.order.OrderService;
import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderConfigV2 {

  @Bean
  public OrderService orderService() {
    return new OrderServiceV2();
  }

  @Bean
  public CountedAspect countedAspect(MeterRegistry registry) {
    return new CountedAspect(registry);
  }
}
```

- `CountedAspect` 빈을 등록하면 `@Counted`를 인식해서 `Counter`를 사용하는 AOP를 적용한다.



### Timer





### @Timed





### 게이지





## 실무 모니터링 환경 구성 팁