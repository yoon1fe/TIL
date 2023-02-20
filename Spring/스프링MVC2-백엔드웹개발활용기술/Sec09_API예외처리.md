API 예외 처리는 어떻게 해야 할까??

오류 페이지는 단순히 고객에게 오류 화면을 보여주면 끝이지만, **API는 각 오류 상황에 맞는 오류 응답 스펙을 정하고, JSON으로 데이터를 내려주어야 한다.**



`@RequestMapping(value = "/error-page/500", produces = MediaType.APPLICATION_JSON_VALUE)`

- 클라이언트가 요청하는 HTTP Header의 `Accept` 값이 `application/json` 이라면 해당 메서드 호출. 즉, 클라이언트가 받고자 하는 미디어 타입이 json 이라면 이 컨트롤러의 메서드가 호출되는 것.

`ResponseEntity`

- 메시지 컨버터가 동작하면서 클라이언트에 JSON이 반환된다.



## 스프링 부트 기본 오류 처리

스프링 부트에서는 `BasicErrorController`로 기본적인 에러를 처리해준다.!!



**BasicErrorController**

``` java
	@RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse
			response) {}
	@RequestMapping
	public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {}
```

- `errorHtml()`: `produces = MediaType.TEXT_HTML_VALUE`: 클라이언트 요청의 Accept 헤더 값이 `text/html` 인 경우에는 `errorHtml()` 을 호출해서 view 제공
- `error()`: 그 외의 경우 `ResponseEntity`로 HTTP 바디에 JSON 데이터 반환



## HandlerExceptionResolver

예외가 발생해서 서블릿을 넘어 WAS까지 예외가 전달되면 HTTP 상태 코드가 500으로 처리된다. 근데 발생하는 예외에 따라서 다른 상태 코드로 처리하고 싶다. 어떻게 하지??



**HandlerExceptionResolver**

스프링 MVC 는 `HandlerExceptionResolver` 를 통해 컨트롤러(핸들러) 밖으로 예외가 던져진 경우 예외를 해결하고, 동작을 새로 정의할 수 있는 방법을 제공한다.



``` java
package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HandlerExceptionResolver {
  ModelAndView resolveException(HttpServletRequest var1, HttpServletResponse var2, Object var3, Exception var4);
}

```

- `try - catch` 처럼 `Exception`을 처리해서 정상 흐름 처럼 변경하는 것이 목적이다. 그래서 `ModelAndView`를 반환해준다.
- 빈 `ModelAndView`를 반환하면 뷰를 렌더링 하지 않고, 정상 흐름으로 서블릿이 리턴된다.
- `null`을 반환하면 다음 `ExceptionResolver`를 찾아서 실행한다. 처리할 수 있는 `ExceptionResolver`가 없으면 예외 처리가 안되고, 기존에 발생한 예외를 서블릿 밖으로 던진다.



`HandlerExceptionResolver` 등록 - `WebMvcConfigurer`에 등록

``` java
@Override
public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver>
resolvers) {
 resolvers.add(new MyHandlerExceptionResolver());
}
```

- `configureHandlerExceptionResolvers()` 메서드도 있는데, 얘를 사용하면 스프링이 기본으로 등록하는 `ExceptionResolver`가 제거되므로 주의하자.



예외가 발생하면 WAS까지 예외가 던져지고, WAS에서 오류 페이지 정보를 찾아서 다시 `/error`를 호출하는 과정은 너무 번거롭다. `ExceptionResolver`를 활용해서 예외가 발생했을 때 여기서 문제를 깔끔하게 해결하자.



근데 직접 `ExceptionResolver`를 구현하는 것도 복잡하다. 스프링이 제공해주는 `ExceptionResolver`들을 알아보자!



## 스프링이 제공하는 ExceptionResolver

스프링 부트가 기본으로 제공하는 `ExceptionResolver` - `HandlerExceptionResolverComposite`에 다음 순서로 등록

- 





1. `ExceptionHandlerExceptionResolver`
   - `@ExceptionHandler` 처리한다. API 예외 처리 대부분은 이 기능으로 해결한다.
2. `ResponseStatussExceptionResolver`
   - HTTP 상태 코드 지정해준다.
   - ex) `@ResponseStatus(value = HttpStatus.NOT_FOUND)`
3. `DefaultHandlerExceptionResolver`
   - 스프링 내부 기본 예외를 처리한다.



**ResponseStatussExceptionResolver**

예외에 따라서 HTTP 상태 코드를 지정해준다. 다음 두 가지 경우를 처리한다

- `@ResponseStatus`가 달려있는 예외
  - 개발자가 직접 변경할 수 없는 예외에는 적용할 수 없다. 그리고 애너테이션을 사용하기 때문에 조건에 따라 동적으로 변경하기도 어렵다.
- `ResponseStatusException` 예외



**DefaultHandlerExceptionResolver**

스프링 내부에서 발생하는 스프링 예외를 해결해준다. 파라미터 바인딩 시점에 타입이 맞지 않으면 내부에서 `TypeMismatchException`이 발생하면 이 리졸버가 HTTP 상태 코드 400 오류로 변경해서 내려보내 준다. 클라이언트가 요청을 잘못 한거니깐!!



스프링은 `@ExceptionHandler`라는 매우 혁신적인 예외 처리 기능을 제공한다. 얘가 바로 `ExceptionHandlerExceptionResolver` 이다.



## @ExceptionHandler

웹 브라우저에 HTML 화면을 제공할 때 오류가 발생하면 `BasicErrorController`를 사용하면 편하다. 

반면에 API는 시스템마다 스펙이 모두 달라서 각각 세밀한 제어가 필요하다.



**API 예외 처리의 어려운 점**

- `HandlerExceptionResolver`에서는 `ModelAndView`를 반환해야 했다. 이는 API 응답에는 적절치 않다.
- API 응답을 위해서 `HttpServletResponse`에 직접 응답 데이터를 넣어주어야 한다.
- 특정 컨트롤러에서만 발생하는 예외를 별도로 처리하기 어렵다.



**`@ExceptionHandler`**

스프링은 `ExceptionHandlerExceptionResolver`를 기본으로 제공한다.

애너테이션 선언하고, 해당 컨트롤러에서 처리하고 싶은 예외를 지정해주면 된다. 그러면 해당 컨트롤러에서 예외가 발생하면 이 메서드가 호출된다.



``` java
@ExceptionHandler(IllegalArgumentException.class)
public ErrorResult illegalExHandle(IllegalArgumentException e) {
 log.error("[exceptionHandle] ex", e);
 return new ErrorResult("BAD", e.getMessage());
}
```

- 여러 예외 처리 가능 (`@ExceptionHandler({AException.class, BException.class})`)
- 예외 생략하면 메서드 파라미터의 예외가 지정된다



## @ControllerAdvice

`@ExceptionHander`는 정상 코드와 예외 처리 코드가 하나의 컨트롤러에 섞여 있다. `@ControllerAdvice` 또는 `@RestControllerAdvice`를 사용하면 둘을 분리할 수 있다.



**`@ControllerAdvice`**

- 대상으로 지정한 여러 컨트롤러에 `@ExceptionHandler`, `@InitBinder` 기능을 부여한다.
- `@ControllerAdvice`에 대상을 지정하지 않으면 모든 컨트롤러에 적용
- `@RestControllerAdivce`는 `@ControllerAdvice`에 `@ResponseBody` 추가된 버전.



**대상 컨트롤러 지정 방법**

``` java
// Target all Controllers annotated with @RestController
@ControllerAdvice(annotations = RestController.class)
public class ExampleAdvice1 {}

// Target all Controllers within specific packages
@ControllerAdvice("org.example.controllers")
public class ExampleAdvice2 {}

// Target all Controllers assignable to specific classes
@ControllerAdvice(assignableTypes = {ControllerInterface.class,
AbstractController.class})
public class ExampleAdvice3 {}
```