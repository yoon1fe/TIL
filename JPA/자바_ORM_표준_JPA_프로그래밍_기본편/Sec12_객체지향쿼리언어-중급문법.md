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





## 다형성 쿼리





## 엔티티 직접 사용





## Named 쿼리





## 벌크 연산





