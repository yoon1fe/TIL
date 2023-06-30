## 스프링 트랜잭션 소개

**스프링 트랜잭션 추상화**

각각의 데이터 접근 기술들은 트랜잭션 처리하는 방식이 다르다. 스프링은 이를 위해 트랜잭션 추상화를 제공한다. `PlatformTransactionManager` 라는 인터페이스를 통해 트랜잭션 추상화.



**`PlatformTransactionManager.java**

``` java
package org.springframework.transaction;
public interface PlatformTransactionManager extends TransactionManager {
    
  TransactionStatus getTransaction(@Nullable TransactionDefinition definition) throws TransactionException;
    
  void commit(TransactionStatus status) throws TransactionException;
  void rollback(TransactionStatus status) throws TransactionException;
}
```

- 트랜잭션은 트랜잭션 시작(획득), 커밋, 롤백으로 단순하게 추상화 가능!
- 트랜잭션이 필요한 비즈니스 로직에서 인터페이스에 의존하면 된다~
- 추가로 스프링은 주로 사용하는 데이터 접근 기술에 대한 트랜잭션 매니저의 구현체도 제공한다!! 스프링 빈으로 등록하고 주입받아서 쓰기만 하면 됨
- 또또 추가로 스프링 부트는 어떤 데이터 접근 기술을 사용하는지 자동으로 인식해서 적절한 트랜잭션 매니저를 선택해서 스프링 빈으로 등록해준다!!!!



**스프링 트랜잭션 사용 방식**

선언적 트랜잭션 관리 vs 프로그래밍 방식 트랜잭션 관리

- 선언적 트랜잭션 관리>
  - `@Transactionl`
  - 간편하고 실용적
- 프로그래밍 방식
  - 트랜잭션 매니저나 트랜잭션 템플릿 등을 사용해서 트랜잭션 관련 코드를 직접 작성
  - 애플리케이션 코드가 트랜잭션 기술 코드와 강하게 결합된다.



프록시 도입 후 스프링 AOP로 트랜잭션 프록시를 자동으로 만들어 주게 되었다. 프록시 기술을 사용하면

- 트랜잭션을 처리하는 객체와
- 비즈니스 로직을 처리하는 서비스 객체를

명확하게 분리 가능



## 트랜잭션 적용

### 확인

```java
package hello.springtx.apply;

@Slf4j
@SpringBootTest
public class TxBasicTest {
  @Autowired BasicService basicService;

  @Test
  void proxyCheck() {
    log.info("aop class = {}", basicService.getClass());
    Assertions.assertThat(AopUtils.isAopProxy(basicService)).isTrue();
  }

  @Test
  void txTest() {
    basicService.tx();
    basicService.nonTx();
  }

  @TestConfiguration
  static class TxApplyBasicConfig {
    @Bean
    BasicService basicService() {
      return new BasicService();
    }
  }

  @Slf4j
  static class BasicService {

    @Transactional
    public void tx() {
      log.info("call tx");
      boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
      log.info("tx active = {}", txActive);
    }

    public void nonTx() {
      log.info("call nonTx");
      boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
      log.info("tx active = {}", txActive);
    }
  }
}
```

- `proxyCheck()` 실행

  - `basicService.getClass()`를 호출하면 `class hello.springtx.apply.TxBasicTest$BasicService$$EnhancerBySpringCGLIB$$1e4c0591` 라고 프록시 클래스의 이름이 출력되는 것을 확인 할 수 있다. `@Transactional` 애너테이션이 특정 클래스나 메서드에 하나라도 붙어 있으면 트랜잭션 AOP는 **프록시**를 만들어서 스프링 빈으로 등록한다. 따라서 실제 `basicService` 객체 대신 프록시인 `basicService$$CGLIB`가 빈으로 등록되는것.!! 

    `@Autowired`로 `BasicService` 객체를 주입받을 때, 스프링 컨테이너는 실제 객체 대신 프록시가 스프링 빈으로 등록되어 있기 때문에 프록시를 주입해준다. 프록시는 원 객체를 상속해서 만들어지기 때문에 주입 가능.

  - `AopUtils.isAopProxy()`: `true`를 반환

- `txTest()` 호출

  - `logging.level.org.springframework.transaction.interceptor=TRACE` 설정하면 트랜잭션 프록시가 호출하는 트랜잭션의 시작과 종료를 로그로 확인 가능

    ```
    2023-06-27 23:48:52.895 TRACE 10560 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [hello.springtx.apply.TxBasicTest$BasicService.tx]
    2023-06-27 23:48:52.902  INFO 10560 --- [    Test worker] h.s.apply.TxBasicTest$BasicService       : call tx
    2023-06-27 23:48:52.902  INFO 10560 --- [    Test worker] h.s.apply.TxBasicTest$BasicService       : tx active = true
    2023-06-27 23:48:52.902 TRACE 10560 --- [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [hello.springtx.apply.TxBasicTest$BasicService.tx]
    2023-06-27 23:48:52.904  INFO 10560 --- [    Test worker] h.s.apply.TxBasicTest$BasicService       : call nonTx
    2023-06-27 23:48:52.904  INFO 10560 --- [    Test worker] h.s.apply.TxBasicTest$BasicService       : tx active = false
    ```

  - `basicService.tx()` 호출

    - 프록시의 `tx()`가 호출된다. 여기서 프록시는 해당 메서드가 트랜잭션을 사용할 수 있는지 확인해본다(`@Transactional` 존재하므로 사용 가능).
    - 따라서 트랜잭션을 시작한 다음 해당 메서드를 호출한다.

  - `basicService.nonTx()` 호출

    - 여긴 `@Transactional` 이 없다. 따라서 트랜잭션을 따로 시작하지 않고 메서드 호출

  - `TransactionSynchronizationManager.isActualTransactionActive()`: 현재 스레드에 트랜잭션 적용 여부를 리턴하는 메서드. 트랜잭션 적용 여부를 가장 확실하게 확인할 수 있다.



### 적용 위치

`@Transactional` 의 적용 위치에 따른 우선 순위?

스프링에서 우선순위는 항상 **더 구체적이고 자세한 것이 우선순위가 높다!!**

ex) 애너테이션 위치: 메서드 > 클래스, 인터페이스 구현체 > 인터페이스



```java
package hello.springtx.apply;

@SpringBootTest
public class TxLevelTest {

  @Autowired
  LevelService service;

  @Test
  void orderTest() {
    service.write();
    service.read();
  }

  @TestConfiguration
  static class TxApplyLevelConfig {

    @Bean
    LevelService levelService() {
      return new LevelService();
    }
  }

  @Slf4j
  @Transactional(readOnly = true)
  static class LevelService {

    @Transactional(readOnly = false)
    public void write() {
      log.info("call write");
      printTxInfo();
    }

    public void read() {
      log.info("call read");
      printTxInfo();
    }

    private void printTxInfo() {
      boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
      log.info("tx active={}", txActive);
      boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
      log.info("tx readOnly={}", readOnly);
    }
  }

}
```



**스프링 `@Transactional` 의 두 가지 규칙**

1. 우선순위 규칙: 클래스보다 메서드가 더 구체적이므로 `readOnly = false` 가 적용된다.
2. 클래스에 적용하면 메서드는 자동 적용: `read()` 메서드에는 `readOnly = true`가 적용

- `TransactionSynchronizationManager.isCurrentTransactionReadOnly`: 현재 트랜잭션에 적용된 `readOnly` 옵션 값을 리턴.



**인터페이스에 `@Transactional` 적용**

인터페이스에도 적용 가능. 다음 순서로 적용된다.

1. 클래스의 메서드
2. 클래스의 타입
3. 인터페이스의 메서드
4. 인터페이스의 타입

클래스의 메서드를 찾고, 만약 없으면 클래스의 타입을 찾고 만약 없으면 인터페이스의 메서드를 찾고 그래도 없으면 인터페이스의 타입을 찾는다.

근데 인터페이스에 `@Transactional`을 사용하는 것은 스프링 공식 매뉴얼에서 권장하지 않는 방법이다. AOP를 적용하는 방식에 따라서 인터페이스에 애너테이션을 두면 AOP가 적용 안되는 경우도 있기 때문. 가급적 구체 클래스에 사용하자.



## 트랜잭션 AOP 주의사항

### 프록시 내부 호출

`@Transactional` 을 사용하면 스프링의 트랜잭션 AOP가 적용된다. 트랜잭션 AOP는 기본적으로 프록시 방식의 AOP를 사용함. 따라서 만약 프록시 객체를 거치지 않고 대상 객체를 직접 호출하게 되면 AOP가 적용되지 않고, 트랜잭션도 적용되지 않게 된다.



AOP를 적용하면 스프링은 대상 객체 대신 **프록시만을 스프링 빈**으로 등록하기 때문에 의존 관계 주입 시에는 항상 프록시 객체가 주입된다. 하지만 **대상 객체의 내부에서 메서드 호출이 발생하면 프록시를 거치지 않고 대상 객체를 직접 호출해버리게 된다!!** 이렇게 되면 트랜잭션이 적용되지 않는다...



``` java
package hello.springtx.apply;

@Slf4j
@SpringBootTest
public class InternalCallV1Test {

  @Autowired
  CallService callService;

  @Test
  void printProxy() {
    log.info("callService class={}", callService.getClass());
  }

  @Test
  void internalCall() {
    callService.internal();
  }

  @Test
  void externalCall() {
    callService.external();
  }

  @TestConfiguration
  static class InternalCallV1Config {

    @Bean
    CallService callService() {
      return new CallService();
    }
  }

  @Slf4j
  static class CallService {

    public void external() {
      log.info("call external");
      printTxInfo();
      internal();
    }

    @Transactional
    public void internal() {
      log.info("call internal");
      printTxInfo();
    }

    private void printTxInfo() {
      boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
      log.info("tx active={}", txActive);
    }
  }
}
```

- `@Transactional` 애너테이션이 붙어있으므로 `CallService` 에 대한 트랜잭션 프록시 객체가 만들어지고, 이 프록시 객체가 주입된다.
- `internalCall()` 호출: 클라이언트(테스트 코드)가 호출하는 `callService.internal()` 은 트랜잭션 프록시 객체의 메서드이므로 트랜잭션이 적용된다.
- `externalCall()` 호출: `external()`은 `@Transactional` 이 없으므로 트랜잭션 프록시는 트랜잭션을 적용하지 않는다. 따라서 실제 `callService` 객체 인스턴스의 `external()`을 호출. `external()` 이 내부에서 호출하는 `internal()`은 트랜잭션 프록시 객체의 메서드가 아니다!! 따라서 트랜잭션이 적용안됨.



**문제의 원인**

자바에서 메서드 앞에 별도의 참조가 없으면 `this`라는 뜻으로 자기 자신의 메서드를 의미한다. 따라서 위에서의 메서드 호출은 프록시를 거치지 않고 대상 객체(`target`)의 인스턴스의 메서드를 바로 호출하는 것.



**프록시 방식의 AOP 한계** 

`@Transactional` 를 사용하는 트랜잭션 AOP는 프록시를 사용한다. 프록시를 사용하면 메서드 내부 호출에 프록시를 적용할 수 없다. 

그렇다면 이 문제를 어떻게 해결할 수 있을까? 가장 단순한 방법은 내부 호출을 피하기 위해 `internal()` 메서드를 별도의 클래스로 분리하는 것이다.



**`internal()` 메서드 분리**



``` java
package hello.springtx.apply;

@SpringBootTest
public class InternalCallV2Test {

  @Autowired
  CallService callService;

  @Test
  void externalCallV2() {
    callService.external();
  }

  @TestConfiguration
  static class InternalCallV2Config {

    @Bean
    CallService callService() {
      return new CallService(innerService());
    }

    @Bean
    InternalService innerService() {
      return new InternalService();
    }
  }

  @Slf4j
  @RequiredArgsConstructor
  static class CallService {

    private final InternalService internalService;

    public void external() {
      log.info("call external");
      printTxInfo();
      internalService.internal();
    }

    private void printTxInfo() {
      boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
      log.info("tx active={}", txActive);
    }
  }

  @Slf4j
  static class InternalService {

    @Transactional
    public void internal() {
      log.info("call internal");
      printTxInfo();
    }

    private void printTxInfo() {
      boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
      log.info("tx active={}", txActive);
    }
  }
}
```

- 정상적으로 `internal()`에 트랜잭션 적용!
- 다른 해결방안도 있지만, 실무에서는 보통 별도의 클래스로 분리하는 방법을 주로 사용한다.



**public 메서드만 트랜잭션 가능**

스프링의 트랜잭션 AOP 기능은 `public` 메서드에만 트랜잭션을 적용하도록 기본 설정이 되어 있다. 프록시의 내부 호출 문제는 아니고, 스프링이 걍 막아둠. 이유는??

- 트랜잭션은 주로 비즈니스 로직의 시작점에 걸기 때문에 대부분 외부에 열어준 곳을 시작점으로 사용한다. 클래스 레벨에 트랜잭션을 적용했을 때 모든 메서드에 트랜잭션이 걸릴 수 있는데, 이렇게 되면 트랜잭션을 의도하지 않은 곳까지 트랜잭션이 과도하게 적용될 수 있기 때문에 `public` 메서드에만 허용한다~
- 참고로 부트 3.0부터는 `protected`, `default` 접근 제한자에도 트랜잭션이 적용된다. (`private` 만 안걸리네)



### 초기화 시점

스프링 초기화 시점에는 트랜잭션 AOP가 적용되지 않을 수 있다!



``` java
package hello.springtx.apply;

@SpringBootTest
public class InitTxTest {

  @Autowired
  Hello hello;

  @Test
  void go() {
    //초기화 코드는 스프링이 초기화 시점에 호출한다.
  }

  @TestConfiguration
  static class InitTxTestConfig {

    @Bean
    Hello hello() {
      return new Hello();
    }
  }

  @Slf4j
  static class Hello {

    @PostConstruct
    @Transactional
    public void initV1() {
      boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
      log.info("Hello init @PostConstruct tx active={}", isActive);
    }

    @EventListener(value = ApplicationReadyEvent.class)
    @Transactional
    public void init2() {
      boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
      log.info("Hello init ApplicationReadyEvent tx active={}", isActive);
    }
  }
}
```

- 초기화 코드(예: `@PostConstruct` )와 `@Transactional` 을 함께 사용하면 트랜잭션이 적용되지 않는다.

  초기화 코드가 먼저 호출되고, 그 다음에 트랜잭션 AOP가 적용되기 때문이다. 따라서 초기화 시점에는 해당 메서드에서 트랜잭션을 획득할 수 없다.

- 해결방법: `@EventListener(ApplicationReadyEvent.class)` - 스프링 컨테이너가 모두 뜬 뒤에 이벤트가 붙은 메서드를 호출해준다.



## 트랜잭션 옵션

 

**`@Transactional`**

``` java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Transactional {

	@AliasFor("transactionManager")
	String value() default "";

	@AliasFor("value")
	String transactionManager() default "";

	String[] label() default {};

	Propagation propagation() default Propagation.REQUIRED;

	Isolation isolation() default Isolation.DEFAULT;

	int timeout() default TransactionDefinition.TIMEOUT_DEFAULT;

	String timeoutString() default "";

	boolean readOnly() default false;

	Class<? extends Throwable>[] rollbackFor() default {};

	String[] rollbackForClassName() default {};

	Class<? extends Throwable>[] noRollbackFor() default {};

	String[] noRollbackForClassName() default {};

}

```



**value, transactionManager**

사용할 트랜잭션 매니저를 지정할 때 사용. 생략하면 기본으로 등록된 트랜잭션 매니저를 사용한다. 사용하는 트랜잭션 매니저가 두 개 이상이라면 이름을 지정해야 한다.



**rollbackFor**

예외 발생시 스프링 트랜잭션의 기본 정책

- 언체크 예외인 `RuntimeException`, `Error`와 그 하위 예외가 발생하면 롤백
- 체크 예외인 `Exception` 과 그 하위 예외가 발생하면 커밋

`@Transactional(rollbackFor = Exception.class)`: 체크 예외인 `Exception` 예외도 롤백하도록 설정 (하위 예외도 대상에 포함)



**noRollbackFor**

반대



**isolation**

트랜잭션 격리 수준 지정. 보통 DB에 설정된 값 사용.



**timeout**

트랜잭션 수행 시간에 대한 타임아웃을 초 단위로 지정.



**label**

트랜잭션 애너테이션에 있는 값을 직접 읽어서 어떤 동작을 하고 싶을 때 사용. 잘 안쓴다.



**readOnly**

`readOnly=true`: 읽기 전용 트랜잭션 생성. 이 경우 읽기만 작동한다. 드라이버나 DB에 따라 정상 동작하지 않을 수도 있다. 읽기 최적화를 위해 많이 쓴다. 이 옵션은 크게 다음 세 곳에서 적용된다.

- 프레임워크: JPA에서는 읽기 전용 트랜잭션의 경우 커밋 시점에 플러시를 호출하지 않는다. + 변경이 필요없으니 변경 감지를 위한 스냅샷 객체도 생성X. 이런 식으로 최적화
- JDBC 드라이버: 읽기, 쓰기(마스터, 슬레이브) DB 구분해서 요청
- DB: 내부에서 성능 최적화



## 예외와 트랜잭션 커밋/롤백

예외 발생시 스프링 트랜잭션 AOP는

- 언체크 예외인 `RuntimeException`, `Error`와 그 하위 예외가 발생하면 롤백
- 체크 예외인 `Exception` 과 그 하위 예외가 발생하면 커밋



``` java
package hello.springtx.exception;

@SpringBootTest
public class RollbackTest {

  @Autowired
  RollbackService service;

  @Test
  void runtimeException() {
    assertThatThrownBy(() -> service.runtimeException())
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  void checkedException() {
    assertThatThrownBy(() -> service.checkedException())
        .isInstanceOf(MyException.class);
  }

  @Test
  void rollbackFor() {
    assertThatThrownBy(() -> service.rollbackFor())
        .isInstanceOf(MyException.class);
  }

  @TestConfiguration
  static class RollbackTestConfig {

    @Bean
    RollbackService rollbackService() {
      return new RollbackService();
    }
  }

  @Slf4j
  static class RollbackService {
    //런타임 예외 발생: 롤백
    @Transactional
    public void runtimeException() {
      log.info("call runtimeException");
      throw new RuntimeException();
    }

    //체크 예외 발생: 커밋
    @Transactional
    public void checkedException() throws MyException {
      log.info("call checkedException");
      throw new MyException();
    }

    //체크 예외 rollbackFor 지정: 롤백
    @Transactional(rollbackFor = MyException.class)
    public void rollbackFor() throws MyException {
      log.info("call rollbackFor");
      throw new MyException();
    }
  }

  static class MyException extends Exception {
  }

}
```

- `runtimeException()`: 런타임 예외가 발생하므로 트랜잭션 롤백
- `checkedException()`: 예외 발생해도 트랜잭션 커밋
- `rollbackFor()`: 체크 예외이지만 트랜잭션 롤백



스프링은 왜 **체크 예외는 커밋하고, 언체크 예외는 롤백**할까??

스프링은 기본적으로

- 체크 예외: 비즈니스 의미가 있을 때 사용
- 언체크 예외: 복구 불가능한 예외 (DB 이슈, 네트워크 이슈 등..)



**비즈니스 요구 사항에서의 예외 발생**

- 시스템 예외: DB나 네트워크 이슈 등을 이유로 복구가 불가능한 예외

- 비즈니스 예외: 주문시 결제 잔고가 부족하면 주문 데이터를 저장하고, 결제 상태를 대기로 처리. 이 경우 **고객에게 잔고 부족을 알리고 별도의 계좌로 입금하도록 안내해야 함**

결제 잔고 부족하면 `NotEnoughMoneyException`이라는 체크 예외가 발생한다고 가정할 때, 이 예외는 시스템에 문제가 있어서 발생하는 시스템 예외가 아닌다. > 비즈니스 상황에서 발생한 비즈니스 예외. 비즈니스 예외는 매우 중요하고, 반드시 처리해야 할 경우가 많으므로 체크 예외를 고려할 수 있다.



``` java
package hello.springtx.order;

public class NotEnoughMoneyException extends Exception {
  
  public NotEnoughMoneyException(String message) {
    super(message);
  }
}
```

- 결제 잔고 부족하면 발생하는 비즈니스 예외. 체크 예외이다. 얘가 발생하면 롤백하지 않고, 주문 데이터 저장해야 한다.



**`OrderService.java`**

``` java
package hello.springtx.order;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;

  //JPA는 트랜잭션 커밋 시점에 Order 데이터를 DB에 반영한다.
  @Transactional
  public void order(Order order) throws NotEnoughMoneyException {
    log.info("order 호출");
    orderRepository.save(order);

    log.info("결제 프로세스 진입");

    if (order.getUsername().equals("예외")) {
      log.info("시스템 예외 발생");

      throw new RuntimeException("시스템 예외");
    } else if (order.getUsername().equals("잔고부족")) {
      log.info("잔고 부족 비즈니스 예외 발생");
      order.setPayStatus("대기");

      throw new NotEnoughMoneyException("잔고가 부족합니다");
    } else {
      //정상 승인
      log.info("정상 승인");
      order.setPayStatus("완료");
    }
    
    log.info("결제 프로세스 완료");
  }
}
```

- 정상 주문의 경우 주문 프로세스 정상 수행

- `userName == 예외`: 런타임 예외로 롤백이 수행
- `userName == 잔고부족`: 체크 예외로 커밋이 수행
