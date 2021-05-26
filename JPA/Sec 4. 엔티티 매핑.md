## Sec 4. 엔티티 매핑

### 엔티티 매핑

- 객체와 테이블 매핑: @Entity, @Table
- 필드와 컬럼 매핑: @Column
- 기본 키 매핑: @Id
- 연관관계 매핑: @ManyToOne, @JoinColumn



### 객체와 테이블 매핑

#### @Entity

- @Entity 가 붙은 클래스는 JPA가 관리(=엔티티)한다.

- JPA를 사용해서 테이블과 매핑할 클래스는 @Entity 어노테이션이 필수@

**주의**

- 기본 생성자 필수 (파라미터가 없는 public 또는 protected 생성자)
- final 클래스, enum, interface, inner 클래스 사용할 수 없다.
- 저장할 필드에 final 사용할 수 없다.

속성: name

- Name - JPA에서 사용할 엔티티 이름을 지정한다. 

- 디폴트는 클래스 이름과 동일

  `@Entity(name = "Member")` 

- 헷갈리니깐 기본값 쓰자



#### @Table

- 엔티티와 매핑할 테이블을 지정한다.

  `@Table(name = "mbr")` -> `select * from mbr ...`



### 데이터베이스 스키마 자동 생성

- JPA에서는 DDL을 애플리케이션 실행 시점에 자동 생성한다. 운영환경에서는 쓸 일 없겠지?

- 테이블 중심 -> 객체 중심으로 넘어간다.

- 데이터베이스 방언을 활용해서 데이터베이스에 맞는 적절한 DDL 생성해준다.

- 생성된 DDL은 **개발 장비에서만 사용**하자

자동 생성 속성

*  참고 `<property name="hibernate.hbm2ddl.auto" value="create" />`

  - 기존 테이블이 존재하면 삭제하고 새로 생성한다..

* 옵션

* create

  기존 테이블 삭제 후 다시 생성 (DROP + CREATE)

* create-drop

  create와 같으나 종료 시점에 테이블 DROP

* update

  추가분만 반영. 멤버변수 지우는건 반영 안된다. 실수로 컬럼 날아가면 큰일난다!!! (운영 DB에는 사용하면 안된다!)

* validate

  엔티티와 테이블이 정상 매핑되었는지만 확인

* none

  사용하지 않음

**주의할 점!**

- 운영 장비에는 절대 create, create-drop, update 사용하면 안된다.
- 개발 초기 단계 - create | update
- 테스트 서버 - update | validate
- 스테이징과 운영 서버 - validate | none

팁: 이거 그냥 쓰지 마라.. 로컬에서만 쓰도록..!

alter 해버리면 전체 테이블에 Lock 걸리니깐 난리날 수도 있다..!!



- `@Column(unique = true, length = 10)`

  DDL 자동 생성할 때 길이 제한, 유니크 키 제약조건도 생성해준다.

이런 DDL 생성 기능은 JPA 실행 메커니즘 자체에 영향을 주지 않는다.



### 필드와 컬럼 매핑

- 예제 요구사항 추가
  1. 회원은 일반 회원과 관리자로 구분
  2. 회원 가입일, 수정일
  3. 회원 설명하는 필드

```java
@Entity
public class Member {

  @Id
  private Long id;

  @Column(name = "name")	// 컬럼 매핑
  private String username;

  private Integer age;

  @Enumerated(EnumType.STRING) // enum 타입 매핑
  private RoleType roleType;

  @Temporal(TemporalType.TIMESTAMP)	// 날짜 타입 매핑
  private Date createdDate;

  @Temporal(TemporalType.TIMESTAMP)
  private Date lastModifiedDate;

  @Lob	// BLOB, CLOB 매핑
  private String description;

  public Member() {
  }
}
```

* `@Transient` - 특정 필드를 컬럼에 매핑하고 싶지 않은 경우



#### @Column

| 속성                  | 설명                                                         | 기본값                              |
| --------------------- | ------------------------------------------------------------ | ----------------------------------- |
| name                  | 필드와 매핑할 테이블의 컬럼 이름                             | 객체의 필드 이름                    |
| insertable, updatable | 등록, 변경 가능 여부                                         | TRUE                                |
| nullable(DDL)         | null 값의 허용 여부. false로 설정하면 not null 제약 조건이 붙는다. |                                     |
| unique(DDL)           | @Table의 uniqueConstraints와 같지만 한 컬럼에 간단히 유니크 제약조건 걸 때 사용. 잘 안쓴다.. 만들어봤자 유니크 제약 조건 이름이 이상하게 나온다. @Table 에서 이름을 설정할 수 있으니 여기서 걸자. |                                     |
| columnDefinition(DDL) | 데이터베이스 컬럼 정보를 직접 줄 수 있다.                    | 필드의 자바 타입과 방언 정보를 사용 |
| length(DDL)           | 문자 길이 제약조건, String 타입에만 사용한다.                | 255                                 |
| precision, scale(DDL) | BigDecimal 타입에서 사용.                                    | precision=19,                       |



#### @Enumerated

| 속성  | 설명                                                         | 기본값           |
| ----- | ------------------------------------------------------------ | ---------------- |
| Value | - EnumType.ORDINAL: enum 순서를 데이터베이스에 저장<br />- EnumType.STRING: enum 이름을 데이터베이스에 저장 | EnumType.ORDINAL |

**주의! ORDINAL 사용하지 않기!!**

칼럼의 타입이 integer 로 생성된다!!!!

enum의 값이 하나 추가되어도 기존의 테이블의 데이터가 변경되진 않기 때문에 답이 없다..



#### @Temporal

요샌(Java 8 이상) 사실 별로 필요 없다.

LocalDate(DB에서 date), LocalDateTime(DB에서 timestamp) 사용할 때는 생략 가능하기 때문에 ^^



#### @Lob

매핑하는 타입이 문자면 CLOB, 나머지는 BLOB으로 매핑된다.



---

여기서부터 다시 듣기!!

### 기본 키 매핑

직접 할당: @Id만 사용

자동 생성: @GeneratedValue

IDENTIY

- 기본 키 생성을 DB에 위임, MySQL - DB야 알아서 해줘!
- em.persist(member); -> 호출한 시점에 실제로 insert 쿼리를 날린다.
- 

SEQUENCE

- 데이터베이스 시퀀스 오브젝트 사용, ORACLE

TABLE: 

AUTO: 디폴트!



권장하는 식별자 전략

- 기본키 제약 조건: not null, unique, 불변
- 미래까지 이 조건을 만족하는 자연키(전화번호, 주민등록번호 등)는 찾기 어렵다. 대리키(대체키, 비즈니스와 상관없는 키)를 사용하자
- 예를 들어 주민등록번호도 기본 키로 적절하지 않다!!
- 권장: Long 형 + 대체키 + 키 생성 전략







### 실전 예제 -1. 요구사항 분석과 기본 매핑

테이블 설계 -> 엔티티 설계와 매핑

