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

- JPQL은 객체지향 쿼리 언어이다. 따라서 테이블을 대상으로 쿼리하는 것이 아닌, **엔티티 객체를 대상으로 쿼리**한다.
- JPQL은 SQL을 추상화해서 특정 데이터베이스 SQL에 의존하지 않음



**문법**

```
select_문 :: =
 select_절
 from_절
 [where_절]
 [groupby_절]
 [having_절]
 [orderby_절]
update_문 :: = update_절 [where_절]
delete_문 :: = delete_절 [where_절]
```

- `select m from Memberas m where m.age > 18`
- 엔티티와 속성의 대소문자 구분 O - Member, age
- JPQL 키워드는 대소문자 구분 X - SELECT, FROM, where

- 엔티티 이름  사용, 테이블 이름이 아님 - Member
- 별칭은 필수! (m) as는 생략 가능



**TypeQuery, Query**

- TypeQuery: 반환 타입이 명확할 때 사용
- Query: 반환 타입이 명확하지 않을 때 사용



**결과 조회 API**

- query.getResultList(): 결과가 하나 이상일 때
  - 결과가 없으면 빈 리스트 반환
- Query.getSingleResult(): 결과가 정확히 하나
  - 결과가 없으면: NoResultException
  - 둘 이상이면: NonUniqueResultException



**파라미터 바인딩 - 이름 기준, 위치기준**

``` java
SELECT m FROM Member m where m.username=:username
query.setParameter("username", usernameParam);

SELECT m FROM Member m where m.username=?1
query.setParameter(1, usernameParam);	// 위치 기반은 쓰지 말자~
```



## 프로젝션(SELECT)

- SELECT 절에 조회할 대상을 지정하는 것
- 프로젝션 대상: 엔티티, 임베디드 타입, 스칼라 타입(숫자, 문자등 기본 데이터 타입)
- SELECT **m** FROM Member m -> 엔티티 프로젝션 
- SELECT **m.team** FROM Member m -> 엔티티 프로젝션
  - JPQL은 쿼리처럼 쓰자. join 명시해주는 것이 좋다.
- SELECT **m.address** FROM Member m -> 임베디드 타입 프로젝션
- SELECT **m.username, m.age** FROM Member m -> 스칼라 타입 프로젝션
- DISTINCT로 중복 제거



**여러 값 조회**

- `SELECT m.username, m.age FROM Member m`
  1. Query 타입으로 조회 
  2. Object[] 타입으로 조회
  3. new 명령어로 조회 
     - 단순 값을 DTO로 바로 조회 
     - `SELECT new jpabook.jpql.MemberDTO(m.username, m.age) FROM Member m`
     - 패키지 명을 포함한 전체 클래스 명 입력
     - 순서와 타입이 일치하는 생성자 필요



## 페이징

- JPA는 페이징을 다음 두 API로 페이징을 추상화
- setFirstResult(int startPosition): 조회 시작 위치 (0부터 시작)
- setMaxResults(int maxResult): 조회할 데이터 수

``` java
String jpql = "select m from Member m order by m.name desc";
List<Member> resultList = em.createQuery(jpql, Member.class)
        .setFirstResult(10)
        .setMaxResults(20)
        .getResultList();
```



## 조인

- 내부 조인: `SELECT m FROM Member m [INNER] JOIN m.team t`
- 외부 조인: `SELECT m FROM Member m LEFT [OUTER] JOIN m.team t`
- 세타 조인: `select count(m) from Member m, Team t where m.username = t.name`



**ON 절**

- ON 절을 활용한 조인(JPA 2.1부터 지원)

  1. 조인 대상 필터링

     예) 회원과 팀을 조인하면서, 팀 이름이 A인 팀만 조인

  2. 연관관계 없는 엔티티 외부 조인

     예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인



## 서브 쿼리

- `select m from Member m where m.age > (select avg(m2.age) from Member m2)`

- `select m from Member m where (select count(o) from Order o where m = o.member) > 0`
  - 서브쿼리에 m 땡겨오면 성능 저하 있음



**지원 함수**

- [NOT] EXISTS (subquery): 서브쿼리에 결과가 존재하면 참
- {ALL | ANY | SOME} (subquery)
- ALL 모두 만족하면 참
- ANY, SOME: 같은 의미, 조건을 하나라도 만족하면 참
- [NOT] IN (subquery): 서브쿼리의 결과 중 하나라도 같은 것이 있으면 참



**JPA 서브 쿼리 한계**

- JPA는 WHERE, HAVING 절에서만 서브 쿼리 사용 가능 + 하이버네이트의 지원으로 SELECT 까지
- FROM 절의 서브 쿼리는 현재 JPQL에서 불가능
  - 조인으로 풀 수 있으면 풀어서 해결하자



## JPQL 타입 표현과 기타식

- 문자: ‘HELLO’, ‘She’’s’
- 숫자: 10L(Long), 10D(Double), 10F(Float)
- Boolean: TRUE, FALSE
- ENUM: jpabook.MemberType.Admin (**패키지명 포함해야 함!**)
- 엔티티 타입: TYPE(m) = Member (상속 관계에서 사용)



## 조건식(CASE 등)

- 기본 CASE 식

  ``` java
  select
   case when m.age <= 10 then '학생요금'
   when m.age >= 60 then '경로요금'
   else '일반요금'
   end
  from Member m
  ```

- 단순 CASE 식

  ``` java
  select
   case t.name
   when '팀A' then '인센티브110%'
   when '팀B' then '인센티브120%'
   else '인센티브105%'
   end
  from Team t
  ```

- COALESCE: 하나씩 조회해서 null이 아니면 반환
- NULLIF: 두 값이 같으면 null 반환, 다르면 첫번째 값 반환



## JPQL 함수

**기본 함수**

- CONCAT
- SUBSTRING
- TRIM
- LOWER, UPPER
- LENGTH
- LOCATE
- ABS, SQRT, MOD
- SIZE, INDEX(JPA 용도)



**사용자 정의 함수  호출**

- 하이버네이트는 사용전 방언에 추가해야 한다.
  - 사용하는 DB 방언을 상속받고, 사용자 정의 함수를 등록
- `select function('group_concat', i.name) from Item i`