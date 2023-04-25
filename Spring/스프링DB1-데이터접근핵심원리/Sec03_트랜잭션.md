## 트랜잭션 개념

DB는 **트랜잭션**이라는 개념을 지원한다. 트랜잭션이란 **하나의 거래를 안전하게 처리하도록 보장**해주는 것을 의미.

모든 작업이 성공해서 DB에 정상 반영하는 것을 커밋, 작업 중 하나라도 실패해서 거래 이전으로 되돌리는 것을 롤백이라고 한다.



**트랜잭션 ACID**

- **원자성(Atomicity)**: 트랜잭션 내에서 실행한 작업들은 마치 하나의 작업인 것처럼 모두 성공하거나 모두 실패해야 한다.
- **일관성(Consistency)**: 모든 트랜잭션은 일관성있는 DB 상태를 유지해야 한다. ex) 무결성 제약 조건 항상 만족..
  - **격리성(Isolation)**: 동시에 실행되는 트랜잭션들이 서로에게 영향을 미치지 않도록 격리한다. ex) 동시에 같은 데이터 수정 못하도록. 격리성은 동시성과 관련된 성능 이슈로 인해, 트		랜잭션 격리 수준을 선택할 수 있다.
- **지속성(Durability)**: 트랜잭션을 성공적으로 끝내면 그 결과가 항상 기록되어야 한다. 중간에 시스템에 문제가 발생해도 DB 로그 등을 사용해서 성공한 트랜잭션 내용을 복구해야 한다.



**트랜잭션 격리 수준 - Iolation level**

- `READ UNCOMMITTED` (커밋되지 않은 읽기)
- `READ COMMITTED` (커밋된 읽기)
- `RETEATABLE READ` (반복 가능한 읽기)
- `SERIALIZABLE` (직렬화 가능)



## 데이터베이스 연결 구조와 DB 세션

- 사용자는 WAS나 DB 접근 툴같은 클라이언트를 통해 DB 서버에 접근한다. 
- 클라이언트는 DB 서버에 연결을 요청하고 **커넥션**을 맺는다. 이때 DB 서버에서는 내부에 **세션**을 생성하고, 해당 커넥션을 통한 모든 요청은 이 세션을 통해 실행된다.
- 세션은 **트랜잭션**을 시작하고, 커밋/롤백을 통해 트랜잭션을 종료한다.
- 사용자가 커넥션을 닫거나 DB 관리자가 세션을 강제로 종료하면 세션은 종료된다.
- 만약 커넥션 풀이 10개의 커넥션을 생성하면, 세션도 10개가 생성된다.



## 트랜잭션 - DB 예제

**트랜잭션 사용법**

- 데이터 변경 쿼리를 실행하고 DB에 결과를 반영하려면 `commit` 명령어 호출, 그렇지 않다면 `rollback` 명령어 호출
- 커밋을 호출하기 전까지는 임시로 데이터를 저장하는 것! 해당 트랜잭션을 시작한 세션(사용자)에게만 변경 데이터가 보이고, 다른 세션(사용자)에게는 변경 데이터가 보이지 않는다.(`set autocommit true;`)
- 등록, 수정, 삭제 모두 같은 원리로 동작한다.



**커밋하지 않은 데이터를 다른 곳에서 조회할 수 있다면 어떤 문제가 발생할까??**

- 세션1에서 변경한 데이터를 세션2에서 조회했을 때 변경된 데이터가 보이면 그 데이터를 갖고 어떤 로직을 수행할 수 있다. 근데 세션1이 롤백해버리면 데이터 정합성에 큰 문제가 발생한다. 따라서 **커밋 전의 데이터는 다른 세션에 보여지면 안된다!**



**자동 커밋**

각각의 쿼리 실행 직후 자동으로 커밋 호출한다. 커밋/롤백을 직접 호출하지 않아도 되는 편리함이 있지만, 우리가 원하는 트랜잭션 기능을 제대로 사용할 수 없다.



**수동 커밋** `set autocommit false;`

보통 자동 커밋 모드가 디폴트이므로, **수동 커밋 모드로 설정하는 것을 트랜잭션의 시작**이라고 생각하면 된다.

수동 커밋 모드로 설정하면 이후에 꼭 `commit`, `rollback`을 호출해야 한다. 안그러면 트랜잭션 실행 타임아웃 시간이 지나면 롤백된다.



커밋 모드는 한 번 설정하면 해당 세션에서는 계속 유지된다.



## DB 락

세션이 트랜잭션을 시작하고 데이터를 수정하는 동안 커밋이나 롤백 전까지 다른 세션에서 해당 데이터를 수정할 수 없도록 막는 것. 동시에 데이터를 수정하는 문제를 락으로 해결한다.



**락 동작 방식**

- 락을 획득해야 데이터 변경 가능

- 커밋으로 트랜잭션이 종료되면 락 반납

- 락 획득을 위해 대기하던 세션2에서 락을 획득



### 변경

**락 타임 아웃**

`SET LOCK_TIMEOUT <milliseconds>`



### 조회

**일반적인 조회는 락을 사용하지 않는다!**



**조회와 락**

- 데이터를 조회할 때도 락을 획득하고 싶다면, `select for update` 구문을 사용하면 된다.

  ex) `select * from member where member_id='memberA' for update;`

- 세션1이 조회 시점에 락을 가져가기 때문에 다른 세션에서 해당 데이터를 변경할 수 없다. 물론 트랜잭션을 커밋하면 락을 반납한다.



**조회할 때 락이 필요한 경우는 언제??**

- 트랜잭셕 종료 시점까지 해당 데이터를 다른 곳에서 변경하지 못하도록 강제로 막아야 할 때 사용



## 트랜잭션 적용

### 트랜잭션 X

```java
package hello.jdbc.service;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.*;

import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import java.sql.SQLException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * 기본 동작, 트랜잭션이 없어서 문제 발생
 */
class MemberServiceV1Test {

  public static final String MEMBER_A = "memberA";
  public static final String MEMBER_B = "memberB";
  public static final String MEMBER_EX = "ex";

  private MemberRepositoryV1 memberRepository;
  private MemberServiceV1 memberService;

  @BeforeEach
  void before() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
    memberRepository = new MemberRepositoryV1(dataSource);
    memberService = new MemberServiceV1(memberRepository);
  }

  @AfterEach
  void after() throws SQLException {
    memberRepository.delete(MEMBER_A);
    memberRepository.delete(MEMBER_B);
    memberRepository.delete(MEMBER_EX);
  }

  @Test
  @DisplayName("정상 이체")
  void accountTransfer() throws SQLException {
    // given
    Member memberA = new Member(MEMBER_A, 10000);
    Member memberB = new Member(MEMBER_B, 10000);

    memberRepository.save(memberA);
    memberRepository.save(memberB);

    // when
    memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

    // then
    Member findMemberA = memberRepository.findById(memberA.getMemberId());
    Member findMemberB = memberRepository.findById(memberB.getMemberId());

    assertThat(findMemberA.getMoney()).isEqualTo(8000);
    assertThat(findMemberB.getMoney()).isEqualTo(12000);
  }

  @Test
  @DisplayName("이체중 예외 발생")
  void accountTransferEx() throws SQLException {
    // given
    Member memberA = new Member(MEMBER_A, 10000);
    Member memberEx = new Member(MEMBER_EX, 10000);

    memberRepository.save(memberA);
    memberRepository.save(memberEx);

    // when
    assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
        .isInstanceOf(IllegalStateException.class);

    // then
    Member findMemberA = memberRepository.findById(memberA.getMemberId());
    Member findMemberEx = memberRepository.findById(memberEx.getMemberId());

    assertThat(findMemberA.getMoney()).isEqualTo(8000);
    assertThat(findMemberEx.getMoney()).isEqualTo(10000);
  }
}
```

- `@AfterEach`: 각각의 테스트가 실행되고 난 이후에 실행됨. 테스트 데이터 삭제용
- 테스트 전에 트랜잭션을 시작하고, 테스트 이후 트랜잭션을 **롤백**해버리면 위처럼 할 필요 없다.



트랜잭션이 적용되지 않았기 때문에,

```java
assertThat(findMemberA.getMoney()).isEqualTo(8000);
assertThat(findMemberEx.getMoney()).isEqualTo(10000);
```



이렇게 예외가 터지고 난 뒤 `findMemberEx`의 값은 변하지 않았다.



### 트랜잭션 O

애플리케이션에서 트랜잭션을 어떤 계층에 걸어야 할까?? 트랜잭션을 어디서 시작하고, 어디서 커밋해야할까???

- 트랜잭션은 **비즈니스 로직이 있는 서비스 계층에서 시작해야 한다**. 비즈니스 로직이 잘못되면 해당 비즈니스 로직으로 인해 문제가 되는 부분을 함께 롤백해야 하기 때문!
- 근데 트랜잭션을 시작(`set autocommit false`)하려면 커넥션이 필요하다. 결국 서비스 계층에서 커넥션을 만들고, 트랜잭션 커밋 이후 커넥션을 종료해야 한다.
- 애플리케이션에서 DB 트랜잭션을 사용하려면 **트랜잭션을 사용하는 동안 같은 커넥션을 유지**해야 한다. 그래야 같은 세션을 사용할 수 있다.



그럼 애플리케이션에서 같은 커넥션을 유지하려면 어떻게 해야 할까?? 단순한 방법으로 커넥션을 파라미터로 전달해서 같은 커넥션을 사용하도록 할 수 있겠다..

```java
package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

  private final DataSource dataSource;
  private final MemberRepositoryV2 memberRepository;

  public void accountTransfer(String fromId, String toId, int money) throws SQLException {
    Connection con = dataSource.getConnection();
    try {
      con.setAutoCommit(false); // 트랜잭션 시작!

      // 비즈니스 로직 수행
      bizLogic(con, fromId, toId, money);

      con.commit(); // 성공시 커밋!

    } catch (Exception e) {
      con.rollback(); // 실패시 롤백 ㅜ
      throw new IllegalStateException(e);
    } finally {
      release(con);
    }
  }

  private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
    Member fromMember = memberRepository.findById(con, fromId);
    Member toMember = memberRepository.findById(con, toId);

    memberRepository.update(con, fromId, fromMember.getMoney() - money);
    // 오류 케이스
    validation(toMember);
    memberRepository.update(con, toId, toMember.getMoney() + money);
  }

  private static void release(Connection con) {
    if (con != null) {
      try {
        con.setAutoCommit(true);    // 커넥션 풀 고려..
        con.close();
      } catch (Exception e) {
        log.info("error", e);
      }
    }
  }

  private static void validation(Member toMember) {
    if (toMember.getMemberId().equals("ex")) {
      throw new IllegalStateException("이체 중 예외 발생");
    }
  }
}
```



비즈니스 로직에서 오류가 발생시 롤백된다!

**남은 문제**

- 애플리케이션에서 DB 트랜잭션을 적용하려면 서비스 계층이 매우 지저분해지고, 매우 복잡한 코드가 필요하다. 커넥션을 유지하도록 코드를 변경하는 것도 힘들다. 스프링을 사용해서 이같은 문제를 해결 가능!