**연관관계 매핑시 고려사항 3가지**

- 다중성
- 단방향, 양방향
- 연관관계의 주인



**다중성**

- 다대일: `@ManyToOne`
- 일대다: `@OneToMany`
- 일대일: `@OneToOne`
- 다대다: `@ManyToMany`
  - 실무에서 쓰면 안된다잉.



**단방향, 양방향**

- 테이블
  - 외래키 하나로 양쪽 조인 가능
  - 방향이라는 개념이 없음
- 객체
  - 참조용 필드가 있는 쪽으로만 참조 가능
  - 한쪽만 참조: 단방향
  - 양쪽 서로 참조: 양방향



**연관관계의 주인**

- 테이블은 **외래키** 하나로 두 테이블이 연관관계를 맺음
- 객체 양방향 관계는 A->B, B->A 처럼 **참조가 2군데**
- 객체 양방향 관계는 참조가 2군데 있음. 둘중 테이블의 외래 키를 관리할 곳을 지정해야함
- **연관관계의 주인: 외래 키를 관리하는 참조**
- 주인의 반대편: 외래 키에 영향을 주지 않음, **단순 조회만 가능**



## 다대일 N:1



**다대일 단방향**

![img](https://blog.kakaocdn.net/dn/sGJ7T/btsnEL7XNF1/Q5HmRv2r3yy9Rnmu12j4Mk/img.png)

- JPA 에서 가장~~ 많이 사용!

- DB 설계는 항상 '다'에 외래키 == '다'가 연관관계의 주인!



```java
// Member.java
public class Member {

  @Id @GeneratedValue
  private Long id;
  @Column(name = "USERNAME")
  private String username;
  private int age;

  @ManyToOne
  @JoinColumn(name = "TEAM_ID")
  private Team team;
}

// Team.java
public class Team {

  @Id
  @GeneratedValue
  @Column(name = "TEAM_ID")
  private Long id;
  private String name;
}
```



**다대일 양방향**

![img](https://blog.kakaocdn.net/dn/bHxECb/btsnD2vAS1h/RaPBJPzIL5MyK8LDv03PR0/img.png)

- 테이블에 영향은 전~혀 없다.

- 외래키가 있는 쪽이 연관관계의 주인!
- 양쪽을 서로 참조하도록 개발하자.



```java
public class Team {

  @Id
  @GeneratedValue
  @Column(name = "TEAM_ID")
  private Long id;
  private String name;

  @OneToMany(mappedBy = "team")
  private List<Member> members = new ArrayList<>();

  public void addMember(Member member) {
    member.setTeam(this);
    this.members.add(member);
  }
}
```



## 일대다 1:N



**일대다 단방향**

![img](https://blog.kakaocdn.net/dn/dlaDNG/btq8NcMKe5j/qIKWpnaqKmcbjMFW5waiD1/img.png)

- 권장하진 않는 모델.

- Member가 Team 을 알고 싶지 않을때..
- Member에 TEAM_ID가 들어가야 한다..
- 팀이 변경될 때 TEAM 테이블이 아닌 다른 테이블(MEMBER)가 수정이 됨. -> 성능상 약간의 손해.
- 개발자가 건드는 테이블(엔티티)가 아닌 다른 테이블(연관관계의 주인인 엔티티)가 수정되게 된다.



```java
// Member.java
public class Member {

  @Id
  @GeneratedValue
  private Long id;
  @Column(name = "USERNAME")
  private String username;
  private int age;
}

// Team.java
public class Team {

  @Id
  @GeneratedValue
  @Column(name = "TEAM_ID")
  private Long id;
  private String name;

  @OneToMany(mappedBy = "team")
  @JoinColumn(name = "TEAM_ID")
  private List<Member> members = new ArrayList<>();
}
```

- 일대다 단방향은 '일'이 연관관계의 주인 / '다'쪽에 외래키가 있음
- 객체와 테이블의 차이 때문에 반대편 테이블의 외래키를 관리하는 특이한 구조가 된다.
- `@JoinColumn`을 사용하지 않으면 조인 테이블 방식을 사용하여 중간에 테이블이 하나 추가된다!!
- **다대일 양방향 매핑을 사용하자~**



**일대다 양방향**

![img](https://blog.kakaocdn.net/dn/MRurK/btq8PwcC7Jd/xh0JDrPPqL9RwvDtcb7lFk/img.png)

- 억지인 감이 없지 않아 있다~
- 이런 매핑은 공식적으로 존재하지도 않음.



```java
public class Member {

  @Id
  @GeneratedValue
  private Long id;
  @Column(name = "USERNAME")
  private String username;
  private int age;

  @ManyToOne
@JoinColumn(name = "TEAM_ID", insertable = false, updatable = false)
  private Team team;
}
```

- `insertable = false, updatable = false`: 읽기 전용 필드로 만들어서 양방향처럼 사용
- **다대일 양방향을 사용하자!!!**



## 일대일 1:1

- **일대일** 관계는 반대도 **일대일**
- 주 테이블이나 대상 테이블 중에 외래키 선택 가능
  - 주 테이블에 외래키
  - 대상 테이블에 외래키
- 외래키에 DB 유니크 제약조건 추가



**주 테이블에 외래키 단방향**

![img](https://blog.kakaocdn.net/dn/EqiGD/btsnOtdyd2c/97qAqFXSBvCNvvvzMyHApk/img.png)

- 다대일(`@ManyToOne`) 단방향 매핑과 비슷하다.



```java
// Locker.java
public class Locker {

    @Id @GeneratedValue
    private Long id;

    private String name;
}

// Member.java
public class Member {

  @Id @GeneratedValue
  private Long id;
  @Column(name = "USERNAME")
  private String username;
  private int age;


  @OneToOne
  @JoinColumn(name = "LOCKER_ID")
  private Locker locker;
}
```



**주 테이블에 외래키 양방향**

![img](https://blog.kakaocdn.net/dn/cmiifd/btsnFEm9lMv/9UDakZbGgRVnEoQxQOFpy1/img.png)



```java
public class Locker {

    @Id @GeneratedValue
    private Long id;

    private String name;
    
    @OneToOne(mappedBy = "MEMBER_ID")
    private Member member;
}
```

- 다대일 양방향 매핑 처럼 외래 키가 있는 곳이 연관관계의 주인
- 반대편은 mappedBy 적용



**대상 테이블에 외래키 단방향**

![img](https://blog.kakaocdn.net/dn/boTrw3/btsnEnNehnv/fJPjFiuH16DrMsqHzLxovk/img.png)

- 단방향 관계는 JPA가 지원하지 않음
- 양방향 관계는 지원한다.



**대상 테이블에 외래키 양방향**

![img](https://blog.kakaocdn.net/dn/csP1dB/btrDkVhfBEv/7NpkJOak4z3ZFDrCxUfB5k/img.png)

- 사실 일대일 주 테이블에 외래키 양방향 매핑과 같음ㅎ;;



**정리**

- 테이블에 외래 키
  - 주 객체가 대상 객체의 참조를 가지는 것 처럼 주 테이블에 외래 키를 두고 대상 테이블을 찾음
  - 객체지향 개발자 선호
  - JPA 매핑 편리
    - 장점: 주 테이블만 조회해도 대상 테이블에 데이터가 있는지 확인 가능
    - 단점: 값이 없으면 외래 키에 null 허용
- 대상 테이블에 외래 키
  - 대상 테이블에 외래 키가 존재
  - 전통적인 데이터베이스 개발자 선호
  - 장점: 주 테이블과 대상 테이블을 일대일에서 일대다 관계로 변경할 때 테이블 구조 유지
  - 단점: 프록시 기능의 한계로 **지연 로딩으로 설정해도 항상 즉시 로딩**됨



## 다대다 N:M



## 다대다 N:M

- 실무에서 쓰지 마라!!!
- RDB는 정규화된 테이블 2개로 다대다 관계를 표현할 수 없다.
- 연결 테이블을 추가해서 일대다, 다대일 관계로 풀어내야 함;

![img](https://blog.kakaocdn.net/dn/cddfvZ/btq8RtGuOfP/qs96j88wzqFMWfs8JtQGk0/img.png)

- 객체는 컬렉션을 사용해서 객체 2개로 다대다 관계 표현 가능

![img](https://blog.kakaocdn.net/dn/bceQ0C/btsnOrNCokI/e9zuKla6CgnEHHmJhGtTF0/img.png)



**다대다**

- `@ManyToMany` 
- `@JoinTable` 로 연결 테이블 지정

```java
// Product.java
public class Product {

    @Id @GeneratedValue
    private Long id;
    private String name;
  
    // 양방향
    @ManyToMany(mappedBy = "products")
    private List<Member> members = new ArrayList<>();
}

// Member.java
public class Member {

  @Id
  @GeneratedValue
  private Long id;
  @Column(name = "USERNAME")
  private String username;
  private int age;

  @ManyToMany
  @JoinTable(name = "MEMBER_PRODUCT")
  private List<Product> products = new ArrayList<>();
}
```



**한계**

- 편리해 보이지만 실무에서 쓰지 마라
- 연결 테이블이 보통 단순히 연결만 하고 끝나지 않음.
- 주문 시간, 수량같은 데이터가 들어갈 수 있다.



**한계 극복**

- 연결 테이블용 엔티티를 추가..
- `@ManyToMany` -> `@OneToMany` + `@ManyToOne`

![img](https://blog.kakaocdn.net/dn/cVwgeK/btsnLciQudY/8D36k7lv0kYCMSHmUCSxS0/img.png)



## 실전 예제 - 다양한 연관관계 매핑

배송, 카테고리 추가 - 엔티티

- 주문과 배송은 1:1(@OneToOne)
- 상품과 카테고리는 N:M(@ManyToMany)



![img](https://blog.kakaocdn.net/dn/bmZYDI/btsnLaS1eDW/qgEpu0VLUGj6BCCZP1tOnk/img.png)

![img](https://blog.kakaocdn.net/dn/RJa6s/btsnF6jA9ry/WKEfMKTFZBcO0Kwg3EsEFk/img.png)