## Sec 3. 영속성 컨테스트

### 영속성 컨텍스트

JPA 에서 가장 중요한 두 가지

- 객체와 관계형 데이터베이스 매핑하기 (Object Relational Mapping)
- **영속성 컨테스트** <- 실제 JPA가 내부에서 어떻게 동작해..?



어플리케이션에서, 하나의 EntityManagerFactory 가 존재하고 사용자의 요청이 들어올 때마다 EntityManager를 생성해서 준다.



### 영속성 컨테스트가 뭐냐??

- **엔티티를 영구 저장하는 환경**

- EntityManager.persist(entity); 

  > 사실 얘는 단순히 디비에 저장하는 역할을 하는게 아니다. 영속성 컨텍스트를 통해서 entity를 영속한다는 의미. 즉, persist()는 디비에 저장하는게 아니라, 영속성 컨텍스트에 저장하는 것.

- 영속성 컨텍스트는 논리적인 개념
- 눈에 보이지 않는다
- EntityManager를 통해서 영속성 컨텍스트에 접근!



### 엔티티의 생명 주기

- 비영속 (new / transient)

  영속성 컨텍스트와 전혀 관계없는 **새로운** 상태 

  ```java
  // 객체를 생성만 한 상태(JPA랑 아무 관련없는 상태)
  Member member = new Member();
  member.setId("member1");
  ```

  

- 영속 (managed)

  영속성 컨텍스트에 **관리**되는 상태

  ```java
  ...
  EntityMeneger em = emf.createEntityManager();
  em.getTransaction().begin();
  
  // 객체를 영속성 컨텍스트에 저장한 상태(영속)
  em.persist(member);
  ```

  Transaction 커밋하는 시점에 영속성 컨텍스트에 있는 애들이 커밋(실제로 쿼리가 날라감)된다.

  

- 준영속

  영속성 컨텍스트에 저장되었다가 **분리**된 상태

  ```java
  // 회원 엔티티를 영속성 컨텍스트에서 분리
  em.detach(member);
  ```

  

  

- 삭제

  **삭제**된 상태

  ```java
  // 객체를 삭제한 상태 - 디비에서 지울래!
  em.remove(member);
  ```

  

### 영속성 컨텍스트의 이점

- **1차 캐시**

  영속성 컨텍스트는 내부에 1차 캐시를 들고 있다. 

  캐시의 key - value 가 각각 @Id - Entity 가 된다...!!!!

  ```java
  // 1차 캐시에 저장됨
  em.persist(member);
  
  // 1차 캐시에서 조회
  Member findMemeber = em.find(Member.class, "member1");
  ```

  1차 캐시에 없는 멤버를 조회하면?

  1. 1차 캐시에 없음
  2. DB 조회
  3. 1차 캐시에 저장
  4. 반환

  영속성 컨텍스트(entityManager, 둘이 완전 동일한 개념은 아니다) 은 보통 데이터베이스 트랜잭션 단위로 만들고, 트랜잭션이 끝나면 close() 한다. 즉, 고객 당 각각의 캐시. 따라서 성능 향상의 효과는 크게 없다.

- **영속 엔티티의 동일성 보장** (== 비교 가능)

  1차 캐시덕분에 반복 가능한 읽기(REPEATABLE READ) 등급의 트랜잭션 격리 수준을 애플리케이션 차원에서 제공해준다.

- 엔티티 등록할 때, **트랜잭션을 지원하는 쓰기 지연**

  커밋하는 순간(`tx.commit();`) 데이터베이스에 SQL 을 보낸다.

  영속성 컨텍스트에는 `쓰기 지연 SQL 저장소`란 공간이 있다. persist() 가 호출되면 이 쓰기 지연 SQL 저장소에 쌓아두고, 커밋하는 시점에 flush가 되면서 쿼리가 날아간다. 그 다음 커밋.

  버퍼링 기능. hibernate 옵션 중에 hibernate.jdbc.batch_size 란 애가 있는데, 요걸 주면 size 만큼 모았다가  한번에 날려준다.

  `<property name="hibernate.jdbc.batch_size" value="10" />`

- 엔티티 수정 **변경 감지 (Dirty Checking)** 

  JPA의 목적이 컬렉션같은 거라고 했지 아까?? List<> 에 있는 값 변경하고 다시 List에 넣지 않는 것처럼 엔티티의 속성이 변경되더라도 em.persist(entity); 호출할 필요 없다. 오히려 하면 맞지 않다! 

  트랜잭션이 커밋되는 시점에 1. flush()가 호출되고, 2. 엔티티의 스냅샷을 비교(읽어온 최초 시점 - 커밋되는 시점) 한 후 3. 변경된 부분을 update SQL 을 쓰기 지연 SQL 저장소에 생성해서 넣고, 그 뒤에 SQL 문을 4. flush 하고 5. 커밋한다.

- **지연 로딩 (Lazy Loading)**



### 플러시

- 영속성 컨텍스트의 변경 내용을 데이터베이스에 반영

#### 플러시 발생하면 생기는 일

- 변경 감지 (Dirty Checking)
- 수정된 엔티티를 쓰기 지연 SQL 저장소에 등록
- 쓰기 지연 SQL 저장소의 쿼리를 데이터베이스에 전송(등록, 수정, 삭제 쿼리)



#### 영속성 컨텍스트를 플러시하는 방법 - 개발자가 직접 쓸 일은 없다

- em.flush() -> 직접 호출
- 트랜잭션 커밋할 때 -> 플러시 자동 호출
- JPQL 실행할 때 -> 플러시 자동 호출

** flush 호출한다고 1차 캐시가 지워지지는 않는다!



#### JPQL 쿼리 실행 시 플러시가 자동으로 호출되는 이유!?

```java
em.persist(memberA);
em.persist(memberB);
em.persist(memberC);
// 이 상태에서는 member들이 영속성 컨텍스트에만 존재한다.

// 중간에 JPQL 실행하면?
query = em.createQuery("select m from Member m", Member.class);
List<Member> members = query.getResultLst();	// DB에서 가져올 애가 없잖아..
```



#### 플러시 모드 옵션

`em.setFlushMode(FlushModeType.COMMIT)`

- FlushModeType.AUTO

  커밋이나 쿼리를 실행할 때 플러시 (default)

- FlushModeType.COMMIT

  커밋할 때만 플러시



#### 결론 - 플러시는 !

- **영속성 컨텍스트를 비우지 않는다**
- 영속성 컨텍스트의 변경 내용을 데이터베이스에 동기화시켜주는 단계
- 트랜잭션이라는 작업 단위가 중요하다 ! -> 데이터베이스 커밋 직전에만 동기화하면 된다~



### 준영속 상태

- 영속 상태의 엔티티가 영속성 컨텍스트에서 분리(detached) 된 상태

- 영속성 컨텍스트가 제공하는 기능을 사용할 수 없다.



#### 준영속 상태로 만드는 방법

- `em.detach(member);`

  특정 엔티티만 준영속 상태로 전환할 때 사용한다.

  즉, 더이상 JPA에서 관리하지 않는다. 사실 직접 쓸 일은 별로 없다..

- `em.clear();`

  영속성 컨테스트 완전히 초기화한다. 테스트 코드짤 때 .. 눈으로 보고 싶을때.. 종종 쓰일 수 있겠다.

- `em.close();`

  영속성 컨테스트 종료한다.

