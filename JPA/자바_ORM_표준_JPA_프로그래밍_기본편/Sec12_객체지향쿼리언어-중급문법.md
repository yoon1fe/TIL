## 경로 표현식

- .(점)을 찍어 객체 그래프를 탐색하는 것

- ``` java
  select m.username -> 상태 필드
   from Member m
   join m.team t -> 단일 값 연관 필드
   join m.orders o -> 컬렉션 값 연관 필드
  where t.name = '팀A'
  ```



**경로 표현식 용어 정리**

- 상태 필드(state field): 단순히 값을 저장하기 위한 필드
  - ex: m.username
- 연관 필드(association field): 연관관계를 위한 필드
  - 단일 값 연관 필드: @ManyToOne, @OneToOne, 대상이 엔티티(ex: m.team)
  - 컬렉션 값 연관 필드: @OneToMany, @ManyToMany, 대상이 컬렉션(ex: m.orders)



**경로 표현식 특징**

- 상태 필드(state field): 경로 탐색의 끝, 탐색X, 끝.
  - JPQL: `select m.username, m.age from Member m`
  - SQL: `select m.username, m.age from Member m`
- 단일 값 연관 경로: 묵시적 **내부 조인(inner join) 발생**, 탐색O, 더 가능
  - JPQL: `select o.member from Order o`
  - SQL: `select m.* from Orders o inner join Member m on o.member_id = m.id`
- 컬렉션 값 연관 경로: 묵시적 내부 조인 발생, **탐색X**
  - 컬렉션으로 들어가게 되어서 탐색 불가능하다. size 정도.. 사용 가능
  - FROM 절에서 **명시적 조인**을 통해 별칭을 얻으면 별칭을 통해 탐색 가능
- 웬만하면 묵시적 내부 조인은 발생되도록 하면 안됨!! 쿼리 튜닝이 상당히 어렵다.



**경로 탐색을 사용한 묵시적 조인 시 주의사항**

- 항상 내부 조인
- 컬렉션은 경로 탐색의 끝. 명시적 조인을 통해 별칭을 얻어야 함.

- 경로 탐색은 주로 SELECT, WHERE 절에서 사용하지만 묵시적 조인으로 인해 SQL의 FROM (JOIN)절에 영향을 줌
- **가급적 묵시적 조인 대신 명시적 조인 사용!**
  - 조인은 SQL 튜닝에 주요한 포인트이기 때문에..
  - 묵시적 조인은 조인이 일어나는 상황을 한 눈에 파악하기 어렵다.



## fetch join

**짱짱 중요하다.**



### 기본

**페치 조인 (fetch join)**

- SQL 조인 종류가 아니다
- JPQL에서 성능 최적화를 위해 제공하는 기능!
- 연관된 엔티티나 컬렉션을 하나의 SQL 로 조회하게 해주는 기능
- join fetch 명령어를 사용
- 페치 조인 ::= [ LEFT [OUTER] | INNER ] JOIN FETCH 조인 경로



**엔티티 페치 조인**

- 회원을 조회하면서 연관된 팀도 함께 조회하고 싶어.(SQL 한 번에)

- SQL을 보면 회원뿐만 아니라 팀(`T.*`)도 함께 SELECT

- JPQL

  ``` java
  select m from Member m join fetch m.team
  ```

- SQL

  ``` sql
  SELECT M.*, T.* FROM MEMBER M
  INNER JOIN TEAM T ON M.TEAM_ID=T.ID
  ```

- FetchType.LAZY 보다 fetch join 이 우선!



**컬렉션 페치 조인**

- 일대다 관계, 컬렉션 페치 조인

- JPQL

  ``` java
  select t
  from Team t join fetch t.members
  where t.name = '팀A'
  ```

- SQL

  ``` sql
  SELECT T.*, M.*
  FROM TEAM T
  INNER JOIN MEMBER M ON T.ID=M.TEAM_ID
  WHERE T.NAME = '팀A'
  ```

- 일대다 조인은 데이터 뻥튀기 가능성 있음.



**페치 조인과 DISTINCT**

- SQL의 DISTINCT 는 중복된 결과를 제거하는 명령

- JPQL의 DISTINCT 는 2가지 기능을 제공한다.

  1. SQL에 DISTINCT 추가

  2. 애플리케이션에서 엔티티 중복 제거. 

     - 단, 기본 DISTINCT는 모든 컬럼의 데이터가 동일해야 중복 제거되므로 애플리케이션 레벨에서 중복 제거함.

       ex) 같은 식별자를 가진 Team 엔티티 제거

- **하이버네이트6부터는 DISTINCT 명령어를 사용하지 않아도 애플리케이션단에서 자동으로 중복 제거가 적용된다!**



**페치 조인과 일반 조인의 차이**

- 일반 조인 실행시 연관된 엔티티를 함께 조회하지 않는다.

- JPQL

  ``` java
  select t
  from Team t join t.members m
  where t.name = 'teamA'
  ```

  - JPQL은 결과를 반환할 때 연관관계 고려하지 않음

  - SELECT 절에 지정한 엔티티만 조회한다.

- SQL

  ``` sql
  SELECT T.*
  FROM TEAM T
  INNER JOIN MEMBER M ON T.ID=M.TEAM_ID
  WHERE T.NAME = 'teamA'
  ```

	- 페치 조인을 사용하면 연관된 엔티티도 함께 조회(즉시 로딩)
	- 페치 조인은 객체 그래프를 SQL 한 번에 조회하는 개념



### 한계

- 페치 조인 대상에는 별칭을 줄 수 없다.
  - 하이버네이트는 가능하지만 가급적 사용하지 마라.
- 둘 이상의 컬렉션은 페치 조인 할 수 없다.
- 컬렉션을 페치 조인하면 페이징 API(setFirstResult, setMaxResults)를 사용할 수 없다.
  - 일대일, 다대일같은 단일 값 연관 필드들은 페치 조인해도 페이징 가능
  - 일대다의 경우) 데이터 뻥튀기 + 페이징해버리면 제대로된 데이터를 갖고 올 수 없다.
  - 하이버네이트는 경고 로그를 남기고 메모리에서 페이징(매우 위험!)

- 연관된 에티티들을 SQL 한 번으로 조회 - 성능 최적화
- 엔티티에 직접 적용하는 글로벌 로딩 전략보다 우선!
  - fetch = FetchType.LAZY 보다 먼저
- 실무에서 글로벌 로딩 전략은 모두 지연 로딩
- 최적화가 필요한 곳은 페치 조인을 적용하면 된다.



**정리**

- 모든 것을 페치 조인으로 해결할 수는 없다!
- 페치 조인은 객체 그래프를 유지할 때 사용하면 효과적이다
- 여러 테이블을 조인해서 엔티티가 가진 모양이 아닌 전혀 다른 결과를 내야 한다면, 페치 조인보다는 일반 조인을 사용하고, 필요한 데이터들만 조회해서 DTO로 반환하는 것이 효과적이다.



## 다형성 쿼리

**TYPE**

- 조회 대상을 특정 자식으로 한정
- 예) Item 중에 Book, Movie 를 조회해라

- **JPQL**

  ``` java
  select i from Item i where type(i) IN (Book, Movie)
  ```

- **SQL**

  ``` sql
  select i from i where i.DTYPE in ('B', M)
  ```



**TREAT**

- 자바의 타입 캐스팅과 유사

- 상속 구조에서 부모 타입을 특정 자식 타입으로 다룰 때 사용

- FROM, WHERE, SELECT(하이버네이트가 지원) 사용

- **JPQL**

  ``` java
  select i from Item i
    where treat(i as Book).auth = 'kim'
  ```

- **SQL**

  ``` sql
  select i.* from Item i
  where i.DTYPE = 'B' and i.auther = 'kim'
  ```



## 엔티티 직접 사용

**기본 키 값**

- JPQL에서 엔티티를 직접 사용하면 SQL에서 해당 엔티티의 기본 키 값을 사용

- **JPQL**

  ``` java
  select count(m.id) from Member m
  select count(m) form Member m
  ```

- **SQL**

  ``` sql
  -- 위에꺼 둘다 동일한 쿼리
  select count(m.id) as cnt from Member m
  ```



## Named 쿼리

- 미리 정의해서 이름을 부여해두고 사용하는 JPQL
- 정적 쿼리만 가능!
- 어노테이션, XML에 정의
  - XML이 우선권을 가진다.
  - XML에 정의하면 phase 등에 따라 다른걸 쓸 수 있겠지.
- 애플리케이션 로딩 시점에 초기화 후 재사용
- **애플리케이션 로딩 시점에 쿼리를 검증**

- Spring Data JPA에서) `@Query(...)` 이게 내부적으로 @NamedQuery를 사용한다!



``` java
@Entity
@NamedQuery(
	name = "Member.findByUsername",
  query = "select m from Member m where m.username = :username"
)
public class Member {
  ...
}

List<Member> resultList = 
  em.createNamedQuery("Member.findByUsername", Member.class)
  .setParameter("username", "won")
  .getResultList();
```



## 벌크 연산

- 재고가 10개 미만인 모든 상품의 가격을 10% 상승하려면?
- JPA 변경 감지 기능으로 실행하려면 너무 많은 SQL 실행 
  1. 재고가 10개 미만인 상품을 리스트로 조회한다.
  2. 상품 엔티티의 가격을 10% 증가한다.
  3. 트랜잭션 커밋 시점에 변경감지가 동작한다.

- 변경된 데이터가 100건이라면 100번의 UPDATE SQL 실행



**예제**

- 쿼리 한 번으로 여러 테이블 로우 변경(엔티티)
- executeUpdate()의 결과는 영향받은 엔티티 수를 반환
- UPDATE, DELETE 지원
- INSERT(insert into .. select, 하이버네이트 지원)

``` java
String qlString = "update Product p " +
 "set p.price = p.price * 1.1 " +
 "where p.stockAmount < :stockAmount";
int resultCount = em.createQuery(qlString)
 .setParameter("stockAmount", 10)
 .executeUpdate(); 
```



**주의!**

- 벌크 연산은 영속성 컨텍스트를 무시하고 DB에 직접 쿼리한다!

- 어떻게 해결하나?

  - 벌크 연산을 먼저 실행

  - 벌크 연산 수행 후 영속성 컨텍스트 초기화
