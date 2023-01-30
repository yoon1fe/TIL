현업에서는 `enum` 타입 클래스(열거형)를 참 많이 씁니다. 저희 팀에선 에러 코드부터 유입 경로, 결제 수단 등등 정말 많은 `enum` 클래스를 정의하여 사용하고 있는데요, 많은 비즈니스 로직에서 `enum`의 값을 비교하고 있습니다. 그럼 `enum` 타입을 비교할 때는 `==`을 사용해야 할까요, `equals()`를 사용해야 할까요? 이번 글에서는 이 `enum` 열거형에 대한 간단한 소개 및 `enum` 타입의 비교에 대해서 간단히 정리해봅니다.



## Enum 개요

`enum` 타입은 동서남북, 요일 등과 같이 서로 관련있는 상수들을 미리 정의된 **상수들의 집합** 형태로 정의해놓은 특수한 데이터 타입입니다. 간단히 말해서 클래스로 모아놓은 상수의 집합이라고 생각하면 되겠습니다. 상수의 집합이기 때문에 필드명은 대문자를 사용합니다. 그리고 `enum` 클래스의 바디에는 메서드와 다른 필드도 포함될 수 있습니다.



아래는 요일을 표현한 `enum` 예제입니다.

``` java
public enum Week {
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY,
    THURSDAY, FRIDAY, SATURDAY 
}
```



얘를 컴파일하면 다음과 같은 `.class` 파일이 생성됩니다.

``` java
public final class Week extends java.lang.Enum<Week> {
  public static final Week SUNDAY;
  public static final Week MONDAY;
  public static final Week TUESDAY;
  public static final Week WEDNESDAY;
  public static final Week THURSDAY;
  public static final Week FRIDAY;
  public static final Week SATURDAY;
  public static Week[] values();
  public static Week valueOf(java.lang.String);
  static {};
}
```



이렇듯 `enum` 타입을 컴파일하면 `Enum<E>` 추상 클래스를 상속받는 클래스가 만들어집니다. 그리고 필드에는 우리가 정의해놓았던 요일 친구들이 `static final` 키워드가 붙어서 생성이 됩니다. 그리고 `enum` 에 있는 모든 열거형 상수를 배열 형태로 반환하는 `values()` 메서드와, 매개변수로 들어오는 문자열에 해당하는 열거형 상수를 반환하는 `valueOf()` 메서드도 생성이 되네요.



추가로`Week valueOf(java.lang.String);` 메서드에 대해서 간단히 살펴볼텐데요, 그에 앞서 먼저 `Enum` 클래스에 있는 `valueOf(Class<T> enumType, String name)` 이라는 스태틱 메서드를 먼저 봅시다.



```java
public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
    T result = enumType.enumConstantDirectory().get(name);
    if (result != null)
        return result;
    if (name == null)
        throw new NullPointerException("Name is null");
    throw new IllegalArgumentException(
        "No enum constant " + enumType.getCanonicalName() + "." + name);
}
```



위 메서드는 넘겨받은 열거형 `enumType`에서 `name`에 해당하는 `enum` 상수를 찾아 반환합니다. 그리고 컴파일 시점에 생성된 `valueOf(String name)` 메서드는 내부적으로 `enumType`에 자기 자신 Class 를 갖고와서 호출하게 됩니다.



## Enum 타입의 비교

열거형 타입을 비교하는 방법은 세 가지가 있습니다.

- `compareTo()`
- `equals()`
- `==`



`Enum` 클래스는 `Comparable` 인터페이스를 구현하기 때문에 `compareTo()` 메서드가 있습니다. 근데 얘는 정수를 반환하기 때문에 굳이 쓸 일이 없을 것 같네요. 그럼 처음 말했던 `Enum`의 비교, 어떻게 해야 할까요?



먼저 `Enum` 형이 JVM 메모리 공간 어디에 저장되는지 알아봅시다. `Enum` 클래스 컴파일 후 생성된 `.class` 는 아래 그림처럼 JVM 메모리 공간 중 메서드 영역에 올라가고, 힙 영역에는 열거형의 객체 인스턴스가 저장됩니다.



![img](https://blog.kakaocdn.net/dn/df5amQ/btrXrOiDni5/5lEud1k6MyWr6FAvFu4jw0/img.png)



스택 영역에 있는 변수들은 힙 영역에 있는 데이터의 주소값을 참조하는 형태를 띄는데요, `==` 연산자는 주소값을 비교하기 때문에 열거형을 비교할 때 `==` 연산자 사용이 가능합니다.



두번째로 `equals()` 메서드는 어떻게 비교를 수행할까요?

``` java
public abstract class Enum<E extends Enum<E>> implements Comparable<E>, Serializable {
  private final String name;
  private final int ordinal;
  ...
    
  public final boolean equals(Object other) {
        return this==other;
  }

  ...
}
```



 `Enum` 에서 `equals()`는 내부적으로 `==` 을 사용하기 때문에 열거형을 비교할 때, `equals()` 나 `==` 연산자나 내부 동작은 동일합니다. 다만 두 방식은 각각의 장단점이 있습니다.



```java
enum Color { BLACK, WHITE };
enum Animal { CAT, DOG };

@Test
public void enumTest() {

    Color nothing = null;
    if (nothing == Color.BLACK);						// 정상 동작 (false)
    if (nothing.equals(Color.BLACK));				// NPE 발생

    if (Color.BLACK == Animal.CAT);					// 컴파일 X
    if (Color.BLACK.equals(Animal.CAT));		// 정상적으로 컴파일
}
```

- `==` 연산자를 통한 비교에서는 `NullPointerException` 예외가 발생하지 않는 반면에, `equals()`를 통한 비교에서는 `NPE`가 발생합니다.
- `==` 연산자를 통한 비교에서는 컴파일 시점에 타입 체크가 이루어지는 반면에, `equals()`를 통한 비교에서는 정상적으로 컴파일이 됩니다.



엥? 그럼 무조건 `==` 연산자를 사용하는 것이 최고지 않냐? 라고 생각할 수도 있습니다. 하지만 `NPE`가 발생하지 않는 것이 항상 능사는 아닙니다. [Optional 때려 뿌수기](https://yoon1fe.tistory.com/244) 글에서도 언급했던 것처럼, **로직 상 `null`이 들어가면 안되는 부분에서는 `NPE`를 던져주는 것이 좋습니다.** 만약 그러한 부분에서 열거형이  `null`이라면, `false`를 반환해주는 것보다 `NPE`를 던져주는 것이 바람직하겠죠. 이는 명백한 오류일테니까요.



결론을 내자면, `==` 연산자를 사용하면 컴파일 시점, 런타임 시점에 각각 타입 체크 컴파일 오류와 `NullPointerException` 예외가 발생하지 않습니다. 하지만 `NPE` 발생을 피하기 위해 무작정 `==` 연산자를 사용하는 것보단, 상황에 맞게 `==` 연산자와 `equals()` 메서드를 적절히 사용해야 할 듯 싶습니다.









##### Reference

https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html

https://stackoverflow.com/questions/1750435/comparing-java-enum-members-or-equals

https://www.tutorialspoint.com/Comparing-enum-members-in-Java

https://hudi.blog/java-enum/