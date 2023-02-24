## 스프링 타입 컨버터

문자 <-> 숫자 와 같이 타입 변환해야 하는 경우가 많다.



**String data = request.getParameter("data")**

HTTP 요청 파라미터는 모두 문자로 처리된다. 따라서 요청 파라미터를 자바에서 다른 타입으로 변환해서 사용하고 싶으면 변환하는 과정을 거쳐야 한다.



**@RequestParam**

스프링이 중간에서 타입을 변환해준다.

- `@ModelAttribute`, `@PathVariable`도 마찬가지



**스프링의 타입 변환 적용 예**

- 스프링 MVC 요청 파라미터
  - `@RequestParam`, `@ModelAttribute`, `@PathVariable`
- `@Value` 등으로 YML 정보 읽기
- XML에 넣은 스프링 빈 정보를 변환
- 뷰를 렌더링할 때



그럼 개발자가 새로운 타입을 만들어서 변환하고 싶으면 어떻게 할까??

스프링은 확장 가능한 컨버터 인터페이스를 제공한다. 이 컨버터 인터페이스를 구현해서 등록하면 된다.





## 타입  컨버터 - Converter

타입 컨버터를 사용하려면 `org.springframework.core.convert.converter.Converter` 인터페이스를 구현하면 된다.



근데 이렇게 타입 컨버터를 하나하나 직접 등록/사용하면 개발자가 직접 컨버팅하는 것과 큰 차이가 없다. 타입  컨버터를 등록하고 관리하면서 편리하게 변환 기능을 제공하는 역할을 하는 무언가가 있으면 좋겠다.



## 컨버전 서비스 - ConversionService

스프링은 개별 컨버터를 모아두고 그것들을 묶어서 편리하게 사용할 수 있는 기능을 제공한다. 요것이 바로 컨버전 서비스(`ConversionService`)



``` java
package org.springframework.core.convert;

import org.springframework.lang.Nullable;

public interface ConversionService {

	boolean canConvert(@Nullable Class<?> sourceType, Class<?> targetType);

	boolean canConvert(@Nullable TypeDescriptor sourceType, TypeDescriptor targetType);

	@Nullable
	<T> T convert(@Nullable Object source, Class<T> targetType);

	@Nullable
	Object convert(@Nullable Object source, @Nullable TypeDescriptor sourceType, TypeDescriptor targetType);

}

```

- 사용: `Integer value = conversionService.convert("10", Integer.class)`



## 스프링에 Converter 적용

스프링은 내부에서 `ConversionSservice`를 제공한다. 우리는 `WebMvcConfigurer`가 제공하는 `addFormatters()`를 사용해서 추가하고 싶은 컨버터를 등록하면 된다. 그럼 스프링은 내부에서 사용하는 `ConversionService`에 컨버터를 추가해준다.



`@RequestParam` 을 처리하는 ArgumentResolver인 `RequestParamMethodArgumentResolver`에서 `ConversionService`를 사용해서 타입을 변환한다.



## 뷰 템플릿에 컨버터 적용

타임리프에서는 `${{...}}`를 사용하면 자동으로 컨버전 서비스를 사용해서 변환된 결과를 출력해준다.

- 변수 표현식: `${...}`
- 컨버전 서비스 적용: `${{...}}`

`th:field` 에는 자동으로 컨버전 서비스가 적용된다.



## 포맷터 - Formatter

`Converter`는 입력과 출력 타입에 제한이 없는 **범용 타입 변환 기능**을 제공한다. 보통 웹 애플리케이션에서는 문자 <-> 다른 타입 으로 변환하는 경우가 많다.

Ex) 

- `1000` <-> `1,000`
- 날짜 객체 <-> `2023-01-01`



객체를 특정한 포맷에 맞추어 문자로 출력하거나 그 반대 역할을 하는 것에 특화된 기능이 바로 포맷터(`Formatter`)라고 한다. 컨버터의 특별한 버전이라고 이해하면 됨



**Converter vs Formatter**

- `Converter`는 범용 (객체 -> 객체)
- `Formatter`는 문자에 특화 + 현지화 (Locale)



**Formatter 인터페이스**

``` java
public interface Printer<T> {
	String print(T object, Locale locale);
}

public interface Parser<T> {
	T parse(String text, Locale locale) throws ParseException;
}

public interface Formatter<T> extends Printer<T>, Parser<T> {
}
```

- `String print(T object, Locale locale)`: 객체 -> 문자
- `T parse(String text, Locale locale)`: 문자 -> 객체



스프링은 용도에 따라 다양한 방식의 포맷터를 제공한다.



## 포맷터를 지원하는 컨버전 서비스

포맷터를 지원하는 컨버전 서비스를 사용하면 컨버전 서비스에 포맷터를 추가하여 사용할 수 있다. 내부에서 어댑터 패턴을 사용해서 `Formatter`가 `Converter` 처럼 동작하도록 지원한다. 

`FormattingConversionService`: 포맷터를 지원하는 컨버전 서비스

`DefaultFormattingConversionService`: `FormattingConversionService`에 기본적인 통화, 숫자 관련 몇가지 기본 포맷터 추가 제공



## 스프링이 제공하는 기본 포맷터

스프링은 자바에서 기본으로 제공하는 타입들에 대해 많은 포맷터를 기본으로 제공한다. 근데 포맷터는 기본 형식이 지정되어 있기 때문에 객체의 각 필드마다 다른 형식응로 포맷을 지정하기 어렵다.



스프링은 위의 문제를 해결하기 위해 애너테이션 기반으로 원하는 형식을 지정해서 사용할 수 있는 매우 유용한 포맷터 두 개를 기본으로 제공한다.

- `@NumberFormat`
- `@DateTimeFormat`



``` java
static class Form {
 @NumberFormat(pattern = "###,###")
 private Integer number;

 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
 private LocalDateTime localDateTime;
 }
```



**주의**

메시지 컨버터(`HttpMessageConverter`)에는 컨버전 서비스가 적용되지 않는다. 얘 역할은 **HTTP 메시지 바디의 내용을 객체로 변환하거나, 객체를 HTTP 메시지 바디에 입력하는 것!!**
