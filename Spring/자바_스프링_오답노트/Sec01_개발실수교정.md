## 실천할 수 있는 컨벤션 교정

**컨벤션 애매할 땐 유명한 오픈소스 코드 참조하면 좋다!**



### 이름

**축약어**

`userId` ? `userID`?

축약어는 대문자로 표현하지 않는다. 일반 명사와 같은 취급.

- `private String userId`
- `private String oidcId`
- `private String ip`



**spring-security 코드**

- `public interface OAuth2UserService`

- `OAuth2Error oauthError = new ..()`
  - `oAuthError` (X)



**유의미한 단어 사용하기**

- simple / light / base
  - 다른 개발자는 어느 정도가 simple 이고 light 인지 모름
- Util
  - Util 이라는 이름 아래 모든 static 메서드가 모일 거



### 동사

**get vs find**

- get: **항상 인스턴스를 돌려 받음**. 일반적으로 데이터가 없을 시 exception 을 던진다.
- find: **`Optional<T>`** 을 리턴.

**get 남발 X**

- get 접두어는 **내가 갖고 있는 속성 정보를 그대로 돌려준다**는 의미가 크다. 찾아오라는 지시가 아님.



### 롬복

**getter / setter 남발 X**

- 캡슐화를 망치는 주범
- 사실상 멤버 변수를 public 으로 선언하는 것
- 객체를 수동적이게 만든다.



as-is

- 수동적인 객체

``` java
class UserManager {
  public void doSomething(User user) {
    user.setStatus(Status.ACTIVE);
    user.setLastLoginTimestamp(Clock.systemUTC().millis());
  }
}
```



to-be

- 능동적인 객체 (TDA(Tell Don't Ask)원칙, 디미터 법칙)

``` java
class UserManager {
  public void doSomething(User user) {
    user.inactive();
    user.login(Clock.systemUTC());
  }
}
```



### 가독성

**Optional 적극 사용하기**

- 코드 완성도를 높이고 런타임 에러(NPE)를 줄여준다.



**Collection.Map 남발 X**

- 클래스로 분리되어야 하는 코드도 Map으로 사용하게 될 수도 있다. 이러면 본인만 이해할 수 있는 코드가 됨.

- 가급적 일급 클래스로 만들고, 사용하더라도 지정된 scope 밖을 넘나들지 않도록



### 관습

- **range는 [start, end)** - 시작 포함 / 끝 제외
- 단어 조합은 3개 이하로 ..



**더 알아볼 주제 ? ?**

- 검증이 필요할 때? - verify vs validate vs check vs is



## 객체 지향적인 코드 짜기

### 객체의 종류

**User 클래스**

``` java
public class User {
  private long id;
  private String username;
  private String password;
  private String email;
  
  public void changePassword(String before, String after) {
    ...
  }
}
```



**VO, Value Object**

- VO는 불변해야 하며, 이는 동일하게 생성된 두 VO는 영원히 동일한 상태임이 유지되어야 함을 뜻함. 
- VO는 잘못된 상태로는 만들어질 수 없기 때문에 인스턴스화된 VO는 **항상 유효**하므로 버그를 줄이는데도 유용하다.
- 프로그램의 복잡도를 낮추는데 중요한 역할을 한다.
- 생성자는 가급적 두 개의 역할만 해야 함.
  1. 값을 검증
  2. 값을 할당

``` java
class UserInfo {
  private final long id;
  private final String username;
  private final String email;
  
  public UserInfo(long id, String username, String email) {
    // 값 유효성 체크
    this.id = id;
    this.username = username;
    this.email = email;
  }
}
```



**DTO, Data Transfer Object**

- 단순 데이터 전달 객체
- 상태를 보호하지 않으며 모든 속성을 노출하므로 **획득자와 설정자가 필요없다**. == public 속성으로 충분.



**Entity**

- **유일한 식별자**가 있고,
- **수명 주기**가 있으며,
- 쓰기 모델 **저장소에 저장함**으로써 지속성을 가지며 나중에 저장소에 불러올 수 있고,
-  명명한 생성자와 명령 메서드를 사용해 인스턴스를 만들거나 그 상태를조작하는 방법을 사용자에게 제공하며,
- 인스턴스를 만들거나 변경할 때 **도메인 이벤트**를 만들어낸다.

- 엄밀히 말하면 Entity 는 DB와 아무 관계가 없다. JPA의 @Entity 애너테이션 때문에 만들어진 오해
  - @Entity가 붙은 클래스는 Persistencd Object(= DB Entity) 의 개념에 가깝다.



**객체를 만들 때의 고민**

- 객체를 분류하는 것 보다 아래의 고민들이 더 중요하다.
  - 어떤 값을 불변으로 만들 것인가?
  - 어떤 인터페이스를 노출할 것인가?



**DAO, Data Access Object**

- 요즘엔 사실상 Repository 동치되면서 거의 사용안함.



### 디미터 법칙 (최소 지식의 법칙)

- 모듈은 자신이 조작하는 객체의 속사정을 몰라야 한다.

- ex) 객체 내부를 체이닝으로 줄줄이 들어가서 값을 참조/조작하는 코드 == 디미터 법칙 위반

  ``` java
  class ComputerManager {
    public void printSpec(Computer computer) {
      long size = 0;
      for (int i = 0; i < computer.getDisks().size(); i++) {
        size += computer.getDisks().get(i).getSize();
      }
      log.info("{}", size);
    }
  }
  ```

  -> 체이닝 제거. 아래 코드도 좋은 코드는 아니다. (Tell Don't Ask)

  디스크 용량이 얼마인지 물어봐서 출력하지 말고, Computer에게 디스크 용량을 출력하는 일을 시키자.

  ``` java
  class ComputerManager {
    public void printSpec(Computer computer) {
      log.info("{}", computer.getDiskSize());
    }
  }
  ```



### 행동

**데이터 위주의 사고 vs 행동 위주의 사고**

``` java
// 데이터 위주
class Car {
  private Frame frame;
  private Engine engine;
  private List<Wheel> wheels;
  private Direction direction;
  private Speed speed;
}

// 행동 위주
class Car {
  public void drive() {}
  public void changeDirection() {}
  public void accelerate(Speed speed) {}
  public void decelerate(Speed speed) {}
}
```

- 일반적으로 행동 위주의 사고를 하는 것이 객체지향적일 확률이 높다.
- 데이터 위주의 클래스는 struct에 불과함.



### 순환 참조

- 순환 참조는 만들지 말자!!
- 순환 참조가 부자연스러운 이유
  - 순환 참조를 넘어 순환 의존성 자체가 **결합도**를 높이는 원인이 된다. 순환 참조가 있는 클래스들은 원래 하나의 클래스였어야..
  - 순환 참조 때문에 Serialize 가 불가능해짐.
- 순환 참조보다 id를 기록해서 필요할 때마다 찾아오는 간접 참조
- 컴포넌트 분리



## 설계

### SOLID



**단일 책임 원칙 (SRP, Single Responsibility Principle)**

- 클래스는 하나의 목적만을 가져야 한다.
- 클래스의 코드 라인이 100줄 이상이라면 하나의 클래스에 너무 많은 책임이 할당되어 있진 않은지 확인해보는 것이 좋음



**개방-폐쇄 원칙 (OCP, Open-Closed Principle)**

- 확장에는 열려있고, 수정(변경)에는 닫혀 있어야 한다.
- 해당 원칙이 잘 적용되면, 기능을 추가하거나 변경할 때 기존 잘 동작하고 있는 코드를 변경하지 않아도 기존의 코드에 새로운 코드를 추가함으로써 기능의 추가나 변경이 가능하다.



**리스코프 치환 원칙 (Liskov Substitution)**

- 하위 자료형이 상위 자료형의 모든 동작을 완전히 대체 가능해야 한다.



**인터페이스 분리 원칙 (Interface-Segregation)**

- 인터페이스 == 계약, 경계..

  == public method. "이 기능을 사용하고 시다면 이 방법을 사용해라" 알려주는 것

- 인터페이스 분리 원칙: 클라이언트가 **자신이 이용하지 않는 메서드에 의존하지 않아야 한다.**



**의존성 역전 원칙 (DIP, Dependency Inversion Principle)**

- 상위 모듈은 하위 모듈에 의존해서는 안된다. 상위 모듈과 하위 모듈 모두 추상화에 의존해야 한다.
- 추상화는 세부 사항에 의존해서는 안된다. **세부사항이 추상화에 의존해야 한다.**
- AS-IS: McDonald -> HamburgerChef // 맥도날드는 햄버거 셰프에 의존적
- TO-BE: McDonal -> Chef `<<Interface>>` <- HamburgerChef // 맥도날드는 셰프라는 인터페이스를 통해 일을 시키는 것 뿐이고, 햄버거 셰프는 셰프를 구현한 것일 뿐.
- 의존성 역전 하라 != 무조건 추상화 하라
  - 추상화는 좋은 방법론이지만, 개발할 때 비용을 증가시킬 수 있음.
- IoC != DIP



### 의존성 조언

**의존성을 드러내라**

- 의존성이 숨겨진 예시

  ``` java
  class User {
    private long lastLoginTimestamp;
    
    public void login() {
      // ...
      this.lastLoginTimestamp = Clock.systemUTC().millis();
    }
  }
  ```

  - login() 메서드는 Clock 클래스에 의존적임.
  - 외부에서 보면 login() 이 Clock 에 의존하고 있는지 알 수 없음. (`user.login();`)
  - 의존성이 숨겨져 있으면 디버깅할 때 계속 드릴 다운하면서 원인 분석해야 한다..
  - 변하는 값을 테스트하기 난해해진다.



**변하는 값은 주입받아라**

- 랜덤값, 시간값 등 => 변하는 값

- `user.login(Clock.systemUTC());`: 외부에서 보면 login() 메서드가 시간이 필요하다는 것 알 수 있음
- 테스트도 수월해짐.



**변하는 값을 추상화시켜라**

- `user.login()` 메서드를 사용하는 UserService.login() 메서드에도 결국 Clock 클래스를 파라미터에 추가해야 되고, 얘를 또 사용하는 Controller 에도... .. 결국 폭탄 돌리기임

- 해답: 변하는 값을 추상화시켜라

  == 런타임 의존성과 컴파일 타임 의존성을 다르게 하자.



### CQRS (Command and Query Responsibility Segregation)

- 명령(일을 시키는 메서드)과 질의의 책임 분리
- 메서드를 명령과 질의로 나누자.
- 명령 메서드
  1. 상태를 변경
  2. return 값을 갖지 않음.
- 질의 메서드
  - 상태를 물어보는 메서드
  - 상태를 변경해서는 안된다.



## 기타 팁..

**오늘날 더 나은 소프트웨어를 향한 9단계 - 마틴 파울러**

1. 한 메서드에 오직 한 단계의 들여쓰기만 한다.
2. else 예약어를 쓰지 않는다
3. 모든 원시값과 문자열을 포장한다.
4. 한 줄에 점 하나만 찍는다.
5. 줄여쓰지 않는다.
6. 모든 엔티티를 작게 유지한다.
7. 3개 이상의 인스턴스 변수를 가진 클래스를 쓰지 않는다.
8. 일급 컬렉션을 쓴다.
9. getter/setter/property를 쓰지 않는다.



**습관들이면 좋은 것들..**

- apache utils
  - CollectionUtils / StringUtils / ObjectUtils ..
- 상속
  - 상속을 지양하고 Composition 을 지향할 것.
- 테스트를 먼저 생각할 것
  - 테스트하기 쉬운 코드가 좋은 설계일 확률이 높다.
- 블락이 생긴다면 메서드 분할을 고려해보자.
- 들여쓰기 2개 이상 들어가지 않도록 해보자. 이 역시 메서드 분할의 신호