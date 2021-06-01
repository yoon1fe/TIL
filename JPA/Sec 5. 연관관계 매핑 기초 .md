## Sec 5. 연관관계 매핑 기초

- 연관관계를 이쁘게 매핑해서 좀더 객체지향적으로 만들어보자!

RDB의 패러다임 != 객체지향의 패러다임에서 오는 문제 해결이 목표!!



### 목표

- 객체와 테이블 연관 관계의 차이를 이해
- **객체의 참조와 테이블의 외래키를 매핑**
- 용어 이해
  - **방향**: 단방향, 양방향
  - **다중성**: 다대일(N:1), 일대다(1:N), 일대일(1:1), 다대다(N:M) 이해
  - **연관관계의 주인**(owner): JPA 계의 포인터.... 객체 양방향 연관관계는 관리 주체가 필요하다



### 예제 시나리오

- 회원과 팀이 있다
- 회원은 하나의 팀에만 소속될 수 있다
- 회원과 팀은 다대일(N:1) 관계이다



### 객체를 테이블에 맞추어 모델링 

참조 대신에 외래 키를 그대로 사용

``` java
@Entity
public class Member {
  @Id @GeneratedValue
  private Long id;
  
  @Column(name = "USERNAME")
  private String name;
  
  @Column(name = "TEAM_ID")
  private Long teamId;
  ...
}

@Entity
public class Team {
  @Id @GeneratedValue
  private Long id;
  private String name;
  ...
}

```

Member 객체의 멤버변수에 teamId(외래 키)가 고대로 들어가도록 설계.

외래 키 식별자를 직접 다루어야 한다.

``` java
// 팀 저장
Team team = new Team();
team.setName("TEAM_A");
em.persist(team);
// 회원 저장
Member member = new Member();
member.setName("member1");
member.setTeamId(team.getId()); // 짜친다
em.persist(member);

// 조회
Member findMember = em.find(Member.class, member.getId());

// 연관관계가 없음
Team findTeam = em.find(Team.class, team.getId());
```



#### 객체를 테이블에 맞추어 데이터 중심으로 모델링하면, 협력 관계를 만들 수 없다!

- 테이블은 외 키로 조인을 사용해서 연관된 테이블을 찾는다
- 객체는 참조를 사용해서 연관된 객체를 찾는다 
- 테이블과 객체 사이에는 이런 클 간격이 있다.



### 단방향 연관관계 - 가장 기본적인!

### 객체 지향 모델링 (객체 연관관계 사용)

``` java
@Entity
public class Member {
  @Id @GeneratedValue
  @Column(name = "MEMBER_ID")
  private Long id;
  
  @Column(name = "USERNAME")
  private String name;
  
  @ManyToOne // 멤버 many 팀 one, fetch="FetchType.EAGER" 가 디폴트
  @JoinColumn(name = "TEAM_ID") // 조인해야 하는 칼럼 이름
  private Team team;
  ...
}

@Entity
public class Team {
  @Id @GeneratedValue
  private Long id;
  private String name;
  ...
}
```



``` java
// 팀 저장
Team team = new Team();
team.setName("teamA");
em.persist(team);

// 회원 저장
Member member = new Member();
member.setUsername("member1");
member.setTeam(team); // 단방향 연관관계 설정, 참조 저장
em.persist(member);

// 회원 조회
Member findMember = em.find(Member.class, member.getId());

// 팀 조회 - getTeam() 으로 깔끔하게
Team findTeam = findMember.getTeam();
System.out.println("findTeam : " + findTeam.getName());

// 새로운 팀B
Team teamB = new Team();
teamB.setName("TeamB");
em.persist(teamB);

// 회원1에 새로운 팀B 설정
member.setTeam(teamB);
```



### 양방향 연관관계와 연관관계의 주인

단방향 매핑 -> 양방향 매핑

위에서는 member -> team 으로는 가능했다. 그럼 team -> member 는?

```java
// Team.java

private Long id;
private String name;

@OneToMany(mappedBy = "team")	// Team 1 : N Member
//mappedBy : Member의 멤버 변수 "team"과 매핑되어 있단 의미
List<Member> members = new ArrayList<>();
```

테이블 연관관계는 바뀌는 게 없다. 왜냐? `team_id`로 조인하면 되니깐!!

- 관례적으로 초기화해두면 좋다. add 할 때 NPE 뜰 일 없도록!



``` java
// 조회
Member findMember = em.find(Member.class, member.getId());
List<Member> members = findMember.getTeam().getMembers();
```

사실 객체는 가급적 단방향이 좋다.. 양방향이면 신경쓸 게 너무 많아





### 연관관계의 주인과 mappedBy

- mappedBy == JPA 계의 진정한 포인터..! 처음에 이해하기 몹시 어렵다.

- 객체와 테이블 간의 연관관계를 맺는 차이를 이해해야 한다.

#### 객체와 테이블이 관계를 맺는 차이

- **객체** 연관관계: 2개
  - 회원 -> 팀 연관관계 1개 (단방향)
  - 팀 -> 회원 연관관계 1개 (단방향)
- **테이블** 연관관계: 1개
  - 회원 <-> 팀의 연관관계 1개 (양방향)



#### 객체에서의 양방향 관계는 사실 양방향 관계가 아닌, 서로 다른 단방향 관계 두 개이다.

- 객체를 양방향으로 참조하려면 단방향 연관관계를 두 개 만들어야 한다.
- `A->B (a.getB())`
- `B->A (b.getA())`

#### 테이블의 양방향 연관관계

- 테이블은 **외래 키 하나**로 두 테이블의 연관관계를 관리한다.

- MEMBER.TEAM_ID 외래 키 하나로 양방향 연관관계를 가진다. (양쪽으로 조인 가능)

- ``` sql
  SELECT *
  FROM MEMBER M
  JOIN TEAM T ON M.TEAM_ID = T.TEAM_ID
  ```

- ``` SQL
  SELECT *
  FROM TEAM T
  JOIN MEMBER M ON T.TEAM_ID = M.TEAM_ID
  ```



**여기서 오는 딜레마... 둘 중에 뭘로 매핑해야돼??**

Member 의 team 값이 바뀌었을 때 외래 키 값이 업데이트 되어야 하나.. 아니면 Team의 members 가 바뀌면 업데이트 되어야 하나...

멤버 한 명이 팀을 옮기는 경우, 객체에서는 어떻게 하든 테이블 입장에서는 외래 키만 업데이트하면 되는데.. 양방향이면 어떡하나!?

-> 둘 중에 하나를 딱 정하자! 연관 관계의 주인을!!



### 연관관계의 주인 (Owner) - 양방향 매핑 규칙

- 객체의 두 관계 중 하나를 연관관계의 주인으로 지정
- **연관관계의 주인만이 외래 키를 관리(등록, 수정)할 수 있다!**
- 주인이 아닌 쪽은 읽기만 가능하도록
- 주인은 `mappedBy` 속성 사용 X
- 주인이 아니면 `mappedBy` 속성으로 주인을 지정해준다.



#### 그럼, 누구를 주인으로?  

- 선생님의 깨달음 - **외래키가 있는 곳을 주인으로** 정해라
- 여기서는 Member.team 이 연관관계의 주인

``` java
@Entity
public class Team {
  @Id @GeneratedValue
  private Long id;

  private String name;

  // Member 에 외래 키가 있으므로 Member 가 연관관계의 주인이 된다.
  // 따라서 Team.members에 mappedBy 속성!
  @OneToMany(mappedBy = "team")
  List<Member> members = new ArrayList<>();
}
```

만약, Team.members가 연관관계의 주인이면.. Team.members 에서 멤버 정보를 수정하면 MEMBER 테이블이 수정된다. Team 객체에 어떤 액션을 했는데 MEMBER 테이블에 대한 쿼리가 나간다? 완전 짜친다. 성능 이슈도 있단다.

1대다 관계에서  항상 다(N)쪽이 연관관계의 주인이 된다. 그럼 이 쪽에 @ManyToOne -> 여긴 mappedBy 속성이 없네!!





---

### 양방향 매핑 시 가장 많이 하는 실수







