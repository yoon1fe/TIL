## 템플릿 메서드 패턴

로그 추적기가 도입됨에 따라 핵심 기능보다 로그 출력하는 부가 기능 코드가 훨씬 더 많고 복잡해졌다.

- 핵심 기능: 객체가 제공하는 고유의 기능.
- 부가 기능: 핵심 기능을 보조하기 위해 제공되는 기능. 로그 추적 로직, 트랜잭션 로직 등..



동일한 패턴을 분리해서 별도의 메서드로 뽑아내자! 근데 핵심 기능 부분이 중간에 있어서 단순히 메서드로 추출하는 것은 어렵다.



**변하는 것과 변하지 않는 것을 분리**

좋은 설계는 변하는 것과 변하지 않는 것을 분리하는 것.

핵심 기능 -> 변하고 , 로그 추적기 -> 변하지 않는 부분



### **템플릿 메서드 패턴**

![템플릿 메서드 디자인 패턴 구조](https://refactoring.guru/images/patterns/diagrams/template-method/structure.png?id=924692f994bff6578d8408d90f6fc459)

- `AbstractTemplate` 에 변하지 않는 부분을 모아둔다.
- 자식 클래스에서 변하는 부분을 구현



``` java
package hello.advanced.trace.template.code;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTemplate {

  public void execute() {
    long startTime = System.currentTimeMillis();

    //비즈니스 로직 실행
    call(); //상속

    //비즈니스 로직 종료
    long endTime = System.currentTimeMillis();
    long resultTime = endTime - startTime;
    log.info("resultTime={}", resultTime);
  }

  protected abstract void call();
}
```

- 이름 그대로 템플릿(기준이 되는 거대한 틀)을 사용.
- 부모 클래스에 변하지 않는 템플릿 코드를 두고,
- 변하는 부분은 상속과 오버 라이딩을 통해 구현한다.

- 코드 중복 제거 가능!!!



``` java
package hello.advanced.trace.template.code;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubClassLogic1 extends AbstractTemplate {

  @Override
  protected void call() {
    log.info("비즈니스 로직1 실행");
  }
}
```



템플릿 메서드 패턴은 위의 클래스처럼 로직 클래스를 하나하나 만들어야 하는 단점이 있다. 익명 내부 클래스를 사용하면 이런 단점을 보완 가능!

```java
  @Test
  void templateMethodV2() {
    AbstractTemplate template1 = new AbstractTemplate() {
      @Override
      protected void call() {
        log.info("비즈니스 로직1 실행");
      }
    };

    log.info("클래스 이름1={}", template1.getClass());
    template1.execute();

    AbstractTemplate template2 = new AbstractTemplate() {
      @Override
      protected void call() {
        log.info("비즈니스 로직1 실행");
      }
    };
    
    log.info("클래스 이름2={}", template2.getClass());
    template2.execute();
  }
```



### 프로젝트 적용

``` java
package hello.advanced.trace.template;

import hello.advanced.trace.TraceStatus;
import hello.advanced.trace.logtrace.LogTrace;

public abstract class AbstractTemplate<T> {

  private final LogTrace trace;

  public AbstractTemplate(LogTrace trace) {
    this.trace = trace;
  }

  public T execute(String message) {
    TraceStatus status = null;
    try {
      status = trace.begin(message);
      
      //로직 호출
      T result = call();
      
      trace.end(status);
      return result;
    } catch (Exception e) {
      trace.exception(status, e);
      throw e;
    }
  }

  protected abstract T call();
}
```

- 템플릿 역할을 하는 부모 클래스
- `<T>` 제네릭 사용함으로써 반환 타입 정의
- `abstract T call()`은 변하는 부분을 처리하는 메서드. 상속으로 구현한다.



``` java
package hello.advanced.app.v4;

import hello.advanced.trace.TraceStatus;
import hello.advanced.trace.logtrace.LogTrace;
import hello.advanced.trace.template.AbstractTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderControllerV4 {

  private final OrderServiceV4 orderService;
  private final LogTrace trace;

  @GetMapping("/v4/request")
  public String request(String itemId) {
    AbstractTemplate<String> template = new AbstractTemplate<String>(trace) {
      @Override
      protected String call() {
        orderService.orderItem(itemId);
        return "ok";
      }
    };

    return template.execute("OrderController.request()");
  }

}
```



``` java
package hello.advanced.app.v4;

import hello.advanced.trace.TraceStatus;
import hello.advanced.trace.logtrace.LogTrace;
import hello.advanced.trace.template.AbstractTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceV4 {

  private final OrderRepositoryV4 orderRepository;
  private final LogTrace trace;

  public void orderItem(String itemId) {

    AbstractTemplate<Void> template = new AbstractTemplate<>(trace) {
      @Override
      protected Void call() {
        orderRepository.save(itemId);
        return null;
      }
    };

    template.execute("OrderServiceV4.orderItem()");
  }
}

```



- `OrderServiceV0`: 핵심 기능만
- `OrderServiceV3`: 핵심 기능 + 부가 기능
- `OrderServiceV4`: 핵심 기능 + 템플릿 호출하는 부분



**좋은 설계란?**

진정한 좋은 설계는 **변경**이 일어날 때 드러난다.



**단일 책임 원칙 (SRP)**

`V4`는 단순히 템플릿 메서드 패턴을 적용해서 소스 코드 몇 줄 줄인게 전부가 아니다. 로그 추적이란 기능(책임)을 지키게 된 것!! 변경 지점을 하나로 모아서 변경에 쉽게 대처할 수 있는 구조를 만든 것이다.ㅎㅎ



**템플릿 메서드 패턴의 정의**

GOF 디자인 패턴에서는 다음과 같이 정의 했다.

> 템플릿 메서드 디자인 패턴의 목적은 다음과 같습니다.
>
> "작업에서 알고리즘의 골격을 정의하고 일부 단계를 하위 클래스로 연기합니다. 템플릿 메서드를 사용하면 하위 클래스가 알고리즘의 구조를 변경하지 않고도 알고리즘의 특정 단계를 재정의할 수 있습니다." [GOF]



부모 클래스에 알고리즘의 골격인 **템플릿**을 정의하고, 일부 변경되는 로직은 자식 클래스에 정의하는 것.

**하지만**

템플릿 메서드 패턴은 **상속**을 사용한다. 따라서 상속에서 오는 단점들을 모두 안고 간다. 자식 클래스<-> 부모 클래스간의 컴파일 시점에 강하게 결합된다. 자식 클래스는 부모 클래스의 기능을 전혀 사용하지 않는데도.

자식 클래스의 코드에 부모 클래스의 코드가 명확하게 적혀 있다. -> 자식이 부모에 의존하게 된다. 부모 클래스에 코드가 수정되면 자식 클래스에도 모두 영향이 가게 된다.

자식 클래스 입장에서는 부모 클래스의 기능을 전혀 사용하지 않는데, 부모 클래스를 알아야 한다...



템플릿 메서드 패턴과 비슷한 역할을 하면서 상속의 단점을 제거할 수 있는 패턴이 바로 **전략 패턴**!!



## 전략 패턴

위에서 다루었던 동일한 문제를 전략 패턴으로 해결해보자.

전략 패턴은

- 변하지 않는 부분 -> `Context`
- 변하는 부분 -> `Strategy` 인터페이스

**상속이 아니라 위임(구성)으로 문제를 해결!**



![전략 디자인 패턴의 구조](https://refactoring.guru/images/patterns/diagrams/strategy/structure.png)



GOF 디자인 패턴 - 전략 패턴

> 알고리즘 제품군을 정의하고 각각을 캡슐화하여 상호 교환 가능하게 만들자. 전략을 사용하면 알고리즘을 사용하는 클라이언트와 독립적으로 알고리즘을 변경할 수 있다.



**`Strategy`**

``` java
package hello.advanced.trace.strategy.code.strategy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StrategyLogic1 implements Strategy {
 @Override
 public void call() {
 log.info("비즈니스 로직1 실행");
 }
}


@Slf4j
public class StrategyLogic1 implements Strategy {

  @Override
  public void call() {
    log.info("비즈니스 로직1 실행");
  }
}

```

- 변하는 알고리즘 역할. `call()` 메서드에서 구현한다.



**`Context`**

``` java
package hello.advanced.trace.strategy.code.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 필드에 전략을 보관하는 방식
 */
@Slf4j
@RequiredArgsConstructor
public class ContextV1 {

  private final Strategy strategy;

  public void execute() {
    long startTime = System.currentTimeMillis();

    //비즈니스 로직 실행
    strategy.call();  // 위임!

    //비즈니스 로직 종료
    long endTime = System.currentTimeMillis();
    long resultTime = endTime - startTime;
    log.info("resultTime={}", resultTime);
  }

}

```

- 변하지 않는 로직을 가지고 있는 템플릿 역할. 전략 패턴에서는 이것을 컨텍스트(문맥)이라고 한다.
- `Context`에서는 `Strategy` 인터페이스에만 의존한다.



``` java
  @Test
  void strategyV2() {
    Strategy strategyLogic1 = new Strategy() {
      @Override
      public void call() {
        log.info("비즈니스 로직1 실행");
      }
    };
    log.info("strategyLogic1={}", strategyLogic1.getClass());
    ContextV1 context1 = new ContextV1(strategyLogic1);
    context1.execute();
    Strategy strategyLogic2 = () -> log.info("비즈니스 로직2 실행");
    
    log.info("strategyLogic2={}", strategyLogic2.getClass());
    ContextV1 context2 = new ContextV1(strategyLogic2);
    context2.execute();
  }
```

- 익명 내부 클래스도 당근 사용 가능



**선 조립 후 실행**

`Context`와 `Strategy`를 한 번 조립하고 나면 이후로는 `Context`를 실행하기만 하면 된다. 스프링 프레임워크로 개발할 때 애플리케이션 로딩 시점에 의존 관계 모두 맺고 난 다음 실제 요청을 처리하는 것과 같은 원리.

한 번 조립한 이후에는 전략을 변경하기 번거롭다는 단점이 있다. 좀 더 유연하게 전략 패턴을 사용하는 방법이 없을까!?



**전략을 파라미터로 전달 받기**

``` java
package hello.advanced.trace.strategy.code.strategy;

import lombok.extern.slf4j.Slf4j;

/**
 * 전략을 파라미터로 전달받는 방식
 */
@Slf4j
public class ContextV2 {
  public void execute(Strategy strategy) {
    long startTime = System.currentTimeMillis();

    //비즈니스 로직 실행
    strategy.call(); //위임
    //비즈니스 로직 종료

    long endTime = System.currentTimeMillis();
    long resultTime = endTime - startTime;
    log.info("resultTime={}", resultTime);
  }
}
```



**`Test`**

``` java
package hello.advanced.trace.strategy;

@Slf4j
public class ContextV2Test {

  /**
   * 전략 패턴 적용
   */
  @Test
  void strategyV1() {
    ContextV2 context = new ContextV2();
    context.execute(new StrategyLogic1());
    context.execute(new StrategyLogic2());
  }
}
```

- `Context`를 실행할 때마다 전략을 인수로 전달한다.

 

## 템플릿 콜백 패턴

`ContextV2` 는 변하지 않는 템플릿 역할을 한다. 그리고 변하는 부분은 파라미터로 넘어온 `Strategy` 의 코드를 실행해서 처리한다. 이렇게 다른 코드의 인수로서 넘겨주는 실행 가능한 코드를 **콜백(callback)**이라 한다.



콜백의 정의

> 프로그래밍에서 콜백(callback) 또는 콜애프터 함수(call-after function)는 다른 코드의 인수로서 넘겨주는 실행 가능한 코드를 말한다. 콜백을 넘겨받는 코드는 이 콜백을 필요에 따라 즉시 실행할 수도 있고, 아니면 나중에 실행할 수도 있다.



**템플릿 콜백 패턴**

- 스프링에서는 `ContextV2`와 같은 방식의 전략 패턴을 **템플릿 콜백 패턴**이라고 한다. 전략 패턴에서 `Context`가 템플릿 역할을 하고, `Strategy` 부분이 콜백으로 넘어옴.
- 스프링에서는 `JdbcTemplate`, `RestTemplate` 등등 다양한 템플릿 콜백 패턴이 사용된다.!!

 

### 프로젝트 적용

``` java
package hello.advanced.trace.callback;

public interface TraceCallback<T> {

  T call();

}
```



``` java
package hello.advanced.trace.callback;

import hello.advanced.trace.TraceStatus;
import hello.advanced.trace.logtrace.LogTrace;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TraceTemplate {

  private final LogTrace trace;

  public <T> T execute(String message, TraceCallback<T> callback) {
    TraceStatus status = null;
    try {
      status = trace.begin(message);

      //로직 호출
      T result = callback.call();

      trace.end(status);
      return result;
    } catch (Exception e) {
      trace.exception(status, e);
      throw e;
    }
  }
}
```



``` java
package hello.advanced.app.v5;

import hello.advanced.trace.callback.TraceTemplate;
import hello.advanced.trace.logtrace.LogTrace;
import hello.advanced.trace.template.AbstractTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderControllerV5 {

  private final OrderServiceV5 orderService;
  private final TraceTemplate template;

  public OrderControllerV5(OrderServiceV5 orderService, LogTrace trace) {
    this.orderService = orderService;
    template = new TraceTemplate(trace);
  }

  @GetMapping("/v5/request")
  public String request(String itemId) {
    return template.execute("OrderController.request()", () -> {
      orderService.orderItem(itemId);
      return "ok";
    });
  }

}
```



**한계..**

아무리 최적화를 해도 결국 로그 추적기를 적용하기 위해 원본 코드를 수정해야 한다. 원본 코드를 손대지 않고 로그 추적기를 적용할 수 있는 방법이 없을까!?!? -> **프록시**!