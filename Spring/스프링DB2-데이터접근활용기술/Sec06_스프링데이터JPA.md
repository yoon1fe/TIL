## Spring Data JPA 소개

EJB -> 스프링

EJB 엔티티 빈 -> Hibernate



**Spring Data**

몽고DB, redis 등등 데이터를 다루는 기술을 통합해둔 스프링 프로젝트

- Spring Data JPA
- Spring Data Mongo
- Spring Data ...



단순한 통합 그 이상이다.

- CRUD + 쿼리
- 공통 인터페이스
- 페이징 처리
- 메서드 이름으로 쿼리 생성
- 스프링 MVC에서 id 값만 넘겨도 도메인 클래스로 바인딩



**Spring Data JPA**

- `JpaRepository` 인터페이스만 상속받으면 기본적인 CRUD 를 실행하는 메서드 사용 가능! 그럼 구현체는??? **동적 프록시 기술로 구현 클래스를 자동으로 생성해준다.**
- 메서드 이름으로 쿼리(JPQL) 자동 생성
- `@Query` 애너테이션: 네이티브 쿼리도 지원



장점

- 코딩량이 줄어든다
- 도메인 클래스를 중요하게 다루게 된다
- 비즈니스 로직 이해가 쉬워진다
- 더 많은 테스트 케이스 작성 가능



## 주요 기능

스프링 데이터 JPA는 JPA를 편리하게 사용할 수 있도록 도와주는 라이브러리.

- 공통 인터페이스 기능
- 쿼리 메서드 기능



**공통 인터페이스 기능**

![그림1](https://backtony.github.io/assets/img/post/jpa/datajpa//1-1.PNG)

- `JpaRepository` 인터페이스를 통해 기본적인 CRUD 기능 제공
- 공통화 가능한 기능이 거의 모두 있다. 카운트..



**JpaRepository 사용법**

``` java
public interface ItemRepository extends JpaRepository<Member, Long> {
  
}
```

- `JpaRepository` 인터페이스를 상속받고, 제네릭에 관리할 `<엔티티, 엔티티ID>`를 주면 된다.
- 그러면 `JpaRepository`가 제공하는 기본 CRUD 기능을 모두 사용 가능!



**쿼리 메서드 기능**

스프링 데이터 JPA는 메서드 이름만 정의하면 메서드 이름을 분석해서 쿼리를 자동으로 만들고 실행해준다.



``` java
public interface MemberRepository extends JpaRepository<Member, Long> {
  List<Member> findByUsernameAndAngeGreaterThan(String username, int age);
}
```

- 메서드 이름을 분석해서 필요한 JPQL을 만들고 실행해준다.



**쿼리 메서드 규칙**

- 조회: `find_By`, `read_By`, `query_By`, `get_By`
  - ex) `findHelloBy`
- COUNT: `count_By` 반환타입 `long`
- EXISTS: `exists_By` 반환타입 `boolean`
- 삭제: `delete_By`, `remove_By`, 반환타입 `long`
- DISTINCT: `findDistinct`, `findMemberDistinctBy`
- LIMIT: `findFirst3`, `findFirst`, `findTop` ..



**JPQL 직접 사용**

- `@Query` 애너테이션과 함께 JPQL 작성하면 된다.
- 스프링 데이터 JPA는 네이티브 쿼리 기능도 지원한다. JPQL 대신 SQL 직접 사용도 가능



## 적용



**`SpringDataJpaRepository.java**

``` java
package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataJpaItemRepository extends JpaRepository<Item, Long> {

  List<Item> findByItemNameLike(String itemName);

  List<Item> findByPriceLessThanEqual(Integer price);

  // 쿼리 메서드
  List<Item> findByItemNameLikeAndPriceLessThanEqual(String itemName, Integer price);

  // 쿼리 직접 실행
  @Query("select i from Item i where i.itemName like :itemName and i.price <= :price")
  List<Item> findItems(@Param("itemName") String itemName, @Param("price") Integer price);

}
```



스프링 데이터 JPA는 동적 쿼리에 약하다.. `Example` 이라는 기능을 지원하지만 실무에서 사용하기에는 기능이 빈약함..



쿼리를 직접 실행하려면 `@Query` 애너테이션 사용하면 된다.

메서드 이름으로 쿼리를 실행할 때는 파라미터를 순서대로 입력하면 되지만, 쿼리를 직접 실행할 때는 파라미터를 명시적으로 바인딩해야 한다. `@Param("itemName")`
