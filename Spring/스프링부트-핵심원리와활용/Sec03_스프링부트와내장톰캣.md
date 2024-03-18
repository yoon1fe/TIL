## WAR 배포 방식의 단점

- 웹 애플리케이션을 구동하고 싶으면 웹 애플리케이션 서버를 별도로 설치해야 한다.

- 개발 환경 설정이 복잡하다.
  - 단순한 자바라면 별도의 설정을 고민하지 않고 `main()` 메서드만 실행하면 되는데.
  - WAS 실행하고, WAR 와 연동하고 등등 복잡한 설정
- 배포 과정이 복잡하다.
- 톰캣 버전을 변경하려면 톰캣을 재설치해야 함.



**고민**

- 단순히 자바의 main() 메서드만 실행하면 웹 서버까지 실행되도록 하면 되지 않을까?
- 톰캣도 자바로 만들어져 있으니 WAS를 라이브러리처럼 포함해도 되지 않을까?

**--> 내장 톰캣 !!**



## 내장 톰캣

내장 톰캣은 쉽게 얘기해서 톰캣을 라이브러리로 포함하고, 자바 코드로 직접 실행하는 것.



#### 서블릿 설정

```java
package hello.embed;

import hello.servlet.HelloServlet;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

public class EmbedTomcatServletMain {

  public static void main(String[] args) throws LifecycleException {
    System.out.println("EmbedTomcatServletMain.main");
    // 톰캣 설정
    Tomcat tomcat = new Tomcat();
    Connector connector = new Connector();
    connector.setPort(8080);
    tomcat.setConnector(connector);

    // 서블릿 등록
    Context context = tomcat.addContext("", "/");
    tomcat.addServlet("", "helloServlet", new HelloServlet());
    context.addServletMappingDecoded("/hello-servlet", "helloServlet");

    // 톰캣 시작
    tomcat.start();
  }

}
```

- 내장 톰캣이 어떤 방식으로 동작하는지 원리 정도만 알아두자.



#### 스프링

```java
package hello.embed;

import hello.spring.HelloConfig;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class EmbedTomcatSpringMain {

  public static void main(String[] args) throws LifecycleException {
    System.out.println("EmbedTomcatSpringMain.main");

    // 톰캣 설정
    Tomcat tomcat = new Tomcat();
    Connector connector = new Connector();
    connector.setPort(8080);
    tomcat.setConnector(connector);

    // 스프링 컨테이너 생성
    AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
    appContext.register(HelloConfig.class);

    // 스프링 MVC 디스패처 서블릿 생성, 스프링 컨테이너 연결
    DispatcherServlet dispatcherServlet = new DispatcherServlet(appContext);

    // 디스패처 서블릿 등록
    Context context = tomcat.addContext("", "/");
    tomcat.addServlet("", "dispatcher", dispatcherServlet);
    context.addServletMappingDecoded("/", "dispatcher");

    tomcat.start();

  }

}
```



**main() 실행되면**

1. 내장 톰캣을 생성해서 `8080` 포트로 연결
2. 스프링 컨테이너를 만들고 필요한 빈 등록
3. 스프링 MVC 디스패처 서블릿을 만들고 앞서 만든 스프링 컨테이너에 연결
4. 디스패처 서블릿을 내장 톰캣에 등록
5. 내장 톰캣 실행



#### 빌드와 배포

`main()` 메서드를 실행하기 위해서는 `jar` 형식으로 빌드해야 한다! 그리고 jar 안에 `META-INF/MANIFEST.MF` 파일에 실행할 main() 메서드의 클래스를 지정해주어야 한다.



```groovy
task buildJar(type: Jar) {
    manifest {
        attributes 'Main-Class': 'hello.embed.EmbedTomcatSpringMain'
    }
    with jar
}
```

- MANIFEST.MF 파일 자동 생성



이렇게 하고 빌드하면 JAR 파일 안에 스프링 라이브러리나 내장 톰캣 라이브러리가 없어서 실행 오류가 발생한다. --> **jar 파일 내부에는 jar 파일을 포함할 수 없기 때문!!!**



**FatJar**

- Jar 안에는 Jar를 포함할 순 없지만, 클래스는 얼마든지 포함할 수 있다.
- Jar 를 풀어서 나온 클래스들을 Jar 에 넣는 방식

```groovy
task buildFatJar(type: Jar) {
    manifest {
        attributes 'Main-Class': 'hello.embed.EmbedTomcatSpringMain'
    }
    duplicatesStrategy = DuplicatesStrategy.WARN
    from { configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
    with jar
}
```



**FatJar 의 단점**

- 어떤 라이브러리가 포함되어 있는지 확인하기 어려움
- **파일명 중복을 해결할 수 없음**



## 편리한 부트 클래스 만들기

앞선 내장 톰캣 실행, 스프링 컨테이너 생성, 디스패처 서블릿 등록 등의 과정을 모두 편리하게 퍼리해주는 부트 클래스를 만들어보자.



```java
package hello.boot;

import hello.spring.HelloConfig;
import java.util.List;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class MySpringApplication {

  public static void run(Class configClass, String[] args) {
    System.out.println("MySpringApplication.run args=" + List.of(args));

    // 톰캣 설정
    Tomcat tomcat = new Tomcat();
    Connector connector = new Connector();
    connector.setPort(8080);
    tomcat.setConnector(connector);

    // 스프링 컨테이너 생성
    AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
    appContext.register(configClass);

    // 스프링 MVC 디스패처 서블릿 생성, 스프링 컨테이너 연결
    DispatcherServlet dispatcherServlet = new DispatcherServlet(appContext);

    // 디스패처 서블릿 등록
    Context context = tomcat.addContext("", "/");
    tomcat.addServlet("", "dispatcher", dispatcherServlet);
    context.addServletMappingDecoded("/", "dispatcher");

    try {
      tomcat.start();
    } catch (LifecycleException e) {
      throw new RuntimeException(e);
    }
  }

}
```

- `configClass`: 스프링 설정을 파라미터로 전달



```java
package hello.boot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.ComponentScan;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ComponentScan
public @interface MySpringBootApplication {

}
```



```java
package hello.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class HelloConfig {

    @Bean
    public HelloController helloController() {
        return new HelloController();
    }
}
```



```java
package hello;

import hello.boot.MySpringApplication;
import hello.boot.MySpringBootApplication;

@MySpringBootApplication
public class MySpringBootMain {

  public static void main(String[] args) {
    System.out.println("MySpringBootMain.main");
    MySpringApplication.run(MySpringBootMain.class, args);
  }

}
```



**일반적인 스프링 부트 사용법**

```java
@SpringBootApplication
public class BootApplication {

  public static void main(String[] args) {
    SpringApplication.run(BootApplication.class, args);
  }

}
```



## 스프링 부트와 웹 서버

**라이브러리 버전**

- 스프링 부트를 사용하면 라이브러리 뒤에 버전 정보를 생략해도 현재 부트 버전에 가장 적절한 외부 라이브러리 버전을 자동으로 선택해준다.



#### 스프링 부트 실행 과정

```java
@SpringBootApplication
public class BootApplication {

  public static void main(String[] args) {
   SpringApplication.run(BootApplication.class, args);
  }

}
```

- run() 메서드에 메인 설정 정보를 넘겨주는데, 보통 `@SpringBootApplication` 애너테이션이 있는 현재 클래스를 지정

- 핵심 두 가지
  - 스프링 컨테이너를 생성
  - WAS(내장 톰캣)를 생성



#### 빌드와 배포

- JAR 를 푼 결과를 보면 Fat Jar 가 아니라 처음 보는 구조로 이루어져 있다.
- BOOT-INF 가 생성되는데, 여기 jar 파일들이 들어 있음.



## 스프링 부트 실행 가능 JAR

**실행 가능 JAR**

- 스프링 부트는 Fat JAR 의 문제를 해결하기 위해 jar 내부에 jar를 포함할 수 있는 특별한 구조의 jar 만들고, 만든 jar 를 내부 jar를 포함해서 실행할 수 있도록 했다. (`BOO-INF`)
- 어떤 라이브러리가 있는지 확인 가능
- 파일명 중복을 해결할 수 있음

- **실행 가능 JAR 는 자바 표준이 아니고, 스프링 부트에서 새롭게 정의한 것**



**실행 가능 Jar 내부 구조** 

- boot-0.0.1-SNAPSHOT.jar 
  - META-INF 
    - MANIFEST.MF 
  - org/springframework/boot/loader 
    - JarLauncher.class : 스프링 부트 main() 실행 클래스
  - BOOT-INF 
    - classes : 우리가 개발한 class 파일과 리소스 파일 
      - hello/boot/BootApplication.class 
      - hello/boot/controller/HelloController.class
      -  … 
    - lib : 외부 라이브러리 
      - spring-webmvc-6.0.4.jar 
      - tomcat-embed-core-10.1.5.jar 
      - ...
    - classpath.idx : 외부 라이브러리 모음 
    - layers.idx : 스프링 부트 구조 정보



**Jar 실행 정보**

```
Manifest-Version: 1.0
Main-Class: org.springframework.boot.loader.JarLauncher
Start-Class: hello.boot.BootApplication
Spring-Boot-Version: 3.0.2
Spring-Boot-Classes: BOOT-INF/classes/
Spring-Boot-Lib: BOOT-INF/lib/
Spring-Boot-Classpath-Index: BOOT-INF/classpath.idx
Spring-Boot-Layers-Index: BOOT-INF/layers.idx
Build-Jdk-Spec: 17
```

- `java -jar xxx.jar` 를 실행하면 먼저 `META-INF/MANIFEST.MF` 파일을 찾고, 여기에 있는 `Main-Class` 를 읽어서 `main()` 메서드를 실행한다.
  - `Main-Class: org.springframework.boot.loader.JarLauncher`
- JarLauncher 는 스프링 부트가 빌드 시에 넣어준다.
  - JarLauncher 가 jar 안의 jar 를 읽어들이는 등의 작업을 먼저 처리하고, `Start-Class`에 지정된 클래스의  `main()`  메서드를 호출한다.



**실행 과정 정리**

1. java -jar xxx.jar
2. MANIFEST.MF 인식 
3. JarLauncher.main() 실행 
   - BOOT-INF/classes/ 인식 
   - BOOT-INF/lib/ 인식 
4. BootApplication.main() 실행