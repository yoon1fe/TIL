# PART 1 기초



## CHAPTER 1. 자바 8~11: 뭔일 일어나고 있?

자바 8에서 제공하는 새로운 기술

- 스트림 API
- 메서드에 코드를 전달하는 기법
  - 이를 이용하면 간결한 방식으로 동작 파라미터화`behavior parameterization`을 구현할 수 있다.
- 인터페이스의 디폴트 메서드



자바 8 설계의 밑바탕을 이루는 세 가지 프로그래밍 개념

#### 스트림 처리

스트림이란?

- 한 번에 한 개씩 만들어지는 연속적인 데이터 항목들의 모임
- 자바 8에는 `java.util.stream` 패키지에 스트림 API가 추가됐다.
- 핵심은 기존에 한 번에 한 항목을 처리하던 작업을 데이터베이스 질의처럼 고수준으로 추상화해서 일련의 스트림으로 만들어 처리할 수 있다는 것 + 스레드를 사용하지 않고도 병렬성을 얻을 수도 있다.



#### 동작 파라미터화로 메서드에 코드 전달하기

코드 일부를 API로 전달하는 기능.

메서드를 다른 메서드로 전달하는 것 -> 동작 파라미터화

customerId로 정렬하고 싶다 -> `compareUsingCustomerId()` 메서드를 이용해 `sort`의 동작을 파라미터화

연산의 동작을 파라미터화할 수 있는 코드를 전달



#### 병렬성과 공유 가변 데이터

병렬성을 공짜로 얻을 수 있다. 대신 스트림 메서드로 전달하는 코드의 동작 방식을 수정해야 한다.

다른 코드와 동시에 실행하더라도 **안전하게 실행**할 수 있는 코드를 만들려면 공유된 가변 데이터에 접근하지 않아야 한다. 이를 순수(pure) 함수, 부작용 없는(side-effect-free) 함수, 상태없는(stateless) 함수라고 부른다.



### 자바 함수

프로그래밍 언어의 핵심은 값을 바꾸는 것이다. 

일급 시민

- `int`, `double`과 같은 기본 자료형, 객체(의 참조), 배열 등
- 



메서드나 클래스같은 구조체는 값의 구조를 표현하는데 도움을 주지만, 전달할 수 없는 구조체는 이급 시민

- 메서드, 클래스 등
- 그 자체로 값이 될 수 없다.
- 이급 시민을 일급 시민으로 만들면, 프로그래밍에 유용하게 활용 가능



#### 메서드 참조(`::`)

- 디렉터리에서 모든 숨겨진 파일을 필터링하는 코드

  ``` java
  File[] hiddenFiles = new File(".").listFiles(new FilFilter() {
    public boolean accept(File file) {
      return file.isHidden();		// 숨겨진 파일 필터링
    }
  });
  ```

  - `File` 클래스에 이미 `isHidden()` 메서드가 있는데 `FileFilter`로 `isHidden()`을 감싼 다음 `FileFilter`를 인스턴스화? 복잡하다.

- 메서드 참조를 이용한 코드

  ``` java
  File[] hiddenFiles = new File(".").listFiles(File::isHidden);
  ```

  - `File` 클래스에 있는 `isHidden()` 메서드를 참조해서 갖다 써라~
  - 이처럼 자바 8의 코드는 이전에 비해 문제 자체를 더 직접적으로 설명한다.



위 코드에서 메서드가 이급값이 아닌 일급값으로 사용된다. 기존의 `new`로 객체 참조를 생성해 객체 참조를 이용하지 않음



#### 람다 : 익명 함수

자바 8에서는 람다(익명 함수)를 포함하여함수도 값으로 취급 가능.



#### 코드 넘겨주기

- `Apple` 리스트에서 녹색 사과만 필터링하는 메서드

  ``` java
  public static List<Apple> filterGreenApples(List<Apple> inventory) {
    List<Apple> result = new ArrayList<>();
    
    for (Apple apple : inventory) {
      if (GREEN.equals(apple.getColor())) {	// <--
        result.add(apple);
      }
    } 
    return result;
  }
  ```

- 사과 무게로 필터링하는 메서드

  ``` java
  public static List<Apple> filterHeavyApples(List<Apple> inventory) {
    List<Apple> result = new ArrayList<>();
    
    for (Apple apple : inventory) {
      if (apple.getWeight() > 150) {	// <--
        result.add(apple);
      }
    } 
    return result;
  }
  ```



두 메서드는 딱 한 줄만 같다. 자바 8에서는 코드를 인수로 넘길 수 있으므로 위의 코드를 다음과 같이 구현 가능



``` java
// 녹색 사과 필터링
public static boolean isGreenApple(Apple apple) {
  return GREEN.equals(apple.getColor());
}

// 무게 필터링
public static boolean isHeavyApple(Apple apple) {
  return apple.getWeight() > 150;
}

public interface Predicate<T> {
  boolean test(T t);
}

static List<Apple> filterApples(List<Apple> inventory, Predicate<Apple> p) {
  List<Apple> result = new ArrayList<>();
  for (Apple apple : inventory) {
    if (p.test(apple)) {
      result.add(apple);
    }
  }
  return result;
}


// 메서드 호출
filterApples(inventory, Apple::isGreenApple);
filterApples(inventory, Apple::isHeavyApple);
```



> 참고: predicate
>
> 수학에서 인수로 값을 받아 `true`/`false` 반환하는 함수를 프레디케이트라고 한다. 이처럼 사용하는 것이 표준적인 방식!



#### 메서드 전달 -> 람다로 변경

자바 8에서는 `isHeavyApple`, `isGreenApple`과 같이 한두 번만 사용할 메서드를 매번 정의하지 않고, 람다라는 개념을 이용해 간단히 구현 가능하다.

`filterApples(inventory, (Apple a) -> GREEN.equals(a.getColor()));`



단, 람다가 꽤 복잡하다면 익명 람다보다 메서드를 정의하고 메서드 참조를 활용하는 것이 바람직하다. **코드의 명확성이 우선시되어야 하기 때문**



### 스트림

- 리스트에서 고가의 트랜잭션만 필터링한 다음, 통화로 결과를 그룹화하는 코드

  ``` java
  Map<Currency, List<Transaction>> transactionsByCurrencies = new HashMap<>();
  
  for (Transaction t : transactions) {
    if (t.getPrice() > 1000) {
      Currency c = t.getCurrency();
      List<Transaction> transactionsForCurrency = transactionsByCurrencies.get(currency);
      
      if (transactionsForCurrency == null) {
        transactionsForCurrency = new ArrayList<>();
        transactionsForCurrencies.put(c, transactionsForCurrency);
      }
      
      transactionsForCurrency.add(t);
    }
  }
  ```

  - 중첩된 제어 흐름 문장이 많아서 코드 이해가 어렵다

- 스트림 API를 이용한 코드

  ``` java
  Map<Currency, List<Transaction>> transactionsByCurrencies = transactions.stream()
    .filter((Transaction t) -> t.getPrice() > 1000)	// 고가의 트랜잭션 필터링
    .collect(groupingBy(Transaction::getCurrency));	// 통화로 그루핑
  ```




- 컬렉션 API에서는 반복 과정을 직접 처리해야 한다.(for-each 루프 이용해서, 이를 외부 반복이라고 함)
- 스트림 API를 이용하면 루프를 신경쓸 필요없다. 라이브러리 내부에서 모든 데이터가 처리된다.(내부 반복) + 멀티 코어 활용 가능



스트림을 이용하면 병렬성을 공짜로 얻을 수 있다. 그래서 컬렉션을 필터링하는 가장 빠른 방법은 

1. 컬렉션을 스트림으로 바꾸고
2. 병렬로 처리한 다음
3. 리스트로 다시 복원

하는 것이다.



### 디폴트 메서드와 자바 모듈

인터페이스의 디폴트 메서드

- 인터페이스를 **쉽게 바꿀 수 있도록 도와준다.** 
- 구현 클래스에서 구현하지 않아도 되는 메서드를 인터페이스에 추가하기 때문에 기존 코드를 건드리지 않고도 원래의 인터페이스 설계를 자유롭게 확장 가능



### 함수형 프로그래밍에서 가져온 다른 유용한 아이디어

`Optional<T>` 클래스

- 값을 갖거나 갖이 않을 수 있는 컨테이너 객체
- 값이 없는 상황을 어떻게 처리할지 명시적으로 구현하는 메서드를 포함

- `NPE` 피할 수 있게 해줌



## CHAPTER 2. 동작 파라미터화 코드 전달

#### 동작 파라미터화

- 아직은 어떻게 실행할 지 결정하지 않은 코드 블록
  - 나중에 실행된 메서드의 인수로 코드 블록 전달하면 나중에 프로그램에서 호출된다. -> 코드 블록에 따라 메서드의 **동작이 파라미터화된다.**
- 자주 바뀌는 요구사항에 대해 효과적으로 대응 가능



### 변화하는 요구사항에 대응하기

농장 재고목록 리스트에서 녹색 사과만 필터링하는 코드

``` java
public static List<Apple> filterGreenApples(List<Apple> inventory) {
  List<Apple> result = new ArrayList<>();
  for (Apple apple : inventory) {
    if (GREEN.equals(apple.getColor())) {
      result.add(apple);
    }
  }
  return result;
}
```



빨간 사과도 필터링하고 싶다면? -> 색을 파라미터화하면 된다

``` java
public static List<Apple> filterApplesByColor(List<Apple> inventory, Color color) {
  List<Apple> result = new ArrayList<>();
  for (Apple apple : inventory) {
    if (apple.getColor().equals(color)) {
      result.add(apple);
    }
  }
  return result;
}
```



그럼 , 무게로 필터링하고 싶다면?!?!?!

``` java
public static List<Apple> filterApplesByWeight(List<Apple> inventory, int weight) {
  List<Apple> result = new ArrayList<>();
  for (Apple apple: inventory) {
    if ( apple.getWeight() > weight ) {
      result.add(apple);
    }
  }
  return result;
}
```



색깔/무게로 필터링하는 코드의 대부분이 중복된다. 이는 소프트웨어 공학의 DRY(같은 것을 반복하지 말 것) 원칙을 어기는 것이다.

색/무게를 `filter`라는 메서드 하나로 합치는 방법도 있지만 좋지 않다. 특히 어떤 것을 기준으로 할지 가리키는 플래그를 사용하는 방법은 절대 사용하면 안된다.



### 동작 파라미터화

무식하게 파라미터를 하나하나 추가하지 말고 요구사항 변경에 대해 좀 더 유연하게 대응하는 방법을 알아보자.

- 사과의 특정 속성에 대해 불리언값을 반환하는 방법

  - 참/거짓을 반환하는 함수: predicate

  - 선택 조건 결정하는 인터페이스

    ``` java
    public interface ApplePredicate {
      boolean test (Apple apple);
    }
    ```



- 무거운 사과만 선택하는 Predicate 함수

``` java
public class AppleHeavyWeightPredicate implements ApplePredicate {
  public boolean test(Apple apple) {
    return apple.getWeight() > 150;
  }
}
```

- 녹색 사과만 선택하는 Predicate 함수

``` java
public class AppleGreenColorPredicate implements ApplePredicate {
  public boolean test(Apple apple) {
    return GREEN.equals(apple.getColor());
  }
}
```



이러한 방식이 바로 전략 디자인 패턴이다.

- (전략이라 불리는) 각 알고리즘을 캡슐화하는 알고리즘 패밀리를 정의한 다음, **런타임**에 알고리즘을 선택하는 기법
  - 여기선 `ApplePredicate`가 알고리즘 패밀리고, `AppleHeavyWeightPredicate` 등등이 전략이 된다



이제 `filterApples` 메서드에서 `ApplePredicate` 객체를 받아와서 사과의 조건을 검사하면 된다.

- 동작 파라미터화: 메서드가 다양한 동작을 **받아서** 내부적으로 다양한 동작을 수행

``` java
public static List<Apple> filterApples(List<Apple> inventory, ApplePredicate p) {
  List<Apple> result = new ArrayList<>();
  for (Apple apple : inventory) {
    if (p.test(apple)) {
      result.add(apple);
    }
  }
  return result;
}
```

이제 우리가 만들어서 전달한 `ApplePredicate` 객체에 의해 `filterApples` 메서드의 동작이 결정된다!! -> **동작 파라미터화!!**



### 복잡한 과정 간소화

근데 여러 클래스를 구현해서 인터페이스화하는 과정이 좀 귀찮다.

**익명 클래스**: 클래스의 선언과 인스턴스화를 동시에 처리 -> 즉석에서 필요한 구현을 만들어서 사용



``` java
List<Apple> redApples = filterApples(inventory, new ApplePredicate() {
  public boolean test(Apple apple) {
    return RED.equals(apple.getColor());
  }
});
```



그래도 길다. 자바8부터 람다 표현식을 사용하면 획기적으로 짧게 줄일 수 있다.

``` java
List<Apple> result = filterApples(inventory, (Apple apple) -> RED.equals(apple.getColor()));
```



리스트 형식으로 추상화

``` java
public interface Predicate<T> {
  boolean test(T t);
}

public static <T> List<T> filter(List<T> list, Predicate<T> p) {
  List<T> result = new ArrayList<T>;
  for (T e : list) {
    if (p.test(e)) {
      result.add(e);
    }
  }
  return result;
}
```



## CHAPTER 3. 람다 표현식

람다 표현식: 메서드로 전달할 수 있는 익명 함수를 단순화한 것

- 특징: 익명, 함수, 전달, 간결성

람다를 이용하면 간결한 방식으로 코드 전달 가능



``` java
(Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight());
```

- 파라미터 리스트 `(Apple a1, Apple a2)` (`Comparator`의 `compare` 메서드 파라미터 (사과 두 개))
- 화살표
- 람다 바디 `a1.getWeight().compareTo(a2.getWeight()`



예제..

- `List<String> list -> list.isEmpty()`
- `() -> new Apple(10)`
- `(Apple a) -> { System.out.println(a.getWeight());}` // 중괄호 생략 가능. 한 개의 `void` 메서드 호출은 중괄호로 감쌀 필요 X
- `(String s) -> s.length()`
- `(int a, int b) -> a * b `



람다 표현식은 **함수형 인터페이스**에서 사용할 수 있다.

- 함수형 인터페이스: 오직 하나의 추상 메서드만 지정하는 인터페이스 (`Comparator`, `Runnable`, `Predicate<T>` 등...)
- 람다로 함수형 인터페이스의 추상 메서드 구현을 직접 할 수 있으므로 **전체 표현식을 함수형 인터페이스의 인스턴스**로 취급 가능



함수 디스크립터: 람다 표현식의 시그니처를 서술하는 메서드



- `@FunctionalInterface`: 함수형 인터페이스임을 명시하는 어노테이션. 추상 메서드가 두 개 이상이라면 컴파일 에러 발생



### 함수형 인터페이스 사용

#### `Predicate`

- 추상 메서드 `test`: 제네릭 `T` 받아서 불리언 반환

- ex

  ``` java
  @FunctionalInterface
  public interface Predicate<T> {
    boolean test(T t);
  }
  
  public <T> List<T> filter(List<T> list, Predicate<T> p) {
    List<T> results = new ArrayList<>();
    for (T t : list) {
      if (p.test(t)) {
        results.add(t);
      }
    }
    return results;
  }
  
  Predicate<String> nonEmptyStringPredicate = (String s) -> !s.isEmpty();
  List<String> nonEmpty = filter(listOfStrings, nonEmptyStringPredicate);
  ```



#### `Consumer`

- `accept`: `T` 받아서 `void` 반환. `T` 객체를 인수로 받아서 특정 동작을 수행할 때 사용

- ex

  ``` java
  @FunctionalInterface
  public interface Consumer<T> {
    void accept(T t);
  }
  
  public <T> void forEach(List<T> list, Consumer<T> c) {
    for (T t : list) {
      c.accept(t);
    }
  }
  
  forEach(
  	Arrays.asList(1, 2, 3, 4, 5), (Integer i) -> System.out.println(i)
  );
  ```



#### `Function`

- `apply`: `T` 받아서 제네릭 `R` 객체 반환. 입력 -> 출력 매핑

- ex

  ``` java
  @FunctionalInterface
  public interface Function<T, R> {
    R apply(T t);
  }
  
  public <T, R> List<R> map(List<T> list, Function<T, R> f) {
    List<R> result = new ArrayList<>();
    for (T t : list) {
      result.add(f.apple(t));
    }
    return result;
  }
  
  List<Integer> l = map(
  	Arrays.asList("lambdas", "in", "action"),
    (String s) -> s.length()
  );
  ```



함수형 인터페이스는 확인된 예외를 던지는 것을 허용하지 않는다. 예외를 던지는 람다를 만들려면 확인된 예외를 선언하는 함수형 인터페이스를 직접 정의하거나, 람다를 `try/catch` 블록으로 감싸야 한다.



#### 형식 추론

- 자바 컴파일러는 람다 표현식이 사용된 컨텍스트를 이용해서 람다 표현식과 관련된 함수형 인터페이스를 추론

- 대상 형식을 이용해서 함수 디스크립터를 알 수 있기 때문에 람다의 시그니처도 추론 가능

- 따라서 컴파일러가 람다 파라미터 형식에 접근할 수 있으므로 람다에서 생략 가능

- ``` java
  Comparator<Apple> c = (Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight());
  Comparator<Apple> c = (a1, a2) -> a1.getWeight().compareTo(a2.getWeight());	// 형식 추론하므로 Apple 생략 가능
  ```



#### 지역 변수 사용

- 람다에서는 자유 변수 사용 가능.

  - 자유 변수: 파라미터로 넘겨진 변수가 아닌 외부에서 정의된 변수

- 단, 명시적으로 `final`로 선언되거나, 실질적으로  `final`처럼 취급되어야 함.

  - 람다는 인스턴스 변수(힙에 저장)와 정적 변수(스택에 저장)를 자유롭게 캡쳐(자신의 바디에서 참조)할 수 있기 때문

  - 아래 코드는 컴파일 불가

  - ``` java
    int portNumber = 1233;
    Runnable r = () -> System.out.println(portNumber);
    portNumber = 11233;
    ```



### 메서드 참조











