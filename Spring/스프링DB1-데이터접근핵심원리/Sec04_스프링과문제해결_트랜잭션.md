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

```java
private Connection getConnection() throws SQLException {
    //주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
    Connection con = DataSourceUtils.getConnection(dataSource);
    log.info("get connection={}, class={}", con, con.getClass());
    return con;
}

private void close(Connection con, Statement stmt, ResultSet rs) {
    JdbcUtils.closeResultSet(rs);
    JdbcUtils.closeStatement(stmt);
    //주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
    DataSourceUtils.releaseConnection(con, dataSource);
 }
```

- `DataSourceUtils.getConnection()`
  - **트랜잭션 동기화 매니저가 관리하는 커넥션이 있으면 해당 커넥션을 반환**
  - 트랜잭션 동기화 매니저가 관리하는 커넥션이 없으면 새로운 커넥션을 생성해서 반환
- `DataSourceUtils.releaseConnection()`
  - `con.close()`로 직접 닫으면 커넥션이 유지되지 않는다. 이 커넥션은 이후 로직은 물론, 트랜잭션 종료(커밋 | 롤백) 때 까지 살아있어야 함
  - `releaseConnection()`을 사용하면 커넥션을 바로 닫는 것이 아님!
    - **트랜잭션을 사용하기 위해 동기화된 커넥션은 커넥션을 닫지 않고 그대로 유지**
    - 트랜잭션 동기화 매니저가 관리하는 커넥션이 없는 경우 해당 커넥션 종료



**서비스 레이어**

```java
package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        //트랜잭션 시작
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            //비즈니스 로직
            bizLogic(fromId, toId, money);
            transactionManager.commit(status); //성공시 커밋
        } catch (Exception e) {
            transactionManager.rollback(status); //실패시 롤백
            throw new IllegalStateException(e);
        }

    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

}
```

- 트랜잭션 매니저를 사용하면 서비스 레이어에서 `DataSource` 를 직접 사용하는 부분을 제거할 수 있다.
- `PlatformTransactionManager`
  - 트랜잭션 매니저를 주입받는다. JDBC 기술을 사용중이므로 `DataSourceTransactionManager` 구현체를 주입받아야 함
- `transactionManager.getTransaction()`
  - 트랜잭션 시작
  - `TransactionStatus status`를 반환한다. 현재 트랜잭션의 상태 정보 포함되어 있음. 롤백 / 커밋할 때 파라미터로 넘겨준다.



### 트랜잭션 템플릿

`try-catch`, 성공하면 `commit(status)`, 실패하면 `rollback(status)`과 같은 반복되는 패턴은 어떻게 처리하는 것이 좋을까!?



**트랜잭션 사용 코드**

```java
//트랜잭션 시작
TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

try {
    //비즈니스 로직
    bizLogic(fromId, toId, money);
    transactionManager.commit(status); //성공시 커밋
} catch (Exception e) {
    transactionManager.rollback(status); //실패시 롤백
    throw new IllegalStateException(e);
}
```

- 트랜잭션 시작 -> 비즈니스 로직 -> 성공: 커밋 | 예외: 롤백
- 달라지는 부분은 비즈니스 로직뿐!
- **템플릿 콜백 패턴**을 활용하면 이러한 반복 문제를 깔끔히 해결 가능!!



**트랜잭션 템플릿**

스프링은 `TransactionTemplate`라는 템플릿을 제공하는 클래스를 제공한다.

```java
public class TransactionTemplate {
    private PlatformTransactionManager transactionManager;
    public <T> T execute(TransactionCallback<T> action){..}
    void executeWithoutResult(Consumer<TransactionStatus> action){..}
}
```

- `execute()`: 응답값이 있을 때 사용
- `executeWithoutResult()`: 응답값이 없을 때 사용



```java
txTemplate.executeWithoutResult((status) -> {
            //비즈니스 로직
            try {
                bizLogic(fromId, toId, money);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
```

- 비즈니스 로직이 정상 수행되면 커밋
- 언체크 예외가 발생하면 롤백
- 하지만 이 곳은 서비스 로직이 있는 서비스 레이어인데, 트랜잭션을 처리하는 기술 로직이 포함되어 버렸다..







### 트랜잭션 AOP

스프링 AOP를 이용해서 프록시를 도입하면 서비스 계층에 순수한 비즈니스 로직만 남길 수 있다.



**TransactionProxy**

```java
public class TransactionProxy {
    private MemberService target;
    public void logic() {
        //트랜잭션 시작
        TransactionStatus status = transactionManager.getTransaction(..);
        try {
            //실제 대상 호출
            target.logic();
            transactionManager.commit(status); //성공시 커밋
        } catch (Exception e) {
            transactionManager.rollback(status); //실패시 롤백
            throw new IllegalStateException(e);
        }
    }
}
```



**Service 계층**

```java
public class Service {
    public void logic() {
        //트랜잭션 관련 코드 제거, 순수 비즈니스 로직만 남음
        bizLogic(fromId, toId, money);
    }
}
```

- 프록시를 도입하면 트랜잭션 프록시가 트랜잭션 처리 로직을 모두 가져간다. 그리고 트랜잭션을 시작한 후에 실제 서비스를 대신 호출한다.



**스프링이 제공하는 트랜잭션 AOP**

- 스프링이 제공하는 AOP 기능을 사용하면 프록시를 매우 편리하게 적용 가능!
- 스프링에서 트랜잭션 AOP를 위한 모든 기능을 제공해준다. 스프링 부트를 사용하면 트랜잭션 AOP를 처리하기 위해 필요한 스프링 빈들도 자동으로 등록해준다.
- 개발자는 트랜잭션 처리가 필요한 곳에 **`@Transactional`** 애너테이션만 붙여주면 된다!! 스프링의 트랜잭션 AOP는 이 애너테이션을 인식해서 트랜잭션 프록시를 적용해준다.



> 어드바이저: `BeanFactoryTransactionAttributeSourceAdvisor`
>
> 포인트컷: `TransactionAttributeSourcePointcut`
>
> 어드바이스: `TransactionInterceptor`



**MemberServiceV3_3**

```java
		@Transactional
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        bizLogic(fromId, toId, money);
    }
```

- `@Transactional` 애너테이션은 메서드에 붙여도 되고, 클래스에 붙여도 된다. 클래스에 붙이면 외부에서 호출 가능한 `public` 메서드가 AOP 적용 대상이 된다.!



**테스트 코드 부분**

```java
@TestConfiguration
static class TestConfig {
    @Bean
    DataSource dataSource() {
        return new DriverManagerDataSource(URL, USERNAME, PASSWORD);
    }
    @Bean
    PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }
    @Bean
    MemberRepositoryV3 memberRepositoryV3() {
        return new MemberRepositoryV3(dataSource());
    }
    @Bean
    MemberServiceV3_3 memberServiceV3_3() {
        return new MemberServiceV3_3(memberRepositoryV3());
    }
}
```

- `@SpringBootTest`: 스프링 AOP를 적용하려면 스프링 컨테이너가 필요하다. 이 애너테이션이 있으면 테스트를 실행할 때 스프링 부트를 통해 스프링 컨테이너를 생성한다. 그리고 테스트에서 `@Autowired` 등을 통해 스프링 컨테이너가 관리하는 빈들을 사용할 수 있다.
- `@TestConfiguration`: 테스트 안에서 내부 설정 클래스를 만들어서 사용하면서 이 애너테이션을 붙이면 스프링 부트가 자동으로 만들어주는 빈들에 추가로 필요한 스프링 빈들을 등록하고 테스트를 수행할 수 있다.
- 스프링이 제공하는 트랜잭션 AOP는 스프링 빈에 등록된 트랜잭션 매니저를 찾아서 사용하기 때문에 트랜잭션 매니저를 스프링 빈으로 등록해두어야 한다.



**AOP 프록시 적용 확인**

```java
@Test
void AopCheck() {
    log.info("memberService class={}", memberService.getClass());
    log.info("memberRepository class={}", memberRepository.getClass());
      Assertions.assertThat(AopUtils.isAopProxy(memberService)).isTrue();
      Assertions.assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
}
```



실행 결과

``` bash
memberService class=class hello.jdbc.service.MemberServiceV3_3$
$EnhancerBySpringCGLIB$$...
memberRepository class=class hello.jdbc.repository.MemberRepositoryV3
```

- 프록시 클래스는 기존 클래스를 상속받아서 생성된다. (MemberServiceV3_3$
  $EnhancerBySpringCGLIB$$...)



**선언적 트랜잭션 관리 vs 프로그래밍 방식 트랜잭션 관리**

- 선언적 트랜잭션 관리
  - `@Transactional` 애너테이션 하나만 선언해서 매우 편리하게 트랜잭션 적용
- 프로그래밍 방식의 트랜잭션 관리
  - 트랜잭션 매니저 또는 트랜잭션 템플릿 등을 사용해서 트랜잭션 관련 코드를 직접 작성



- 선언적 트랜잭션 관리가 프로그래밍 방식에 비해서 훨씬 간편하고 실용적이기 때문에 실무에서는 대부분 선언적 트랜잭션 관리를 사용한다. 
- 프로그래밍 방식의 트랜잭션 관리는 스프링 컨테이너나 스프링 AOP 기술 없이 간단히 사용할 수 있지만 실무에서는 대부분 스프링 컨테이너와 스프링 AOP를 사용하기 때문에 거의 사용되지 않는다. 
- 프로그래밍 방식 트랜잭션 관리는 테스트 시에 가끔 사용될 때는 있다.



## 스프링 부트의 자동 리소스 등록

스프링 부트가 등장하기 이전에는 데이터소스와 트랜잭션 매니저를 개발자가 직접 스프링 빈으로 등록해서 사용했다. 스프링 부트는 이들을 직접 등록 해준다.

```java
@Bean
DataSource dataSource() {
    return new DriverManagerDataSource(URL, USERNAME, PASSWORD);
}
@Bean
PlatformTransactionManager transactionManager() {
    return new DataSourceTransactionManager(dataSource());
}
```



**데이터 소스 - 자동 등록**

- 스프링 부트가 자동으로 등록해줌! 빈 이름: `dataSource`

- 참고로 개발자가 직접 데이터 소스를 빈으로 등록하면 스프링 부트가 따로 등록하지 않는다.

- 스프링 부트는 다음과 같이 `application.properties`에 있는 속성을 사용해서 `DataSource`를 생성하고 스프링 빈으로 등록한다.

  ``` properties
  spring.datasource.url=jdbc:h2:tcp://localhost/~/test
  spring.datasource.username=sa
  spring.datasource.password=
  ```

- 스프링 부트가 기본으로 생성하는 데이터 소스는 커넥션 풀을 제공하는 `HikariDataSource`.  커넥션 풀 관련 설정도 프로퍼티 파일로 설정 가능
- `spring.datasource.url` 속성이 없으면 내장 데이터베이스(메모리 DB)를 생성하려고 시도한다.



**트랜잭션 매니저 - 자동 등록**

- 스프링 부트는 적절한 트랜잭션 매니저(`PlatformTransactionManager`)를 자동으로 스프링 빈에 등록한다. 자동으로 등록되는 스프링 빈 이름: `transactionManager`
- 마찬가지로 개발자가 직접 빈 등록하면 따로 등록하지 않는다.
- 어떤 트랜잭션 매니저를 선택할지는 현재 등록된 라이브러리를 보고 판단하는데, JDBC를 기술을 사용하면 `DataSourceTransactionManager` 를 빈으로 등록하고, JPA를 사용하면 `JpaTransactionManager` 를 빈으로 등록한다. 둘다 사용하는 경우 `JpaTransactionManager` 를 등록한다. 참고로 `JpaTransactionManager` 는 `DataSourceTransactionManager` 가 제공하는 기능도 대부분 지원!