## 문제점

트랜잭션을 적용하면서 발생했던 여러 문제들을 스프링에서는 어떻게 해결할까??



**애플리케이션 구조**

- 프레젠테이션 계층
  - UI 와 관련된 처리 담당
  - 웹 요청과 응답
  - 사용자 요청을 검증
  - 주 사용 기술: 서블릿과 HTTP 같은 웹 기술, 스프링 MVC

- 서비스 계층
  - 비즈니스 로직 담당
  - 주 사용 기술: 가급적 특정 기술에 의존하지 않고, 순수 자바 코드로 작성
- 데이터 접근 계층
  - 실제 DB에 접근하는 코드
  - 주 사용 기술: JDBC, JPA, File, Redis, Mongo..



가장 중요한 곳은 핵심 비즈니스 로직이 들어있는 서비스 계층. 서비스 계층은 특정 기술에 종속적이지 않게 개발해야 한다.



**문제점들**

서비스 계층을 순수하게 유지하려면 어떻게 해야 할까??

`throws SQLException` 은 JDBC 기술에 종속적이다. `repository`에서 올라오는 예외이기 때문에 `repository`에서 해결하는 것이 좋다.



트랜잭션은 비즈니스 로직이 있는 서비스 계층에서 시작하는 것이 좋다. 근데 서비스 계층에서 트랜잭션을 사용하기 위해 JDBC 기술에 의존하고, 비즈니스 로직보다 트랜잭션 처리 코드가 더 많다. 핵심 비즈니스 로직과 JDBC 기술이 섞여 있어서 유지보수 하기 어렵다.



**문제 정리**

- 트랜잭션 문제
  - JDBC 구현 기술이 서비스 계층에 누수되는 문제
  - 트랜잭션 동기화 문제
  - 트랜잭션 적용 반복 문제
- 예외 누수
  - 데이터 접근 계층의 JDBC 구현 기술 예외가 서비스 계층으로 전파됨
  - `SQLException` 은 체크 예외이기 때문에 데이터 접근 계층을 호출한 서비스 계층에서 해당 예외를 잡아서 처리하거나, 명시적으로 `throws`를 통해서 다시 밖으로 던져야 함
- JDBC 반복 문제
  - `try`, `catch`, `finally`...



스프링은 서비스 계층을 순수하게 유지하면서, 지금까지 봤던 문제들을 해결할 수 있는 다양한 방법과 기술들을 제공한다.!!



## 트랜잭션 추상화



## 트랜잭션 추상화

구현 기술마다 트랜잭션 사용 방법이 다르다..

- JDBC: `con.setAutoCommit(false);`
- JPA: `transaction.begin();`



**트랜잭션 추상화 인터페이스**

``` java
public interface TxManager {
  begin();
  commit();
  rollback();
}
```



서비스는 특정 트랜잭션 기술에 직접 의존하는 것이 아니라, `TxManager`라는 추상화된 인터페이스에 의존하면 원하는 구현체를 DI를 통해서 주입하면 된다.

클라이언트인 서비스는 인터페이스에 의존하고 DI를 사용했기 때문에 OCP 원칙을 지키게 되었다.!!



**스프링의 트랜잭션 추상화**

`PlatformTransactionManager.java`

``` java
package org.springframework.transaction;

public interface PlatformTransactionManager extends TransactionManager {
  TransactionStatus getTransaction(@Nullable TransactionDefinition definition) throws TransactionException;
  void commit(TransactionStatus status) throws TransactionException;
  void rollback(TransactionStatus status) throws TransactionException;
}
```

- `getTransaction()`: 트랜잭션 시작. 기존에 진행중인 트랜잭션이 있는 경우 해당 트랜잭션에 참여할 수 있다.
- `commit()`: 트랜잭션 커밋
- `rollback()`: 트랜잭션 롤백



## 트랜잭션 동기화

스프링이 제공하는 트랜잭션 매니저는 크게 두 가지 역할을 한다.

- 트랜잭션 추상화
- 리소스 동기화



**리소스 동기화**

트랜잭션을 유지하려면 트랜잭션의 시작~끝까지 같은 DB 커넥션을 유지해야 한다. 이전에는 파라미터로 커넥션을 전달했었다..



**트랜잭션 매니저와 트랜잭션 동기화 매니저**

- 스프링은 **트랜잭션 동기화 매니저**를 제공한다. 트랜잭션 동기화 매니저는 `ThreadLocal` 을 사용해서 커넥션을 동기화해준다.
- 트랜잭션 동기화 매니저는 스레드 로컬을 사용하기 때문에 멀티 스레드 상황에서 안전하게 커넥션을 동기화할 수 있다.





## 트랜잭션 문제 해결



### 트랜잭션 매니저





### 트랜잭션 템플릿





### 트랜잭션 AOP





## 스프링 부트의 자동 리소스 등록