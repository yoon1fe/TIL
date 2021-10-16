## 스프링과 Oracle Database 연동

데이터베이스로는 오라클 데이터베이스 `11g`를 사용합니다.

`SQL Developer`를 통해 데이터베이스를 쉽게 사용해보겠습니다.

참고로 컴퓨터에 설치되어 있는 자바가 오라클 jdk가 아니면 javaFx 뭐시기 오류가 뜨는 것 같습니다..

그냥 jdk8 포함되어 있는 SQL Developer 설치하면 됩니다.



예제를 위한 계정을 만들어 봅시다.

```sql
CREATE USER book_ex IDENTIFIED BY book_ex
DEFAULT TABLESPACE USERS
TEMPORARY TABLESPACE TEMP;
```

계정을 잘 만들고 권한을 줍시다.

```sql
grant CONNECT, DBA TO BOOK_EX;
```



오라클의 기본 포트는 8080인데, `Tomcat`의 포트 번호도 8080라서 포트 번호를 변경해줍니다.

````sql
select dbms_xdb.gethttpport() from dual;
````

현재 사용하는 포트 번호가 몇 번인지 확인한 후 포트 번호를 수정해줍니다. 저는 9090으로 바꿨습니다.

```sql
exec dbms_xdb.sethttpport(9090);
```



우선 `JDBC` 연결을 하려면 `JDBC Driver`가 필요합니다. 오라클 데이터베이스의 `JDBC Driver`는 `11g`까지 공식적으로 `Maven`으로는 지원되지 않기 때문에 `ojdbc8.jar` 를 직접 프로젝트에 추가해 주어야 합니다.

`Build Path > Configure Build Path > Libraries > Add External JARs `에서 추가해 줍니다.



### JDBC 테스트

`JDBC` 드라이버를 정상적으로 추가하고, 데이터베이스 연결이 가능하다면 이를 직접 확인하는 테스트 코드를 작성 해봅시다.

```java
package org.zerock.persistence;

import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.Test;

import lombok.extern.log4j.Log4j;

@Log4j
public class JDBCTests {
	static {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testConnection() {
		try(Connection con = DriverManager.getConnection(
				"jdbc:oracle:thin:@localhost:1521:orcl",
				"book_ex",
				"book_ex")){
			
			log.info(con);
		}catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
```

테스트 코드는 일단 자바와 `JDBC` 드라이버만으로 구현해서 테스트 합니다. 데이터베이스 연결이 정상적으로 가능하다면 데이터베이스와 연결된 `Connection` 객체가 출력됩니다.

![image-20200810211903175](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FE5x3S%2FbtqGqnhZc5K%2FqxCucVev2RgHbymMxdnJ5K%2Fimg.png)





### 커넥션 풀 설정

일반적으로 여러 명의 사용자를 동시에 처리하는 웹 애플리케이션의 경우, 데이터베이스 연결을 이용할 때는 커넥션 풀`(Connection Pool)`을 이용합니다.

자바에서는 `DataSource`라는 인터페이스를 통해 커넥션 풀을 사용합니다. `DataSource`를 통해 매번 데이터베이스와 연결하는 것이 아니라, 미리 연결을 맺어주고 반환하는 구조를 이용하여 성능 향상을 꾀합니다.

커넥션 풀은 여러 종류가 있고, `spring-jdbc` 라이브러리를 사용하는 방식도 있지만, 최근 유행하는 `HikariCP`를 이용해 보겠습니다. 이 친구는 스프링 부트 2.0에서도 사용될 만큼 핫한 친구랩니다.



#### 라이브러리 추가 및 DataSource 설정

`pom.xml` 파일을 수정해서 `HikariCP`를 추가합니다.

```xml
<!-- https://mvnrepository.com/artifact/com.zaxxer/HikariCP -->
<dependency>
	<groupId>com.zaxxer</groupId>
	<artifactId>HikariCP</artifactId>
	<version>3.4.5</version>	
</dependency>
```



`root-context.xml` 파일 안에 설정은 직접 `<bean>` 태그를 정의해 작성합니다. `<bean>` 태그 내에서는 `<property>` 태그를 이용해 속성을 설정할 수 있습니다.

```xml
	<bean id="hikariConfig" class="com.zaxxer.hikari.HikariConfig">
		<property name="driverClassName"
		value="oracle.jdbc.driver.OracleDriver"></property>
		<property name="jdbcUrl"
		value="jdbc:oracle:thin:@localhost:1521:orcl"></property>
		<property name="username" value="book_ex"></property>
		<property name="password" value="book_ex"></property>
	</bean>
	
	<!-- HikariCP configuration -->
	<bean id="dataSource" class="com.zaxxer.hikari.HikariDataSource"
	destroy-method="close">
	<constructor-arg ref="hikariConfig" />
	</bean>
```

스프링에서 `root-context.xml` 파일은 스프링이 로딩되면서 읽어 들이는 문서이므로, 주로 이미 만들어진 클래스들을 이용해서 스프링의 빈으로 등록할 때 사용됩니다.

일반적으로 프로젝트에 직접 작성하는 클래스들은 어노테이션을 이용하고, 외부 `jar` 파일 등으로 사용하는 클래스들은 `<bean>` 태그를 이용해 작성합니다.



#### 자바 설정

자바 설정을 이용하는 경우에는 `RootConfig` 클래스와 `@Bean` 어노테이션을 이용해 처리합니다. 여기서 `@Bean`은 `XML` 설정에서 `<bean>` 태그와 동일한 역할을 합니다. `@Bean` 이 선언된 메소드의 실행 결과로 반환된 객체는 스프링의 빈`(Bean, 객체)`이 됩니다.

```java
package org.zerock.config;

import javax.sql.DataSource;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ComponentScan(basePackages= {"org.zerock.sample"})
@MapperScan(basePackages = {"org.zerock.mapper"})
public class RootConfig {
	
	@Bean
	public DataSource dataSource() {
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setDriverClassName("oracle.jdbc.driver.OracleDriver");
		hikariConfig.setJdbcUrl("jdbc:oracle:thin:@localhost:1521:orcl");
		hikariConfig.setUsername("book_ex");
		hikariConfig.setPassword("book_ex");
		
		HikariDataSource dataSource = new HikariDataSource(hikariConfig);
		
		return dataSource;
	}
}
```

스프링이 시작되면, `root-context.xml` 을 읽어서 다음과 같은 형태로 `id`가 `dataSource`인 객체가 처리됩니다.

![image-20200810213820218](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbwlAT9%2FbtqGqQDZPIZ%2F32IhWmfgdvaUYD1oapkLaK%2Fimg.png)

스프링에 대한 경험이 적다면 위와 같이 빈을 정의한 다음에 항상 테스트를 작성하는 습관을 가지는 것이 좋습니다. `DataSourceTests` 클래스를 통해 테스트해봅시다.

```java
package org.zerock.persistence;

import static org.junit.Assert.fail;

import java.sql.Connection;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zerock.config.RootConfig;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
// XML 설정을 사용하는 경우
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
// 자바 설정을 사용하는 경우
@ContextConfiguration(classes= {RootConfig.class})
@Log4j
public class DataSourceTests {
	@Setter(onMethod_ = {@Autowired})
	private DataSource dataSource;
	@Test
	public void testConnection() {
		
		try(Connection con = dataSource.getConnection()){
			log.info(con);
		}catch(Exception e) {
			fail(e.getMessage());
		}
	}
}
```

위의 테스트 코드는 스프링에 빈으로 등록된 `DataSource` 를 이용해서 `Connection`을 제대로 처리할 수 있는지를 테스트 하는 코드입니다. `testConnection()` 을 실행해보면 내부적으로 `HikariCP`가 시작, 종료되는 로그를 확인할 수 있습니다.

![image-20200810214224965](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FDDAwF%2FbtqGrBmithr%2FRw2Ae1nWhQmqMIwKnbkz80%2Fimg.png)
