## 상속 관계 매핑

- RDB는 상속 관계란게 없다.
- 슈퍼 타입-서브 타입 관계라는 모델링 기법이 객체 상속과 유사함.
- 상속관계 매핑: 객체의 상속과 구조와 DB의 슈퍼 타입-서비 타입 관계를 매핑



**구현 방법**

- 조인 전략: 각각의 테이블 -> 가장 정규화된 모델링
- 단일 테이블 전략: 통합 테이블. JPA의 기본 전략.(SINGLE_TABLE)
- 구현 클래스마다 테이블 전략: 서브 타입 테이블
- 객체 입장에선 모두 동일하다.



**주요 애너테이션**

- `@Inheritance(strategy=InheritanceType.XX)`
  - JOINED: 조인 전략
  - SINGLE_TABLE: 단일 테이블 전략
  - TABLE_PER_CLASS: 구현 클래스마다 테이블 전략
- `@DiscriminatorColumn(name="DTYPE")`
  - 부모 테이블에 추가
  - 어떤 자식 테이블인지 식별할 수 있는 컬럼 추가.
- `@DiscriminatorValue("XXX")`
  - 자식 테이블에 추가
  - DTYPE 컬럼에 들어가는 각각의 자식 테이블 이름.



**조인 전략**

- 장점
  - 테이블 정규화
  - 외래 키 참조 무결성 제약조건 활용가능 
  - 저장공간 효율화
- 단점
  - 조회시 조인을 많이 사용, 성능 저하
  - 조회 쿼리가 복잡함
  - 데이터 저장시 INSERT SQL 2번 호출



**단일 테이블 전략**

- 장점
  - 조인이 필요 없으므로 일반적으로 조회 성능이 빠름
  - 조회 쿼리가 단순함
- 단점
  - 자식 엔티티가 매핑한 컬럼은 모두 null 허용
  - 단일 테이블에 모든 것을 저장하므로 테이블이 커질 수 있다. 상황에 따라서 조회 성능이 오히려 느려질 수 있다.



**구현 클래스마다 테이블 전략**

- **이 전략은 데이터베이스 설계자와 ORM 전문가 둘 다 추천X**
- 장점
  - 서브 타입을 명확하게 구분해서 처리할 때 효과적
  - not null 제약조건 사용 가능
- 단점
  - 여러 자식 테이블을 함께 조회할 때 성능이 느림(UNION SQL 필요)
  - 자식 테이블을 통합해서 쿼리하기 어려움



## Mapped Superclass - 매핑 정보 상속

`@MappedSuperclass`

- 공통 매핑 정보가 필요할 때 사용(id, name와 같은..)
- 상속관계 매핑X
- 엔티티X, 테이블과 매핑X
- 부모 클래스를 상속 받는 **자식 클래스에 매핑 정보만 제공**
- 조회, 검색 불가(em.find(BaseEntity) 불가)
- 직접 생성해서 사용할 일이 없으므로 추상 클래스 권장

- 테이블과 관계 없고, 단순히 엔티티가 공통으로 사용하는 매핑 정보를 모으는 역할
- 주로 등록일, 수정일, 등록자, 수정자 같은 전체 엔티티에서 공통으로 적용하는 정보를 모을 때 사용
- 참고: @Entity 클래스는 엔티티나 @MappedSuperclass로 지정한 클래스만 상속 가능



## 실전 예제 - 상속 관계 매핑

**요구사항**

- 상품의 종류: 음반, 도서, 영화 ...
- 모든 데이터는 등록일, 수정일 필수



```java
// Item.java => Item 자체는 테이블 만들 필요 X
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
public abstract class Item {

  @Id @GeneratedValue
  @Column(name = "ITEM_ID")
  private Long id;

  private String name;
  private int price;
  private int stockQuantity;

  @ManyToMany(mappedBy = "items")
  private List<Category> categoryList = new ArrayList<>();

}

// Album.java
@Entity
public class Album extends Item {

    private String artis;
    private String etc;
}

// Book.java
@Entity
public class Book extends Item {

    private String author;
    private String isbn;
}

// Movie.java
@Entity
public class Movie extends Item {

    private String director;
    private String actor;
}

// BaseEntity.java
@MappedSuperclass
public abstract class BaseEntity {

    private String createdBy;
    private LocalDateTime createdDate;
    private String updatedBy;
    private LocalDateTime updatedDate;
}
```