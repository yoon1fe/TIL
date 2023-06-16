스프링과 더불어 JPA는 자바 엔터프라이즈 시장의 주력 기술.

스프링이 DI 컨테이너를 포함한 애플리케이션 전반의 다양한 기능을 제공한다면, JPA는 ORM 데이터 접근 기술을 제공한다.



실무에서는 JPA를 더욱 편리하게 사용하기 위해 스프링 데이터 JPA와 Querydsl 기술을 함께 사용한다.



## ORM 개념

### SQL 중심적인 개발의 문제점

- 지금 시대는 객체를 관계형 DB에 관리하는 시대!! - SQL 중심적인 개발이 발생한다.
- SQL 의존적인 개발을 피하기 어렵다.
- 객체 vs RDB - 패러다임의 불일치가 발생함
- 객체 - (SQL 변환) - RDB
  - 개발자 == SQL 매퍼
  - SQL 변환 굉장히 번잡하다.
- 연관 관계 - 객체 참조 == JOIN
- 엔티티 신뢰 문제
- 모든 객체를 미리 로딩할 수 없다.
  - 상황에 따라 동일한 회원 조회 메서드를 여러 번 생성



**계층형아키텍처.. 진정한 의미의 계층 분할이 어렵다.**

객체를 자바 컬렉션에 저장하듯이 DB에 저장할 수는 없을까??

-> JPA!!



### JPA 소개

- Java Persistence API
- 자바 진영의 ORM 기술 표준



**ORM(Object-Relational Mapping)**

- 객체는 객체대로 설계
- RDB는 RDB대로 설계
- ORM 프레임워크가 중간에서 매핑을 해준다.

- JPA는 애플리케이션 <-> JDBC 사이에서 동작



**JPA를 왜 사용해야 하나??**

- SQL 중심적인 개발 -> 객체 중심적인 개발
- 생산성 + 유지보수
- 패러다임의 불일치 해결
- 성능
- 데이터 접근 추상화와 벤더 독립성
- 표준



JPA는 동일한 트랜잭션에서 조회한 엔티티는 같음을 보장해준다.



**성능 최적화**

- 1차 캐시와 동일성 보장
  - 같은 트랜잭션 안에서는 같은 엔티티를 반환.
  - DB Isolation Level이 Read Commit 이어도 애플리케이션에서 Repeatable Read 보장
- 트랜잭션을 지원하는 쓰기 지연
  - 트랜잭션을 커밋할 때까지 INSERT 문을 모아놓음
  - JDBC BATCH SQL 기능을 사용해서 한 번에 SQL 전송
- 지연 로딩(lazy loading), 즉시 로딩
  - 지연 로딩: 객체가 실제 사용될 때 로딩
  - JOin SQL로 한 번에 연관된 객체까지 미리 조회



## JPA 설정

`spring-boot-starter-data-jpa` 라이브러리 사용하면 JPA + 스프링 데이터 JPA 간단히 설정 및 사용 가능!

해당 라이브러리에는 `spring-boot-starter-jdbc` 도 포함되어 있으므로 기존에 있는 부분을 제거해도 된다. (+ `hibernate-core`, `jakarta.persistence-api`, `spring-data-jpa`도 추가됨)



## JPA 적용

### 개발

JPA에서 가장 중요한 부분은 **객체와 테이블을 매핑하는 것!** JPA가 제공하는 애너테이션을 사용해서 `Item` 객체와 테이블을 매핑해보자!!!!!!!!!!



**`Item.java`**

``` java
@Data
@Entity
public class Item {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name", length = 10)
    private String itemName;
    private Integer price;
    private Integer quantity;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}

```

- `@Entity`: JPA가 사용하는 객체라는 뜻. 이 애너테이션이 있어야 JPA가 인식할 수 있다. 얘가 붙은 객체를 JPA에서는 엔티티라고 한다.
- `@Id`: 테이블의 PK와 해당 필드를 매핑
- `@GeneratedValue(strategy = GenerationType.IDENTITY)`: PK 생성 값을 DB에서 생성하는 `IDENTITY` 방식을 사용
- `@Column`: 객체의 필드를 테이블의 컬럼과 매핑.
  - `name = "item_name"`: 필드명이랑 컬럼명이랑 다를 경우 명시
  - `length = 10`: JPA의 매핑 정보로 DDL도 생성할 수 있는데, 그 때 컬럼의 길이 값으로 활용됨
  - `@Column`을 생략할 경우 필드의 이름을 테이블 컬럼 이름으로 사용한다. 참고로 스프링 부트와 통합해서 사용하면 필드 이름을 테이블 컬럼 명으로 변경할 때 카멜 케이스 -> 스네이크 케이스로 자동 변환해준다!!
- **JPA는 `public` 또는 `protected` 기본 생성자가 필수!!**  



**`JpaItemRepository.java`**

``` java
@Slf4j
@Repository
@Transactional
@RequiredArgsConstructor
public class JpaItemRepository implements ItemRepository {

  private final EntityManager em;

  @Override
  public Item save(Item item) {
    em.persist(item);
    return item;
  }

  @Override
  public void update(Long itemId, ItemUpdateDto updateParam) {
    Item findItem = em.find(Item.class, itemId);

    findItem.setItemName(updateParam.getItemName());
    findItem.setPrice(updateParam.getPrice());
    findItem.setQuantity(updateParam.getQuantity());

    // em.persist() 따로 안해줘도 됨~~
  }

  @Override
  public Optional<Item> findById(Long id) {
    return Optional.ofNullable(em.find(Item.class, id));
  }

  @Override
  public List<Item> findAll(ItemSearchCond cond) {
    String jpql = "select i from Item i";
    Integer maxPrice = cond.getMaxPrice();
    String itemName = cond.getItemName();
    if (StringUtils.hasText(itemName) || maxPrice != null) {
      jpql += " where";
    }
    boolean andFlag = false;
    if (StringUtils.hasText(itemName)) {
      jpql += " i.itemName like concat('%',:itemName,'%')";
      andFlag = true;
    }
    if (maxPrice != null) {
      if (andFlag) {
        jpql += " and";
      }
      jpql += " i.price <= :maxPrice";
    }
    log.info("jpql={}", jpql);
    TypedQuery<Item> query = em.createQuery(jpql, Item.class);
    if (StringUtils.hasText(itemName)) {
      query.setParameter("itemName", itemName);
    }
    if (maxPrice != null) {
      query.setParameter("maxPrice", maxPrice);
    }
    return query.getResultList();
  } 
}

```

- `private final EntityManage em`: 스프링을 통해서 엔티티 매니저라는 것을 주입받음. 엔티티 매니저는 내부에 데이터소스를 갖고 있고, DB에 접근할 수 있다.
- `@Transactional`: JPA의 모든 데이터 변경은 트랜잭션 안에서 이루어져야 함(조회 제외). 일반적으로는 비즈니스 로직을 시작하는 서비스 계층에 트랜잭션을 걸어주는 것이 맞다. 



**참고**: JPA를 설정하려면 `EntityManagerFactory` , JPA 트랜잭션 매니저(`JpaTransactionManager` ), 데이터소스 등등 다양한 설정을 해야 한다. 스프링 부트는 이 과정을 모두 자동화 해준다.



**`JpaConfig.java`**

 ``` java
 package hello.itemservice.config;
 
 @Configuration
 @RequiredArgsConstructor
 public class JpaConfig {
 
   private final EntityManager em;
 
   @Bean
   public ItemService itemService() {
     return new ItemServiceV1(itemRepository());
   }
 
   @Bean
   public ItemRepository itemRepository() {
     return new JpaItemRepository(em);
   }
 }
 ```



### 레포지토리 분석

**`save()` - 저장**

- `em.persist(item)`: JPA에서 객체를 테이블에 저장할 때는 엔티티 매니저가 제공하는 `persist()` 메서드를 사용하면 된다.



**`update()` - 수정**

- `em.update()` 와 같은 메서드 호출할 필요 없다. JPA는 트랜잭션이 커밋되는 시점에 변경된 엔티티 객체가 있는지 확인한다. 특정 엔티티 객체가 변경된 경우 UPDATE SQL을 실행
- 처음 조회하는 시점에 내부에 스냅샷을 만들어서 갖고 있고, 커밋 시점에 변경 사항이 있다면 UPDATE 쿼리를 만들어서 실행한다.



**`findById()` - 단 건 조회**

- JPA에서 엔티티 객체를 PK 기준으로 조회할 때는 `find()`를 사용하고 조회 타입과 PK값을 주면 된다.
- JPA(Hibernate)가 만들어서 실행한 SQL은 별칭이 조금 복잡하다..



**`findAll()` - 목록 조회**

- 여러 데이터를 복잡한 조건으로 조회 어떻게?
- **JPQL(Java Persistence Query Language)**: 객체지향 쿼리 언어. SQL이 테이블을 대상으로 하는 쿼리라면, JPQL은 엔티티 객체를 대상으로 SQL을 실행한다고 생각하면 됨. `select i from Item i` 엔티티 객체와 속성의 대소문자 구분해야 함.



**동적 쿼리 문제**

JPA를 사용해도 동적 쿼리 문제가 남아있다. 이 문제는 Querydsl 기술을 활용하면 매우 깔끔하게 사용 가능!



### 예외 변환

JPA의 경우 예외가 발생하면 JPA 예외가 발생한다.

- `EntityManager`는 순수 JPA 기술이고, 스프링과는 관계 없다. 따라엇 엔티티 매니저는 예외가 발생하면 JPA 관련 예외를 발생시킨다.
- JPA는 `PersistenceException`과 그 하위 예외를 발생시킴
- `@Repository`  이 JPA 예외를 스프링 예외 추상화(`DataAccessException`)으로 변환해준다.



**`@Repository`의 기능**

- 컴포넌트 스캔의 대상이 됨

- 예외 변환 AOP의 적용 대상이 됨

  - 스프링과 JPA를 함께 사용하는 경우 스프링은 JPA 예외 변환기(`PersistenceExceptionTranslator`)를 등록한다.

  - 예외 변환 AOP 프록시는 JPA 관련 예외가 발생하면 JPA 예외 변환기를 통해 발생한 예외를 스프링 데이터 접근 예외로 변환한다.