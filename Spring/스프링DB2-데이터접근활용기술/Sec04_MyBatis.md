## MyBatis

JdbcTemplate 보다 더 많은 기능을 제공하는 SQL Mapper 이다. SQL을 xml 파일에 편리하게 작성할 수 있고, **동적 쿼리**를 매우 편리하게 작성할 수 있음



**설정의 장단점**

- JdbcTemplate는 스프링에 내장된 기능이고, 별도의 설정없이 사용 가능

- MyBatis는 추가 설정이 필요



단순한 쿼리가 많으면 JdbcTemplate로도 처리 가능하겠다.



### 설정

`mybatis-spring-boot-starter` 라이브러리를 사용하면 된다. 참고로 스프링 부트가 버전을 관리해주는 공식 라이브러리가 아니기 때문에 뒤에 버전 정보를 붙어야 한다.



**application.properties**

``` properties
#MyBatis
mybatis.type-aliases-package=hello.itemservice.domain
mybatis.configuration.map-underscore-to-camel-case=true
logging.level.hello.itemservice.repository.mybatis=trace
```

- `mybatis.type-aliases-package`
  - MyBatis에서 타입 정보를 사용할 때는 패키지 이름을 적어야 하는데, 여기 명시해두면 패키지 이름을 생략할 수 있다
  - 여러 위치를 지정하려면 `,`, `;`로 구분
- `mybatis.configuration.map-underscore-to-camel-case`
  - 언더바 > 카멜 케이스로 변경
- `logging.level.hello.itemservice.repository.mybatis`
  - 쿼리 로그



### 적용

``` java
@Mapper
public interface ItemMapper {

  void save(Item item);
  void update(@Param("id") Long id, @Param("updateParam")ItemUpdateDto updateParam);
  Optional<Item> findById(Long id);
  List<Item> findAll(ItemSearchCond itemSearch);

}
```

- MyBatis 매핑 XML 호출해주는 매퍼 인터페이스. `@Mapper` 애너테이션을 붙여야 MyBatis가 인식할 수 있다.
- 같은 패키지 위치에 실행할 SQL이 있는 XML 매핑 파일을 만들어야 한다.
- 파라미터가 두 개 이상이라면 `@Param` 애너테이션으로 이름을 지정해주어야 한다.



**ItemMapper.xml**

``` xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="hello.itemservice.repository.mybatis.ItemMapper">

  <insert id="save" useGeneratedKeys="true" keyProperty="id">
    insert into item (item_name, price, quantity)
    values (#{itemName}, #{price}, #{quantity})
  </insert>

  <update id="update">
    update item
    set item_name=#{updateParam.itemName},
        price=#{updateParam.price},
        quantity=#{updateParam.quantity}
    where id = #{id}
  </update>

  <select id="findById" resultType="Item">
    select id, item_name, price, quantity
    from item
    where id = #{id}
  </select>

  <select id="findAll" resultType="Item">
    select id, item_name, price, quantity
    from item
    <where>
      <if test="itemName != null and itemName != ''">
        and item_name like concat('%',#{itemName},'%')
      </if>
      <if test="maxPrice != null">
        and price &lt;= #{maxPrice}
      </if>
    </where>
  </select>
  
</mapper>
```

- `#{}` 문법 == `PreparedStatement`. JDBC의 `?`를 치환

- `resultType`: 반환 타입 명시

- 자바 코드에서 반환 객체가 하나라면 `Item`, `Optional<Item>` 과 같이 사용하면 되고, 두 개 이상이라면 컬렉션을 사용하면 된다.

- `<if>` 로 동적 쿼리 지원

- `<`, `>` 등은 xml 파일에서 사용할 수 없으므로 `&lt;` 등으로 적어야 한다. `CDATA` 구문 문법을 사용해도 된다.

  `<![CDATA[and price <= #{maxPrice}]]>`



`MyBatisRepository`는 단순히 `ItemMapper`에 기능을 위임한다.



**ItemMapper의 구현체**

MyBatis 스프링 연동 모듈에서 자동으로 처리해준다.

1. 애플리케이션 로딩 시점에 MyBatis 스프링 연동 모듈은 `@Mapper` 애너테이션이 붙은 인터페이스를 조사한다.
2. 해당 인터페이스가 발견되면 동적 프록시 기술을 사용해서 `ItemMapper` 인터페이스의 구현체를 만든다.
3. 생성된 구현체를 스프링 빈으로 등록한다.



### 기능 정리

**동적 쿼리**

- `if`
- `choose(when, otherwise)`
- `trim (where, set)`
- `foreach`



**애너테이션으로 SQL 작성**

XML 대신 애너테이션에 SQL 작성 가능. 잘 쓰진 않는다.

`@Select("select id, item_name, price .. from item where id=#{id}")`

XML에 select 구문은 제거되어야 한다.

동적 SQL이 해결되지 않으므로 간단한 경우에만 사용



**문자열 대체**

파라미터 바인딩이 아니라 문자 그대로를 처리하고 싶다면 `${}`를 사용하면 된다. **주의!** SQL 인젝션 공격을 당할 수 있다.



**재사용 가능한 SQL 조각**

`<sql>` 을 사용하면 SQL 코드를 재사용할 수 있다.



**Result Maps**

컬럼명과 객체의 프로퍼티 명이 다르면 `as` (별칭)를 사용하면 된다.

`<resultMap>`을 선언하면 별칭을 사용하지 않아도 된다.