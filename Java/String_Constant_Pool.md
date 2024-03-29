자바에서 `==` 연산은 **객체의 주소값을 비교**할 때 사용하고 문자열의 값을 비교할 때는 `equals()` 메소드를 사용합니다.



그럼 다음 코드의 실행 결과는 어떻게 될까요?



```java
    String a = new String("aaa");
    String b = new String("aaa");

    System.out.println(a == b ? "true" : "false");				// false
    System.out.println(a.equals(b) ? "true" : "false");		// true
```



출력 결과는 `false` 와 `true` 입니다. 위에서 말했던 것처럼 `a`와 `b`의 값은 같지만, 모두 새로운 객체 (`new String()`) 를 만들어서 그 곳의 주소를 가리키기 때문입니다.



그럼 다음 코드의 결과는 어떻게 될까요?



```java
    String a = "aaa";
    String b = "aaa";
    System.out.println(a == b ? "true" : "false");
```



값은 `true` 입니다. 아니 `equals()` 메소드가 아닌 `==` 연산으로 비교했음에도 `true`가 나오는 이유가 뭘까요?



답은 **String Constant Pool** 때문입니다.



String 객체를 생성하는 방법은 크게 두 가지가 있습니다.



1.  `new` 연산자 방식
2.  literal을 이용한 방식



이 중 literal 로 문자열을 선언한 경우, String 객체는 내부적으로 **`intern()`**이란 메소드를 호출합니다. 이 `intern()` 메소드는 **JVM에서 관리하는 String Constant Pool란 곳에서 해당 문자열을 조회하여 존재하는 경우 그 주소값을 반환하고, 아닌 경우 Pool에 문자열을 등록하고 해당 문자열의 주소를 반환**합니다.

`intern()` 메서드를 살펴 보면 `public native String intern();` 로 선언이 되어있습니다. native 키워드는 자바 프로그램에서 다른 언어(C, C++, 어셈블리 등)로 작성된 코드를 실행할 수 있는 JNI(Java Native Interface) 키워드를 말합니다.

이러한 동작 원리에 따라 `String b`를 선언하고 `"aaa"`로 초기화할 때, intern() 메소드가 **String Constant Pool**에서 "aaa"를 찾아서 이 주소값을 리턴하므로 ==(주소값 비교) 연산의 결과가 `true` 가 되는 것이지요.



그럼 **intern() 메소드를 명시적으로 호출하는 경우**에는 어떻게 될까요?



```java
  String a = new String("aaa"); 
  String b = "aaa"; 
  String c = a.intern();

  System.out.println(a == b); // false
  System.out.println(b == c); // true
```



new 연산자로 선언한 a와 literal로 선언한 b 객체의 주소값은 다르지만, b와 a의 intern() 메소드를 호출한 주소값은 같은 것을 볼 수 있습니다.



#### [String Constant Pool의 위치]



Java 6까지 String Constant Pool의 위치는 Permanenet Generation 영역이었습니다. 하지만 이는 `intern()` 메소드를 호출하는 것이 `OutOfMemoryException` 을 발생시킬 수 있었고, Java 7부터는 Perm 영역이 아닌 Heap 영역에 위치하게 되었습니다. 이로써, String Constant Pool의 모든 문자열들도 GC의 대상이 될 수 있게 되었습니다.













**Reference**

https://www.baeldung.com/java-string-constant-pool-heap-stack

[https://bbchu.tistory.com/13](https://bbchu.tistory.com/13)

[https://www.latera.kr/blog/2019-02-09-java-string-intern/](https://www.latera.kr/blog/2019-02-09-java-string-intern/)

https://stackoverflow.com/questions/4918399/where-does-javas-string-constant-pool-live-the-heap-or-the-stack
