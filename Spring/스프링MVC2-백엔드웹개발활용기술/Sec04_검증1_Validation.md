## 검증 요구사항

검증 로직

- 타입
- 필드
- 범위..

웹 서비스는 폼 입력 시 오류가 발생하면 고객이 입력한 데이터를 유지한 채로 어떤 오류가 발생했는지 알려주어야 한다.

**컨트롤러의 중요한 역할 중 하나가 HTTP 요청이 정상인지 검증하는 것!**



##### 참고: 클라이언트 검증, 서버 검증

- 클라이언트 검증은 조작할 수 있으므로 보안에 취약
- 서버만으로 검증하면 즉각적인 고객 사용성이 부족해짐
- 둘을 적절히 섞어 사용하되, 최종적으로 서버 검증 필수
- API 방식을 이용하면 스펙을 잘 정의해서 검증 오류를 API 응답 결과에 잘 넘겨주어야 함



## 검증 직접 처리

상품 저장 컨트롤러에서 validation 필요!

- 검증 오류 결과를 담아주는 객체 필요
- 우리가 간단하게 만든다면.. HashMap 요런데다가 넣을 수 있겠다. 얘가 있다면(검증하다가 잘못 된 부분이 있다면) `model`에 넣고 다시 상품 등록 폼이 있는 뷰 템플릿으로 이동



#### 정리

- 검증 오류가 발생하면 입력 폼을 다시 보여준다
- 검증 오류를 고객에게 안내해서 다시 제대로 입력할 수 있게 해준다
- 검증 오류가 발생해도 고객이 입력한 데이터가 유지된다

 

#### 남은 문제점

- 뷰 템플릿에서 중복 처리가 많다
- 타입 오류 처리가 안된다. 숫자 타입에 문자가 들어오는 등의 오류는 스프링 MVC에서 컨트롤러에 진입하기 전에 예외가 발생하기 때문에 컨트롤러가 호출되지도 않고 400 예외가 발생하면서 오류 페이지를 띄운다.
- 위의 경우에도 고객이 입력한 문자를 화면에 남겨야 하므로 별도로 관리가 되어야 한다.



## BindingResult

스프링이 제공하는 검증 오류 처리 방법의 핵심!!

``` java
public String addItem(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName", "상품 이름 필수"));
        }
  
			  //특정 필드 예외가 아닌 전체 예외
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
            }
        }
  
			  if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "validation/v2/addForm";
        }
}
```

- `BindingResult`: `item`에 바인딩된 결과. `errors` 역할
- `FieldError`: 필드에 대한 에러. `public FieldError(String objectName, String field, String defaultMessage) {}`
- `ObjectError`: 필드에 대한 에러가 아닌 에러. `public ObjectError(String objectName, String defaultMessage) {}`
- `BindingResult`는 뷰에 함께 넘어가기 때문에 `model`에 담지 않아도 된다.
- `BindingResult bindingResult` 파라미터의 위치는 `@ModelAttribute Item item` 바로 다음에 와야 한다. `item` 객체에 대한 바인딩 결과를 담기 때문



### BindingResult

- 스프링ㅇ이 제공하는 **검증 오류**를 보관하는 객체.
- `BindingResult` 가 있으면 `@ModelAttribute` 에 데이터 바인딩 시 오류가 발생해도 컨트롤러가 호출된다!
  - `BindingResult` 없으면: 400 오류 발생하면서 컨트롤러 미호출, 오류 페이지 이동
  - `BindingResult` 있으면: 오류 정보(`FieldError`)를 `BindingResult`에 담아서 컨트롤러 정상 호출



#### BindingResult에 검증 오류 적용하는 세 가지 방법

- `@ModelAttribute`의 객체에 타입 오류 등으로 바인딩이 실패하는 경우, 스프링이 `FieldError` 생성해서 `BindingResult`에 넣기
- 개발자가 직접 넣기
- `Validator` 사용 -> 뒤에서 설명



#### 주의

- `BindingResult`는 그래서 순서가 중요하다. 검증할 대상 바로 다음에 와야 한다.
- `BindingResult`는 Model에 자동으로 포함된다.



## FieldError, ObjectError

`FieldError` 생성자 파라미터 중 `rejectedValue` 에 사용자가 입력한 값(거절된 값)을 추가.

`FieldError`가 사용자가 입력한 값(`rejectedValue`)을 갖고 있다.



## 오류 코드와 메시지 처리



## 오류 코드와 메시지 처리

### V1

`FieldError`, `ObjectError` 의 생성자는 `errorCode`, `arguments` 파라미터를 제공하는데, 오류 발생 시 오류 코드로 메시지를 찾기 위해 사용된다.



errors.properties

```properties
required.item.itemName=상품 이름은 필수입니다.
range.item.price=가격은 {0} ~ {1} 까지 허용합니다.
max.item.quantity=수량은 최대 {0} 까지 허용합니다.
totalPriceMin=가격 * 수량의 합은 {0}원 이상이어야 합니다. 현재 값 = {1}
```



.java

``` java
bindingResult.addError(new FieldError("item", "itemName",
                item.getItemName(), false, new String[]{"required.item.itemName"}, null,
                null));
bindingResult.addError(new FieldError("item", "price", item.getPrice(),
                false, new String[]{"range.item.price"}, new Object[]{1000, 1000000}, null));
```

- `codes`: `required.item.itemName` 을 사용해서 메시지 코드 지정. 메시지 코드는 배열로 여러 값을 전달할 수도 있는데, 순서대로 매칭해서 처음 매칭되는 메시지가 사용된다.
- `arguments`: `Object[]{1000, 1000000}`를 사용해서 코드의 `{0}`, `{1}`로 치환할 값을 전달한다.



### v2

오류 코드 좀 더 자동화할 수 있지 않을까?



컨트롤러에서 `BindingResult` 는 검증해야 할 객체인 `target` 바로 다음에 온다. 따라서 `BindingResult` 는 이미 본인이 검증해야 할 객체인 `target` 을 알고 있다.



#### `rejectValue()`, `reject()`

- `FieldError`, `ObjectError`를 직접 생성하지 않고 검증 오류를 다룰 수 있다.



``` java
rejectValue() void rejectValue(@Nullable String field, String errorCode, @Nullable Object[] errorArgs, @Nullable String defaultMessage);
```

-  field : 오류 필드명 
- errorCode : 오류 코드(이 오류 코드는 메시지에 등록된 코드가 아니다. 뒤에서 설명할 messageResolver를 위한 오류 코드이다.) 
- errorArgs : 오류 메시지에서 {0} 을 치환하기 위한 값 
- defaultMessage : 오류 메시지를 찾을 수 없을 때 사용하는 기본 메시지



``` java
            bindingResult.rejectValue("itemName", "required");
```

- `required.item.itemName` 이라고 명시하지 않아도 알아서 찾아서 출력한다. `MessageCodesResolver` 가 해결해준다.



### v3

오류코드를 어떻게 만드는 것이 좋을까? 디테일하게? 단순하게?

단순하게 만들면 범용성이 좋지만, 세밀하게 작성하기 어렵다. 반대로 너무 자세하게 만들면 범용성이 떨어진다.

가장 좋은 방법은 범용성이 좋게 만들어서 사용하다가, 세밀하게 작성해야 하는 경우, 세밀한 내용이 적용되도록 메시지에 **단계**를 두는 법이 있다.



``` properties
#Level1
required.item.itemName: 상품 이름은 필수 입니다.
#Level2
required: 필수 값 입니다.
```

`MessageCodesResolver`가 만들어준다~~

Ex) `new String[] {"required.item.itemName", "required"};`



### v4

`MessageCodesResolver codesResolver = new DefaultMessageCodesResolver();`

`MesageCodesResolver`: 검증 오류 코드로 메시지 코드들 생성한다.



##### `DefaultMessageCodesResolver`의 기본 메시지 생성 규칙

객체 오류

1. `code + "." + object name`
2. `code`



필드 오류

1. `code + "." + object name + "." + field`
2. `code + "." + field`
3. `code + "." + field type`
4. `code`



- `rejectValue()`, `reject()`는 내부에서 `MessageCodesResolver`를 사용한다. 여기서 메시지 코드들을 생성한다.
- `FieldError`, `ObjectError`의 생성자를 보면 오류 코드를 여러 개 가질 수 있는데, `MessageCodesResolver`로 생성된 순서대로 보관한다.



### v5

**핵심은 구체적인 것에서 ~> 덜 구체적인 것으로**

- 메시지와 관련된 공통 전략을 편리하게 도입할 수 있다.



``` java
if (!StringUtils.hasText(item.getItemName())) {
  bindingResult.rejectValue("itemName", "required");
}
// ==
// Empty, 공백같은 단순한 기능만 제공
ValidationUtils.rejectIfEmptyOrWhitespace(bindingResult, "itemName", "required");
```



### v6

#### 스프링이 직접 만든 오류 메시지 처리

검증 오류 코드

- 개발자가 직접 설정 -> `rejectValue()` 직접 호출
- 스프링이 직접 검증 오류에 추가 > 주로 타입 안 맞는 경우..



스프링은 타입 오류가 발생하면  `typeMismatch` 라는 오류 코드를 사용한다. `errors.properties`에 따로 메시지를 정의해주지 않았기 때문에 스프링이 생성한 기본 메시지가 출력된다.



``` proper
typeMismatch.java.lang.Integer=~~~
typeMismatch.item.price=~~~
```



## Validator 분리

**복잡한 검증 로직을 별도로 분리**하자.

현재 코드는 컨트롤러 단에 검증 로직이 차지하는 부분이 많다. 이럴 경우 별도의 클래스로 역할을 분리하는 것이 좋다.



#### `Validator` 인터페이스

```java
public interface Validator {
  // 사용하는 클래스가해당 검증기(Validator)를 지원하는가?
  boolean supports(Class<?> clazz);

  // 실제 검증 로직
  void validate(Object target, Errors errors);
}
```



```java
@PostMapping("/add")
public String addItemV5(@ModelAttribute Item item, BindingResult bindingResult,
    RedirectAttributes redirectAttributes) {

    itemValidator.validate(item, bindingResult);

    if (bindingResult.hasErrors()) {
        return "validation/v2/addForm";
    }
    //성공 로직
    Item savedItem = itemRepository.save(item);
    redirectAttributes.addAttribute("itemId", savedItem.getId());
    redirectAttributes.addAttribute("status", true);
    return "redirect:/validation/v2/items/{itemId}";
}
```



스프링이 `Validator` 인터페이스를 제공해주는 이유는 **체계적으로 검증 기능을 도입**하기 위해서이다. `Validator` 인터페이스의 구현체로 검증기를 만들면 **스프링의 도움을 받을 수 있다(알아서 호출 해줌)**!!



`WebDataBinder`: 스프링의 파라미터 바인딩의 역할을 해주고 검증 기능도 포함하고 있다.



```java
public class ValidationItemControllerV2 {

  private final ItemRepository itemRepository;
  private final ItemValidator itemValidator;

  @InitBinder
  public void init(WebDataBinder dataBinder) {
    log.info("init binder {}", dataBinder);
    dataBinder.addValidators(itemValidator);
  }
}
```

- 컨트롤러가 호출될 때마다 `WebDataBinder`가 새로 생성됨
- `WebDataBinder`에 검증기를 추가하면 해당 컨트롤러에서는 검증기를 자동으로 적용한다.
- `@InitBinder`: 해당 컨트롤러에만 적용



#### `@Validated` 적용



```java
@PostMapping("/add")
public String addItemV6(@Validated @ModelAttribute Item item, BindingResult
    bindingResult, RedirectAttributes redirectAttributes) {
  if (bindingResult.hasErrors()) {
    log.info("errors={}", bindingResult);
    return "validation/v2/addForm";
  }
  //성공 로직
  Item savedItem = itemRepository.save(item);
  redirectAttributes.addAttribute("itemId", savedItem.getId());
  redirectAttributes.addAttribute("status", true);
  return "redirect:/validation/v2/items/{itemId}";
}
```

- `@Validated`: 검증기를 실행하라는 어노테이션. 얘가 붙으면 `WebDataBinder`에 등록한 검증기를 찾아서 실행한다. 여러 검증기가 등록된다면 `supports()` 메서드를 통해서 구분된다.



`Validator`를 글로벌로 설정하고 싶으면 메인 컨트롤러에다가 추가하면 된다.
