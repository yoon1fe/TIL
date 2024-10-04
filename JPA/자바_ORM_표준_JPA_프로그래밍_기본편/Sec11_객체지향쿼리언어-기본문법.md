## 소개

**객체지향 쿼리 언어 소개**

JPA는 다양한 쿼리 방법을 지원한다.

- JPQL
- JPA Criteria
- QueryDSL
- 네이티브 SQL
- JDBC API 직접 사용, MyBatis, SpringJdbcTemplate 함께 사용



**JPQL**

- 엔티티를 조회하는 가장 단순한 방법
  - EntityManager.find()
- 나이가 18살인 회원을 모두 검색하고 싶다면??

- JPA를 사용하면 엔티티 객체를 중심으로 개발하게 된다.
- 문제는 검색 쿼리!
- 검색을 할 때도 **테이블이 아닌 엔티티 객체를 대상으로 검색**
- 애플리케이션이 필요한 데이터만 DB에서 불러오려면 결국 검색 조건이 포함된 SQL이 필요함.
- JPA는 SQL을 추상화한 **JPQL**이라는 객체 지향 쿼리 언어 제공
- SQL과 문법이 유사하고, SELECT, FROM, WHERE, GROUP BY, HAVING, JOIN 지원
- JPQL은 엔티티 객체를 대상으로 쿼리
- SQL은 DB 테이블을 대상으로 쿼리

```java
List<Member> result = em.createQuery(
        "select m from Member m where m.username like '%kim%'",
        Member.class
).getResultList();
```



**Criteria**

- JPQL 쿼리는 문자열.. 동적 쿼리 생성하는데 상당히 번거롭다.
- Criteria 를 사용하면 문자가 아닌 자바 코드로 JPQL 작성 가능
- JPQL 빌더 역할
- JPA의 공식 기능임
- **단점: 너무 복잡하고 실용성이 없다;**
- Criteria 대신 QueryDSL 사용을 권장.

```java
//Criteria 사용 준비
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Member> query = cb.createQuery(Member.class);

//루트 클래스 (조회를 시작할 클래스)
Root<Member> m = query.from(Member.class);

//쿼리 생성
CriteriaQuery<Member> cq = query.select(m).where(cb.equal(m.get("username"), "kim"));
List<Member> resultList = em.createQuery(cq).getResultList();
```



**QueryDSL**

```java
//JPQL
//select m from Member m where m.age > 18
JPAFactoryQuery query = new JPAQueryFactory(em);
QMember m = QMember.member;
List<Member> list =
        query.selectFrom(m)
                .where(m.age.gt(18))
                .orderBy(m.name.desc())
                .fetch();
```

- 컴파일 시점에 문법 오류 찾을 수 있음
- 동적 쿼리 작성 편리함~
- 단순하고 쉽다
- 실무 사용 권장!



**네이티브 SQL**

- JPA가 제공하는 SQL을 직접 사용하는 기능
- JPQL로 해결할 수 없는 특정 데이터베이스에 의존적인 기능
- 예) 오라클 CONNECT BY, 특정 DB만 사용하는 SQL 힌트

```java
String sql = "SELECT ID, AGE, TEAM_ID, NAME FROM MEMBER WHERE NAME = ‘kim";
List<Member> resultList = em.createNativeQuery(sql, Member.class).getResultList();
```



**JDBC 직접 사용, SpringJdbcTemplate 등**

- JPA 사용하면서 JDBC 커넥션을 직접 사용하거나, 스프링 JdbcTemplate, MyBatis 등을 함께 사용 가능
- 단, 영속성 컨텍스트를 적절한 시점에 강제로 flush 필요
- 예) JPA를 우회해서 SQL을 실행하기 직전에 영속성 컨텍스트 수동 플러시



## 기본 문법과 쿼리 API







## 프로젝션(SELECT)





## 페이징





## 조인





## 서브 쿼리





## JPQL 타입 표현과 기타식





## 조건식(CASE 등)





## JPQL 함수