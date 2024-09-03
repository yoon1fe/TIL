## Hello JPA 프로젝트

**pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>jpa-basic</groupId>
  <artifactId>ex1-hello-jpa</artifactId>
  <version>1.0-SNAPSHOT</version>

  <properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencies>
    <!-- JPA 하이버네이트 -->
    <dependency>
      <groupId>org.hibernate</groupId>
      <artifactId>hibernate-entitymanager</artifactId>
      <version>5.6.15.Final</version>
    </dependency>

    <!-- H2 데이터베이스 -->
    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
      <version>2.3.230</version>
    </dependency>
  </dependencies>
</project>
```





### JPA 설정하기

**persistence.xml** - JPA 설정 파일

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.2"
  xmlns="http://xmlns.jcp.org/xml/ns/persistence" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd">
  <persistence-unit name="hello">
    <properties>
      <!-- 필수 속성 -->
      <property name="javax.persistence.jdbc.driver" value="org.h2.Driver"/>
      <property name="javax.persistence.jdbc.user" value="sa"/>
      <property name="javax.persistence.jdbc.password" value=""/>
      <property name="javax.persistence.jdbc.url" value="jdbc:h2:tcp://localhost/~/test"/>
      <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>

      <!-- 옵션 -->
      <property name="hibernate.show_sql" value="true"/>
      <property name="hibernate.format_sql" value="true"/>
      <property name="hibernate.use_sql_comments" value="true"/>
      <!--<property name="hibernate.hbm2ddl.auto" value="create" />-->
    </properties>
  </persistence-unit>
</persistence> 
```



**데이터베이스 방언**

- JPA는 특정 DB에 종속적이지 않다
- 각각의 DB가 제공하는 문법과 함수가 조금씩 다름
- 방언(dialect): SQL 표준을 지키지 않는 특정 DB만의 고유한 기능
- 하이버네이트는 40개 이상의 방언 지원



**JPA 구동 방식**

1. persistence.xml 설정 정보 조회해서 Persistence 클래스 생성
2. Persistence 클래스가 EntityManagerFactory 클래스 생성
3. EntityManagerFactory 클래스가 EntityManager 생성



**객체, 테이블 생성/매핑**

``` sql
create table Member (
 id bigint not null,
 name varchar(255),
 primary key (id)
);
```



```java
package hellojpa;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Member {

  @Id
  private Long id;
  private String name;

}
```



```java
public class JpaMain {

  public static void main(String[] args) {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
    EntityManager em = emf.createEntityManager();

    EntityTransaction tx = em.getTransaction();
    tx.begin();

    Member member = new Member();
    member.setId(1L);
    member.setName("Jack");

    em.persist(member);
    tx.commit();

    em.close();
    emf.close();
  }
}
```

- insert: `em.persist()`
- select: `em.find()`
- delete: `em.remove()`
- update: `presist()` 안해도 됨. 자바 컬렉션 다루는 것처럼 설계됐기 때문에 객체만 수정하면 update 쿼리가 실행된다! JPA가 커밋하는 시점에 해당 객체가 수정되었는지 체크한다.



**주의**

- EntityManagerFactory는 하나만 생성해서 애플리케이션 전체에서 공유.
- EntityManager는 스레드간에 공유X(사용하고 버려야 한다.)
- **JPA의 모든 데이터 변경은 트랜잭션 안에서 실행**



### JPQL

- JPA는 SQL을 추상화한 JPQL이라는 객체 지향 쿼리 언어를 제공
- SQL과 문법 유사, SELECT, FROM, WHERE, GROUP BY, HAVING, JOIN 지원
- JPQL은 **엔티티 객체**를 대상으로 쿼리
  - SQL은 데이터베이스 테이블을 대상으로 쿼리

- SQL을 추상화해서 특정 데이터베이스 SQL에 의존X



```java
List<Member> result = em.createQuery("select m from Member", Member.class).getResultList();
```

- `Member` 객체를 대상으로 쿼리.