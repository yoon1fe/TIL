## Querydsl 소개

### 기존 방식의 문제점

쿼리는 문자열이다.. 따라서 타입 체크가 불가능함. 실행하기 전까지 에러를 확인하지 못함.



컴파일 에러 - 좋은 에러

런타임 에러 - 나쁜 에러



만약 SQL이 클래스처럼 타입이 있고, 자바 코드로 작성할 수 있다면?? -> 타입 세이프 하겠지. 컴파일 시점에 체크도 가능



### 해결

**Querydsl** (Domain Specific Language)

- 쿼리를 **Java로 type-safe하게 개발**할 수 있게 지원하는 프레임워크
- 주로 JPA쿼리(JPQL)에 사용
- 도메인 + 특화 + 언어: 특정한 도메인에 초점을 맞춘 제한적인 표현력을 가진 컴퓨터 프로그래밍 언어
- 단순, 간결, 유창하다
- JPA, MongoDB, SQL같은 기술들을 위해 type-safe SQL을 만드는 프레임워크!
- APT(Annotation Processing Tool)이 `@Entity`가 붙은 클래스를 읽어와서 `QClassName.java` 클래스를 생성
- Querydsl -> JPQL 생성 -> SQL 생성 및 실행



## Querydsl 설정 및 적용

### 설정

**build.gradle**

``` groovy
dependencies {
//Querydsl 추가
implementation 'com.querydsl:querydsl-jpa'
annotationProcessor "com.querydsl:querydsl-apt:$
{dependencyManagement.importedProperties['querydsl.version']}:jpa"
annotationProcessor "jakarta.annotation:jakarta.annotation-api"
annotationProcessor "jakarta.persistence:jakarta.persistence-api"
}

//Querydsl 추가, 자동 생성된 Q클래스 gradle clean으로 제거
clean {
delete file('src/main/generated')
}
```



**Gradle 통해서 빌드할 경우**

- `Gradle -> Tasks -> build -> clean`
- `Gradle -> Tasks -> other -> compileJava`
- `build -> generated -> sources -> annotationProcessor -> java/main` 하위에 `hello.itemservice.domain.QItem` 생성 확인
- `git clean` 수행하면 `build` 폴더 자체가 삭제된다.



**IntelliJ IDEA를 통해서 빌드할 경우**

- `Build -> Build Project` | `Build -> Rebuild` | `main()` 실행

- `src/main/generated` 하위에 `hello.itemservice.domain.QItem` 생성 확인

- ``` groovy
  clean {
  delete file('src/main/generated')
  }
  ```

  `gradle clean`  명령어 실행할 때 `src/main/generated` 의 파일도 함께 삭제해준다.



### 적용

``` java
@Repository
@Transactional
public class JpaItemRepositoryV3 implements ItemRepository {

  private final EntityManager em;
  private final JPAQueryFactory query;

  public JpaItemRepositoryV3(EntityManager em) {
    this.em = em;
    this.query = new JPAQueryFactory(em);
  }

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

  }

  @Override
  public Optional<Item> findById(Long id) {
    return Optional.ofNullable(em.find(Item.class, id));
  }

  public List<Item> findAllOld(ItemSearchCond cond) {
    String itemName = cond.getItemName();
    Integer maxPrice = cond.getMaxPrice();

    QItem item = QItem.item;
    BooleanBuilder builder = new BooleanBuilder();
    if (StringUtils.hasText(itemName)) {
      builder.and(item.itemName.like("%" + itemName + "%"));
    }
    if (maxPrice != null) {
      builder.and(item.price.loe(maxPrice));
    }

    return query.select(item)
        .from(item)
        .where(builder)
        .fetch();
  }
  
  @Override
  public List<Item> findAll(ItemSearchCond cond) {
    String itemName = cond.getItemName();
    Integer maxPrice = cond.getMaxPrice();

    return query.select(item)
        .from(item)
        .where(likeItemName(itemName), maxPrice(maxPrice))
        .fetch();
  }

  private BooleanExpression likeItemName(String itemName) {
    if (StringUtils.hasText(itemName)) {
      return item.itemName.like("%" + itemName + "%");
    }
    return null;
  }

  private Predicate maxPrice(Integer maxPrice) {
    if (maxPrice != null) {
      return item.price.loe(maxPrice);
    }
    return null;
  }
}
```

- Querydsl 사용하려면 `JPAQueryFactory`가 필요하다. `JPAQueryFactory`는 JPA 쿼리인 JPQL을 만들기 때문에 `EntityManager`가 필요함. `JPAQueryFactory`는 스프링 빈으로 등록해서 사용해도 된다.

- 동적 쿼리는 `BooleanBuilder`를 사용해서 원하는 `where` 조건들을 넣어주면 된다.
- `where()` 절 안에 다양한 조건을 넣을 수 있는데, 이렇게 넣으면 AND 조건으로 처리된다. 위처럼 조건 부분들 메서드로 따로 빼면 다른 쿼리를 작성할 때 재사용할 수 있다는 장점이 있다.