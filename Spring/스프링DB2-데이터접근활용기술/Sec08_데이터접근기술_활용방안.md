## 스프링 데이터 JPA 예제와 트레이드 오프

`JpaItemRepositoryV2`가 어댑터 역할을 하면서 `ItemService`가 사용하는 `ItemRepository` 인터페이스를 그대로 유지할 수 있었다. 그런데 중간에 어댑터도 들어가고 해서 구조가 복잡해진다.



`ItemService`에서 `SpringDataJpaItemRepository`를 직접 사용하면 DI, OCP 원칙을 포기하는 대신에, 복잡한 어댑터를 제거하고 구조를 단순하게 가져갈 수 있는 장점이 있다.



**트레이드 오프**

여기서 발생하는 트레이드 오프는 구조의 안정성 vs 단순한 구조와 개발의 편리성 사이의 선택.

> 영한쌤 의견: 간단 & 단순, 빨리 개발할 수 있는 방향으로. 추후에 리팩토링을.. 추가적으로 추상화 요소가 들어가야 할 것 같을 때...



## 실용적인 구조

미자믹 Querydsl을 사용한 리포지토리는 스프링 데이터 JPA를 사용하지 않는 아쉬움이 있다. 스프링 데이터 JPA의 기능은 최대한 살리면서 Querydsl도 편리하게 사용할 수 있는 구조를 만들어보자.



`ItemService`가 `ItemRepositoryV2(스프링데이터JPA)`와 `ItemQueryRepositoryV2(Querydsl) - 복잡한 쿼리 기능` 둘 다 사용.

이렇게 둘을 분리하면 기본 CRUD와 단순 조회는 스프링 데이터 JPA가 담당하고, 복잡한 조회 쿼리는 Querydsl이 담당하게 된다.



**`ItemServiceV2.java`**

``` java
@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceV2 implements ItemService {

  private final ItemRepositoryV2 itemRepositoryV2;
  private final ItemQueryRepositoryV2 itemQueryRepositoryV2;

  @Override
  public Item save(Item item) {
    return itemRepositoryV2.save(item);
  }

  @Override
  public void update(Long itemId, ItemUpdateDto updateParam) {
    Item findItem = itemRepositoryV2.findById(itemId).orElseThrow();
    findItem.setItemName(updateParam.getItemName());
    findItem.setPrice(updateParam.getPrice());
    findItem.setQuantity(updateParam.getQuantity());
  }

  @Override
  public Optional<Item> findById(Long id) {
    return itemRepositoryV2.findById(id);
  }

  @Override
  public List<Item> findItems(ItemSearchCond itemSearch) {
    return itemQueryRepositoryV2.findAll(itemSearch);
  }
}
```



**`ItemRepositoryV2.java` - Spring Data JPA**

``` java
public interface ItemRepositoryV2 extends JpaRepository<Item, Long> {
}
```



**`ItemQueryRepositoryV2.java` - Querydsl**

``` java
@Repository
public class ItemQueryRepositoryV2 {

  private final JPAQueryFactory query;

  public ItemQueryRepositoryV2(EntityManager em) {
    this.query = new JPAQueryFactory(em);
  }

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



## 다양한 데이터 접근 기술 조합

어떤 데이터 접근 기술을 채택/사용할 지는 비즈니스 상황과 프로젝트 구성원의 역량에 따라 결정하는 것이 옳다. SQL에 익숙하기만 하다면 JdbcTemplate 이나 MyBatis 를 손쉽게 사용할 수 있을 것. JPA, Querydsl과 같은 기술들은 개발 생산성을 혁신적으로 향상시킬 수 있지만, 학습 곡선이 높다. 그리고 매우 복잡한 통계 쿼리를 주로 작성하는 경우에는 잘 맞지 않다.



**트랜잭션 매니저 선택**

JPA, 스프링 데이터 JPA, Querydsl 은 모두 JPA 기술을 사용하기 때문에 JpaTransactionManager를 트랜잭션 매니저로 사용하면 된다. 그런데 JdbcTemplate, MyBatis 같은 기술들은 내부에서 JDBC를 직접 사용하기 때문에 DataSourceTransactionManager를 사용한다. 따라서 JPA와 JdbcTemplate 두 기술을 함께 사용하면 트랜잭션 매니저가 달라진다. 근데 요 부분은 신경 안써도 됨.



**JpaTransactionManager의 다양한 지원**

`JpaTransactionManager`는 `DataSourceTransactionManager`가 제공하는 대부분의 기능을 제공한다!! 따라서 함께 써도 된다. ㅎㅎ 결과적으로 `JpaTransactionManager` 하나만 스프링 빈에 등록하면 대부분의 기술을 하나의 트랜잭션으로 묶어서 사용할 수 있다!!



**주의!**

다만 위 두 기술을 함께 사용할 경우 **JPA의 플러시(트랜잭션 커밋과 상관없이 강제로 DB에 변경사항 반영) 타이밍**에 주의해야 한다. JPA는 데이터를 변경하면 변경 사항을 즉시 DB에 반영하지 않는다. 따라서 하나의 트랜잭션 안에서 JPA를 통해 데이터를 변경한 다음에 JdbcTemplate을 호출하는 경우, JdbcTemplate에서는 JPA가 변경한 데이터를 읽기 못하는 문제가 발생한다.

위 문제를 해결하려면 JPA 호출이 끝난 시점에 플러시해서 변경 사항을 DB에 반영해주어야 한다.