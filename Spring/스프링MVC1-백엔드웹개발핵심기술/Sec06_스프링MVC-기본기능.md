## 로깅

#### 로깅 라이브러리

스프링 부트 라이브러리를 사용하면 `spring-boot-starter-logging` 이 포함되는데, 얘는 기본으로 다음 로깅 라이브러리를 사용한다.

- SLF4J
- Logback



로깅 라이브러리는 Logback, Log4J, Log4J2 등등이 있는데, 그것을 통합해서 인터페이스로 제공하는 것이 SLF4J 라이브러리이다.



단순히 `System.out.print` 로 출력하는 것과 달리, log를 이용하면 출력 시간, 실행 쓰레드 등등 다 나온다.

- 시간, 로그 레벨, 프로세스 ID, 쓰레드 명, 클래스명, 로그 메시지

```java
package hello.springmvc.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogTestController {

  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @RequestMapping("/log-test")
  public String logTest() {

    String name = "spring";
    System.out.println("name = " + name);
    log.info("log : {}", name);

    return "ok";
  }

}
```



```
name = spring
2022-08-15 15:59:21.565  INFO 12456 --- [nio-8080-exec-3] hello.springmvc.basic.LogTestController  : log : spring
```



또한, 로그 레벨별로 나눌 수 있다.

LEVEL: TRACE > DEBUG > INFO > WARN > ERROR

- 개발 서버는 debug 출력 
- 운영 서버는 info 출력

``` java
log.trace("trace log={}", name);
log.debug("debug log={}", name);
log.info("info log={}", name);
log.warn("warn log={}", name);
log.error("error log={}", name);
```



![image-20220815160228694](C:\Users\1Fe\AppData\Roaming\Typora\typora-user-images\image-20220815160228694.png)



#### 로그 레벨 설정

`application.properties`

``` pro
#전체 로그 레벨 설정(기본 info)
logging.level.root=info
#hello.springmvc 패키지와 그 하위 로그 레벨 설정
logging.level.hello.springmvc=debug
```



`private final Logger log = LoggerFactory.getLogger(getClass());` 대신 `@Slf4j`만 명시해줘도 된다.



#### 올바른 로그 사용법

- `log.debug("data = " + data);`
  - 로그 출력 레벨을 info로 설정해도 문자열 더하기 연산이 발생한다.
- `log.debug("data={}", data);`
  - 로그 출력 레벨을 info로 설정하면 아무 일도 발생하지 않는다. 따라서 의미 없는 연산이 발생하지 않는다.



#### 로그 사용시 장점

- 쓰레드 정보, 클래스 이름 같은 부가 정보를 함께 볼 수 있고, 출력 모양을 조정할 수 있다. 
- 로그 레벨에 따라 개발 서버에서는 모든 로그를 출력하고, 운영서버에서는 출력하지 않는 등 로그를 상황에 맞게 조절할 수 있다. 
- 시스템 아웃 콘솔에만 출력하는 것이 아니라, 파일이나 네트워크 등, 로그를 별도의 위치에 남길 수 있다. 특히 파일로 남길 때는 일별, 특정 용량에 따라 로그를 분할하는 것도 가능하다. 
- 성능도 일반 System.out보다 좋다. (내부 버퍼링, 멀티 쓰레드 등등) 그래서 실무에서는 꼭 로그를 사용해야 한다.



## 요청 매핑

- `@RestController`

  - `@Controller`는 반환 값이 String이면 뷰 이름으로 인식된다. 그래서 뷰를 찾고 뷰가 렌더링된다.
  - `@RestController`는 반환 값으로 뷰를 찾는 것이 아니라, **HTTP 메시지 바디에 바로 입력**한다.
    - `@ResponseBody`가 붙어있다.
  - `@RequestMapping("/hello-basic")`
    - /hello-basic URL 호출이 오면 이 메서드가 실행되도록 매핑한다.
    - 대부분의 속성을 배열로 제공하므로 다중 설정이 가능하다.

- HTTP 메서드

  - `@RequestMapping`에 `method` 속성으로 HTTP 메서드를 지정하지 않으면 HTTP 메서드와 무관하게 호출된다.
  - 축약 애너테이션도 있다. HTTP 메서드를 축약한 애너테이션을 사용하는 것이 더 직관적이다.
    - `@GetMapping`, `@PostMapping` 등등..

- `@PathVariable`(경로 변수) 사용

  - ``` java
    /**
     * PathVariable 사용
     * 변수명이 같으면 생략 가능
     * @PathVariable("userId") String userId -> @PathVariable userId
     */
    @GetMapping("/mapping/{userId}")
    public String mappingPath(@PathVariable("userId") String data) {
     log.info("mappingPath userId={}", data);
     return "ok";
    }
    ```



최근 HTTP API는 다음과 같이 리소스 경로에 식별자를 넣는 스타일을 선호한다.

- `/mapping/userA`
- `/users/1`



`@RequestMapping`은 URL 경로를 템플릿화할 수 있는데, `@PathVariable`을 사용하면 매칭되는 부분을 편리하게 조회할 수 있다.



- 특정 파라미터 조건 매핑

  - 특정 파라미터가 있거나 없는 조건을 추가할 수 있다. 잘 쓰진 않는다..

  - ``` java
    /**
     * 파라미터로 추가 매핑
     * params="mode",
     * params="!mode"
     * params="mode=debug"
     * params="mode!=debug" (! = )
     * params = {"mode=debug","data=good"}
     */
    @GetMapping(value = "/mapping-param", params = "mode=debug")
    public String mappingParam() {
     log.info("mappingParam");
     return "ok";
    }
    ```

- 특정 헤더 조건 매핑
  - `@GetMapping(value = "/mapping-header", headers = "mode=debug")`
  - 파라미터 매핑과 비슷하지만, HTTP 헤더를 사용한다.
- 미디어 타입 조건 매핑 
  - HTTP 요청 Content-Type, consume
    - `@PostMapping(value = "/mapping-consume", consumes = "application/json")`
    - HTTP 요청의 Content-Type 헤더를 기반으로 미디어 타입으로 매핑한다. 만약 맞지 않으면 HTTP 415 상태 코드를 반환한다.
  - HTTP 요청 Accept, produce
    - `@PostMapping(value = "/mapping-produce", produces = "text/html")`
    - Accept 헤더를 기반으로 미디어 타입으로 매핑한다. 맞지 않으면 406 상태 코드를 반환한다.



## 요청 매핑 - API 예시

URL 예시

- 회원 목록 조회: GET `/users `
- 회원 등록: POST `/users `
- 회원 조회: GET `/users/{userId} `
- 회원 수정: PATCH `/users/{userId} `
- 회원 삭제: DELETE `/users/{userId}`



머 요래 한다.



## HTTP 요청 - 기본, 헤더 조회

HTTP 헤더 정보를 조회하는 방법을 알아보자.

- `@RequestHeader MultiValueMap<String, String> headerMap`
  - 모든 HTTP 헤더를 MultiValueMap 형식으로 조회
- `@RequestHeader("host") String host`
  - 특정 HTTP 헤더를 조회
- `@CookieValue(value="myCookie", required = false) String cookie `
  - 특정 쿠키 조회



## HTTP 요청 파라미터 - 쿼리 파라미터, HTML Form

클라이언트에서 서버로 요청 데이터를 전달할 때는 주로 다음 세 가지 방법을 사용한다.

- GET - 쿼리 파라미터
  - /url?username=hello&age=20
  - 메시지 바디 없이, URL의 쿼리 파라미터에 데이터를 포함해서 전달 
  - 예) 검색, 필터, 페이징등에서 많이 사용하는 방식
- POST - HTML Form
  - content-type: application/x-www-form-urlencoded 
  - 메시지 바디에 쿼리 파리미터 형식으로 전달 `username=hello&age=20` 
  - 예) 회원 가입, 상품 주문, HTML Form 사용
- HTTP message body에 데이터를 직접 담아서 요청
  - HTTP API에서 주로 사용, 
  - JSON, XML, TEXT 데이터 형식은 주로 JSON 사용 
  - POST, PUT, PATCH



`HttpServletRequest`의 `request.getParameter()`를 사용하면 다음 두 가지 요청 파라미터를 조회할 수 있다.

- GET - 쿼리 파라미터 전송

  - `http://localhost:8080/request-param?username=hello&age=20`

- POST - HTML Form 전송

  - ``` http
    POST /request-param ...
    content-type: application/x-www-form-urlencoded
    username=hello&age=20
    ```



GET 쿼리 파리미터 전송 방식이든, POST HTML Form 전송 방식이든 둘다 형식이 같으므로 구분없이 조회할 수 있다. 이것을 간단히 **요청 파라미터(request parameter)** 조회라 한다.



## HTTP 요청 파라미터 - @RequestParam

스프링이 제공하는 `@RequestParam`을 사용하면 요청 파라미터를 매우 편리하게 사용할 수 있다.



- `@RequestParam`: 파라미터 이름으로 바인딩. 

  - `name` 속성이 파라미터 이름으로 사용된다.

  - `@RequestParam("username") String memberName` -> `request.getParameter("username")`
  - HTTP 파라미터 이름이 변수 이름과 같으면 `name` 생략 가능
  - String, int, Integer 등의 단순 타입이면 `@RequestParam` 도 생략 가능하다. 하지만 완전히 생략하는 것은 과하다는 생각도 든다. 해당 애너테이션이 있으면 명확하게 요청 파라미터에서 데이터를 읽는 다는 것을 알 수 있다.
  - `required` 속성이 true(default)라면 이 값이 꼭 들어와야 한다. 없으면 400(Bad Request) 예외가 발생한다.
  - false 인 파라미터가 만약 기본형(int같은..)이라면 500 에러가 난다. 기본형에는 `null`값이 들어갈 수 없기 때문!!
  - `defaultValue` 속성으로 디폴트 값을 넣어줄 수도 있다.
  - 파라미터를 Map, MultiValueMap으로 조회할 수도 있다.

- `ResponseBody`: View 조회를 무시하고 HTTP message body에 직접 해당 내용 입력



## HTTP 요청 파라미터 - @ModelAttribute

보통 개발을 할 때 요청 파라미터를 받아서 필요한 객체를 만들고 그 객체에 값을 넣어준다. 스프링은 이 과정을 완전히 자동화해주는 `@ModelAttribute` 기능을 제공한다.



``` java
@ResponseBody
@RequestMapping("/model-attribute-v1")
public String modelAttributeV1(@ModelAttribute HelloData helloData) {
 log.info("username={}, age={}", helloData.getUsername(),
helloData.getAge());
 return "ok";
}
```



스프링 MVC는 `@ModelAttribute`가 있으면 다음과 같이 실행된다.

1. `HelloData` 객체를 생성한다.
2. 요청 파라미터의 이름으로 `HelloData` 객체의 프로퍼티를 찾는다. 그리고 해당 프로퍼티의 setter를 호출해서 파라미터의 값을 입력(바인딩)한다.



만약 숫자가 들어가야 할 곳에 문자를 넣으면 `BindException`이 발생한다.



`@ModelAttribute`는 생략 가능하다.

스프링은 해당 생략 시 다음과 같은 규칙을 적용한다.

- String, int, Integer같은 단순 타입 = `@RequestParam`
- 나머지 = `@ModelAttribute`(argument resolver로 지정해둔 타입 외)



## HTTP 요청 메시지 - 단순 텍스트

요청 파라미터(query string)와는 다르게, HTTP 메시지 바디를 통해 데이터가 직접 넘어오는 경우에는 `@RequestParam`, `@ModelAttribute`를 사용할 수 없다. 그럼 어떻게 갖고 올 수 있을까??



- HTTP 메시지 바디의 데이터는 `InputStream`을 사용해서 직접 읽을 수 있다.



스프링 MVC는 다음 파라미터를 지원한다.

- `InputStream(Reader)`: HTTP 요청 메시지 바디의 내용을 직접 조회
- `OutputStream(Writer)`: HTTP 응답 메시지의 바디에 직접 결과 출력
- `HttpEntity`: HTTP header, body 정보를 편리하게 조회
  - 메시지 바디 정보를 직접 조회
  - 요청 파라미터를 조회하는 기능과 관련없다.
  - 응답에도 사용 가능하다.
    - 메시지 바디 정보 직접 반환
    - 헤더 정보 포함 가능
    - view 조회 X



`HttpEntity`를 상속받은 다음 객체들도 같은 기능을 제공한다.

- `RequestEntity` - HttpMethod, url 정보 추가됨
- `ResponseEntity` - HTTP 상태 코드 설정 가능



스프링 MVC 내부에서 HTTP 메시지 바디를 읽어서 문자나 객체로 변환해서 전다해주는데, 이때 HTTP 메시지 컨버터`HttpMessageConverter`라는 기능을 사용한다.



`@RequestBody`

- HTTP 메시지 바디 정보를 편리하게 조회할 수 있다.

`@ResponseBody`

- 응답 결과를 HTTP 메시지 바디에 직접 담아서 전달할 수 있다.



## HTTP 요청 메시지 - JSON

보통 JSON 데이터를 `ObjectMapper`를 통해 자바 객체로 변환한다. 근데 `@ModelAttribute`처럼 한 번에 객체로 변환하고 싶다!!

- `@RequestBody` 객체 파라미터
  - `@RequestBody HelloData data`
  - -> 직접 객체 지정
  - HTTP 메시지 컨버터가 JSON도 객체로 변환해준다.
  - 얘는 생략 불가능하다. `HelloData`인 경우 아무것도 명시해주지 않으면 `@ModelAttribute`가 적용되기 때문에 HTTP 메시지 바디가 아니라 요청 파라미터를 처리하게 된다!

참고로!! HTTP 요청 시에 `content-type`이 `application/json`인지 꼭 확인해야 한다. 그래야 JSON을 처리할 수 있는 HTTP 메시지 컨버터가 실행된다.



- `@RequestBody` 요청
  - JSON 요청 -> HTTP 메시지 컨버터 -> 객체
- `@ResponseBody` 응답
  - 객체 -> HTTP 메시지 컨버터 -> JSON 응답



## 응답 - 정적 리소스, 뷰 템플릿

스프링에서 응답 데이터를 만드는 방법은 크게 세 가지이다.

- 정적 리소스
  - 웹 브라우저에 정적인 HTML, css, js를 제공할 때는 **정적 리소스**를 사용한다.
- 뷰 템플릿
  - 웹 브라우저에 동적인 HTML을 제공할 때는 뷰 템블릿을 사용한다.
- HTTP 메시지 사용
  - HTTP API를 제공하는 경우에는 HTML이 아니라 데이터를 전달해야 하므로, HTTP 메시지 바디에 JSON같은 형식으로 데이터를 실어 보낸다.



#### 정적 리소스

스프링 부트는 classpath의 다음 디렉토리에 있는 정적 리소스를 제공한다.

- /static, /public, /resources, /META-INF/resources
- 정적 리소스는 해당 파일을 변경없이 그대로 서비스하는 것이다.



#### 뷰 템플릿

뷰 템플릿을 거쳐서 HTML이 생성되고, 뷰가 응답을 만들어서 전달한다. 일반적으로 HTML을 동적으로 생성하는 용도로 사용하지만, 다른 것들도 가능하다.

스프링 부트는 기본 뷰 템플릿 경로를 제공한다.

- `src/main/resources/templates`



#### String을 반환하는 경우 - View or HTTP 메시지

`@ResponseBody` 가 없으면 `response/hello` 로 뷰 리졸버가 실행되어서 뷰를 찾고, 렌더링 한다. `@ResponseBody` 가 있으면 뷰 리졸버를 실행하지 않고, HTTP 메시지 바디에 직접 `response/hello` 라는 문자가 입력된다.



#### Void를 반환하는 경우

`@Controller` 를 사용하고, `HttpServletResponse` , `OutputStream(Writer)` 같은 HTTP 메시지 바디를 처리하는 파라미터가 없으면 요청 URL을 참고해서 논리 뷰 이름으로 사용한다.

- 참고로 이 방식은 명시성이 너무 떨어지고 이렇게 딱 맞는 경우도 없어서 권장하지 않는다.



#### HTTP 메시지

`@ResponseBody`, `HttpEntity` 를 사용하면, 뷰 템플릿을 사용하는 것이 아니라, HTTP 메시지 바디에 직접 응답 데이터를 출력할 수 있다.



### Thymeleaf 스프링 부트 설정

thymeleaf 라이브러리를 추가하면 스프링 부트가 자동으로 `ThymeleafViewResolver`와 필요한 스프링 빈들을 등록한다. 추가로 `application.properties` 에 다음과 같은 설정값이 추가된다.

``` properties
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
```



## HTTP 응답 - HTTP API, 메시지 바디에 직접 입력

HTTP API를 제공하는 경우에는 HTML이 아니라 데이터를 전달해야 하므로, HTTP 메시지 바디에 JSON 같은 형식으로 데이터를 실어 보낸다.



## HTTP 메시지 컨버터

뷰 템플릿으로 HTML을 생성해서 응답하는 것이 아니라, HTTP API처럼 JSON 데이터를 HTTP 메시지 바디에서 직접 읽거나 쓰는 경우 HTTP 메시지 컨버터를 사용하면 편리하다.



- `@ResponseBody`를 사용하면
  - HTTP의 body에 문자 내용을 직접 반환한다.
  - `viewResolver` 대신 `HttpMessageConverter`가 동작한다.
  - 기본 문자 처리: `StringHttpMessageConverter`
  - 기본 객체 처리: `MappingJackson2HttpMessageConverter`
  - byte 처리 등 기타 여러 HttpMessageConverter가 기본으로 등록되어 있다.



**스프링 MVC는 다음의 경우에 HTTP 메시지 컨버터를 적용한다.**

- HTTP 요청: `@RequestBody`, `HttpEntity(RequestEntity)`
- HTTP 응답: `@ResponseBody`, `HttpEntity(ResponseEntity)`



`HttpMessageConverter`는 HTTP 요청/응답시에 사용된다.

- `canRead()`, `canWrite()`:메시지 컨버터가 해당 클래스, 미디어 타입(content-type)을 지원하는지 체크
- `read()`, `write()`: 메시지 컨버터를 통해서 메시지를 읽고 쓰는 기능



#### 스프링 부트 기본 메시지 컨버터

1. `ByteArrayHttpMessageConverter``
2. ``StringHttpMessageConverter`
3. MappingJackson2HttpMessageConverter`



#### HTTP 요청 데이터 읽기

- HTTP 요청이 오고, 컨트롤러에서 `@RequestBody`, `HttpEntity` 파라미터를 사용한다면
- 메시지 컨버터가 메시지를 읽을 수 있는지 확인하기 위해 `canRead()`를 호출한다.
- 조건 만족하면 `read()`를 호출해서 객체 생성, 반환한다.



응답 데이터는 반대~

- `canWrite()` 에서 HTTP 요청의 Accept 미디어 타입을 지원하는지 체크한다. (더 정확히는 `@ResponseMapping`의 `produces`)



## 요청 매핑 핸들러 어댑터 구조





