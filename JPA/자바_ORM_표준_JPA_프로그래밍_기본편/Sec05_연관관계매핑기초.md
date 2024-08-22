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





### 주의점, 정리







## 실전 예제 2 - 연관관계 매핑 시작
