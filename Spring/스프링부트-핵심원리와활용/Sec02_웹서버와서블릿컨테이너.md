## 웹 서버와 스프링 부트 소개

**전통적인 방식**

- WAS(ex. 톰캣..) 설치
- WAS 에서 동작하도록 서블릿 스펙에 맞춰 개발
- WAS 형식으로 빌드 및 배포



**최근 방식**

- 스프링 부트가 내장 톰캣(라이브러리로) 포함
- JAR로 빌드, JAR 실행하면 WAS도 실행



## 프로젝트 설정

순수 서블릿 생성

```java
package hello.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * http://localhost:8080/test
 */
@WebServlet(urlPatterns = "/test")
public class TestServlet extends HttpServlet {

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    System.out.println("TestServlet.service");
    resp.getWriter().println("test");
  }
}
```

- http://localhost:8080/test 로 해당 서블릿을 실행하려면 톰캣같은 WAS에 이 코드를 등록해야 함.





## WAR 빌드와 배포

- 프로젝트 폴더에서 `./gradlew build`

- `build/libs/server-0.0.1-SNAPSHOT.war` 에 WAR 파일 생성



**JAR**

- 자바는 여러 클래스와 리소스를 묶어서 JAR(Java Archive) 압축 파일을 생성할 수 있다
- 이 파일은 JVM 위에서 직접 실행되거나, 다른 곳에서 사용하는 라이브러리로 제공
- 직접 실행하는 경우 `main()` 메서드가 필요하고, `MANIFEST.MF` 파일에 실행할 메인 메서드가 있는 클래스를 지정해두어야 함.
- **클래스와 관련 리소스를 압축한 단순한 파일**



**WAR**

- Web Application Archive
- WAS 에 배포할 때 사용하는 파일
- JAR 가 JVM 위에서 실행 / WAR 는 웹 애플리케이션 서버 위에서 실행
- HTML 과 같은 정적 리소스도 포함되므로 JAR 보다 구조가 더 복잡 && WAR 구조도 지켜야 함.

- 구조
  - `WEB-INF`
    - `classes`: 실행 클래스 모음
    - `lib`: 라이브러리 모음
    - `web.xml`: 웹 서버 배치 설정 파일 (생략 가능)
  - `index.html`: 정적 리소스



**WAR 배포**

1. 톰캣 서버 종료
2. 톰캣 폴더/webapps 에 war 파일을 두고
3. 이름을 ROOT.war 로 변경
4. 톰캣 서버 실행

- tomcat 폴더/logs/catalina.out 에 톰캣 로그 남음



## 서블릿 컨테이너 초기화

- WAS 실행하는 시점에 필요한 초기화 작업..
  - 서비스에 필요한 필터, 서블릿 등록
  - 스프링 사용한다면 스프링 컨테이너 만들고, 서블릿과 스프링 연결하는 디스패처 서블릿도 등록..
- WAS 가 제공하는 초기화 기능을 사용하면 WAS 실행 시점에 이런 초기화 과정을 진행할 수 있다
- 과거에는 web.xml 을 사용해서 초기화했는데, 지금은 서블릿 스펙에서 자바 코드를 사용한 초기화도 지원한다.



![ServletContainer와 SpringContainer는 무엇이 다른가? | by Sigrid Jin | Medium](https://miro.medium.com/v2/resize:fit:1400/0*iH2CikmkQN5qe43M.png)



**서블릿 컨테이너 초기화 개발**

- 서블릿 컨테이너 초기화 인터페이스: `ServletContainerInitializer`
- 서블릿 컨테이너는 실행 시점에 초기화 메서드인 `onStartUp()` 호출한다. 여기서 애플리케이션에 필요한 기능들을 초기화하거나 등록할 수 있음



```java
package hello.container;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import java.util.Set;

public class MyContainerInitV1 implements ServletContainerInitializer {

  @Override
  public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
    System.out.println("MyContainerInitV1.onStartup");
    System.out.println("MyContainerInitV1.c = " + c);
    System.out.println("MyContainerInitV1.ctx = " + ctx);
  }
}
```



- WAS에게 실행할 초기화 클래스(MyContainerInitV1)를 알려줘야 한다.
- 다음 경로에 파일 생성
  - `resources/META-INF/services/jakarta.servlet.ServletContainerInitializer`
  - 이 파일에 MyContainerInitV1 클래스를 패키지 경로 포함해서 지정
    - `hello.container.MyContainerInitV1`
- 이렇게 하면 WAS 를 실행할 때 해당 클래스를 초기화 클래스로 인식하고 로딩 시점에 실행한다.

- 실행 결과 로그

  - ```
    MyContainerInitV1.onStartup
    MyContainerInitV1.c = null
    MyContainerInitV1.ctx = org.apache.catalina.core.ApplicationContextFacade@66b7bbae
    ```



**서블릿을 등록하는 2가지 방법**

- `@WebServlet` 애너테이션
- 프로그래밍 방식



`HelloServlet` 이라는 서블릿을 서블릿 컨테이너 초기화 시점에 프로그래밍 방식으로 직접 등록해보자.

```java
package hello.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HelloServlet extends HttpServlet {

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    System.out.println("HelloServlet.service");
    resp.getWriter().println("hello servlet!");
  }
}
```



**애플리케이션 초기화**

- 서블릿 컨테이너는 조금 더 유연한 초기화 기능을 지원한다. == 애플리케이션 초기화



```java
package hello.container;

import jakarta.servlet.ServletContext;

public interface AppInit {

  void onStartUp(ServletContext servletContext);

}
```

- 애플리케이션 초기화를 진행하려면 인터페이스 생성해야 함. 내용, 형식은 상관없다.



```java
package hello.container;

import hello.servlet.HelloServlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration.Dynamic;

public class AppInitV1Servlet implements AppInit {

  @Override
  public void onStartUp(ServletContext servletContext) {
    System.out.println("AppInitV1Servlet.onStartUp");

    // 순수 서블릿 코드 등록
    Dynamic helloServlet = servletContext.addServlet("helloServlet", new HelloServlet());
    helloServlet.addMapping("hello-servlet");
  }
}
```

- 프로그래밍 방식으로 HelloServlet 서블릿을 서블릿 컨테이너에 직접 등록!
- `/hello-servlet` 호출하면 HelloServlet 서블릿이 실행된다.



**애너테이션 방식**

```java
@WebServlet(urlPatterns = "/test")
public class TestServlet extends HttpServlet {}
```



**애플리케이션 초기화(`AppInit`)은 어떻게 실행될까?**



```java
package hello.container;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HandlesTypes;
import java.util.Set;

@HandlesTypes(AppInit.class)
public class MyContainerInitV2 implements ServletContainerInitializer {

  @Override
  public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
    System.out.println("MyContainerInitV2.onStartup");
    System.out.println("MyContainerInitV2.c = " + c);
    System.out.println("MyContainerInitV2.ctx = " + ctx);

    for (Class<?> appInitClass : c) {
      try {
        //new AppInitV1Servlet()과 같은 코드
        AppInit appInit = (AppInit) appInitClass.getDeclaredConstructor().newInstance();
        appInit.onStartup(ctx);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
```



애플리케이션 초기화 과정

1. @HandlesTypes 애너테이션에 애플리케이션 초기화 인터페이스 지정
2. 서블릿 컨테이너 초기화는 파라미터로 넘어오는 Set<Class<?>> c 에 애플리케이션 초기화 인터페이스의 구현체들을 모두 찾아서 클래스 정보로 전달
3. appInitClass.getDeclaredConstructor().newInstance(): 리플렉션을 사용해서 객체 생성.
4. appInit.onStartup(ctx): 애플리케이션 초기화 코드를 직접 실행하면서 서블릿 컨테이너 정보가 담긴 ctx 도 함께 전달



서블릿 컨테이너 초기화만 있어도 될 것 같은데, 왜 애플리케이션 초기화라는 개념도 만들었을까?

- 편리함
  - 서블릿 컨테이너를 초기화 하려면 `ServletContainerInitializer` 인터페이스를 구현한 코드를 작성해야 함. 추가로 `META-INF` 폴더에 파일도 추가하고 해당 코드 경로를 직접 지정해야 한다.
  - 반면에 애플리케이션 초기화는 특정 인터페이스만 구현하면 된다.
- 의존성
  - 애플리케이션 초기화는 서블릿 컨테이너에 상관없이 원하는 모양으로 인터페이스 만들 수 있다. 이를 통해 애플리케이션 초기화 코드가 서블릿 컨테이너에 대한 의존을 줄일 수 있음.



## 스프링 컨테이너 등록

초기화 과정에서 스프링 컨테이너까지 올려보자.

1. 스프링 컨테이너 생성
2. 스프링 MVC 컨트롤러를 스프링 컨테이너에 빈으로 등록
3. 스프링 MVC를 사용하는 데 필요한 디스패처 서블릿을 서블릿 컨테이너에 등록

```java
package hello.spring;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

  @GetMapping("hello-spring")
  public String hello() {
    System.out.println("HelloController.hello");

    return "hello spring!";
  }

}
```



```java
package hello.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HelloConfig {

  @Bean
  public HelloController helloController() {
    return new HelloController();
  }

}
```





애플리케이션 초기화를 통해 서블릿 컨테이너에 스프링 컨테이너를 생성하고 등록!

```java
package hello.container;

import hello.spring.HelloConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration.Dynamic;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class AppInitV2Spring implements AppInit {

  @Override
  public void onStartup(ServletContext servletContext) {
    System.out.println("AppInitV2Spring.onStartup");

    // 스프링 컨테이너 생성
    AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
    appContext.register(HelloConfig.class);

    // 스프링 MVC 디스패처 서블릿 생성, 스프링 컨테이너 연결
    DispatcherServlet dispatcher = new DispatcherServlet(appContext);

    // 디스패처 서블릿을 서블릿 컨테이너에 등록
    Dynamic servlet = servletContext.addServlet("dispatcherV2", dispatcher);
    // /spring/* 요청이 모두 디스패처 서블릿을 통하도록 설정
    servlet.addMapping("/spring/*");
  }
}
```



**스프링 컨테이너 생성**

- `AnnotationConfigWebApplicationContext`: 스프링 컨테이너
- `appConext.register(HelloConfig.class)`: 컨테이너에 스프링 설정 추가



**스프링 MVC 디스패처 서블릿 생성, 스프링 컨테이너 연결**

- `new DispatcherServlet(appContext)`: 디스패처 서블릿을 생성하고, 생성자에 앞에서 만든 스프링 컨테이너를 전달한다. 이러면 디스패처 서블릿에 스프링 컨테이너가 연결됨.
- 이 디스패처 서블릿에 HTTP 요청이 오면 디스패처 서블릿은 해당 스프링 컨테이너에 들어있는 컨트롤러 빈들을 호출



**디스패처 서블릿을 서블릿 컨테이너에 등록**

- `servletContext.addServlet("dispatcherV2", dispatcher)`: 디스패처 서블릿을 서블릿 컨테이너에 등록
- ` servlet.addMapping("/spring/*")`
  - `/spring/hello-spring` 요렇게 요청이 들어오면, 
    1. 디스패처 서블릿이 실행 (/spring)
    2. 디스패처 서블릿이 스프링 컨트롤러를 찾아서 실행(/hello-spring)



## 스프링 MVC 서블릿 컨테이너 초기화 지원

스프링 MVC 는 이때까지 했던 번거로운 서블릿 컨테이너 초기화 과정을 이미 만들어두었다. 덕분에 개발자는 애플리케이션 초기화 코드만 작성하면 된다!! 스프링에서는 `WebApplicationInitializer` 인터페이스를 지원해줌. 위에서 만든 `AppInit` 이랑 같은 역할.



```java
package hello.container;

import hello.spring.HelloConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration.Dynamic;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class AppInitV3SpringMvc implements WebApplicationInitializer {

  @Override
  public void onStartup(ServletContext servletContext) throws ServletException {
    System.out.println("AppInitV3SpringMvc.onStartup");

    // 스프링 컨테이너 생성
    AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
    appContext.register(HelloConfig.class);

    // 스프링 MVC 디스패처 서블릿 생성, 스프링 컨테이너 연결
    DispatcherServlet dispatcher = new DispatcherServlet(appContext);

    // 디스패처 서블릿을 서블릿 컨테이너에 등록
    Dynamic servlet = servletContext.addServlet("dispatcherV3", dispatcher);
    // 모든 요청이 모두 디스패처 서블릿을 통하도록 설정
    servlet.addMapping("/");
  }
}
```



스프링은 `WebApplicationInitializer` 를 통해 결국 서블릿 컨테이너에서 요구하는 부분을 모두 구현했다.