## 프록시

Member를 DB에서 조회할 때 Team도 함께 조회해야 할까??



**프록시 기초**

- em.find(): DB를 통해서 실제 엔티티 객체 조회
- em.getReference(): DB 조회를 미루는 가짜(프록시) 엔티티 객체 조회
  - DB에 실제 쿼리가 안나감.
  - 엔티티 객체가 실제로 필요한 시점에 쿼리가 나가서 값을 채운다.



**프록시 특징**

- 실제 클래스를 상속받아서 만들어짐
- 실제 클래스와 겉 모양이 같다.
- 사용하는 입장에서는 진짜 객체인지 프록시 객체인지 구분하지 않고 사용하면 된다.(이론상)
- 프록시 객체는 실제 객체의 참조(target)를 보관
- 프록시 객체를 호출하면 프록시 객체는 실제 객체의 메서드를 호출한다.

- 영속성 컨텍스트에 찾는 엔티티가 이미 있으면 em.getReference()를 호출해 도 실제 엔티티 반환된다.

- 영속성 컨텍스트의 도움을 받을 수 없는 준영속 상태일 때, 프록시를 초기화하면 문제 발생



**프록시 객체의 초기화**

1. (프록시객체의) getName() 호출
2. target.getName() 을 하려는데 target이 null이면 영속성 컨텍스트에 초기화 요청
3. DB 요청 및 영속성 컨텍스트에 엔티티를 올림
4. 실제 엔티티(Member) 생성
5. target.getName() 호출하면 실제 엔티티의 값이 호출됨



**프록시 확인**

- 프록시 인스턴스 초기화 여부 확인

  `PersistenceUnitUtil.isLoaded(Object entity)`

- 프록시 클래스 확인 방법

  `entity.getClass().getName()`

- 프록시 강제 초기화

  `org.hibernate.Hibernate.initialize(entity)`



## 즉시 로딩과 지연 로딩

Member를 DB에서 조회할 때 Team도 함께 조회해야 할까???

- Team 정보는 필요없는데, Member를 조회할 때마다 Team도 함께 조회하면 성능상 좋지 않을 수 있음.



**지연 로딩 LAZY를 사용해서 프록시로 조회**

```java
public class Member {

  @Id
  @GeneratedValue
  private Long id;
  @Column(name = "USERNAME")
  private String username;
  private int age;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TEAM_ID")
  private Team team;
}
```

- `fetch = FetchType.LAZY`: TEAM 은 **프록시 객체**로 대체.
- **team 의 값에 접근하는 시점**에 실제로 쿼리가 날아간다.

- 그럼 반대로 Member와 Team 이 자주 함께 사용된다면??



**즉시 로딩 EAGER를 사용해서 함께 조회**

```java
public class Member {

  @Id @GeneratedValue
  private Long id;
  @Column(name = "USERNAME")
  private String username;
  private int age;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "TEAM_ID")
  private Team team;
}
```

- JPA 구현체는 가능하면 조인을 사용해서 하나의 쿼리로 함께 조회한다.



**프록시와 즉시 로딩 주의**

- **가급적 지연 로딩(LAZY) 사용!(특히 실무에서)**
- 즉시 로딩을 적용하면 예상치 못한 SQL이 발생
- 즉시 로딩은 **JPQL에서 N+1 문제가 발생**한다.
  - 해결) JPQL fetch join
- @ManyToOne, @OneToOne은 기본이 즉시 로딩이다. -> LAZY로 설정하자.
- @OneToMany, @ManyToMany는 지연 로딩이 default.



**지연 로딩 활용**

- Member + Team 이 자주 함께 사용 -> 즉시 로딩
- Member + Order 가끔 사용 -> 지연 로딩
- Order + Product 자주 함께 사용 -> 즉시 로딩
- 이론적인 이야기고, **실무에선 모든 연관 관계에 지연 로딩으로 해라~**

- 즉시 로딩은 상상하지 못한 쿼리가 나간다...!



## 영속성 전이(CASCADE)와 고아 객체





## 실전 예제