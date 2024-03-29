## 데이터 접근 기술 진행 방식

**적용 데이터 접근 기술**

- JdbcTemplate
- MyBatis
- JPA, Hibernate
- 스프링 데이터 JPA
- Querydsl



크게 두 가지 분류로 나뉘어짐.

**SQLMapper**

- JdbsTemplate, MyBatis

- 개발자는 SQL만 작성하면 해당 SQL의 결과를 **객체로 편리하게 매핑**해준다.
- 여러 중복 제거, 기타 개발자에게 편리한 기능 제공



**ORM 관련 기술**

- JPA, Hibernate, 스프링 데이터 JPA, Querydsl

- SQL 매퍼 기술은 SQL 쿼리를 개발자가 직접 작성해야 하지만, JPA를 사용하면 기본적인 쿼리는  JPA가 대신 작성/처리해준다. 개발자는 저장하고자 하는 객체를 자바 컬렉션에 저장하고 조회하듯이 사용하면 된다.

- JPA: 자바 진영의 ORM 표준 / Hibernate: JPA에서 가장 많이 사용하는 구현체

- 스프링 데이터 JPA, Querydsl은 JPA를 더 편리하게 사용하게 해주는 프로젝트. 

  영한님 피셜) 실무에서는 거의 필수!!



## 프로젝트 구조

### 기본 구조

`Item`: 상품 자체를 표현하는 객체.

`ItemRepository`: 다양한 데이터 접근 기술 구현체로 손쉽게 변경하기 위해 인터페이스로 선언.

`ItemUpdateDto`: 상품을 수정할 때 사용하는 객체. **단순히 데이터를 전달하는 용도로 사용되므로 DTO**

- Data Transfer Object
  - 데이터 전송 객체
  - DTO는 기능은 없고 데이터를 전달만 하는 용도로 사용되는 객체.

`MemoryItemRepository`: `ItemRepository` 인터페이스를 구현한 메모리 저장소.

`ItemService`: 서비스 역시 구현체를 쉽게 변경하기 위해 인터페이스로 선언. 참고로 서비스는 구현체를 변경할 일이 많지는 않기 때문에 **서비스에 인터페이스를 잘 도입하진 않는다.**



**참고**

DTO는 어느 패키지에 넣을까?? 최종적으로 사용하는 클래스(`ItemRepository`)가 이 DTO의 주인이라고 할 수 있다. 고로 `repository` 패키지가 적절할듯. 서비스에서는 레포지토리의 메서드를 사용하기 위해 DTO를 사용하는 것 뿐.



### 설정

`MemoryConfig`: `ItemServiceV1`, `MemoryItemRepository` 구현체를 스프링 빈으로 등록하고 생성자를 통해 의존관계 주입.

`TestDataInit`: 애플리케이션 실행할 때 초기 데이터 저장.

- `@EventListener(ApplicationReadyEvent.class)`: 스프링 컨테이너가 초기화를 다 끝내고, 실행 준비가 되었을 때 발생하는 이벤트. `@PostConstruct`를 대신사용하는 경우 AOP 같은 부분이 처리가 덜 된 시점에 호출될 수도 있다.



**프로필**

스프링은 로딩 시점에 `application.properties` 의 `spring.profiles.active`  속성을 읽어서 프로필로 사용한다.



main 프로필: `/src/main/resources`하위의 `application.properties`

- 이 위치의 프로퍼티 파일은 `/src/main` 하위의 자바 객체를 실행할 때 동작하는 스프링 설정.

참고로 프로필을 지정하지 않으면 디폴트(`default`) 프로필이 실행된다.



test 프로필: `/src/test/resources`하위의 `application.properties`

- 이 위치의 프로퍼티 파일은 `/src/test` 하위의 자바 객체를 실행할 때 동작하는 스프링 설정.
- 주로 테스트 케이스를 실행할 때 동작한다.



### 테스트

**인터페이스를 테스트하자!!**





## DB 테이블 생성

H2 `generated by default as identity` = MySQL `Auto Increment`



**참고 - 권장하는 식별자 선택 전략**

데이터베이스 PK는 다음 3가지 조건을 모두 만족해야 한다.

1. `null`은 허용하지 않음
2. 유일해야 함
3. 변해선 안됨



테이블의 기본 키를 선택하는 전략은 크게 2가지가 있다.

- 자연 키(natural key)
  - 비즈니스에 의미가 있는 키
- 대리 키(surrogate key)
  - 비즈니스와 관련없는 임의로 만들어진 키, 대체 키로도 불린다.



자연 키보다는 대리 키를 권장!!

비즈니스 환경은 언젠가 변한다.