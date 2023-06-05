## JdbcTemplate 소개

SQL을 직접 사용하는 경우, 스프링이 제공하는 `JdbcTemplate`을 사용하면 좋다. 얘는 JDBC를 매우 편리하게 사용할 수 있도록 도와준다.



**장점**

- 설정의 편리함 - `spring-jdbc` 라이브러리에 포함되어 있어서 별도의 설정 없이 바로 사용 가능
- 반복 문제 해결 - 템플릿 콜백 패턴을 사용해서 JDBC를 직접 사용할 때 발생하는 대부분의 반복 작업을 대신 처리해준다. 개발자는 SQL을 작성하고, 전달할 파라미터를 정의하고, 응답 값을 매핑하기만 하면 된다.



**단점**

- 동적 SQL을 해결하기 어렵다.



## JdbcTemplate 적용

### 기본

**JdbcTemplateItemRepositoryV1.java**

``` java
@Slf4j
@Repository
public class JdbcTemplateItemRepositoryV1 implements ItemRepository {

  private final JdbcTemplate template;

  public JdbcTemplateItemRepositoryV1(DataSource dataSource) {
    this.template = new JdbcTemplate(dataSource);
  }

  @Override
  public Item save(Item item) {
    String sql = "insert into item (item_name, price, quantity) values (?, ?, ?)";
    KeyHolder keyHolder = new GeneratedKeyHolder();
    template.update(connection -> {
      //자동 증가 키 - id로 키가 자동 생성된다는 의미
      PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
      ps.setString(1, item.getItemName());
      ps.setInt(2, item.getPrice());
      ps.setInt(3, item.getQuantity());

      return ps;
    }, keyHolder);

    long key = keyHolder.getKey().longValue();
    item.setId(key);

    return item;
  }

  @Override
  public void update(Long itemId, ItemUpdateDto updateParam) {
    String sql = "update item set item_name=?, price=?, quantity=? where id=?";
    template.update(sql,
        updateParam.getItemName(),
        updateParam.getPrice(),
        updateParam.getQuantity(),
        itemId);
  }

  @Override
  public Optional<Item> findById(Long id) {
    String sql = "select id, item_name, price, quantity from item where id = ?";
    try {
      Item item = template.queryForObject(sql, itemRowMapper(), id);
      return Optional.of(item);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  @Override
  public List<Item> findAll(ItemSearchCond cond) {
    String itemName = cond.getItemName();
    Integer maxPrice = cond.getMaxPrice();
    String sql = "select id, item_name, price, quantity from item";
    //동적 쿼리
    if (StringUtils.hasText(itemName) || maxPrice != null) {
      sql += " where";
    }
    boolean andFlag = false;
    List<Object> param = new ArrayList<>();
    
    if (StringUtils.hasText(itemName)) {
      sql += " item_name like concat('%',?,'%')";
      param.add(itemName);
      andFlag = true;
    }
    if (maxPrice != null) {
      if (andFlag) {
        sql += " and";
      }
      sql += " price <= ?";
      param.add(maxPrice);
    }
    log.info("sql={}", sql);
    return template.query(sql, itemRowMapper(), param.toArray());
  }

  private RowMapper<Item> itemRowMapper() {
    return (rs, rowNum) -> {
      Item item = new Item();
      item.setId(rs.getLong("id"));
      item.setItemName(rs.getString("item_name"));
      item.setPrice(rs.getInt("price"));
      item.setQuantity(rs.getInt("quantity"));
      return item;
    };
  }
}

```

- `JdbcTemplate`은 `DataSource`가 필요함. 생성자 보면 의존 관계 주입받는다. 스프링에서는 관례상 위의 방법대로 많이 쓴다.
- `save()`
  - `template.update()`: 데이터를 변경 (SELECT, UPDATE, DELETE)
  - `KeyHolder` + `prepareStatement`로 INSERT 쿼리 실행 이후에 DB에서 생성된 ID값을 조회 가능. (`SimpleJdbcInsert` 라는 기능 사용하면 더 간단하게 가능!)
- `update()`
  - `?`에 파라미터 순서대로 바인딩하면 됨. 영향받은 로우 수를 반환한다.
- `findById()`
  - `template.queryForObject()`
  - 결과 로우가 하나일 때 사용.
  - `RowMapper`는 DB의 반환 결과인 `ResultSet`을 객체로 변환한다.
  - 결과 없으면 `EmptyResultDataAccessException` 발생
  - 결과가 둘 이상이면 `IncorrectResultSizeDataAccessException` 발생
- `findAll()`
  - `template.query()`
  - 결과가 하나 이상일 때 사용
  - 결과가 없으면 빈 컬렉션 반환
- `itemRowMapper()`
  - DB 조회 결과를 객체로 변환할 때 사용.
  - JdbcTemplate가 resultSet이 끝날 때까지 루프를 대신 돌려준다.



### 동적 쿼리 문제

MyBatis에서는 SQL을 직접 작성할 때 동적 쿼리를 쉽게 작성할 수 있다!!



### 구성과 실행

``` java
@Configuration
@RequiredArgsConstructor
public class JdbcTemplateV1Config {
  private final DataSource dataSource;

  @Bean
  public ItemService itemService() {
    return new ItemServiceV1(itemRepository());
  }
  @Bean
  public ItemRepository itemRepository() {
    return new JdbcTemplateItemRepositoryV1(dataSource);
  }
}
```

- `ItemRepository` 인터페이스의 구현체로 `JdbcTemplateItemReposiotryV1` 사용하도록 구성.



``` properties
spring.profiles.active=local
spring.datasource.url=jdbc:h2:tcp://localhost/~/test
spring.datasource.username=sa
```

- `application.properties` 파일에 이렇게 설정만 하면 스프링 부트가 해당 설정을 사용해서 커넥션 풀과 `DataSource`, 트랜잭션 매니저를 스프링 빈으로 자동 등록한다.



**로그 추가**

JdbcTemplate이 실행하는 SQL 로그를 확인하려면 `application.properties`에 다음을 추가하면 된다.

```properties
logging.level.org.springframework.jdbc=debug
```



### 이름 지정 파라미터

JdbcTemplate을 기본으로 사용하면 파라미터를 **순서대로** 바인딩한다. 소스 코드 수정하다가 순서가 뒤바뀌면 난리난다.

**개발을 할 때는 코드 몇 줄 줄이는 편리함도 중요하지만, 모호함을 제거해서 코드를 명확하게 만드는 것이 유지보수 관점에서 매우 중요하다!**



**이름 지정 바인딩**

JdbcTemplate는 이런 문제를 보완하기 위해 `NamedParameterJdbcTemplate`이라는 이름을 지정해서 파라미터를 바인딩하는 기능을 제공한다.



``` java
insert into item (item_name, price, quantity) " +
 "values (:itemName, :price, :quantity)"
```

- `?` 대신 `:파라미터명`으로 받는다.



**이름 지정 파라미터**

파라미터를 전달하려면 Map처럼 key : value 구조를 만들어서 전달.



- `Map`
- `MapSqlParameterSource`
- `BeanPropertySqlParameterSource`



**BeanPropertyRowMapper**

- `ResultSet` 의 결과를 받아서 자바빈 규약에 맞추어 데이터 변환



### SimpleJdbbcInsert

INSERT SQL을 직접 작성하지 않아도 되도록 `SimpleJdbcInsert`라는 기능이 있다.



``` java
/**
 * SimpleJdbcInsert
 */
@Slf4j
@Repository
public class JdbcTemplateItemRepositoryV3 implements ItemRepository {

  private final NamedParameterJdbcTemplate template;
  private final SimpleJdbcInsert jdbcInsert;

  public JdbcTemplateItemRepositoryV3(DataSource dataSource) {
    this.template = new NamedParameterJdbcTemplate(dataSource);
    this.jdbcInsert = new SimpleJdbcInsert(dataSource)
        .withTableName("item")
        .usingGeneratedKeyColumns("id");
// .usingColumns("item_name", "price", "quantity"); //생략 가능. 생성 시점에 DB 테이블의 메타 데이터를 조회하기 때문.
  }

  @Override
  public Item save(Item item) {
    SqlParameterSource param = new BeanPropertySqlParameterSource(item);
    Number key = jdbcInsert.executeAndReturnKey(param);
    item.setId(key.longValue());
    return item;
  }

  @Override
  public void update(Long itemId, ItemUpdateDto updateParam) {
    String sql = "update item " +
        "set item_name=:itemName, price=:price, quantity=:quantity " +
        "where id=:id";

    SqlParameterSource param = new MapSqlParameterSource()
        .addValue("itemName", updateParam.getItemName())
        .addValue("price", updateParam.getPrice())
        .addValue("quantity", updateParam.getQuantity())
        .addValue("id", itemId);
    template.update(sql, param);
  }

  @Override
  public Optional<Item> findById(Long id) {
    String sql = "select id, item_name, price, quantity from item where id = :id ";
    try {
      Map<String, Object> param = Map.of("id", id);
      Item item = template.queryForObject(sql, param, itemRowMapper());
      return Optional.of(item);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  @Override
  public List<Item> findAll(ItemSearchCond cond) {
    Integer maxPrice = cond.getMaxPrice();
    String itemName = cond.getItemName();
    SqlParameterSource param = new BeanPropertySqlParameterSource(cond);
    String sql = "select id, item_name, price, quantity from item";
    //동적 쿼리
    if (StringUtils.hasText(itemName) || maxPrice != null) {
      sql += " where";
    }
    boolean andFlag = false;
    if (StringUtils.hasText(itemName)) {
      sql += " item_name like concat('%',:itemName,'%')";
      andFlag = true;
    }
    if (maxPrice != null) {
      if (andFlag) {
        sql += " and";
      }
      sql += " price <= :maxPrice";
    }
    log.info("sql={}", sql);
    return template.query(sql, param, itemRowMapper());
  }

  private RowMapper<Item> itemRowMapper() {
    return BeanPropertyRowMapper.newInstance(Item.class);
  }
}
```