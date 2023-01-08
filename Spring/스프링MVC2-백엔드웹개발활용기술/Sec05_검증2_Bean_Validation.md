## Bean Validation 소개, 시작

특정 필드에 대한 검증 로직은 대부분 빈 값 여부, 특정 사이즈 범위 체크 등 매우 일반적인 로직이다. 이러한 검증 로직을 공통/표준화한 것이 **Bean Validation**이다. 얘를 잘 활용하면 애너테이션 하나로 검증 로직을 매우 편리하게 적용할 수 있다.



### Bean Validation

- 특정 구현체가 아니라 기술 표준 
- "검증 애너테이션과 여러 인터페이스의 모음". 
- Bean Validation을 구현한 기술 중 일반적으로 사용하는 구현체는 하이버네이트의 `Validator`이다.



``` java
public class Item {
  private Long id;
    
  @NotBlank
  private String itemName;
    
  @NotNull
  @Range(min = 1000, max = 1000000)
  private Integer price;
    
  @NotNull
  @Max(9999)
  private Integer quantity;
}
```

- `@NotBlank`: 빈값 + 공백만 있는 경우 허용 X
- `@NotNull`: `null` 허용 X
- `@Range(min = 1000, max =1000000)`: 범위 안 값만 허용
- `Max(9999)`: 최대 9999까지 허용



검증기 생성

``` java
ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
Validator validator = factory.getValidator();
```

- 스프링과 통합하면 직접 이런 코드를 작성하지 않아도 된다.



검증 실행

``` java
Set<ConstraintViolation<Item>> violations = validator.validate(item);
```

- 검증 대상(`item`)을 직접 검증기에 넣고 그 결과를 받는다. `Set`에는 `ConstraintViolation`이라는 검증 오류가 담긴다.



## 스프링 적용

스프링 MVC는 어떻게 Bean Validation을 사용할까?

- 스프링 부트가 `spring-boot-starter-validation` 라이브러리를 넣으면 자동으로 Bean Validator를 인지하고 스프링에 통합한다.
- `@Validated` 애너테이션이 정상 동작한다. 어떻게?
  - 스프링 부트가 `LocalValidatorFactoryBean`을 자동으로 글로벌 Validator로 등록한다. 얘가 애너테이션을 보고 Bean을 검증해주는 검증기이다. 검증 오류가 발생하면 `FieldError`, `ObjectError`를 생성해서 `BindingResult`에 저장한다.
- 주의!! 만약 글로벌 Validator를 직접 등록하면 스프링 부트는 Bean Validator를 글로벌 Validator로 등록하지 않기 때문에 애너테이션 기반의 빈 검증기가 동작하지 않게 된다.
- `@Validated`(Spring 전용), `Valid`(JAVA 표준) 둘 다 사용 가능하다.



### 검증 순서

1. `@ModelAttribute` 각각 필드에 타입 변환 시도
   1. 성공하면 다음으로
   2. 실패하면 `typeMismatch`로 `FieldError` 추가
2. Validator 적용

**\* 바인딩에 성공한 필드만 Bean Validation을 적용한다.**



## 에러 코드

Bean Validation이 기본으로 제공하는 오류 메시지를 좀 더 자세하게 변경하고 싶다!!!



`NotBlank` 오류 코드를 기반으로 `MessageCodesResolver`를 통해 다양한 메시지 코드가 순서대로 생성된다.

`@NotBlank`

- NotBlank.item.itemName 
- NotBlank.itemName
- NotBlank.java.lang.String 
- NotBlank



`@Range` 

- Range.item.price 
- Range.price 
- Range.java.lang.Integer 
- Range



뭐 이런 식으로...



Bean Validation 메시지 찾는 순서

1. 생성된 메시지 코드 순서대로 `messageSource`에서 찾아서 사용
2. 애너테이션의 `message` 속성 사용
3. 라이브러리가 제공하는 기본 값 사용



## 오브젝트 오류

특정 필드 에러가 아닌 해당 오브젝트 관련 오류`ObjectError`는 어떻게 처리할까?? > `@ScriptAssert()` 애너테이션을 사용하면 된다.



```java
@Data
@ScriptAssert(lang = "javascript", script = "_this.price * _this.quantity >=
    10000")
    public class Item {
    //...
}
```



근데 얘는 실무에서 사용하기에는 제약도 많고 복잡하다. 따라서 오브젝트 오류(글로벌 오류)의 경우, **오브젝트 오류 관련 부분만 직접 자바 코드로 작성하는 것을 권장**한다.



## groups

### Bean Validation의 한계

데이터를 등록할 때와 수정할 때 요구사항이 다를 수도 있다. 수정 시에는 id값을 필수로 받아와야 하고, 등록 시에는 그러지 않아도 된다고 할 때, `id` 필드에 `@NotNull` 애너테이션을 단다면 등록 시 오류가 날 것이다.



동일한 모델 객체를 등록할 때와 수정할 때 각각 다르게 검증하는 방법을 알아보자.

- BeanValidation의 groups 기능 사용
- `Item`을 직접 사용하지 않고, 각각의 폼 전송을 위한 별도의 모델 객체 만들어서 사용



### Bean Validation의 groups

등록 시에 등록할 때 검증할 기능과, 수정할 때 검증할 기능을 각각 그룹으로 나누어 적용 가능



저장용 groups

``` java
package hello.itemservice.domain.item;
public interface SaveCheck {
}
```



수정용 groups

``` java
package hello.itemservice.domain.item;
public interface UpdateCheck {
}
```



`Item` 모델 객체

``` JAVA
@Data
public class Item {
  @NotNull(groups = UpdateCheck.class) //수정시에만 적용
  private Long id;
  @NotBlank(groups = {SaveCheck.class, UpdateCheck.class})
  private String itemName;
  @NotNull(groups = {SaveCheck.class, UpdateCheck.class})
  @Range(min = 1000, max = 1000000, groups = {SaveCheck.class,
      UpdateCheck.class})
  private Integer price;
  @NotNull(groups = {SaveCheck.class, UpdateCheck.class})
  @Max(value = 9999, groups = SaveCheck.class) //등록시에만 적용
  private Integer quantity;
  public Item() {
  }
  public Item(String itemName, Integer price, Integer quantity) {
    this.itemName = itemName;
    this.price = price;
    this.quantity = quantity;
  }
}
```



사용

``` java
@PostMapping("/add")
public String addItemV2(@Validated(SaveCheck.class) @ModelAttribute Item item,
BindingResult bindingResult, RedirectAttributes redirectAttributes) {
 //...
}
```



- `@Valid`는 groups 기능이 없다. `@Validated`를 사용해야 한다.
- groups 쓰면 복잡도가 높아진다. 사실 groups 잘 안쓴다. 폼 객체를 분리해서 사용하도록 하자.



## Form 전송 객체 분리

실제로 등록과 수정 시 전달하는 데이터가 많이 다르기 때문에 실무에서는 groups를 잘 사용하지 않는다.

ex) `Item`을 직접 전달받는 것이 아니라, 복잡한 폼의 데이터를 컨트롤러까지 전달할 별도의 객체를 만들어서 전달한다.



폼 데이터 전달에 Item 도메인 객체 사용

`HTML Form -> Item -> Controller -> Item -> Repository` 

- 장점: Item 도메인 객체를 컨트롤러, 리포지토리 까지 직접 전달해서 중간에 Item을 만드는 과정이 없어서 간단하다. 
- 단점: **간단한 경우에만** 적용할 수 있다. 수정시 검증이 중복될 수 있고, groups를 사용해야 한다.

폼 데이터 전달을 위한 별도의 객체 사용 

`HTML Form -> ItemSaveForm -> Controller -> Item 생성 -> Repository` 

- 장점: 전송하는 폼 데이터가 복잡해도 거기에 맞춘 별도의 폼 객체를 사용해서 데이터를 전달 받을 수 있다. 보통 등록과, 수정용으로 별도의 폼 객체를 만들기 때문에 검증이 중복되지 않는다. 
- 단점: 폼 데이터를 기반으로 컨트롤러에서 `Item `객체를 생성하는 변환 과정이 추가된다.



## Bean Validation - HTTP 메시지 컨버터

`@Valid`, `@Validated`는 `HttpMessageConverter`(`@RequestBody`)에도 적용할 수 있다!!!



API의 경우 세 가지 경우를 나누어서 생각해야 한다.

- 성공 요청: 성공

- 실패 요청: JSON을 객체로 생성하는 것 자체가 실패

  `HttpMessageConverter` 에서 요청 JSON을 `ItemSaveForm` 객체로 생성하는데 실패한다. 이 경우는 `ItemSaveForm` 객체를 만들지 못하기 때문에 **컨트롤러 자체가 호출되지 않고 그 전에 예외가 발생**한다. Validator도 실행되지 않는다. 

- 검증 오류 요청: JSON을 객체로 생성은 성공, 검증 실패

  `return bindingResult.getAllErrors();`는 `ObjectError`와 `FieldError` 모두 반환한다. 스프링이 이 객체를 JSON으로 변환해서 클라이언트에게 전달한다. 실제 개발할 때는 이 객체를 그대로 사용하지 말고, 필요한 데이터만 뽑아서 별도의 API 스펙을 정의하고, 그에 맞는 객체를 만들어서 반환해야 한다.



### `@ModelAttribute` vs `@RequestBody`

`@ModelAttribute`

- HTTP 요청 파라미터 처리
- 각각의 필드 단위로 세밀하게 적용된다. 그래서 특정 필드 타입 오류가 발생해도 나머지 필드는 정상 처리된다.

`@RequestBody`

- `HttpMessageConverter`는 전체 객체 단위로 적용된다. 그래서 메시지 컨버터가 정상 동작해서 `ItemSaveForm` 객체를 만들어야 검증(`@Valid`, `@Validated`)이 적용된다.