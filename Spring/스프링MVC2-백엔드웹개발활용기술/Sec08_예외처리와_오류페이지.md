## 서블릿 예외 처리

스프링이 아닌 순수 서블릿 컨테이너에서는 예외를 어떻게 처리할까?

- `Exception` (예외)
- `HttpServletResponse.sendError`(HTTP 상태 코드, 오류 메시지)



**Exception**

- 자바 `main` 메서드 직접 실행
  - `main`이라는 이름의 스레드가 실행된다. 실행 도중에 예외 잡지 못하고 처음 실행한 `main()` 메서드를 넘어서 예외가 던져지면, 예외 정보를 남기고 해당 스레드는 종료.
- 웹 애플리케이션
  - **사용자 요청별로 별도의 스레드가 할당**되고, 서블릿 컨테이너 안에서 실행된다. 애플리케이션에선 예외가 발생했을 때, 안에서 `try - catch` 로 잡아서 예외를 처리하면 거기서 끝난다. 서블릿 밖까지 예외가 전달되면 어떻게 동작할까??
  - `컨트롤러(예외 발생) -> 인터셉터 -> 서블릿 -> 필터 -> WAS(여기까지 전파됨)`
  - 톰캣같은 WAS까지 전달된다. 그럼 WAS는 예외가 올라오면 어떻게 처리하지??



`HTTP Status 500 – Internal Server Error`

- `Exception`의 경우 서버 내부에서 처리할 수 없는 오류가 발생한 것으로 생각해서 HTTP 상태 코드 500을 리턴한다.



**response.sendError(HTTP 상태 코드, 오류 메시지)**

- 오류가 발생했을 때 `HttpServletResponse`가 제공하는 `sendError` 메서드 사용 가능. 얘를 호출한다고 해서 당장 예외가 발생하진 않지만, 서블릿 컨테이너에게 오류가 발생했다는 점을 전달할 수 있다.
- `response.sendError(HTTP 상태코드)`
- `response.sendError(HTTP 상태코드, 오류 메시지)`

- `sendError` 흐름
  - `컨트롤러(response.sendError) -> 인터셉터 -> 서블릿 -> 필터 -> WAS(sendError 호출 기록 확인)`
  - `sendError()`를 호출하면 `response` 내부에는 오류가 발생했다는 상태를 저장해둔다. 그리고 서블릿 컨테이너는 고객에게 응답 전에 `response`에 `sendError()`가 호출되었는지 확인한다. 호출되었다면 설정한 오류 코드에 맞추어 기본 오류 페이지 노출.



### 오류 화면 제공

예전에는 `web.xml` 파일에 오류 화면을 등록했었다.

이번 프로젝트에서는 스프링 부트가 서블릿 컨테이너(톰캣)를 제공해주기 때문에, 스프링 부트가 제공하는 기능을 사용해서 서블릿 오류 페이지를 등록한다.



```java
@Component
public class WebServerCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

  @Override
  public void customize(ConfigurableWebServerFactory factory) {
    ErrorPage errorPage404 = new ErrorPage(HttpStatus.NOT_FOUND, "/errorpage/404");
    ErrorPage errorPage500 = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error-page/500");
    ErrorPage errorPageEx = new ErrorPage(RuntimeException.class, "/error-page/500");
    factory.addErrorPages(errorPage404, errorPage500, errorPageEx);
  }
}
```

- `response.sendError(404)`: `errorPage404`(`/errorpage/404` 핸들러) 호출
- `response.sendError(500)`: `errorPage500` 호출
- `RuntimeException` 또는 그 자식 타입의 예외: `errorPageEx` 호출



**에러 페이지 컨트롤러**

```java
@Controller
public class ErrorPageController {

  @RequestMapping("/error-page/404")
  public String errorPage404(HttpServletRequest request, HttpServletResponse response) {
    log.info("errorPage 404");
    return "error-page/404";
  }

  @RequestMapping("/error-page/500")
  public String errorPage500(HttpServletRequest request, HttpServletResponse response) {
    log.info("errorPage 500");
    return "error-page/500";
  }
}
```



### 오류 페이지 작동 원리

서블릿은 예외가 발생해서 서블릿 밖으로 전달되거나 `response.sendError()` 가 호출되었을 때 설정된 오류 페이지를 찾는다.



**예외 발생 흐름 **

```
WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생) 
```



**sendError 흐름** 

```
WAS(sendError 호출 기록 확인) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러 (response.sendError())
```



WAS는 해당 예외를 처리하는 오류 페이지 정보를 확인한다.

`new ErrorPage(RuntimeException.class, "/error-page/500")`

- `RuntimeException`이 발생했을 때는 `/error-page/500` 으로 가라~ WAS는 오류 페이지를 출력하기 위해 `/error-page/500`을 다시 요청하는 것.



**예외 발생 / 오류 페이지 요청 흐름**

``` 
1. WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생)
2. WAS /error-page/500 다시 요청 -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러(/error-page/500) -> View
```

- 내부에서 오류 페이지 호출할 때 필터, 서블릿, 인터셉터, 컨트롤러 모두 다시 호출된다!



**중요한 점은 웹 브라우저(클라이언트)는 서버 내부에서 이런 일이 일어나는지 전혀 모른다는 점! 오직 서버 내부에서 오류 페이지를 찾기 위해 추가적인 호출을 한다.**



**오류 정보 추가**

WAS는 오류 페이지를 호출할 때 오류 정보를 `request`의 `attribute`에 담아서 넘겨준다.



### 필터

오류 페이지 호출할 때 필터, 서블릿, 인터셉터 한 번 더 호출하는게 비효율적이다!! 클라이언트로부터 온 요청인지, 오류 페이지 출력을 위한 내부요청인지 구분할 수 있어야 할텐데, 이를 위해서블릿에서는 `DispatcherType` 이라는 추가 정보를 제공한다.



**DispatcherType**

```java
package javax.servlet;

public enum DispatcherType {
  FORWARD,	// 서블릿에서 다른 서블릿이나 JSP 호출
  INCLUDE,	// 서블릿에서 다른 서블릿이나 JSP 결과를 포함할 때
  REQUEST,	// 클라이언트 요청
  ASYNC,		// 서블릿 비동기 호출
  ERROR;		// 오류 요청

  private DispatcherType() {
  }
}
```



`filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR);`

- 필터 등록할 때 요기 넣으면 해당 경우에 필터를 적용하도록 설정 가능하다. 특별히 오류 페이지 경로에 필터를 적용할 게 아니면 `DispatcherType.REQUEST`만 사용하면 된다. 참고로 디폴트값이 `DispatcherType.REQUEST` !



### 인터셉터

인터셉터는 스프링이 제공하는 기능이기 때문에 `DispatcherType`과는 무관하게 항상 호출된다. 대신 인터셉터를 사용할 땐 오류 페이지 경로를 `excludePathPatterns`로 빼주면 된다! 더 강력함 ㅎ



전체 흐름 **정리**

- `/hello` 정상 요청

  ```
  WAS (/hello, dispatcherType=REQUEST) -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러 -> View
  ```

  

- `/error-ex` 오류 요청

  - 필터는 `DispatcherType`으로 중복 호출 제거
  - 인터셉터는 경로 정보로 중복 호출 제거

  ```
  1. WAS(/error-ex, dispatchType=REQUEST) -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러
  2. WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생)
  3. WAS 오류 페이지 확인
  4. WAS(/error-page/500, dispatchType=ERROR) -> 필터(x) -> 서블릿 -> 인터셉터(x) ->
  컨트롤러(/error-page/500) -> View
  ```



## 스프링 부트

### 오류 페이지1

스프링 부트는 위에서 했던 과정을 모두 기본으로 제공한다~^^

- `ErrorPage`를 자동으로 등록한다. 이때 `/error`라는 경로로 기본 오류 페이지 설정.
- `BasicErrorController`라는 스프링 컨트롤러가 자동으로 등록된다.
  - `ErrorPage`에서 등록한 `/error`를 매핑해서 처리하는 컨트롤러



개발자는 **오류 페이지 화면**만 등록하면 된다!

`BasicErrorController`에 기본적인 로직이 다 개발되어 있다.



**뷰 선택 우선순위** - `BasicErrorController` 의 처리 순서

경로: `resources/templates/error/`

1. 뷰 템플릿
   - `500.html` -> `5xx.html`
2. 정적 리소스 (`static`, `public`)
3. 적용 대상이 없을 때는 뷰 이름 (`error`)

예외는 500으로 처리



**`BasicErrorController` 가 제공하는 기본 정보**

다음 정보를 `model`에 담아서 뷰에 전달한다. 고로 뷰 템플릿은 이 값을 활용해서 출력해줄 수 있다.

- timestamp: Fri Feb 05 00:00:00 KST 2021 

* status: 400 
* error: Bad Request 
* exception: org.springframework.validation.BindException 
* trace: 예외 trace
* message: Validation failed for object='data'. Error count: 1 
* errors: Errors(BindingResult) 
* path: 클라이언트 요청 경로 (`/hello`)



오류 관련 내부 정보를 고객에게 노출하는 것은 좋지 않다. 오류 정보를 `model`에 포함 여부를 `application.properties`에 설정할 수 있다.

사용자에게는 고객이 이해할 수 있는 간단한 오류 메시지와 적절한 오류 화면을 보여줘야 한다!!