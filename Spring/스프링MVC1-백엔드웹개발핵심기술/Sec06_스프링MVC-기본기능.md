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





## HTTP 요청 파라미터 - @ModelAttribute





## HTTP 요청 메시지 - 단순 텍스트





## HTTP 요청 메시지 - JSON





## 응답 - 정적 리소스, 뷰 템플릿





## HTTP 응답 - HTTP API, 메시지 바디에 직접 입력





## HTTP 메시지 컨버터





## 요청 매핑 핸들러 어댑터 구조





