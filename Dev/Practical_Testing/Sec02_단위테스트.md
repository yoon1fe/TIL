## 수동 테스트 vs. 자동화된 테스트

**요구사항**

- 주문 목록에 음료 추가/삭제 기능
- 주문 목록 전체 지우기
- 주문 목록 총 금액 계산
- 주문 생성



```java
package sample.unit;

import org.junit.jupiter.api.Test;
import sample.unit.beverage.Americano;

class CafeKioskTest {

  @Test
  void add() {
    CafeKiosk cafeKiosk = new CafeKiosk();
    cafeKiosk.add(new Americano());

    System.out.println(">>> 담긴 음료 수 : " + cafeKiosk.getBeverages().size());
    System.out.println(">>> 담긴 음료 : " + cafeKiosk.getBeverages().get(0).getName());
  }

}
```

- 이는 올바른 테스트가 아니다.
- 기계의 힘을 빌려서 자동화를 한 테스트 가 아님.
- 특정 상황을 만들고, 실행 결과를 콘솔에 찍어서 사람이 확인함



**위 코드의 문제점**

- 최종적으로 사람이 개입해서 테스트를 확인해야 한다.
- 다른 사람이 봤을 때 명확하게 이해하기 힘들다.



## JUnit5로 테스트

**단위 테스트**

- **작은** 코드 단위를 독립적으로 **검증**하는 테스트. 
  - 클래스 or 메서드 하나를 테스트
  - 네트워크 통신 등 외부 상황에 의존하지 않는 테스트
- 검증 속도가 빠르고, 안정적이다.



**JUnit 5**

- 단위 테스트를 위한 테스트 프레임워크



**AssertJ**

- 테스트 코드 작성을 원활하게 돕는 테스트 라이브러리
- JUnit 5랑 같이 많이 쓴다.
- 풍부한 API, 메서드 체이닝 지원



```java
package sample.unit.beverage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AmericanoTest {

  @Test
  void getName() {
    Americano americano = new Americano();

    // junit api
    assertEquals(americano.getName(), "아메리카노");
    // assertJ api
    assertThat(americano.getName()).isEqualTo("아메리카노");
  }

  @Test
  void getPrice() {
    Americano americano = new Americano();

    assertThat(americano.getPrice()).isEqualTo(4000);
  }
}
```



```java
@Test
void add() {
  CafeKiosk cafeKiosk = new CafeKiosk();
  cafeKiosk.add(new Americano());

  assertThat(cafeKiosk.getBeverages().size()).isEqualTo(1);
  assertThat(cafeKiosk.getBeverages()).hasSize(1);
  assertThat(cafeKiosk.getBeverages().get(0).getName()).isEqualTo("아메리카노");

}
```



## 테스트 케이스 세분화

요구사항 추가 - 한 종류의 음료 여러 잔을 한 번에 담는 기능



**테스트 케이스르 어떻게 나눌 것인가?**

- 요구사항이 들어왔을 때,
  - 아직 드러나지 않은 요구사항이 있는지 고민해보아야 함.
- 해피 케이스(성공), 예외 케이스 고려해야 한다.
  - 경계값 테스트 - 범위(이상, 이하, 초과, 미만), 구간, 날짜 등 



```java
@Test
void addSeveralBeverages() {
  CafeKiosk cafeKiosk = new CafeKiosk();
  Americano americano = new Americano();

  cafeKiosk.add(americano, 2);

  assertThat(cafeKiosk.getBeverages().get(0)).isEqualTo(americano);
  assertThat(cafeKiosk.getBeverages().get(1)).isEqualTo(americano);
}

@Test
void addZeroBeverages() {
  CafeKiosk cafeKiosk = new CafeKiosk();
  Americano americano = new Americano();
  
  assertThatThrownBy(() -> cafeKiosk.add(americano, 0))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("음료는 1잔 이상 주문하실 수 있습니다.");
}
```



## 테스트하기 어려운 영역 분리

요구사항 추가 - 가게 운영 시간(10:00 ~ 22:00) 외에는 주문을 생성할 수 없다.



아래 테스트 코드는 가게 영업시간 (10:00 ~ 22:00) 내에 실행될 때만 성공하는 테스트가 된다.

```java

  public Order createOrder() {
    LocalDateTime currentDateTime = LocalDateTime.now();
    LocalTime currentTime = currentDateTime.toLocalTime();
    if (currentTime.isBefore(SHOP_OPEN_TIME) || currentTime.isAfter(SHOP_CLOSE_TIME)) {
      throw new IllegalArgumentException("주문 시간이 아닙니다. 관리자에게 문의하세요.");
    }

    return new Order(currentDateTime, beverages);
  }

	...
    
  @Test
  void createOrder() {
    CafeKiosk cafeKiosk = new CafeKiosk();
    Americano americano = new Americano();


    cafeKiosk.add(americano);

    Order order = cafeKiosk.createOrder();
    assertThat(order.getBeverages()).hasSize(1);
    assertThat(order.getBeverages().get(0).getName()).isEqualTo("아메리카노");
  }
```



`currentTime` 을 외부에서 주입하도록 함

```java
  public Order createOrder(LocalDateTime currentDateTime) {
    LocalTime currentTime = currentDateTime.toLocalTime();
    if (currentTime.isBefore(SHOP_OPEN_TIME) || currentTime.isAfter(SHOP_CLOSE_TIME)) {
      throw new IllegalArgumentException("주문 시간이 아닙니다. 관리자에게 문의하세요.");
    }

    return new Order(currentDateTime, beverages);
  }

	...
    
  @Test
  void createOrderWithCurrentTime() {
    CafeKiosk cafeKiosk = new CafeKiosk();
    Americano americano = new Americano();

    cafeKiosk.add(americano);

    Order order = cafeKiosk.createOrder(LocalDateTime.of(2023, 1, 17, 14, 0));

    assertThat(order.getBeverages()).hasSize(1);
    assertThat(order.getBeverages().get(0).getName()).isEqualTo("아메리카노");
  }

  @Test
  void createOrderWithOutsideOpenTime() {
    CafeKiosk cafeKiosk = new CafeKiosk();
    Americano americano = new Americano();

    cafeKiosk.add(americano);

    assertThatThrownBy(() -> cafeKiosk.createOrder(LocalDateTime.of(2023, 1, 17, 9, 59)))
        .isInstanceOf(IllegalArgumentException.class);
  }
```



**테스트하기 어려운 영역을 구분하고 분리하기**

- 테스트를 실행할 때마다 값이 달라져서.. 등의 이유로
- 위 코드에서 테스트하고자 하는 영역은 `LocalDateTime.now`(현재 시각) 가 아니다.
- 특정 시간이 들어왔을 때 실행 결과를 판단하고자 함.



**테스트하기 어려운 영역**

- 관측할 때마다 다른 값에 의존하는 코드
  - 현재 날짜/시간, 랜덤 값, 전역 변수/함수, 사용자 입력 값 등
- 외부 세계에 영향을 주는 코드
  - 표준 출력, 메시지 발송, DB에 기록 등



**순수 함수**

- 같은 입력에는 항상 같은 결과
- 외부 세상과 단절된 형태
- **테스트하기 쉬운 코드!**



## 키워드 정리 

**lombok 사용 가이드**

- `@Data`, `@Setter`, `@AllArgsConstructor` 지양
- 양방향 연관관계 시 `@ToString` 순환 참조 문제