## 프로젝트

**예제 상황**

- v1 - 인터페이스와 구현 클래스 - 스프링 빈으로 수동 등록
- v2 - 인터페이스 없는 구체 클래스 - 스프링 빈으로 수동 등록
- v3 - 컴포넌트 스캔으로 스프링 빈 자동 등록



위처럼 다양한 케이스에서 프록시를 적용하는 방법 알아보자.



### v1 - 인터페이스와 구현 클래스 - 스프링 빈으로 수동 등록

`Repository`, `Service`, `Controller` 모두 인터페이스 및 구현 클래스 생성.

- 참고: 스프링은 @Controller 또는 @RequestMapping 이 있어야 스프링 컨트롤러로 인식한다. 스프링 부트 3.0 (스프링 6.0)부터는 오직 `@Controller` 애너테이션이 붙은 클래스만 스프링 컨트롤러로 인식한다!!



**`AppV1Config.java`**

```java
package hello.proxy.config;

import hello.proxy.app.v1.OrderControllerV1;
import hello.proxy.app.v1.OrderControllerV1Impl;
import hello.proxy.app.v1.OrderRepositoryV1;
import hello.proxy.app.v1.OrderRepositoryV1Impl;
import hello.proxy.app.v1.OrderServiceV1;
import hello.proxy.app.v1.OrderServiceV1Impl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppV1Config {

  @Bean
  public OrderControllerV1 orderControllerV1() {
    return new OrderControllerV1Impl(orderServiceV1());
  }

  @Bean
  public OrderServiceV1 orderServiceV1() {
    return new OrderServiceV1Impl(orderRepositoryV1());
  }

  @Bean
  public OrderRepositoryV1 orderRepositoryV1() {
    return new OrderRepositoryV1Impl();
  }

}
```



**`ProxyApplication.java`**

```java
package hello.proxy;

import hello.proxy.config.AppV1Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(AppV1Config.class)
@SpringBootApplication(scanBasePackages = "hello.proxy.app") //주의
public class ProxyApplication {

 public static void main(String[] args) {
  SpringApplication.run(ProxyApplication.class, args);
 }

}
```

- `@Import(AppV1Config.class)`: 클래스를 스프링 빈으로 등록한다. 일반적으로 `@Configuration` 같은 설정 파일을 등록할 때 사용하지만 스프링 빈을 등록할 때도 사용 가능
- `@SpringBootApplication(scanBasePackages = "hello.proxy.app")`: `@ComponentScan` 과 기능 같다. 컴포넌트 스캔 시작할 위치 지정. 해당 패키지와 그 하위 패키지를 스캔한다. 디폴트 값은 해당 애너테이션이 있는 클래스의 패키지와 그 하위 패키지.



### v2 - 인터페이스 없는 구체 클래스 - 스프링 빈으로 수동 등록



**`AppV2Config.java`**

```java
package hello.proxy.config;

import hello.proxy.app.v2.OrderControllerV2;
import hello.proxy.app.v2.OrderRepositoryV2;
import hello.proxy.app.v2.OrderServiceV2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppV2Config {

  @Bean
  public OrderControllerV2 orderControllerV2() {
    return new OrderControllerV2(orderServiceV2());
  }

  @Bean
  public OrderServiceV2 orderServiceV2() {
    return new OrderServiceV2(orderRepositoryV2());
  }

  @Bean
  public OrderRepositoryV2 orderRepositoryV2() {
    return new OrderRepositoryV2();
  }

}
```

- 수동 빈 등록



### v3 - 컴포넌트 스캔으로 스프링 빈 자동 등록

컴포넌트 스캔의 대상이 되도록 `@Repository`, `@Service`, `@RestController` 를 붙이면, `ProxyApplication`에서 `@SpringBootApplication(scanBasePackages = "hello.proxy.app")`을 사용하고 있기 때문에 자동으로 스프링 빈으로 등록이 된다!



### 요구사항 추가

저번에 만든 로그 추적기를 사용하려면 어쨌든 기존 원본 코드를 변경해야 한다..



**요구사항 추가**

- **원본 코드 수정하지 않고 로그 추적기 적용**
  - **프록시!!**
- 특정 메서드는 로그 출력 X
- 다양한 케이스에 모두 적용



## 프록시, 프록시 패턴, 데코레이터 패턴

**프록시란??**

- 클라이언트 - 서버 사이에서 클라이언트의 요청을 대신 받아서 서버에 다시 요청하는 **대리자**.
- 클라이언트는 대리자를 통해 요청했기 때문에 클라이언트는 그 이후의  과정은 알 수 없다.
- 서버와 프록시는 같은 인터페이스를 사용해야 한다. DI를 사용해서 대체 가능하도록.



**프록시의 주요 기능**

- 접근 제어
  - 권한에 따른 접근 차단
  - 캐싱
  - 지연 로딩
- 부가 기능 추가



**GOF 디자인 패턴에서의 프록시**

- 프록시 패턴: 접근 제어가 주 목적
- 데코레이터 패턴: 새로운 기능 추가가 주 목적



### 프록시 패턴





### 데코레이터 패턴





## 인터페이스 기반 프록시





## 구체 클래스 기반 프록시





## 인터페이스 기반 프록시와 클래스 기반 프록시



