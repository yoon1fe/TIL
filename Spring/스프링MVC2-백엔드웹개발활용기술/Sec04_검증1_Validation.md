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







## FieldError, ObjectError





## 오류 코드와 메시지 처리





## Validator 분리



