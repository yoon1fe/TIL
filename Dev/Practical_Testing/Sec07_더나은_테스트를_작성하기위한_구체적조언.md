## 한 문단에 한 주제

- 테스트도 문서로서의 기능이 있다.
- 테스트 코드를 글쓰기의 관점에서 봤을 때, 테스트 하나가 한 문단이라고 한다면 하나의 테스트는 하나의 주제만 다루어야 한다(동일한 구성)!

- 분기문, 반복문 등 논리 구조가 들어간 테스트는 좋지 않다. 한 문단에 두 개 이상의 주제를 다루는 것..



**나쁜 예**

``` java
@DisplayName("상품 타입이 재고 관련 타입인지 체크한다.")
@Test
void containsStockTypeEx() {
    // given
    ProductType[] productTypes = ProductType.values();
    
    for (ProducType productType : productTypes) {
        if (productType == ProductType.HANDMADE) {
            // when
            boolean result = ProductType.containsStockType(productType);
            
            // then
            assertThat(result).isFalse();
        }
        
        if (productType == ProductType.BAKERY || productType == ProductType.BOTTLE) {
            // when
            boolean result = ProductType.containsStockType(productType);
            
            // then
            assertThat(result).isTrue();
        }
    }
}
```

- if 문 분기됐다는 것 == 하나의 테스트에서 두 가지 이상의 경우를 테스트하겠다는 것

- 케이스 확장이 필요하다면 `@ParameterizedTest` 사용하는 것이 좋음.



**한 가지 테스트에서는 한 가지 테스트 목적만 수행. (== DisplayName 을 한 문장으로 명확히 적을 수 있는 테스트인지 ?)**



## 완벽하게 제어하기

- 테스트하기 위한 환경을 조성할 때 모든 조건을 완벽하게 제어할 수 있어야 한다!

- 현재 시각, 랜덤값 등 제어할 수 없는 값은 상위 계층으로 분리해서 테스트 가능한 구조로 만드는 것이 좋음.



## 테스트 환경의 독립성을 보장하자

- 하나의 테스트에 대한 독립성
- 테스트 도중 결합도가 생기는 케이스 등에 대해서 독립성을 보장하자.



```java
@DisplayName("재고가 없는 상품으로 주문을 생성하려는 경우 예외가 발생한다.")
@Test
void createOrderWithNoStock() {
  // given
  LocalDateTime registeredDateTime = LocalDateTime.now();
  Product product1 = createProduct("001", ProductType.BOTTLE, 1000);
  Product product2 = createProduct("002", ProductType.BAKERY, 3000);
  Product product3 = createProduct("003", ProductType.HANDMADE, 5000);

  productRepository.saveAll(List.of(product1, product2, product3));

  Stock stock1 = Stock.create("001", 2);
  Stock stock2 = Stock.create("002", 2);
  stock1.deductQuantity(1); // todo
  stockRepository.saveAll(List.of(stock1, stock2));

  OrderCreateServiceRequest request = OrderCreateServiceRequest.builder()
      .productNumbers(List.of("001", "001", "002", "003"))
      .build();

  // when // then
  assertThatThrownBy(() -> orderService.createOrder(request, registeredDateTime))
      .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("재고가 부족한 상품이 있습니다.");
}
```

- `createOrder()` 메서드의 행위에 대해 테스트하고 싶은데, `deductQuantity()` 메서드가 테스트 코드 내에 포함..
- `deductQuantity()` 메서드의 파라미터를 다른 걸 넣으면 테스트가 깨지게 됨
- 개발자가 한 번 더 맥락을 이해해야 하는 상황.
- **테스트가 실패해야 하는 부분은 when 절이나 then 절이어야 한다!**
- given 절은 순수한 생성자나 builder 로 구성하는 것이 좋다. 팩토리 메서드는 프로덕션 코드에서 특정한 목적이 들어가 있기 때문에 지양하는 것이 좋다.



## 테스트 간 독립성을 보장하자

- 두 개 이상의 테스트간의 독립성을 보장하자



``` java
class StockTest {
  private static final Stock stock = Stock.create("001", 1);

  @DisplayName("재고의 수량이 제공된 수량보다 작은지 확인한다.")
  @Test
  void isQuantityLessThanEx() {
    // given
    int quantity = 2;

    // when
    boolean result = stock.isQuantityLessThan(quantity);

    // then
    assertThat(result).isTrue();
  }

  @DisplayName("재고를 주어진 개수만큼 차감할 수 있다.")
  @Test
  void deductQuantityEx() {
    // given
    int quantity = 1;

    // when
    stock.deductQuantity(quantity);

    // then
    assertThat(stock.getQuantity()).isZero();
  }
}
```

- static 변수인 stock 은 여러 테스트에서 사용되면서 상태가 변경되기 때문에 다른 테스트에 영향을 미치게 됨

- 테스트 실행 순서가 테스트 결과에 영향을 주면 안된다. 각각의 테스트는 항상 독립적으로 수행되며 언제 수행되든 항상 같은 결과를 내야 한다.



## 한 눈에 들어오는 Text Fixture 구성하기

**Test Fixture**

- 테스트를 위해 원하는 상태로 고정시킨 일련의 객체

- given 절에서 생성하는 애들...



**@BeforeEach**

하나의 테스트 클래스에서 given 데이터가 동일한 경우가 많다. 이럴 때 `@BeforeEach` 메서드에서 구성하면 중복 코드 제거가 가능하겠지만, 이렇게 되면 테스트간에 결합도가 생기게 된다.

각 테스트 입장에서 봤을 때 @BeforeEach 메서드를 아예 몰라도 테스트 내용을 이해하는데 문제가 없는지, @BeforeEach 메서드를 수정해도 모든 테스트에 영향을 주지 않는지 고려할 것. 



## Text Fixture 클렌징

**`deleteAll()` 과 `deleteAllInBatch()` 의 차이**

- 내부적으로 관계를 맺고 있는 엔티티(테이블)까지 클렌징

- deleteAllInBatch(): 테이블 전체를 날림. 이때 순서를 잘 고려해야 됨.
- deleteAll(): where 절 없이 전체 데이터를 조회하고, 얘들을 하나씩 delete...



## @ParameterizedTest

하나의 테스트인데 값을 여러 개로 바꿔가면서 테스트를 해야 할 때 ? ->  `@ParameterizedTest`



```java
@DisplayName("상품 타입이 재고 관련 타입인지 체크한다.")
@Test
void containsStockType3() {
  // given
  ProductType givenType1 = ProductType.HANDMADE;
  ProductType givenType2 = ProductType.BOTTLE;
  ProductType givenType3 = ProductType.BAKERY;

  // when
  boolean result1 = ProductType.containsStockType(givenType1);
  boolean result2 = ProductType.containsStockType(givenType2);
  boolean result3 = ProductType.containsStockType(givenType3);

  // then
  assertThat(result1).isTrue();
  assertThat(result2).isTrue();
  assertThat(result3).isTrue();
}

@DisplayName("상품 타입이 재고 관련 타입인지 체크한다.")
@CsvSource({"HANDMADE,false", "BOTTLE,true", "BAKERY,true"})
@ParameterizedTest
void containsStockType4(ProductType productType, boolean expected) {
  // when
  boolean result = ProductType.containsStockType(productType);

  // then
  assertThat(result).isEqualTo(expected);
}
```

- 인풋으로 들어오는 source
  - `@CsvSource`: 콤마로 구분되는 문자열
  - `@MethodSource`: 별도의 메서드로 빼서 테스트 데이터를 저장. `Stream<Arguments>` 타입으로 지정.



## @DynamicTest

- 일련의 시나리오 테스트할 때 사용



```java
@DisplayName("")
@TestFactory
Collection<DynamicTest> dynamicTest() {

  return List.of(
      DynamicTest.dynamicTest("", () -> {

      }),
      DynamicTest.dynamicTest("", () -> {

      })
  );

}
```



```java
@DisplayName("재고 차감 시나리오")
@TestFactory
Collection<DynamicTest> stockDeductionDynamicTest() {
  // given
  Stock stock = Stock.create("001", 1);

  return List.of(
    DynamicTest.dynamicTest("재고를 주어진 개수만큼 차감할 수 있다.", () -> {
      // given
      int quantity = 1;

      // when
      stock.deductQuantity(quantity);

      // then
      assertThat(stock.getQuantity()).isZero();
    }),

    DynamicTest.dynamicTest("재고보다 많은 수의 수량으로 차감 시도하는 경우 예외가 발생한다.", () -> {
      // given
      int quantity = 1;

      // when // then
      assertThatThrownBy(() -> stock.deductQuantity(quantity))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("차감할 재고 수량이 없습니다.");
    })
  );

}
```



## 테스트 수행도 비용. 환경 통합하기

gradle -> test 돌리면 전체 테스트를 돌리는데 테스트 각각 실행할 때마다 스프링 부트 애플리케이션이 따로 뜬다..

통합 환경 추상 클래스 활용

Mocking 하는 테스트 클래스가 있다면 새로 뜰 수 밖에 없다.



##  pravte 메서드 테스트는 어떻게?

결론: 할 필요가 없다. 하려고 해서도 안된다.

private 메서드를 테스트하고 싶은 시점에 해야 할 고민: 객체를 분리할 시점인가?
