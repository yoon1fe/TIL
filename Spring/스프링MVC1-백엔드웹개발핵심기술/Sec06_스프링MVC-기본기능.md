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





## 요청 매핑 - API 예시





## HTTP 요청 - 기본, 헤더 조회



## HTTP 요청 파라미터 - 쿼리 파라미터, HTML Form





## HTTP 요청 파라미터 - @RequestParam





## HTTP 요청 파라미터 - @ModelAttribute





## HTTP 요청 메시지 - 단순 텍스트





## HTTP 요청 메시지 - JSON





## 응답 - 정적 리소스, 뷰 템플릿





## HTTP 응답 - HTTP API, 메시지 바디에 직접 입력





## HTTP 메시지 컨버터





## 요청 매핑 핸들러 어댑터 구조





