## 커넥션 풀

**기존 DB 커넥션을 획득하는 과정**

- DB 드라이버 <-> DB 간에 TCP/IP 커넥션 연결



커넥션을 새로 만드는 것은 과정도 복잡하고 시간도 많이 소모된다. DB뿐만 아니라 애플리케이션 서버에서도 TCP/IP 커넥션을 새로 생성하기 위한 리소스를 사용해야 한다. 더 큰 문제는 커넥션을 새로 만드는 시간이 사용자 응답 시간에 포함되어 버린다.



이런 문제를 해결하기 위한 아이디어: **커넥션 풀**

- 커넥션 여러 개를 미리 만들어두고 이를 꺼내 쓰는 것
- 개수 디폴트 값: 10개
- 대표적인 커넥션 풀 오픈소스 - **HikariCP**, commons-dbcp2, tomcat-jdbc pool



## DataSource

**커넥션을 획득하는 다양한 방법**

- `DriverManager` 사용해서 신규 커넥션 생성
- 커넥션 풀에서 조회



DriverManager를 통해 커넥션을 획득하다가 커넥션 풀로 변경 시에 애플리케이션 로직 변경이 필요하다. 다른 커넥션 풀로 변경할 때도 번거롭다. 이에 대한 해결 방법으로..



**커넥션 획득하는 방법을 추상화**

- `DataSource` interface

  - 핵심 기능: 커넥션 조회!!

  - ``` java
    public interface DataSource {
      Connection getConnection() throws SQLException;
    }
    ```



참고

- `DriverManager`는 `DataSource` 인터페이스를 사용하지 않는다. 이 부분을 해결하기 위해 스프링은 `DriverManager`도 `DataSource`를 통해서 사용할 수 있도록 `DriverManagerDataSource`라는 `DataSource`를 구현한 클래스를 제공한다.







### DriverManager

- `DriverManager`는 커넥션을 획득할 때마다 `URL`, `USERNAME`, `PASSWORD` 파라미터를 계속 전달해야 하는 반면에, `DataSource`는 처음 객체를 생성할 때만 파라미러틑 넘겨주고, 커넥션을 획득할 때는 `dataSource.getConnection()`만 호출하면 된다.



**설정과 사용의 분리**

- 설정: DataSource 를 만들고 필요한 속성들을 사용해서 URL , USERNAME , PASSWORD 같은 부분을 입력하는 것을 말한다. 이렇게 설정과 관련된 속성들은 한 곳에 있는 것이 향후 변경에 더 유연하게 대처할 수 있다. 
- 사용: 설정은 신경쓰지 않고, DataSource 의 getConnection() 만 호출해서 사용하면 된다.
- `Repository`에서는 `DataSource`에만 의존하고, 이런 속성을 몰라도 된다.
- 애플리케이션을 개발하다보면 설정은 한 곳에서, 사용은 여러 곳에서 하는데, 객체 설정 부분과 사용 부분을 명확히 분리할 수 있다.



### 커넥션 풀

```java
import com.zaxxer.hikari.HikariDataSource;

@Test
void dataSourceConnectionPool()throws SQLException,InterruptedException{
    //커넥션 풀링: HikariProxyConnection(Proxy) -> JdbcConnection(Target)
    HikariDataSource dataSource=new HikariDataSource();
    dataSource.setJdbcUrl(URL);
    dataSource.setUsername(USERNAME);
    dataSource.setPassword(PASSWORD);
    dataSource.setMaximumPoolSize(10);
    dataSource.setPoolName("MyPool");
  
    useDataSource(dataSource);
    Thread.sleep(1000); //커넥션 풀에서 커넥션 생성 시간 대기
    }
```

- HikariCP 사용. `HikariDataSource`는 `DataSource` 인터페이스를 구현하고 있다.
- 커넥션 풀 최대 사이즈를 10으로 지정, 풀의 이름을 `MyPool`로 설정
- 커넥션 풀에서 커넥션을 생성하는 작업은 애플리케이션 실행 속도에 영향을 주지 않기 위해 별도의 스레드에서 동작한다. 그렇기 때문에 테스트가 먼저 종료된다. `Thread.sleep()`으로 대기 시간을 주어야 커넥션이 생성되는 로그 확인 가능!



## DataSource 적용

```java
package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;
import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - DataSource 사용, JdbcUtils 사용
 */
@Slf4j
public class MemberRepositoryV1 {

  private final DataSource dataSource;

  public MemberRepositoryV1(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  //save()...
  //findById()...
  //update()....
  //delete()....
  
  private void close(Connection con, Statement stmt, ResultSet rs) {
    JdbcUtils.closeResultSet(rs);
    JdbcUtils.closeStatement(stmt);
    JdbcUtils.closeConnection(con);
  }

  private Connection getConnection() throws SQLException {
    Connection con = dataSource.getConnection();
    log.info("get connection={}, class={}", con, con.getClass());
    return con;
  }
}
```

- 외부에서 `DataSource` 의존관계 주입.
- 스프링은 `JdbcUtils`라는 편의 메서드 제공한다. 좀 더 편리하게 커넥션을 닫을 수 있음.

