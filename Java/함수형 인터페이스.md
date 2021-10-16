함수형 인터페이스 (Functional Interface)는 한 개의 추상 메소드를 갖고 있는 인터페이스를 말한다. 어노테이션 `@Functionalnterface` 를 붙여서 사용한다. Single Method Interface 라고도 하며, Single Abstract Method(SAM) 라고 불리기도 한다. 

함수형 인터페이스에서의 메소드 구현은 람다식으로 간단히 표현할 수 있다.



다음은 Function 인터페이스.

```java
@FunctionalInterface
public interface Function<T, R> {
	// 객체 T를 R로 매핑
	R apply(T t);
}
```



따라서,

`Function<Guestbook, GuestbookDTO> fn = (entity -> entityToDto(entity));`

에서는 entity를 파라미터로 받아서 변환된 DTO를 반환하는 부분(entityToDto())을 람다식으로 표현한 것!



