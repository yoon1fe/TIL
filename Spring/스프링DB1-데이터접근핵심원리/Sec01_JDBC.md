## H2 데이터베이스

**H2 데이터베이스**

- 개발이나 테스트 용도로 사용하기 좋은 가볍고 편리한 DB!! 그리고 SQL 실행할 수 있는 웹 화면 제공한다.

- JDBC URL: jdbc:h2:~/test
- 사용자명: sa
- `~/test.mv.db` 파일 생성된다.



## JDBC 이해

JDBC 등장 이유? 

애플리케이션을 개발할 때 중요한 데이터는 대부분 DB에 보관한다.

클라이언트가 애플리케이션 서버를 통해 데이터를 저장/조회하면, 애플리케이션 서버는 다음 과정을 통해 DB를 사용한다.

1. 커넥션 연결: 주로 TCP/IP를 사용해서 커넥션 연결
2. SQL 전달: 애플리케이션서버는 DB가 이해할 수 있는 SQL을 연결된 커넥션을 통해 DB에 전달
3. 결과 응답: DB는 전달된 SQL을 수행하고 그 결과를 응답. 애플리케이션은 응답 결과 활용



문제: DB마다 각각 커넥션 연결하는 방법, SQL 전달하는 방법, 결과를 응답하는 방법이 모두 다르다.

1. DB를 변경하면 애플리케이션 서버에 개발된 DB 사용 코드도 함께 변경해야 함
2. 개발자가 각각의 DB에 대한 방법을 새로 공부해야 됨



이런 문제를 해결하기 위해 **JDBC**라는 자바 표준이 등장!!



**JDBC(Java DataBase Connectivity) 표준 인터페이스**

> 자바에서 DB에 접속할 수 있도록 하는 자바 API. DB에서 자료를 쿼리하거나 업데이트하는 방법을 제공한다.



JDBC는 대표적으로 다음 세 가지 기능을 표준 인터페이스로 정의해서 제공한다.

- `java.sql.Connection`: 연결
- `java.sql.Statement`: SQL 을 담은 내용
- `java.sql.ResultSet`: SQL 요청 응답



개발자는 이제 얘네만 사용해서 개발하면 된다~~.

위의 애들을 각각의 DB 벤더사에서 자신의 DB에 맞게 구현해서 라이브러리로 제공하는 것을 JDBC 드라이버라고 부른다.



## JDBC와 최신 데이터 접근 기술

JDBC를 직접 사용하는건 번거롭고 복잡하다. 그래서 최근에는 SQL Mapper나 ORM 기술들이 존재하고 활용된다.



**SQL Mapper**

-  `애플리케이션 로직 -(SQL 전달)-> SQL Mapper -(SQL 전달)-> JDBC`
- 장점
  - JDBC를 편리하게 사용하도록 도와줌
  - SQL 응답 결과를 객체로 편리하게 변환
  - JDBC 반복 코드 제거
- 단점
  - 개발자가 SQL 직접 작성해야 함
- 대표 기술: 스프링 JdbcTemplate, MyBatis



**ORM**

- `애플리케이션 로직 -(객체 전달)-> JPA <-JPA 구현체(Hibernate 등) -(SQL 전달)-> JDBC`
- 객체를 관계형 데이터베이스 테이블과 매핑해주는 기술. ORM 기술이 개발자 대신 SQL을 동적으로 만들어서 실행해준다.
- 대표 기술: JPA, Hibernate, 이클립스 링크
- JPA는 자바 진영의 ORM 표준 인터페이스이고, 이것을 구현한 것이 Hibernate와 이클립스 링크



## DB 연결



```java
package hello.jdbc.connection;

...
    
public class DBConnectionUtil {

  public static Connection getConnection() {
    try {
      Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);

      log.info("get connection = {}, class = {}", conn, conn.getClass());
      return conn;
    } catch (SQLException e) {
      throw new IllegalArgumentException(e);
    }
  }

}
```

- DB 연결하려면 JDBC가 제공하는 `DriverManager.getConnection()`을 사용하면 된다. 

- 실행 결과

  `[main] INFO hello.jdbc.connection.DBConnectionUtil - get connection = conn0: url=jdbc:h2:tcp://localhost/~/test user=SA, class = class org.h2.jdbc.JdbcConnection`

  - h2 드라이버가 제공해주는 `JdbcConnection` 임을 확인 가능



**DriverManager 커넥션 요청 흐름**

1. 애플리케이션 로직에서 커넥션이 필요하면 `DriverManager.getConnection()` 을 호출한다. 
2. `DriverManager` 는 라이브러리에 등록된 드라이버 목록을 자동으로 인식한다. 이 드라이버들에게 순서대로 다음 정보를 넘겨서 커넥션을 획득할 수 있는지 확인한다. 여기서 각각의 드라이버는 **URL 정보를 체크**해서 본인이 처리할 수 있는 요청인지 확인한다. 처리할 수 있는 요청이면 커넥션을 획득하고 이 커넥션을 클라이언트에 반환한다. 반면에 URL이 jdbc:h2 로 시작했는데 MySQL 드라이버가 먼저 실행되면 이 경우 본인이 처리할 수 없다는 결과를 반환하게 되고, 다음 드라이버에게 순서가 넘어간다.
3. 이렇게 찾은 커넥션 구현체가 클라이언트에 반환된다.



## JDBC 개발



### 등록

```java
package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.extern.slf4j.Slf4j;

/**
 * JDBC - DriverManager 사용
 */
@Slf4j
public class MemberRepositoryV0 {

  public Member save(Member member) throws SQLException {
    String sql = "insert into member(member_id, money) values (?, ?)";  // DB에 전달한 SQL 정의

    Connection conn = null;
    PreparedStatement pstmt = null;


    try {
      conn = getConnection();
      pstmt = conn.prepareStatement(sql);                               // DB에 전달할 SQL과 파라미터로 전달할 데이터 준비
      pstmt.setString(1, member.getMemberId());
      pstmt.setInt(2, member.getMoney());
      
      pstmt.executeUpdate();                                            // Statement를 통해 준비된 SQL을 커넥션을 통해 실제 DB에 전달. 반환하는 int 는 영향받은 DB의 row 수를 의미한다.

      return member;
    } catch (SQLException e) {
      log.error("DB error", e);
      throw e;
    } finally {
      close(conn, pstmt, null);
    }
  }

  private void close(Connection conn, Statement stmt, ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException e) {
        log.info("error", e);
      }
    }

    if (stmt != null) {
      try {
        stmt.close();
      } catch (SQLException e) {
        log.info("error", e);
      }
    }

    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException e) {
        log.info("error", e);
      }
    }
  }

  private Connection getConnection() {
    return DBConnectionUtil.getConnection();
  }

}
```

굉장히 지저분하다.



리소스 정리 필수. `Connection`, `Statement`, `ResultSet` 모두 `close()` 필요하다. 리소스 정리 안하면 커넥션이 끊어지지 않고 계속 유지된다. -> 리소스 누수!! 결과적으로 커넥션 부족으로 장애 발생 가능



### 조회

```java
public Member findById(String memberId) throws SQLException {
  String sql = "select * from member where member_id = ?";
  Connection conn = null;
  PreparedStatement pstmt = null;
  ResultSet rs = null;
  try {
    conn = getConnection();
    pstmt = conn.prepareStatement(sql);
    pstmt.setString(1, memberId);
    rs = pstmt.executeQuery();
    if (rs.next()) {
      Member member = new Member();
      member.setMemberId(rs.getString("member_id"));
      member.setMoney(rs.getInt("money"));
      return member;
    } else {
      throw new NoSuchElementException("member not found memberId=" + memberId);
    }
  } catch (SQLException e) {
    log.error("db error", e);
    throw e;
  } finally {
    close(conn, pstmt, rs);
  }
}
```

- `ResultSet`은 내부에 커서가 있다. `.next()` 호출하면 커서가 다음으로 이동한다. 처음에는 데이터를 가리키지 않기 때문에 `next()`를 한 번은 호출해야 데이터 조회 가능.



### 수정/삭제

```java
public void update(String memberId, int money) throws SQLException {
  String sql = "update member set money=? where member_id=?";
  Connection conn = null;
  PreparedStatement pstmt = null;
  try {
    conn = getConnection();
    pstmt = conn.prepareStatement(sql);
    pstmt.setInt(1, money);
    pstmt.setString(2, memberId);
    int resultSize = pstmt.executeUpdate();
    log.info("resultSize={}", resultSize);
  } catch (SQLException e) {
    log.error("db error", e);
    throw e;
  } finally {
    close(conn, pstmt, null);
  }
}
```



```java
public void delete(String memberId) throws SQLException {
  String sql = "delete from member where member_id=?";
  Connection conn = null;
  PreparedStatement pstmt = null;
  try {
    conn = getConnection();
    pstmt = conn.prepareStatement(sql);
    pstmt.setString(1, memberId);
    pstmt.executeUpdate();
  } catch (SQLException e) {
    log.error("db error", e);
    throw e;
  } finally {
    close(conn, pstmt, null);
  }
}
```
