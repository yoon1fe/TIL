### MyBatis와 스프링 연동

`MyBatis`란 흔히 `SQL Mapping 프레임워크`로 분류되는데, 개발자들은 `JDBC` 코드의 복잡하고 지루한 작업을 피하는 용도로 많이 사용합니다. 전통적인 `JDBC` 프로그래밍의 구조와 비교해보면 다음과 같은 `MyBatis` 만의 장점을 파악할 수 있습니다.



| 전통적인 JDBC 프로그래밍                                     | MyBatis                                                      |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| 직접 Connection을 맺고 마지막에 close()<br />PreparedStatement 직접 생성 및 처리<br />PreparedStatement의 setXXX() 등에 대한 모든<br />작업을 개발자가 처리<br />SELECT의 경우 직접 ResultSet 처리 | 자동으로 Connection close() 기능<br />MyBatis 내부적으로 PreparedStatement 처리<br />#{prop}와 같이 속성을 지정하면 내부적으로 자동 처리<br />리턴 타입을 지정하는 경우 자동으로 객체 생성 및 ResultSet 처리 |



`MyBatis`는 기존의 `SQL` 을 그대로 활용할 수 있고, 진입장벽이 낮은 편이라 `JDBC`의 대안으로 많이 사용됩니다.

스프링 프레임워크의 특징 중 하나는 다른 프레임워크들을 배척하지 않고 다른 프레임워크들과의 연동을 쉽게 하는 추가적인 라이브러리들이 많다는 것입니다. `MyBatis` 역시 `mybatis-spring` 이라는 라이브러리를 통해 쉽게 연동 작업을 처리할 수 있습니다.

![image-20200810214909558](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbYEM9q%2FbtqGsMA84Ro%2Fe5o5cIh6dKftaGdY8G0pyK%2Fimg.png)



#### MyBatis 관련 라이브러리 추가

`pom.xml` 파일에 다음과 같은 라이브러리들을 추가합니다.

- `spring-jdbc/spring-tx` - 스프링에서 데이터베이스 처리와 트랜잭션 처리(해당 라이브러리들은 `MyBatis`와 무관하게 보이지만 추가하지 않는 경우에는 에러가 발생합니다.)
- `mybatis/mybatis-spring` - `MyBatis`와 스프링 연동용 라이브러리

```xml
		<!-- https://mvnrepository.com/artifact/org.mybatis/mybatis -->
		<dependency>
			<groupId>org.mybatis</groupId>
			<artifactId>mybatis</artifactId>
			<version>3.4.6</version>
		</dependency>
		<!-- https://mvnrepository.com/artifact/org.mybatis/mybatis-spring -->
		<dependency>
			<groupId>org.mybatis</groupId>
			<artifactId>mybatis-spring</artifactId>
			<version>1.3.2</version>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-tx</artifactId>
			<version>${org.springframework-version}</version>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-jdbc</artifactId>
			<version>${org.springframework-version}</version>
		</dependency>
```



#### SQLSessionFactory

`MyBatis`에서 가장 핵심적인 객체는 `SQLSession`이라는 존재와 `SQLSessionFactory`입니다. `SQLSessionFactory`라는 이름에서 알 수 있듯이, 내부적으로 `SQLSession`이라는 것을 만들어 주는 존재인데, 개발에서는 `SQLSession`을 통해 `Connection`을 생성하거나 원하는 `SQL`을 전달하고, 결과를 리턴 받는 구조로 작성하게 됩니다.



`root-context.xml` 파일을 다음과 같이 작성합니다.

```xml
	<bean id="sqlSessoinFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
	<property name="dataSource" ref="dataSource"></property>
	</bean>
```

스프링에 `SqlSessionFactory`를 등록하는 작업은 `SqlSessionFactoryBean`을 이용합니다.



#### 자바 설정

마찬가지로 `RootConfig` 클래스에 `@Bean`을 이용해서 설정해 줍니다.

```java
	@Bean
	public SqlSessionFactory sqlSessionFactory() throws Exception {
		SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
		sqlSessionFactory.setDataSource(dataSource());
		return (SqlSessionFactory) sqlSessionFactory.getObject();
	}
```



`DataSourceTests` 클래스에 `SqlSessionFactoryBean`을 이용해 `SqlSession`을 사용해보는 코드를 추가해봅시다.

```java
	@Setter(onMethod_ = {@Autowired})
	private SqlSessionFactory sqlSessionFactory;
	@Test
	public void testMyBatis() {
		try(SqlSession session = sqlSessionFactory.openSession();
				Connection con = session.getConnection();
				){
			log.info(session);
			log.info(con);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
```

![image-20200810215636921](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FblTfYN%2FbtqGqQDZSow%2FdJneBK48byh24kEmN7ZGA0%2Fimg.png)





### 스프링과 연동 처리

`MyBatis`의 `Mapper`라는 친구는 `SQL`을 어떻게 처리할 것인지를 설정을 따로 두고 자동으로 처리하는 방식을 사용할 수 있습니다.

`Mapper`는 `SQL`과 그에 대한 처리를 지정하는 역할을 합니다. `MyBatis-Spring`을 이용하는 경우에는 `Mapper`를 `XML`과 인터페이스 + 어노테이션 형태로 작성할 수 있습니다.



`TimeMapper`라는 인터페이스를 만들고, `MyBatis`의 어노테이션을 이용해서 `SQL` 을 메소드에 추가합니다.

```java
package org.zerock.mapper;

import org.apache.ibatis.annotations.Select;

public interface TimeMapper {
	@Select("SELECT sysdate FROM dual")
	public String getTime();
	
	public String getTime2();
}
```



#### Mapper 설정

`Mapper`를 작성했다면 `MyBatis`가 동작할 때 `Mapper`를 인식할 수 있도록 `root-context.xml` 파일에 추가적인 설정이 필요합니다. 가장 간단한 방법은 `<mybatis:scan>`이라는 태그를 이용하는 것입니다.

```xml
	<mybatis-spring:scan base-package="org.zerock.mapper"/>
```

이렇게 하면 지정된 패키지의 모든 `MyBatis` 관련 어노테이션을 찾아서 처리합니다.



#### 자바 설정

`RootConfig` 클래스 선언부에 `mybatis-spring`에서 사용하는 `@MapperScan` 어노테이션을 사용하여 처리합니다.

```java
@MapperScan(basePackages = {"org.zerock.mapper"})
```



#### Mapper 테스트

`MyBatis-Spring`은 `Mapper` 인터페이스를 이용해서 실제 `SQL`이 처리되는 클래스를 자동으로 생성합니다. 따라서 개발자들은 인터페이스와 `SQL`문 만 작성해도 모든 `JDBC` 처리를 할 수 있습니다.

`TimeMapperTests` 클래스를 만들어서 처리합시다.

```java
package org.zerock.persistence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zerock.mapper.TimeMapper;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Log4j
public class TimeMapperTests {
	@Setter(onMethod_ = @Autowired)
	private TimeMapper timeMapper;
	
	@Test
	public void testGetTime() {
		log.info(timeMapper.getClass().getName());
		log.info(timeMapper.getTime());
	}
}
```

이 때 실행해보니 시간이 안뜹니다.

![image-20200810221048760](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2F16ezc%2FbtqGuFVkDs8%2FRrKzjirk3kKAimdcN5m6u0%2Fimg.png)



`Failure Trace`를 보니 

**지원되지 않는 문자 집합(클래스 경로에 orai18n.jar 추가): KO16MSWIN949** 라는 게 뜹니다. 

이게 뭔고 하니 `sysdata`가 타입이 `NVARCHAR` 라서 그런 것 같습니다.. 정확하겐 모르겠습니다...

암튼 `orai18n.jar` 는 다국어 지원을 위한 `GDK(Globalization Development Kit)`이라고 합니다.. 얘를 추가해주니 잘 됩니다.

![image-20200810221343013](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FJyRzO%2FbtqGvdYzNOX%2FgMwLblp3ujsqsNgQ4qATb1%2Fimg.png)





#### XML 매퍼와 같이 쓰기

`MyBatis-Spring`은 `Mapper`인터페이스와 `XML`을 동시에 이용할 수 있습니다.

`SQL` 문을 처리할 때 어노테이션을 이용하는 방식이 압도적으로 편리하지만, `SQL` 문이 너무 복잡하거나 길어지는 경우 `XML`을 이용하는 방식을 더 선호하게 됩니다.



`XML`을 작성해서 사용할 때는 `XML` 파일의 위치와 `XML` 파일에 지정하는 `namespace` 속성이 중요한데, `XML` 파일 위치의 경우 `Mapper` 인터페이스가 있는 곳에 같이 작성하거나, `src/main/resources` 밑에 `XML`을 저장할 폴더를 만들어서 작성할 수 있습니다. 이 때, 가능하면 `Mapper` 인터페이스와 같은 이름을 이용해 가독성을 높여줍시다.



`org > zerock > mapper `폴더를 각각 만들고 이 안에 `TimeMapper.xml` 파일을 생성해 줍시다.

`Mapper` 인터페이스와 `XML` 파일을 같이 이용하기 위해 `TimeMapper` 인터페이스에 추가적인 메소드를 선언합시다.

```java
package org.zerock.mapper;

import org.apache.ibatis.annotations.Select;

public interface TimeMapper {
	@Select("SELECT sysdate FROM dual")
	public String getTime();
	
	public String getTime2();
}

```

새로 만든 `getTime2()` 위에는 아무런 어노테이션도 없고, `SQL` 문도 없습니다. `XML`을 이용해서 `SQL`문을 작성하고 처리해 봅시다.



```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.zerock.mapper.TimeMapper">

  <select id="getTime2" resultType="string">
  SELECT sysdate FROM dual 
  </select>

</mapper>
```

`<mapper>` 태그의 `namespace` 속성 값을 신경써야 합니다. `MyBatis`는 `Mapper` 인터페이스와 `XML`을 인터페이스의 이름과 `namespace` 속성 값을 바탕으로 판단합니다. 

위와 같이 `org.zerock.mapper.TimeMapper` 인터페이스가 존재하고, `namespace` 값에 `"org.zerock.mapper.TimeMapper"` 와 같이 동일한 이름이 존재한다면, 이를 병합해서 처리합니다.

`<select>` 태그의 `id` 값은 메소드의 이름과 동일해야 합니다. `<select>` 태그의 경우, `resultType`이란 속성을 가지는데, 이 값은 인터페이스에 선언된 메소드의 리턴 타입과 동일해야 합니다.



테스트를 위해 `TimeMapperTests` 클래스에 다음과 같은 코드를 추가하고 테스트 해봅시다.

```java
	@Test
	public void testGetTime2() {
		log.info("getTime2");
		log.info(timeMapper.getTime2());
	}
```

![image-20200810222651506](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fbr4CyK%2FbtqGvcZFF2Y%2FXnhdkS2B1Uo57CijvuklrK%2Fimg.png)

잘 나옵니당.







### log4jdbc-log4j2 설정

`MyBatis`는 내부적으로 `JDBC`의 `PreparedStatement`를 이용해서 `SQL` 문을 처리합니다. 따라서 `SQL`에 전달되는 파라미터는 `JDBC`와 같이 `'?'`로 치환되어 처리됩니다. 복잡한 `SQL`문의 경우 '?'로 나오는 값이 제대로 되었는지 확인하기 어렵기 때문에 이 녀석이 어떤 값으로 처리되었는지 확인하는 기능을 추가하도록 합니다.

`SQL` 로그를 제대로 보기 위해서는 `log4jdbc-log4j2` 라이브러리를 사용합니다.



`pom.xml`에 라이브러리를 추가합니다.

```xml
		<!-- https://mvnrepository.com/artifact/org.bgee.log4jdbc-log4j2/log4jdbc-log4j2-jdbc4 -->
		<dependency>
			<groupId>org.bgee.log4jdbc-log4j2</groupId>
			<artifactId>log4jdbc-log4j2-jdbc4</artifactId>
			<version>1.16</version>
		</dependency>
```

이 다음에는

1. 로그 설정 파일을 추가
2. `JDBC`의 연결 정보를 수정

해야 합니다.



우선 `src/main/resource` 밑에 `log4jdbc.log4j2.properties` 파일을 만들어 줍니다.

```properties
log4jdbc.spylogdelegator.name=net.sf.log4jdbc.log.slf4j.Slf4jSpyLogDelegator
```



`log4jdbc`를 사용하는 경우에는 `JDBC` 드라이버와 `URL` 정보를 수정해야 합니다. `root-context.xml` 파일에 있는 정보를 수정해 줍시다.

```xml
	<bean id="hikariConfig" class="com.zaxxer.hikari.HikariConfig">
		<property name="driverClassName"
		value="net.sf.log4jdbc.sql.jdbcapi.DriverSpy"></property>
		<property name="jdbcUrl"
		value="jdbc:log4jdbc:oracle:thin:@localhost:1521:orcl"></property> 
		<property name="username" value="book_ex"></property>
		<property name="password" value="book_ex"></property>
	</bean>
```

변경한 후 기존 테스트 코드를 실행해 보면 이전과 달리 `JDBC` 관련 로그들이 출력됩니다.

![image-20200810223339348](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FdkQseQ%2FbtqGsoUAMMQ%2FgAwWRq8yFwKP63SKIoHbK1%2Fimg.png)



#### 자바 설정

위와 같은 내용을 `RootConfig` 클래스에 반영해 줍니다.

```java
	@Bean
	public DataSource dataSource() {
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setDriverClassName("net.sf.log4jdbc.sql.jdbcapi.DriverSpy");
		hikariConfig.setJdbcUrl("jdbc:log4jdbc:oracle:thin:@localhost:1521:orcl");
		hikariConfig.setUsername("book_ex");
		hikariConfig.setPassword("book_ex");
		
		HikariDataSource dataSource = new HikariDataSource(hikariConfig);
		
		return dataSource;
	}
```





#### 로그의 레벨 설정

테스트 코드를 실행해보니 로그가 상당히 많이 출력됩니다. 로그가 쓸데없이 너무 많이 나오면 로그의 레벨을 이용해서 조금 수정해주면 됩니다.



테스트 코드가 실행될 때의 로그와 관련된 설정은 `src/test/resources` 밑에 `log4j.xml` 파일을 이용합니다.

테스트 코드가 실행될 때 나오는 `"INFO ~~ "` 메시지는 `log4j.xml`의 마지막 부분에 있는 설정의 영향을 받습니다.

```xml
	<!-- Root Logger -->
	<root>
		<priority value="info" />
		<appender-ref ref="console" />
	</root>
```



만약 `log4jdbc`에서 출력되는 로그를 조절하고 싶으면 추가적인 `<logger>` 를 지정해서 처리하면 됩니다.

