## 예제 프로젝트

- `@Aspect`를 사용하려면 `@EnableAspectJAutoProxy`를 스프링 설정에 추가해야 하지만, 스프링 부트를 사용하면 자동으로 추가된다.

- 스프링 웹 프로젝트가 아니므로 톰캣이 없다. 따라서 서버가 실행되진 않음.



기존 `OrderRepository`, `OrderService` 사용.

- `AopUtils.isAopProxy(...)`: AOP 프록시 적용 여부 확인 가능.



## 스프링 AOP 구현

### 시작

스프링 AOP 를 구현하는 가장 일반적은 방법은 `@Aspect` 사용하는 것.



```java
package hello.aop.order.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Slf4j
@Aspect
public class AspectV1 {

  @Around("execution(* hello.aop.order..*(..))")
  public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
    log.info("[log] {}", joinPoint.getSignature()); // join point 시그니처
    return joinPoint.proceed();
  }
}
```

- `@Around`

  - 애너테이션의 값인 `execution(* hello.aop.order..*(..))` 가 포인트컷이 된다.
    - `hello.aop.order` 패키지와 그 하위 패키지를 지정하는 포인트컷 표현식

  - `@Around` 애너테이션의 메서드 `doLog` 는 어드바이스가 된다.

- 이제 `OrderService`, `OrderRepository`의 모든 메서드가 AOP 적용 대상이 된다.



```java
package hello.aop;

@Slf4j
@SpringBootTest
@Import(AspectV1.class)
public class AopTest {

  @Autowired
  OrderService orderService;
  @Autowired
  OrderRepository orderRepository;

  @Test
  void aopInfo() {
    log.info("isAopProxy, orderService={}", AopUtils.isAopProxy(orderService));
    log.info("isAopProxy, orderRepository={}", AopUtils.isAopProxy(orderRepository));
  }

  @Test
  void success() {
    orderService.orderItem("itemA");
  }

  @Test
  void exception() {
    assertThatThrownBy(() -> orderService.orderItem("ex"))
        .isInstanceOf(IllegalStateException.class);
  }
}
```

- `@Import`로 `@Aspect`가 붙은 클래스 설정 추가해서 사용하면 `AopUtils.isAopProxy(..)`의 결과로 `true` 가 나온다!
- 로그도 나옴



### 포인트컷 분리

`@Around`에 포인트컷 표현식을 직접 넣을 수도 있지만, `@Pointcut` 애너테이션을 사용해서 별도로 분리할 수도 있다.



```java
package hello.aop.order.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Slf4j
@Aspect
public class AspectV2 {

  // hello.aop.order 패키지와 하위 패키지
  @Pointcut("execution(* hello.aop.order..*(..))")
  private void allOrder() {}  // pointcut signature

  @Around("allOrder()")
  public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
    log.info("[log] {}", joinPoint.getSignature()); // join point 시그니처
    return joinPoint.proceed();
  }
}
```

**`@Pointcut`**

- 포인트컷 표현식 사용
- 메서드 이름과 파라미터를 합쳐서 포인트컷 시그니처라고 한다.
- 메서드 반환 타입은 `void`
- 코드 내용은 비워둔다.
- 이렇게 분리하면 하나의 포인트컷 표현식을 여러 어드바이스에서 함께 사용할 수 있을 것!



### 어드바이스 추가

로그 출력하는 기능에 추가로 트랜잭션을 적용하는 (로그만) 코드도 추가해보자.



- 핵심 로직 실행 직전에 트랜잭션 시작
- 핵심 로직 실행
  - 핵심 로직 실행에 문제가 없으면 커밋
  - 예외 발생하면 롤백



```java
package hello.aop.order.aop;

@Slf4j
@Aspect
public class AspectV3 {

  // hello.aop.order 패키지와 하위 패키지
  @Pointcut("execution(* hello.aop.order..*(..))")
  private void allOrder() {}  // pointcut signature

  // 클래스 이름 패턴이 *Service
  @Pointcut("execution(* *..*Service.*(..))")
  private void allService() {}

  @Around("allOrder()")
  public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
    log.info("[log] {}", joinPoint.getSignature()); // join point 시그니처
    return joinPoint.proceed();
  }

  // hello.aop.order 패키지와 하위 패키지 && 클래스 이름 패턴이 *Service
  @Around("allOrder() && allService()")
  public Object doTransaction(ProceedingJoinPoint joinPoint) throws Throwable {

    try {
      log.info("[트랜잭션 시작] {}", joinPoint.getSignature());
      Object result = joinPoint.proceed();
      log.info("[트랜잭션 커밋] {}", joinPoint.getSignature());
      return result;
    } catch (Exception e) {
      log.info("[트랜잭션 롤백] {}", joinPoint.getSignature());
      throw e;
    } finally {
      log.info("[리소스 릴리즈] {}", joinPoint.getSignature());
    }
  }
}
```

- `allService()`: 타입 이름 패턴이 `*Service` 를 대상으로 하는 포인트컷. 클래스, 인터페이스에 모두 적용된다.
- `@Around("allOrder() && allService()")`
  - `&&`, `||`, `!` 으로 여러 개의 포인트컷 조합 가능
  - `hello.aop.order` 패키지와 하위 패키지에 있으면서 타입 이름 패턴이 `*Service` 인 것을 대상으로 함



**포인트컷 적용된 AOP 결과**

- `orderService`: `doLog()`, `doTransaction()` 어드바이스 적용
- `orderRepository`: `doLog()` 어드바이스 적용



### 포인트컷 참조

포인트컷들을 공용으로 사용하기 위해 별도의 외부 클래스에 모아둘 수 있다.



```java
package hello.aop.order.aop;

import org.aspectj.lang.annotation.Pointcut;

public class Pointcuts {

  //hello.springaop.app 패키지와 하위 패키지
  @Pointcut("execution(* hello.aop.order..*(..))")
  public void allOrder() {
  }

  //타입 패턴이 *Service
  @Pointcut("execution(* *..*Service.*(..))")
  public void allService() {
  }

  //allOrder && allService
  @Pointcut("allOrder() && allService()")
  public void orderAndService() {
  }
}
```

- `orderAndService()`: 포인트컷 조합해서 새로운 포인트컷도 생성 가능



```java
package hello.aop.order.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Slf4j
@Aspect
public class AspectV4Pointcut {

  @Around("hello.aop.order.aop.Pointcuts.allOrder()")
  public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
    log.info("[log] {}", joinPoint.getSignature());
    return joinPoint.proceed();
  }

  @Around("hello.aop.order.aop.Pointcuts.orderAndService()")
  public Object doTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
    try {
      log.info("[트랜잭션 시작] {}", joinPoint.getSignature());
      Object result = joinPoint.proceed();
      log.info("[트랜잭션 커밋] {}", joinPoint.getSignature());
      
      return result;
    } catch (Exception e) {
      log.info("[트랜잭션 롤백] {}", joinPoint.getSignature());
      throw e;
    } finally {
      log.info("[리소스 릴리즈] {}", joinPoint.getSignature());
    }
  }
}
```

- 패키지명을 포함한 클래스 이름과 포인트컷 시그니처를 모두 지정하면 된다.



### 어드바이스 순서

어드바이스는 기본적으로 순서를 보장하지 않는다. 순서를 지정하고 싶으면 `@Aspect` 적용 단위로 스프링이 제공하는 `@Order` (ex. `@Order(1)`, `@Order(2)`)애너테이션을 적용해야 한다. 근데 이건 클래스 단위로 적용할 수 있다. 따라서 하나의 애스펙트에 여러 어드바이스가 있는 경우 순서를 보장받을 수 없음. 결론) **애스펙트를 별도의 클래스로 분리**해야 한다.



```java
package hello.aop.order.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

@Slf4j
@Aspect
public class AspectV5Order {

  @Aspect
  @Order(2)
  public static class LogAspect {
    @Around("hello.aop.order.aop.Pointcuts.allOrder()")
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
      log.info("[log] {}", joinPoint.getSignature());
      return joinPoint.proceed();
    }
  }


  @Aspect
  @Order(1)
  public static class TxAspect {
    @Around("hello.aop.order.aop.Pointcuts.orderAndService()")
    public Object doTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
      try {
        log.info("[트랜잭션 시작] {}", joinPoint.getSignature());
        Object result = joinPoint.proceed();
        log.info("[트랜잭션 커밋] {}", joinPoint.getSignature());

        return result;
      } catch (Exception e) {
        log.info("[트랜잭션 롤백] {}", joinPoint.getSignature());
        throw e;
      } finally {
        log.info("[리소스 릴리즈] {}", joinPoint.getSignature());
      }
    }
  }
}
```

- 하나의 애스펙트 안에 있던 어드바이스를 `LogAspect`, `TxAspect` 애스펙트로 분리하고, `@Order` 애너테이션을 통해 실행 순서 적용. 숫자가 작을수록 먼저 실행된다.



### 어드바이스 종류

- `@Around`: 메서드 호출 전후에 수행. 가장 강력한 어드바이스. 조인 포인트 실행 여부 선택, 반환 값 변환, 예외 변환 등이 가능. 여기서 아래 기능 다 할 수 있다.
- `@Before`: 조인 포인트 실행 이전에 실행
- `@AfterReturning`: 조인 포인트가 정상 완료 후 실행
- `@AfterThrowing`: 메서드가 예외를 던지는 경우실행
- `@After`: 조인 포인트가 정상 또는 예외에 관계없이 실행 (==`finally`)



```java
package hello.aop.order.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

@Slf4j
@Aspect
public class AspectV6Advice {

  @Around("hello.aop.order.aop.Pointcuts.orderAndService()")
  public Object doTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
    try {
      //@Before
      log.info("[around][트랜잭션 시작] {}", joinPoint.getSignature());
      Object result = joinPoint.proceed();
      //@AfterReturning
      log.info("[around][트랜잭션 커밋] {}", joinPoint.getSignature());
      return result;
    } catch (Exception e) {
      //@AfterThrowing
      log.info("[around][트랜잭션 롤백] {}", joinPoint.getSignature());
      throw e;
    } finally {
      //@After
      log.info("[around][리소스 릴리즈] {}", joinPoint.getSignature());
    }
  }

  @Before("hello.aop.order.aop.Pointcuts.orderAndService()")
  public void doBefore(JoinPoint joinPoint) {
    log.info("[before] {}", joinPoint.getSignature());
  }

  @AfterReturning(value = "hello.aop.order.aop.Pointcuts.orderAndService()",
      returning = "result")
  public void doReturn(JoinPoint joinPoint, Object result) {
    log.info("[return] {} return={}", joinPoint.getSignature(), result);
  }

  @AfterThrowing(value = "hello.aop.order.aop.Pointcuts.orderAndService()",
      throwing = "ex")
  public void doThrowing(JoinPoint joinPoint, Exception ex) {
    log.info("[ex] {} message={}", joinPoint.getSignature(),
        ex.getMessage());
  }

  @After(value = "hello.aop.order.aop.Pointcuts.orderAndService()")
  public void doAfter(JoinPoint joinPoint) {
    log.info("[after] {}", joinPoint.getSignature());
  }
}
```

- `@Around` 어드바이스만 사용해도 필요한 기능을 모두 수행할 수 있다.



**참고 정보 획득**

모든 어드바이스는 `org.aspectj.lang.JoinPoint` 를 첫번째 파라미터에 사용 가능하지만, `@Around` 에서는 `ProceedingJoinPoint`를 사용해야 한다. 얘는 `JoinPoint`의 하위 타입임.



**JoinPoint 인터페이스의 주요 기능**

- `getArgs()`: 메서드 인수 반환
- `getThis()`: 프록시 객체 반환
- `getTarget()`: 대상 객체 반환
- `getSignature()`: 조언되는 메서드에 대한 설명 반환
- `toString()`: 조언되는 방법에 대한 유용한 설명 출력



**ProceedingJoinPoint 인터페이스의 주요 기능**

- `proceed()`: 다음 어드바이스나 타겟 호출



**`@Before`**

- 조인 포인트 실행 전에 실행
- `@Around`와 다르게 작업 흐름 변경할 수 없다. 다음 어드바이스나 타겟 호출 로직 필요 없음



**`@AfterReturning`**

- 메서드 실행이 정상적으로 반환될 때 실행
- `returning` 속성에 사용된 이름은 어드바이스 메서드의 매개변수 이름과 일치해야 한다.
- **`returning` 절에 지정된 타입의 값을 반환하는 메서드만 대상으로 실행**!
- `@Around`와 다르게 반환되는 객체를 변경할 수 없다.



**`@AfterThrowing`**

- 메서드 실행이 예외를 던져서 종료될 때 실행
- 매개변수롤 `Exception` 받는다. 
- `throwing` 절에 지정된 타입과 맞는 예외를 대상으로 실행



**`@After`**

- 메서드 실행이 종료되면 실행 (== finally)
- 정상 / 예외 반환 조건 모두 처리
- 일반적으로 리소스 해제하는데 사용한다.



**`@Around`**

- 메서드 실행의 **주변**에서 실행. 메서드 실행 전후에 작업을 수행한다.
- 가장 강력한 어드바이스!
- 어드바이스의 첫 번째 파라미터는 `ProceedingJoinPoint` 사용해야 함
- `proceed()` 를 통해 대상 객체 호출. 여러 번 호출할 수도 있음(재시도)



**호출 순서**

```
[around][트랜잭션 시작] void hello.aop.order.OrderService.orderItem(String)
[before] void hello.aop.order.OrderService.orderItem(String)
[orderService] 실행
[orderRepository] 실행
[return] void hello.aop.order.OrderService.orderItem(String) return=null
[after] void hello.aop.order.OrderService.orderItem(String)
[around][트랜잭션 커밋] void hello.aop.order.OrderService.orderItem(String)
[around][리소스 릴리즈] void hello.aop.order.OrderService.orderItem(String)
```

- 스프링 5.2.7 버전부터 동일한 애스펙트 안에서는 동일한 조인 포인트의 우선 순위
- 실행 순서: `@Around` > `@Before` > `@After` > `@AfterReturning` > `@AfterThrowing`



**`@Around` 외에 다른 어드바이스가 존재하는 이유**

- `@Around`는 항상 `joinPoint.proceed()`를 호출해야 한다.
- 단순 로그 출력 등의 작업을 하려면 `@Before` 애너테이션을 사용함으로써 `joinPoint.proceed()`를 호출하지 않아도 된다.
- **코드를 작성한 의도가 명확하게 드러나는** 장점이 있음