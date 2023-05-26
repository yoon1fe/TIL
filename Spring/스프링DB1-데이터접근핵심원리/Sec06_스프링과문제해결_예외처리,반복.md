## 체크 예외와 인터페이스

서비스 계층은 가급적 특정 구현 기술에 의존하지 않고, 순수하게 유지하는 것이 좋다. 이러려면 예외에 대한 의존도 해결해야 함. ex) `SQLException`

이런 애들은 서비스가 처리할 수 없으므로 레포지토리에서 **체크 예외를 런타임 예외로 전환해서 서비스 계층에 던지자.**



**Repository를 인터페이스로**

- 인터페이스로 구현하면 `Service`는 `Repository` 인터페이스에만 의존하면 된다.
- 구현 기술을 변경하고 싶으면 DI를 통해 `Service` 코드 변경없이 구현 기술 변경 가능



``` java
package hello.jdbc.repository;

import hello.jdbc.domain.Member;

public interface MemberRepository {

  Member save(Member member);
  Member findById(String memberId);
  void update(String memberId, int money);
  void delete(String memberId);
}
```

- 특정 기술에 종속되지 않는 순수한 인터페이스
- 인터페이스의 구현체가 체크 예외를 던지려면 인터페이스에 있는 추상 메서드에 체크 예외를 던지는 부분이 선언되어 있어야 한다..
  - 특정 구현 기술에 종속적이다..



런타임 예외는 이런 부분에서 자유롭다!! 구현부에서 `SQLException`을 `catch`해서 런타임 예외로 바꿔서 던져주자.

** 주의 - 예외를 변화할 때는 꼭 기존 예외를 포함할것!



**남은 문제**

- 레포지토리에서 넘어오는 특정한 예외의 경우 복구를 시도할 수 있다. 그런데 지금 방식은 항상 `MyDbException`이라는 커스텀 예외만 넘어오기 때문에 예외를 구분할 수가 없다. 특정 상황에서 예외를 잡아서 복구하고 싶으면 예외를 어떻게 구분해서 처리할 수 있을까??



## 데이터 접근 예외 직접 만들기

`SQLException` 내부에 있는 `errorCode`를 보면 DB에서 어떤 문제가 발생했는지 알 수 있다.



서비스 계층에서 예외 복구를 위해 `SQLException`을 활용해야 한다. 근데 이러려면 결국 서비스 계층에 JDBC 기술에 의존적인 예외를 던지게 된다..

-> 레포지토리에서 예외를 변환해서 던지자. `SQLException` -> `MyDuplicateKeyException`



``` java
  } catch (SQLException e) {
    //h2 db
    if (e.getErrorCode() == 23505) {
      throw new MyDuplicateKeyException(e);
    }
    throw new MyDbException(e);
  }
```



**또 남은 문제**

- SQL ErrorCode는 DB마다 다르다!!
- 수십 수백가지 오류 코드가 있을텐데.. 각각에 맞는 예외를 모두 다 만들어야 하나??



## 스프링 예외 추상화

스프링은 위의 문제들을 해결하기 위해 데이터 접근과 관련된 예외를 **추상화**해서 제공한다!!!

![스프링 DB 1 정리 - 6. 스프링과 문제 해결 - 예외처리 (22.8.22)](https://velog.velcdn.com/images/dodo4723/post/6c3fb17d-d9dd-43e0-b9e2-c1b268b7fd40/image.png)

- 데이터 접근 계층에 대한 수십 가지 예외가 정의되어 있음
- 각 예외는 특정 기술에 종속적X.
- 각각 다른 데이터 접근 기술을 사용할 때 발생하는 예외를 스프링이 제공하는 예외로 변환하는 역할도 스프링이 한다.

- 최고 상위 예외: `org.springframework.dao.DataAccessException`
- 얘는 크게 두 가지로 구분되는데,
  - `Transient`: 일시적. 동일한 SQL을 다시 시도했을 때 성공할 가능성 있음. 쿼리 타임아웃, 락과 관련된 오류들..
  - `NonTransient`: 일시적이지 않음. 같은 SQL을 반복해서 실행하면 실패한다. SQL 문법 오류, 데이터베이스 제약조건 위배 등..



**스프링이 제공하는 예외 변환기**

스프링은 데이터베이스에서 발생하는 오류 코드를 스프링이 정의한 예외로 자동으로 변환해주는 변환기를 제공한다.



``` java
  } catch (SQLException e) {
    assertThat(e.getErrorCode()).isEqualTo(42122);
    //org.springframework.jdbc.support.sql-error-codes.xml
    SQLExceptionTranslator exTranslator = new
        SQLErrorCodeSQLExceptionTranslator(dataSource);
    //org.springframework.jdbc.BadSqlGrammarException
    DataAccessException resultEx = exTranslator.translate("select", sql,
        e);
    log.info("resultEx", resultEx);

    assertThat(resultEx.getClass()).isEqualTo(BadSqlGrammarException.class);
  }
```

- `translate()` 메서드의 파라미터

  - 설명
  - 실행한 sql
  - 발생된 `SQLException`

  이렇게 하면 적절한 스프링 데이터 접근 계층의 예외로 변환해서 반환해준다.

- 예제에서는 SQL 문법이 잘못되었으므로 `BadSqlGrammarException` 반환해줌.



각각의 DB가 제공하는 SQL ErrorCode는 `sql-error-codes.xml` 파일에서 관리!



## JDBC 반복 문제 해결 - JdbcTemplate

**JDBC 반복 문제**

- 커넥션 조회, 커넥션 동기화
- `PreparedStatement` 생성 및 파라미터 바인딩
- 쿼리 실행
- 결과 바인딩
- 예외 발생 시 스프링 예외 변환기 실행
- 리소스 종료



반복적인 부분을 효과적으로 처리하는 방법 - **템플릿 콜백 패턴**

스프링은 JDBC의 반복 문제를 해결하기 위해 `JdbcTemplate`이라는 템플릿을 제공한다.



``` java
public class MemberRepositoryV5 implements MemberRepository {

  private final JdbcTemplate template;

  public MemberRepositoryV5(DataSource dataSource) {
    template = new JdbcTemplate(dataSource);
  }

  @Override
  public Member save(Member member) {
    String sql = "insert into member(member_id, money) values(?, ?)";
    template.update(sql, member.getMemberId(), member.getMoney());
    return member;
  }

  @Override
  public Member findById(String memberId) {
    String sql = "select * from member where member_id = ?";
    return template.queryForObject(sql, memberRowMapper(), memberId);
  }

  @Override
  public void update(String memberId, int money) {
    String sql = "update member set money=? where member_id=?";
    template.update(sql, money, memberId);
  }

  @Override
  public void delete(String memberId) {
    String sql = "delete from member where member_id=?";
    template.update(sql, memberId);
  }

  private RowMapper<Member> memberRowMapper() {
    return (rs, rowNum) -> {
      Member member = new Member();
      member.setMemberId(rs.getString("member_id"));
      member.setMoney(rs.getInt("money"));
      return member;
    };
  }
}
```

- `JdbcTemplate`은 JDBC로 개발할 때 발생하는 반복을 대부분 해결해줄 뿐더러 트랜잭션을 위한 커넥션 동기화, 스프링 예외 변환기도 자동으로 실행해준다!!