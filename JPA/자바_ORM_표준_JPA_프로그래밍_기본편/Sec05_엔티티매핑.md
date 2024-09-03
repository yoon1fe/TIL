## 객체와 테이블 매핑

**엔티티 매핑**

- 객체와 테이블 매핑: `@Entity`, `@Table`
- 필드와 컬럼 매핑: `@Column`
- 기본 키 매핑: `@Id`
- 연관관계 매핑: `@ManyToOne`, `@JoinColumn`



**@Entity**

- @Entity 가 붙은 클래스는 JPA가 관리하는 엔티티
- JPA를 사용해서 테이블과 매핑할 클래스는 @Entity 필수!
- 주의
  - 기본 생성자 필수(파라미터 없는 public 또는 protected 생성자) - reflection 같은 기술 사용 위해..
  - final 클래스, enum, interface, inner 클래스에 사용 X
  - 저장할 필드에 final 사용 X



## DB 스키마 자동 생성

- DDL을 애플리케이션 실행 시점에 자동 생성! 다만 운영 환경에서 쓰면 안되겠지
- 테이블 중심 -> 객체 중심
- 데이터베이스 dialect를 활용해서 데이터베이스에 맞는 적절한 DDL 생성
- 이렇게 생성된 DDL은 **개발 장비에서만 사용!**
- 생성된 DDL은 운영서버에서는 사용하지 않거나, 적절히 다듬은 후 사용



**속성**

- hibernate.hbm2ddl.auto
  - create: DROP + CREATE TABLE
  - create-drop: 종료 시점에 DROP TABLE
  - update: 변경분만 반영
  - validate: 엔티티와 테이블이 정상 매핑되었는지만 확인
  - none: 사용하지 않음

**주의**

- 운영 장비에는 절대 create, create-drop, update 사용하면 안된다!
- 개발 초기 단계는 create 또는 update
- 테스트 서버는 update 또는 validate
- 스테이징과 운영 서버는 validate 또는 none



**DDL 생성 기능**

- 제약 조건 추가: 회원 이름 필수, 10자 초과 X
  - @Column(nullable = false, length = 10)

- 유니크 제약 조건
  - @Table(uniqueConstraints = {@UniqueConstraint(name = "NAME_AGE_UNIQUE", columnNames = {"NAME", "AGE"} )})
- DDL 생성 기능은 DDL을 자동 생성할 때만 사용되고, JPA 실행 로직에는 영향을 주지 않는다.



## 필드와 컬럼 매핑

- @Column: 컬럼 매핑 
- @Temporal: 날짜 타입 매핑 
- @Enumerated: enum 타입 매핑 
- @Lob: BLOB, CLOB 매핑 
- @Transient: 특정 필드를 컬럼에 매핑하지 않음(매핑 무시)



**@Column**

- name: 필드와 매핑할 테이블의 컬럼 이름
- insertable, updatable: 등록, 변경 가능 여부
- nullable(DDL): null 값의 허용 여부를 설정한다. false로 설정하면 DDL 생성 시에 not null 제약조건이 붙는다. 
- unique(DDL): @Table의 uniqueConstraints와 같지만 한 컬럼에 간단히 유니크 제약조건을 걸 때 사용한다. 임의의 제약조건 이름이 생성되기 때문에 잘 안쓴다.
- columnDefinition(DDL): 데이터베이스 컬럼 정보를 직접 줄 수 있다. ex) varchar(100) default ‘EMPTY'
- length(DDL): 문자 길이 제약조건, String 타입에만 사용한다.
- precision, scale(DDL): BigDecimal 타입에서 사용한다(BigInteger도 사용할 수 있다). precision은 소수점을 포함한 전체 자 릿수를, scale은 소수의 자릿수 다. 참고로 double, float 타입에는 적용되지 않는다. 아주 큰 숫자나 정 밀한 소수를 다루어야 할 때만 사용한다. 



**@Enumerated**

- Value: EnumType.STRING: enum 이름을 DB에 저장. 디폴트 ORDINAL 인데 이거 쓰지 말자.



**@Temporal**

- 날짜 타입(java.util.Date, java.util.Calendar)을 매핑
- LocalDate, LocalDateTime 사용할 땐 생략 가능

- value
  - TemporalType.DATE
  - TemporalType.TIME
  - TemporalType.TIMESTAMP



## 기본 키 매핑

**기본 키 매핑 애너테이션**

- @Id
- @GeneratedValue



**매핑 방법**

- 직접 할당: @Id 만 사용
- 자동 생성(@GeneratedValue)
  - IDENTITY: 데이터베이스에 위임, MYSQL의 AUTO_INCREMENT
    - 영속성 컨텍스트에 존재하려면 PK가 있어야 하는데.. IDENTITY 전략에서의 PK는 DB에 접근해봐야만 알 수 있다.
    - 그래서 요 케이스때만 예외적으로 **em.persist()** 시점에 즉시 INSERT SQL을 실행하고 DB에서 식별자를 조회한다.
    - 
  - SEQUENCE: 데이터베이스 시퀀스 오브젝트 사용, ORACLE
    - @SequenceGenerator 필요
  - TABLE: 키 생성용 테이블 사용, 모든 DB에서 사용 
    - @TableGenerator 필요
    - 장점: 모든 데이터베이스에 적용 가능
    - 단점: 성능
  - AUTO: 방언에 따라 자동 지정, 기본값



**권장하는 식별자 전략**

- 기본 키 제약 조건: null 아님, 유일해야 함, 변하면 안됨!
- 미래까지 이 조건을 만족하는 자연키는 찾기 어렵다. 대리키(대체키)를 사용하자
- **권장: Long + 대체키 + 키 생성 전략 사용**



## 실전 예제 1 - 요구사항 분석과 기본 매핑

**요구사항**

- 회원은 상품 주문 가능
- 주문 시 여러 종류의 상품 선택 가능



회원 : 주문 = 일대다

주문 : 상품 = 다대다(일대다 + 다대일)



**테이블**

- MEMBER
- ORDERS
- ORDER_ITEM
- ITEM



**Order.java**

```java
package jpabook.jpashop.domain;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ORDERS")
public class Order {

  @Id @GeneratedValue
  @Column(name = "ORDER_ID")
  private Long id;

  @Column(name = "MEMBER_ID")
  private Long memberId;
  private LocalDateTime orderDate;

  @Enumerated(EnumType.STRING)
  private OrderStatus status;

}
```

- `Long memberId`: 객체지향적이지 않다.
- 객체를 RDB에 맞추어 설계 == 데이터 중심의 설계



**데이터 중심 설계의 문제점**

- 현재 방식은 객체 설계를 테이블 설계에 맞춤
- 테이블의 외래키를 객체에 그대로 가져옴
- 객체 그래프 탐색이 불가능
- 참조가 없으므로 UML도 잘못됨
