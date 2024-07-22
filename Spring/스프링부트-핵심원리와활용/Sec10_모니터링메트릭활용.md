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

- 시간에 대한 측정
- 카운터와 유사한데, `Timer`를 사용하면 실행 시간도 함께 측정 가능
- `seconds_count`: 누적 실행 수 - 카운터
- `secords_sum`: 실행 시간의 합 - sum
- `seconds_max`: 최대 실행 시간(가장 오래 걸린 실행 시간) - 게이지

```java
@Slf4j
public class OrderServiceV3 implements OrderService {

  private final MeterRegistry registry;
  private AtomicInteger stock = new AtomicInteger(100);

  public OrderServiceV3(MeterRegistry registry) {
    this.registry = registry;
  }

  @Override
  public void order() {
    Timer timer = Timer.builder("my.order")
                         .tag("class", this.getClass().getName())
                         .tag("method", "order")
                         .description("order")
                         .register(registry);

    timer.record(() -> {
      log.info("주문");
      stock.decrementAndGet();
      sleep(500);
    });
  }

  @Override
  public void cancel() {
    Timer timer = Timer.builder("my.order")
                      .tag("class", this.getClass().getName())
                      .tag("method", "cancel")
                      .description("order")
                      .register(registry);

    timer.record(() -> {
      log.info("취소");
      stock.incrementAndGet();
      sleep(200);
    });

  }

  private static void sleep(int l) {
    try {
      Thread.sleep(l + new Random().nextInt(200));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public AtomicInteger getStock() {
    return stock;
  }
}
```

- `Timer.builder(name)`을 통해 타이머 생성. `name` 에는 메트릭 이름 지정
- `tage`: 프로메테우스에서 필터할 수 있는 레이블로 사용됨
- 주문/취소는 메트릭 이름은 같고 `tag` 통해서 구분
- `register(registry)`: 만든 타이머를 MeterRegistry에 등록해야 실제 동작한다.

- 타이머 사용할 때는 `timer.record()` 사용.



**액추에이터 메트릭**

- `COUNT` : 누적 실행 수(카운터와 같다) 
- `TOTAL_TIME` : 실행 시간의 합(각각의 실행 시간의 누적 합이다) 
- `MAX` : 최대 실행 시간(가장 오래 걸린 실행시간이다)



**프로메테우스 메트릭**

- `seconds_count`: 누적 실행 수
- `secords_sum`: 실행 시간의 합
- `seconds_max`: 최대 실행 시간(가장 오래 걸린 실행 시간)



### @Timed

- 타이머(Timer) AOP 버전!

```java
@Timed(value = "my.order")
@Slf4j
public class OrderServiceV4 implements OrderService {

  private AtomicInteger stock = new AtomicInteger(100);

  public OrderServiceV4(MeterRegistry registry) {
    this.registry = registry;
  }

  @Override
  public void order() {
    log.info("주문");
    stock.decrementAndGet();
    sleep(500);
  }

  @Override
  public void cancel() {
    log.info("취소");
    stock.incrementAndGet();
    sleep(200);

  }

  private static void sleep(int l) {
    try {
      Thread.sleep(l + new Random().nextInt(200));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public AtomicInteger getStock() {
    return stock;
  }
}
```



```java
package hello.order.v4;

import hello.order.OrderService;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderConfigV4 {

  @Bean
  OrderService orderService() {
    return new OrderServiceV4();
  }

  @Bean
  public TimedAspect timedAspect(MeterRegistry meterRegistry) {
    return new TimedAspect(meterRegistry);
  }
}
```

- TimedAspect 빈 추가 해줘야 `@Timed` 에 AOP가 적용된다!



### 게이지

- 임의로 오르내릴 수 있는 단일 숫자 값을 나타내는 메트릭
- 현재의 상태를 보는데 사용
- 값이 증가하거나 감소할 수 있음
- 예) 차량 속도, CPU 사용량, 메모리 사용량



```java
@Configuration
public class StockConfigV1 {

  @Bean
  public MyStockMetric myStockMetric(OrderService orderService, MeterRegistry meterRegistry) {
    return new MyStockMetric(orderService, meterRegistry);
  }
  @Slf4j
  static class MyStockMetric {
    private OrderService orderService;
    private MeterRegistry meterRegistry;

    public MyStockMetric(OrderService orderService, MeterRegistry meterRegistry) {
      this.orderService = orderService;
      this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
      Gauge.builder("my.stock", orderService, service -> {
        log.info("stock gauge called");
        return service.getStock().get();
      }).register(meterRegistry);
    }
  }
}
```

- `my.stock` 이라는 이름의 게이지 등록

- init() 메서드의 리턴값이 게이지의 값.
- 해당 함수를 프로메테우스가 계속 호출한다.



**게이지 등록 간단한 방법**

```java
@Slf4j
@Configuration
public class StockConfigV2 {

  @Bean
  public MeterBinder stockSize(OrderService orderService) {
    return meterRegistry -> Gauge.builder("my.stock", orderService, service -> {
      log.info("stock gauge called");
      return service.getStock().get();
    }).register(meterRegistry);
  }

}
```



## 정리

**MeterRegistry**

- 마이크로미터 기능을 제공하는 핵심 컴포넌트
- 스프링을 통해 주입받아서 사용하고, 이 곳을 통해서 카운터, 게이지 등을 등록



**Tag, 레이블**

- Tag를 사용하면 데이터를 나누어서 확인 가능
- Tag는 카디널리티가 낮으면서 그룹화할 수 있는 단위에 사용해야 함! 예) 성별, 주문 상태, 결제 수단 등..
- 카디널리티가 높으면 안된다. 예) 주문번호, PK 같은 것들..



## 실무 모니터링 환경 구성 팁

**모니터링 3단계**

- 대시보드
- 애플리케이션 추적 - pinpoint
- 로그



**대시보드**

- 전체를 한 눈에 볼 수 있는 가장 높은 뷰
- 마이크로미터, 프로메테우스, 그라파나 등..

- 대상: 시스템 메트릭(CPU, 메모리..), 애플리케이션 메트릭, 비즈니스 메트릭 등



**애플리케이션 추적**

- 각각의 HTTP 요청을 추적, 일부는 마이크로서비스 환경에서 분산 추적

- 핀포인트, 스카우트, 와탭, 제니퍼 등



**로그**

- 가장 자세한 추적. 원하는 대로 커스텀 가능
- 같은 HTTP 요청을 묶어서 확인할 수 있는 방법이 중요. MDC 적용



**모니터링 정리**

- 관찰할 때는 전체 -> 점점 좁게!
- 꼭 최소 두 가지 종류로 구분해서 관리
  - 경고 / 심각