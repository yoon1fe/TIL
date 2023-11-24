## TDD: Test Driven Development

**TDD**

- 프로덕션 코드보다 **테스트 코드를 먼저 작성하여 테스트가 구현 과정을 주도**하도록 하는 개발 방법론
- 개발 단계: RED -> GREEN -> REFACTORING
  - RED: 실패하는 테스트 작성. 구현부 없이 테스트 코드 먼저 작성
  - GREEN: 테스트 통과하기 위한 최소한의 코딩.
  - REFACTORING: 구현 코드 개선. 테스트 통과 유지!



전체 금액 계산 테스트 코드 먼저 작성

```java
  // 뼈대만 작성
  public int calculateTotalPrice() {
    return 0;
  }

  ...

  @Test
  void calculateTotalPrice() {
    CafeKiosk cafeKiosk = new CafeKiosk();
    Americano americano = new Americano();
    Latte latte = new Latte();

    cafeKiosk.add(americano);
    cafeKiosk.add(latte);

    int totalPrice = cafeKiosk.calculateTotalPrice();

    assertThat(totalPrice).isEqualTo(8500);
  }

// 실행 결과
Expected :8500
Actual   :0
```

이를 토대로 구현부 작성.



**선 기능 구현, 후 테스트 작성**

- 테스트 자체의 누락 가능성
- 특정 테스트 케이스(해피 케이스)만 검증할 가능성
- 잘못된 구현을 다소 늦게 발견할 가능성



**TDD**

- 복잡도가 낮고 테스트 가능한 코드로 구현할 수 있게 된다.
- 쉽게 발견하기 어려운 엣지 케이스를 놓치지 않게 해준다.
- 구현에 대한 빠른 피드백을 받을 수 있다.
- 과감한 리팩토링이 가능해진다.

- **관점의 변화**
  - as-is: 테스트는 구현부 검증을 위한 보조 수단
  - to-be: 테스트와 상호작용하며 발전하는 프로덕션 코드
  - **클라이언트 관점에서의 피드백을 주는 Test Driven**