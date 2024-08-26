## 단방향 연관관계

**목표**

- 객체와 테이블 연관관계의 차이 이해

- 객체의 참조와 테이블의 외래키 매핑
- 용어
  - 방향: 단방향, 양방향
  - 다중성: 다대일, 일대다, 일대일, 다대다..
  - 연관관계의 주인: 객체 양방향 연관관계는 관리 주인이 필요!



**연관관계가 필요한 이유**

- 객체지향 설계의 목표는 자율적인 객체들의 **협력 공동체**를 만드는 것.



**예제**

- 회원 : 팀 = 다대일



객체를 테이블에 맞춰서 데이터 중심으로 모델링하면, 협력 관계를 만들 수 없다.

- 테이블은 외래키로 조인을 사용해서 연관된 테이블 찾음
- 객체는 참조를 통해 연관된 객체 찾음



**단방향 연관관계**

Member.java

```java
@Entity
public class Member {

  @Id
  @GeneratedValue
  private Long id;
  @Column(name = "USERNAME")
  private String name;
  private int age;
  // @Column(name = "TEAM_ID")
// private Long teamId;
  @ManyToOne // Member Many - Team One
  @JoinColumn(name = "TEAM_ID")
  private Team team;
}
```



``` java
 //팀 저장
 Team team = new Team();
 team.setName("TeamA");
 em.persist(team);
 //회원 저장
 Member member = new Member();
 member.setName("member1");
 member.setTeam(team); //단방향 연관관계 설정, 참조 저장
 em.persist(member);

 //조회
 Member findMember = em.find(Member.class, member.getId());
//참조를 사용해서 연관관계 조회
 Team findTeam = findMember.getTeam();
```



## 양방향 연관관계와 연관관계의 주인

### 기본

Member <-> Team : 양방향 연관관계.

테이블의 연관관계에는 방향이란 개념없다. 외래키만 있으면 됨.



```java
package hellojpa;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Team {

  @Id @GeneratedValue
  @Column(name = "TEAM_ID")
  private Long id;
  private String name;

  @OneToMany(mappedBy = "team")	// Team 으로 매핑된 멤버들이야~
  private List<Member> members = new ArrayList<>();

}
```



양방향 매핑

```javs
Member findMember = em.find(Member.class, member.getId());
List<Member> members = findMember.getTeam().getMembers();
```

객체는 가급적이면 단방향이 좋다..ㅎ



**연관관계의 주인과 mappedBy**

- mappedBy 많이 헷갈림.
  - 객체와 테이블간에 연관관계를 맺는 차이를 이해해야 한다.
- 객체 연관관계: 2개
  - 회원 -> 팀 (단방향)
  - 팀 -> 회원 (단방향)
- 테이블 연관관계: 1개
  - 회원 <-> 팀 (양방향)

- **객체의 양방향 관계**
  - 객체의 양방향 관계는 사실 **서로 다른 단방향 관계 2개이다.**

- 테이블의 양방향 연관관계
  - 테이블은 외래키 하나로 두 테이블의 연관관계를 관리한다.
  - member.team_id 로 양방향 연관관계를 가짐.

- 그럼 객체 패러다임엥서는 둘 중 하나로 외래 키를 관리해야 한다!



**연관관계의 주인**

양방향 매핑 규칙

- 객체의 두 관계중 하나를 연관관계의 주인으로 지정
- **연관관계의 주인만이 외래 키를 관리(등록, 수정)**
- **주인이 아닌쪽은 읽기만 가능**
- 주인은 mappedBy 속성 사용X
- 주인이 아니면 mappedBy 속성으로 주인 지정



**누구를 주인으로 ??**

- **외래 키가 있는 곳을 주인으로!**
- **Member.team** 이 연관관계의 주인
- List members 는 읽기만..



### 주의점, 정리

**양방향 매핑시 가장 많이 하는 실수**

- 연관관계의 주인에 값을 입력하지 않는 케이스

```java
Member member = new Member();
member.setUsername("member1");
em.persist(member);

Team team = new Team();
team.setName("TeamA");

//역방향(주인이 아닌 방향)만 연관관계 설정
team.getMembers().add(member);
em.persist(team);
```

- Team 이 연관관계의 주인이다. team.members는 읽기 전용!(가짜 매핑)!
- `Member.setTeam(team);` 이렇게 해야 한다~
- 순수한 객체 관계를 고려한다면 항상 양쪽 다 값을 입력해주어야 한다.

- 연관관계 편의 메서드를 생성하자.

  ```java
  public void addMember(Member member) {
    member.setTeam(this);
    members.add(member);
  }
  ```

  - 강사 tip) Set 대신 change~~ 컨벤션 추천!

- 양방향 매핑시 무한 루프 조심!
  - toString(), lombok, JSON 생성 라이브러리 등..
  - 컨트롤러단에서는 절대 Entity 반환하지 말 것!



**양방향 매핑 정리**

- **단방향 매핑만으로도 이미 연관관계 매핑은 완료**
- 양방향 매핑은 반대 방향으로 조회(객체 그래프 탐색) 기능이 추가된 것 뿐
- JPQL에서 역방향으로 탐색할 일이 생각보다 많음
- 단방향 매핑을 잘 하고 양방향은 필요할 때 추가해도 된다! (테이블에 영향을 주지 않음)



**연관관계의 주인을 정하는 기준**

- 비즈니스 로직을 기준으로 연관관계의 주인을 선택하면 안됨
- **연관관계의 주인은 외래 키의 위치를 기준으로 정하면 된다~**



## 실전 예제 2 - 연관관계 매핑 시작

**Order.java**

```java
@Entity
@Table(name = "ORDERS")
public class Order {

  @Id @GeneratedValue
  @Column(name = "ORDER_ID")
  private Long id;

//  @Column(name = "MEMBER_ID")
//  private Long memberId;

  @ManyToOne
  @JoinColumn(name = "MEMBER_ID")
  private Member member;


  private LocalDateTime orderDate;

  @Enumerated(EnumType.STRING)
  private OrderStatus status;
}
```



**OrderItem.java**

```java
@Entity
public class OrderItem {

  @Id @GeneratedValue
  @Column(name = "ORDER_ITEM_ID")
  private Long id;

//  @Column(name = "ORDER_ID")
//  private Long orderId;

  @ManyToOne
  @JoinColumn(name = "ORDER_ID")
  private Order order;

//  @Column(name = "ITEM_ID")
//  private Long itemId;


  @ManyToOne
  @JoinColumn(name = "ITEM_ID")
  private Item item;

  private int orderPrice;
  private int count;
}
```
