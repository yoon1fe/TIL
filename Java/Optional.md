`Optional<T>` 클래스는 Integer나 Double 클래스처럼 'T'타입의 객체를 포장해 주는 래퍼 클래스(Wrapper class)입니다. 이러한 Optional 객체를 사용하면 예상치 못한 NullPointerException 예외를 제공되는 메소드로 간단히 피할 수 있습니다. 즉, 복잡한 조건문 없이도 널(null) 값으로 인해 발생하는 예외를 처리할 수 있게 됩니다.



오늘은 이러한 Optional 을 여러 주제로 정리해보겠습니다.



## 1. Optional.of() vs Optional.ofNullable()

먼저 of() 메서드와 ofNullable() 의 코드를 봅시다.

```java
/**
 * Returns an {@code Optional} describing the given non-{@code null}
 * value.
 *
 * @param value the value to describe, which must be non-{@code null}
 * @param <T> the type of the value
 * @return an {@code Optional} with the value present
 * @throws NullPointerException if value is {@code null}
 */
public static <T> Optional<T> of(T value) {
    return new Optional<>(value);
}

/**
 * Returns an {@code Optional} describing the given value, if
 * non-{@code null}, otherwise returns an empty {@code Optional}.
 *
 * @param value the possibly-{@code null} value to describe
 * @param <T> the type of the value
 * @return an {@code Optional} with a present value if the specified value
 *         is non-{@code null}, otherwise an empty {@code Optional}
 */
public static <T> Optional<T> ofNullable(T value) {
    return value == null ? empty() : of(value);
}
```

of() 와 ofNullable()의 가장 큰 차이점은 **NullPointerException** 발생 유무입니다. 그렇다면 of(V) 는 V가 null이 아님이 확실한 경우에만 사용해야 하고, 그렇지 않다면 ofNullable()을 사용해야 합니다.



그럼 of() 메서드는 왜 따로 있는걸까요?? ofNullable() 메서드가 NPE에 대해 안전하면 얘만 쓰면 될텐데 말이져. 요런 궁금증을 저만 가진게 아니었나 봅니다. https://stackoverflow.com/questions/31696485/why-use-optional-of-over-optional-ofnullable 여기 똑같은 질문과 그에 대한 답변이 있습니다. 



> Your question is based on assumption that the code which may throw `NullPointerException` is worse than the code which may not. This assumption is wrong. If you expect that your `foobar` is never null due to the program logic, it's much better to use `Optional.of(foobar)` as you will see a `NullPointerException` which will indicate that your program has a bug. If you use `Optional.ofNullable(foobar)` and the `foobar` happens to be `null` due to the bug, then your program will silently continue working incorrectly, which may be a bigger disaster. This way an error may occur much later and it would be much harder to understand at which point it went wrong.



생각을 아주 단단히 잘못하고 있었습니다. 은연 중에 NPE가 발생하는 것이 무조건 안 좋다고 생각하고 있었는데, 로직 상 절대 null 값이 들어가지 않는 변수 `foobar` 의 경우 of()로 감싸는 것이 맞습니다. 그렇지 않다면 프로그램에 버그가 있어 `foobar` 에 null 값이 들어가더라도 잘못된 상태로 프로그램이 돌아갈테니깐요.

암튼 이렇게 Optional 은 NullPointerException 을 발생시키는 of() 와, 그렇지 않는 ofNullable() 메서드가 있답니다.



## 2. isPresent() - get(), orElse(), orElseGet(), orElseThrow()

isPresent() 메서드는 Optional 객체 안의 값이 null 이면 false, null 이 아니면 true 를 리턴합니다. 그리고 get() 메서드는 그 값을 리턴해주죠. 만약 리턴할 값이 없다면 NoSuchElementException을 던집니다.



``` java
    Optional<CompanyEntity> companyEntityOptional = companyRepository.findById("test");

    if (companyEntityOptional.isPresent()) {
      CompanyEntity companyEntity = companyEntityOptional.get();
    }
```



이런 식으로 CompanyEntity 객체를 받을 수 있습니다. 하지만 상당히 안이쁩니다. 그래서 Optional 에는 orElse(), orElseGet(), orElseThrow() 와 같은 메서드를 제공합니다.



``` java
    // null 인 경우 예외 처리
    CompanyEntity companyEntity = companyEntityOptional.orElseThrow(RuntimeException::new);

    // null 인 경우 다른 값 리턴
    CompanyEntity companyEntity = companyEntityOptional.orElse(null);
    
    // null 인 경우 빈 객체 리턴
    CompanyEntity companyEntity = companyEntityOptional.orElseGet(CompanyEntity::new);
```



orElseThrow() 에 아무 파라미터를 넣지 않으면 NoSuchElementException을 던진다고 하네요.



추가로, orElse(...) 메서드에서 ...는 Optional에 값이 있든 없든 무조건 실행이 됩니다. 따라서 ...가 새로운 객체를 생성하거나 새로운 연산을 수행하는 경우에는 orElse() 대신 orElseGet() 을 사용해야 합니다.

``` java
    // new CompanyEntity() 가 반드시 수행됨
    CompanyEntity companyEntity = companyEntityOptional.orElse(new CompanyEntity());

    // companyEntityOptional에 값이 없을 때만 new CompanyEntity() 수행됨
    CompanyEntity companyEntity = companyEntityOptional.orElseGet(CompanyEntity::new);
```



## 3. ifPresent()

만약 Optional 안에 있는 객체가 아닌 새로운 객체를 만들어 주어야 할 땐 어떻게 하는 것이 좋을까요?

위에서 봤던 isPresent() 를 사용해서 만들어 줄 수도 있겠습니다.

``` java
    Optional<CompanyEntity> companyEntityOptional = companyRepository.findById("test");
		Company company = new Company();

    if (companyEntityOptional.isPresent()) {
      company.setCompanyCode(companyEntityOptional.get().getCompanyCode());
      company.setCompanyName(companyEntityOptional.get().getCompanyName());
    }
```



역시 안이쁩니다. 여기서 우린 ifPresent() 메서드를 사용할 수 있습니다.

``` java
    companyEntityOptional.ifPresent(companyEntity -> {
      company.setCompanyCode(companyEntity.getCompanyCode());
      company.setCompanyName(companyEntity.getCompanyName());
    });
```



이런 식으로 말이죵.



추가로 정리할 내용이 생기면 더 추가해야겠습니다 .허허



##### Reference

http://tcpschool.com/java/java_stream_optional

https://stackoverflow.com/questions/31696485/why-use-optional-of-over-optional-ofnullable

https://www.latera.kr/blog/2019-07-02-effective-optional/