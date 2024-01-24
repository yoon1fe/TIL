## 레이어드 아키텍처와 테스트

**Layered Architecture**

- 스프링 기반에서 가장 많이 사용하는 아키텍처..
- user <-> Presentation Layer <-> Business Layer <-> Persistence Layer
- 왜 분리하나? -> 관심사의 분리!
- 분리된 layer 별로 테스트도 분리하자.



**통합 테스트**

- 여러 모듈이 협력하는 기능을 통합적으로 검증하는 테스트
- 일반적으로 작은 범위의 단위 테스트만으로는 기능 전체의 신뢰성을 보장할 수 없다.
- 풍부한 단위 테스트 & 큰 기능 단위를 검증하는 통합 테스트



## Spring / JPA - 기본 엔티티 설계

- Order : Product = 다 : 다 => 일대다 +다대일로 푸는게 좋다.

  => Order - OrderProduct - Product



## Persistence Layer 테스트

**요구사항**

- 키오스크 주문 위한 상품 후보 리스트 조회
- 상품의 판매 상태: 판매중, 판매 보류, 판매 중지
- id, 상품 번호, 상품 타입, 판매 상태, 상품 이름, 가격



**Spring Data JPA 쿼리 메서드**

- 메서드 명만으로 쿼리를 만들어주는 쿼리 메서드도 테스트가 필요한가?
  - 이도 역시 내가 작성한 코드이기 때문에 테스트 작성하는 것이 좋다.



**Repository 테스트**

- 단위 테스트 성격에 가깝다.
- `@DataJpaTst`: @SpringBootTest 보다 가볍다. JPA 관련된 Bean 들만 주입해주기 때문에 빠르다.

- `extracting()`: 검증하고자 하는 필드만 추출



```java
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
//@SpringBootTest
@DataJpaTest
class ProductRepositoryTest {

  @Autowired
  ProductRepository productRepository;

  @DisplayName("원하는 판매 상태를 가진 상품들을 조회한다.")
  @Test
  void findAllBySellingStatusIn() {
    // given
    Product product1 = Product.builder()
        .productNumber("001")
        .type(ProductType.HANDMADE)
        .sellingStatus(ProductSellingStatus.SELLING)
        .name("아메리카노")
        .price(4000)
        .build();

    Product product2 = Product.builder()
        .productNumber("002")
        .type(ProductType.HANDMADE)
        .sellingStatus(ProductSellingStatus.HOLD)
        .name("카페라떼")
        .price(4500)
        .build();

    Product product3 = Product.builder()
        .productNumber("003")
        .type(ProductType.HANDMADE)
        .sellingStatus(ProductSellingStatus.STOP_SELLING)
        .name("팥빙수")
        .price(7000)
        .build();

    productRepository.saveAll(List.of(product1, product2, product3));

    // when
    List<Product> products = productRepository.findAllBySellingStatusIn(List.of(ProductSellingStatus.SELLING, ProductSellingStatus.HOLD));

    // then
    assertThat(products).hasSize(2)
        .extracting("productNumber", "name", "sellingStatus")
        .containsExactlyInAnyOrder(
            tuple("001", "아메리카노", ProductSellingStatus.SELLING),
            tuple("002", "카페라떼", ProductSellingStatus.HOLD)
        );
  }

}
```



**Persistence Layer**

- Data Access 역할
- 비즈니스 가공 로직이 포함되어서는 안된다.
- Data에 대한 CRUD 에만 집중한 레이어

- **트랜잭션**을 보장해야 한다.



## Business Layer 테스트

Service test = Business layer + Persistence layer

통합적으로 테스트.



**요구사항**

- 상품 번호 리스트 받아 주문 생성
- 주문은 주문 상태, 주문 등록시간을 가짐
- 주문의 총 금액을 계산할 수 있어야 함



**`@SpringBootTest` vs. `@DataJpaTest`**

- @DataJpaTest 애너테이션에는 @Transactional 애너테이션이 포함되어 있음.
- 따라서 테스트 끝나면 자동으로 롤백된다.
- @SpringBootTest + @Transactional 로도 대체 가능. 하지만 이렇게 쓰면 문제점이 있음.



**추가 요구사항**

- 주문 생생 시 재고 확인 및 개수 차감 후 생성
- 재고는 상품번호를 가진다.
- 재고와 관련있는 상품 타입은 병 음료, 베이커리이다.



**@Transactional**

- JPA 의 변경 감지 기능을 사용해서 update 쿼리가 날아가는걸 기대했는데, **트랜잭션 경계가 설정되지 않았기 때문에** @Transactional 애너테이션이 없으면 정상적으로 동작하지 않는다.



## Presentation Layer 테스트

- 외부 세계의 요청을 가장 먼저 받는 계층

- 파라미터에 대한 최소한의 검증을 수행!

- 비즈니스 로직은 별로 없고, **넘어오는 값의 validation 체크가 중요**

  

presentation layer 테스트할 때는 하위 레이어들은 모두 mocking 처리(해야만 하는 건 아님;)



**MockMvc**

- Mock(가짜) 객체를 사용해 스프링 MVC 동작을 재현할 수 있는 테스트 프레임워크
- `@WebMvcTest`: 컨트롤러 관련 빈만 띄울 수 있는 가벼운 애너테이션



**@MockBean**

- Mockito가 제공하는 애너테이션
- 컨테이너에 mockito 가 만든 mock 객체를 넣어주는 역할을 함.



**요구 사항**

- 관리자 페이지에서 신규 상품 등록 가능
- 상품명, 상품 타입, 판매 상태, 가격 등을 입력받음



 **@Transactional 의 readOnly 옵션**

- readOnly default: false
- true 로 주면 읽기 전용 트랜잭션이 열린다.

- CUD 동작 안함. 조회만 가능

- JPA 에서 CUD 스냅샨 저장, 변경 감지를 안해도 됨. -> 성능 향상

- CQRS - Command / Query 의 책임을 분리하자.

  == Command 형 메서드 (CUD) 와 Query 형 메서드(R) 에 잘 분리해서 해당 옵션을 주자.



컨트롤러단 응답은 규격화된 응답이 좋다.



**@NotNull, @NotBlank, @NotEmpty**

- @NotNull: null 여부만 체크

- @NotEmpty: `"   "` 통과
- @NotBlank: `""`, `"    "` 다 통과X



**validation 에 대한 책임 분리**

- ex. 상품 이름은 20자 제한.. 컨트롤러 앞단에서 체크하고 튕겨내는 것이 과연 옳은가? 도메인 성격에 맞는, 비즈니스적인 요구사항..



**controller 단의 DTO 와 service 단의 DTO 를 분리**

- 각 레이어의 결합도를 낮추어 추후 모듈 분리 등 할 때 용이
- validation 책임 분리 가능
