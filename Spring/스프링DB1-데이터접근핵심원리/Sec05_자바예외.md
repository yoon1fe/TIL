## 예외 계층

![img](https://velog.velcdn.com/images/hyun6ik/post/25868047-817a-4bc1-871b-cad5d44b515a/image.png)

- `Throwable`: 최상위 예외. 하위에 `Exception`과 `Error`가 있다.
- `Error`: 메모리 부족이나 심각한 시스템 오류와 같이 애플리케이션에서 보구 불가능한 시스템 예외. 이 예외를 잡으려고 하면 안된다. 애플리케이션 로직은 `Exception`부터 필요한 예외로 생각하고 잡으면 된다! `Error`도 언체크 예외.
- `Exception`: 체크 예외
  - 애플리케이션 로직에서 사용할 수 있는 실질적인 최상위 예외.
  - `Exception`과 그 하위 예외는 모두 컴파일러가 체크하는 **체크 예외**이다. 단, `RuntimeException`은 언체크 예외.
- `RuntimeException`: 언체크 예외, 런타임 예외
  - 컴파일러가 체크하지 않는 **언체크 예외**이다.
  - `RuntimeException`과 그 자식 예외는 모두 언체크 예외
  - `RuntimeException`의 이름을 따라서 얘랑 얘 하위 언체크 예외를 런타임 예외라고 많이 부른다.



## 예외 기본 규칙

예외는 폭탄 돌리기와 같다.. 잡아서 처리하거나, 처리할 수 없으면 밖으로 던져야 함.



**예외 처리**

ex) Repository에서 예외가 발생했을 때 Service에서 얘를 잡아서 처리하고 Controller에게 정상 흐름 반환



**예외 던짐**

예외를 처리하지 못하면 호출한 곳으로 예외를 계속 던지게 된다.



**예외에 대해서는 두 가지 기본 규칙을 기억하자!**

1. 예외는 잡아서 처리하거나 던져야 한다.
2. 예외를 잡거나 던질 때는 지정한 예외 뿐만 아니라 그 예외의 자식들도 함께 처리된다.
   - `Exception`을 `catch`로 잡으면 그 하위 예외들도 모두 잡을 수 있다.
   - `Exception`을 `throws`로 던지면 그 하위 예외들도 모두 던질 수 있다.



**예외를 처리하지 않고 끝까지 던지면 어떻게 될까??**

- 자바 `main()` 스레드의 경우 예외 로그를 출력하면서 시스템이 종료된다.
- 웹 애플리케이션의 경우 여러 사용자의 요청을 처리하기 때문에 하나의 예외 때문에 시스템이 종료되면 안된다. WAS가 해당 예외를 받아서 처리하는데, 주로 사용자에게 개발자가 지정한 오류 페이지를 보여준다.



## 체크 예외

#### 기본

```java
@Slf4j
public class CheckedTest {
 @Test
 void checked_catch() {
  Service service = new Service();
  service.callCatch();
 }
 @Test
 void checked_throw() {
  Service service = new Service();
  assertThatThrownBy(() -> service.callThrow())
    .isInstanceOf(MyCheckedException.class);
 }
 /**
  * Exception을 상속받은 예외는 체크 예외가 된다.
  */
 static class MyCheckedException extends Exception {
  public MyCheckedException(String message) {
   super(message);
  }
 }
 /**
  * Checked 예외는
  * 예외를 잡아서 처리하거나, 던지거나 둘중 하나를 필수로 선택해야 한다.
  */
 static class Service {
  Repository repository = new Repository();
  /**
   * 예외를 잡아서 처리하는 코드
   */
  public void callCatch() {
   try {
    repository.call();
   } catch (MyCheckedException e) {
    //예외 처리 로직
    log.info("예외 처리, message={}", e.getMessage(), e);
   }
  }
  /**
   * 체크 예외를 밖으로 던지는 코드
   * 체크 예외는 예외를 잡지 않고 밖으로 던지려면 throws 예외를 메서드에 필수로
   선언해야한다.
   */
  public void callThrow() throws MyCheckedException {
   repository.call();
  }
 }
 static class Repository {
  public void call() throws MyCheckedException {
   throw new MyCheckedException("ex");
  }
 }
}
```

- `Exception`을 상속받으면 체크 예외가 된다.
- 예외는 1. 잡거나 2. 던져야 한다!! 이에 대한 처리 여부를 컴파일러가 체크해준다 -> 체크 예외
- 체크 예외는 잡지 않고 밖으로 던지려면 `throws` 예외를 메서드에 필수로 선언해야 함.



**체크 예외의 장단점**

예외를 잡아서 처리할 수 없을 때 `method() throws 에외`를 필수로 선언해야 한다. 그렇지 않으면 컴파일 오류가 발생한다.

- 장점: 컴파일 시점에 문제를 잡아주는 안전 장치
- 단점: 너무 번거롭다. 크게 신경쓰고 싶지 않은 예외까지 챙겨야 한다. 의존관계에 따른 단점도 있다.



#### 활용







## 언체크 예외

#### 기본

- `RuntimeException`과 그 하위 예외는 모두 언체크 예외
- 말 그대로 컴파일러가 예외를 체크하지 않는다.
- 메서드 선언 부분에 `throws`를 생략 가능하다. 이 경우 자동으로 예외를 던진다.

```java
@Slf4j
public class UncheckedTest {
  @Test
  void unchecked_catch() {
    Service service = new Service();
    service.callCatch();
  }
  @Test
  void unchecked_throw() {
    Service service = new Service();
    assertThatThrownBy(() -> service.callThrow())
        .isInstanceOf(MyUncheckedException.class);
  }
  /**
   * RuntimeException을 상속받은 예외는 언체크 예외가 된다.
   */
  static class MyUncheckedException extends RuntimeException {
    public MyUncheckedException(String message) {
      super(message);
    }
  }
  /**
   * UnChecked 예외는
   * 예외를 잡거나, 던지지 않아도 된다.
   * 예외를 잡지 않으면 자동으로 밖으로 던진다.
   */
  static class Service {
    Repository repository = new Repository();
    /**
     * 필요한 경우 예외를 잡아서 처리하면 된다.
     */
    public void callCatch() {
      try {
        repository.call();
      } catch (MyUncheckedException e) {
        //예외 처리 로직
        log.info("예외 처리, message={}", e.getMessage(), e);
      }
    }
    /**
     * 예외를 잡지 않아도 된다. 자연스럽게 상위로 넘어간다.
     * 체크 예외와 다르게 throws 예외 선언을 하지 않아도 된다.
     */
    public void callThrow() {
      repository.call();
    }
  }
  static class Repository {
    public void call() {
      throw new MyUncheckedException("ex");
    }
  }
}
```



**언체크 예외의 장단점**

언체크 예외는 예외를 잡아서 처리할 수 없을 때도 `throws` 를 생략 가능하다.

- 장점: 신경쓰고 싶지 않은 언체크 예외를 무시할 수 있다. 신경쓰고 싶지 않은 예외의 의존 관계를 참조하지 않아도 된다.
- 개발자가 실수로 예외를 누락할 수 있다.



#### 활용





## 예외 포함과 스택 트레이스