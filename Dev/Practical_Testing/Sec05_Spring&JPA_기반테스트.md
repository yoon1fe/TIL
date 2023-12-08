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



## Business Layer 테스트







## Presentation Layer 테스트

