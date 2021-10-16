## 스프링 MVC의 기본 구조

`스프링 MVC`는 스프링의 서브 프로젝트입니다. [여기](https://spring.io/projects)를 보면 `Spring Framework`라는 메인 프로젝트 외에도 여러 종류의 서브 프로젝트가 존재하는데, `스프링 MVC` 역시 이 중 하나입니다.

 

스프링은 하나의 기능을 위해서만 만들어진 프레임워크가 아니라, `코어`라고 할 수 있는 프레임워크에서 여러 서브 프로젝트를 결합해 다양한 상황에 대처할 수 있도록 개발되었습니다. 서브 프로젝트는 '별도의 설정이 존재할 수 있다'라는 개념입니다. `Spring Legacy Project`로 생성한 예제에서도 `servlet-context.xml` 파일과 `root-context.xml` 파일로 설정 파일이 분리되어 있는 것 처럼요. 스프링 MVC 역시 서브 프로젝트이므로 구성 방식이나 설정이 조금 다를 수 있습니다.

 

예제로 만들어 볼 구조는 다음과 같습니다.



![img](https://blog.kakaocdn.net/dn/CjGx8/btqGtekKo0M/fyGFiVWNizPDTmeGj6Gb81/img.png)



###  

### 스프링 MVC 프로젝트의 내부 구조

`스프링 MVC` 프로젝트를 구성해서 사용한다는 의미는 내부적으로는 `root-context.xml`로 사용하는 일반 자바 영역 (흔히 `POJO`(`Plain Old Java Object`)) 과 `servlet-context.xml`로 설정하는 웹 관련 영역을 같이 연동해서 구동합니다.



![img](https://blog.kakaocdn.net/dn/eppT3Z/btqGteLQQ1M/89ypuY8E3oyakywplOZKm1/img.png)



`WebApplicationContext`라는 존재는 기존의 구조에 `MVC` 설정을 포함하는 구조로 만들어 집니다. 스프링은 원래 목적 자체가 웹 애플리케이션을 목적으로 나온 프레임워크가 아니기 때문에, 달라지는 영역에 대해서는 완전히 분리하고 연동하는 방식으로 구현되어 있습니다.

 

`Spring Legacy Project > Spring MVC Project` 를 생성합시다.

생성 후 저번과 마찬가지로 `pom.xml` 파일을 수정해야 합니다. 정리해 봅시다.

#### 스프링 버전

```
    <properties>
        <java-version>1.8</java-version>
        <org.springframework-version>5.2.8.RELEASE</org.springframework-version>
        <org.aspectj-version>1.6.10</org.aspectj-version>
        <org.slf4j-version>1.6.6</org.slf4j-version>
    </properties>
```

`3.x.x -> 5.2.8` 메이븐 홈페이지에서 제일 많이 쓰는 걸로 했습니다.

#### Lombok

```
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.12</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-test</artifactId>
            <version>${org.springframework-version}</version>
        </dependency>
```

#### Servlet 관련

```
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>3.1.0</version>
        </dependency>
```

#### Maven 컴파일 옵션

```
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>2.5.1</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                    <compilerArgument>-Xlint:all</compilerArgument>
                    <showWarnings>true</showWarnings>
                    <showDeprecation>true</showDeprecation>
                </configuration>
            </plugin>
```

위와 같이 변경하고, `update project`를 해줍시다.

웹 프로젝트는 가능하면 절대 경로를 이용하는 구조로 사용하는 것이 바람직하므로, `Tomcat`의 `Modules` 메뉴에 들어가서 '/' 경로로 프로젝트가 실행될 수 있도록 처리해줍시다.

`Servers`에 있는 `Tomcat v9.0 Server ..`를 더블 클릭하면 `Overview`가 나오고, 밑에 `Modules` 메뉴를 선택할 수 있습니다.

 



![img](https://blog.kakaocdn.net/dn/befDXc/btqGtClm4fq/MuumRxgFMdTxqZDKD4KAnK/img.png)



###  

### 자바 설정

똑같이 `jex01` 프로젝트를 생성하고, `root-context.xml`, `servlet-context.xml`, `web.xml` 파일을 삭제합시다.

자바 설정을 이용하는 경우에는 `pom.xml`에다가 `web.xml`이 없다는 설정을 추가해야 합니다.

`<plugin>` 설정을 추가합시다.

```
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.2.0</version>
                <configuration>
                    <failOnMissingWebXml>false</failOnMissingWebXml>
                </configuration>
            </plugin>
```

`org.yoon1fe.config` 패키지를 만들고 요 안에 `RootConfig`, `WebConfig` 클래스를 만들어 줍시다.

```
package org.yoon1fe.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class RootConfig {

}
package org.yoon1fe.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebConfig extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] {RootConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String[] getServletMappings() {
        // TODO Auto-generated method stub
        return null;
    }
}
```

 

`스프링 MVC`를 이용하는 경우에는 `servlet-context.xml` 파일 대신 `ServeltConfig` 클래스를 사용합니다.

`ServletConfig` 클래스는 기존의 `servlet-context.xml` 파일에 설정된 모든 내용을 담아야 하는데 ,이 때는 주로 다음과 같은 방식을 이용합니다.

- `@EnableWebMvc` 어노테이션과 `WebMvcConfigurer` 인터페이스를 구현하는 방식

  (과거에는 `WebMvcConfigurerAdapter` 추상 클래스를 사용했으나, 스프링 5.0 버전부터는 `Deprecated` 되었으므로 주의합시다.)

- `@Configuration`과 `WebMvcConfigurationSupport` 클래스를 상속하는 방식

  일반 `@Configuration` 우선 순위가 구분되지 않는 경우에 사용합니다.

 

우리는 `@EnableWebMvc` 어노테이션을 이용해봅시다.

```
package org.yoon1fe.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@EnableWebMvc
@ComponentScan(basePackages = {"org.yoon1fe.controller" })
public class ServletConfig implements WebMvcConfigurer {

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        InternalResourceViewResolver bean = new InternalResourceViewResolver();
        bean.setViewClass(JstlView.class);
        bean.setPrefix("/WEB-INF/views/");
        bean.setSuffix(".jsp");
        registry.viewResolver(bean);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("resources/**").addResourceLocations("/resources/");
    }
}
```

 

`WebMvcConfigurer`는 `스프링 MVC`와 관련된 설정을 메소드로 오버라이드하는 형태를 이용할 때 사용합니다. `ServletConfig` 클래스 또한 `@ComponentScan` 어노테이션을 이용해서 다른 패키지에 작성된 스프링의 객체`(Bean)`을 인식할 수 있습니다.

`WebConfig` 클래스의 `ServletConfig` 관련 설정도 바꿔주고, `스프링 MVC` 기본 경로도 마찬가지로 '/'로 바꿔줍시다.

```
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] {ServletConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }
```

###  

### 예제 프로젝트의 로딩 구조

프로그램이 정상적으로 실행되었다면 서버 구동 시 약간의 로그가 기록됩니다. 이 로그를 통해 어떤 과정을 통해서 프로젝트가 실행되는지를 볼 수 있습니다.

프로젝트 구동 시 관여하는 `XML`은 `web.xml`, `root-context.xml`, `servlet-context.xml` 파일입니다.

- `web.xml` 파일은 **`Tomcat` 구동**과 관련된 설정
- `root-context.xml`, `servlet-context.xml`파일은 **스프링과 관련**된 설정입니다.

프로젝트의 구동은 `web.xml`에서 시작합니다. `web.xml`의 상단에는 가장 먼저 구동되는 `Context Listener`가 등록되어 있습니다.

```
<!-- web.xml의 일부 -->
    <!-- The definition of the Root Spring Container shared by all Servlets and Filters -->
    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/spring/root-context.xml</param-value>
    </context-param>

    <!-- Creates the Spring Container shared by all Servlets and Filters -->
    <listener>
        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
    </listener>
```

`ContextLoaderListener`는 해당 웹 애플리케이션 구동 시 같이 동작하므로 해당 프로젝트를 실행하면 맨 먼저 로그를 출력하면서 기록합니다.



![img](https://blog.kakaocdn.net/dn/4AqQa/btqGvcTN8m8/XroE3tRcGdFxhNTplmC1sk/img.png)



 

`root-context.xml`이 처리되면 파일에 있는 빈`(Bean)` 설정들이 동작합니다. 이를 그림으로 표현하면 다음과 같습니다.



![img](https://blog.kakaocdn.net/dn/bgzLna/btqGxPQ7mS7/hcM6Jww7ygA9sbBGPuGiJK/img.png)



`root-context.xml`에 정의된 객체`(Bean)`들은 스프링의 영역`(context)`안에 생성되고, 객체들 간의 의존성이 처리됩니다.

`root-context.xml`이 처리된 후에는 `스프링 MVC`에서 사용하는 `DispatcherServlet`이라는 서블릿과 관련된 설정이 동작합니다.

```
    <!-- Processes application requests -->
    <servlet>
        <servlet-name>appServlet</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>/WEB-INF/spring/appServlet/servlet-context.xml</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>appServlet</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>
```

 

`org.springframework.web.servlet.DispatcherServlet` 클래스는 `스프링 MVC`의 구조에서 가장 핵심적인 역할을 하는 클래스입니다. 내부적으로 웹과 관련된 처리의 준비 작업을 진행하는데, 이 때 사용하는 파일이 `servlet-context.xml` 파일입니다.

 

`DispatcherServlet`에서 `XmlWebApplicationContext`를 이용해서 `servlet-context.xml` 파일을 로딩하고 해석합니다. 이 과정에서 등록된 빈들은 기존에 만들어진 빈들과 같이 연동되게 됩니다.



![img](https://blog.kakaocdn.net/dn/bioZY9/btqGyxCELGE/mIwqhroKSNBbACFkKgqs21/img.png)



###  

### 스프링 MVC의 기본 사상

자바를 이용한 웹 애플리케이션을 제작할 때 흔히 `MVC 모델 2`를 많이 사용하는데, `스프링 MVC`의 경우 이러한 부분은 **개발자들에게 보여주지 않고** 개발자들은 **자신이 필요한 부분만을 집중해서 개발**할 수 있는 구조로 만들어져 있습니다.

서블릿/JSP에서는 `HttpServletRequest`와 `HttpServletResponse`라는 타입의 객체를 이용해서 브라우저에서 전송한 정보를 처리합니다.

`스프링 MVC`에서는 그 위에 하나의 계층을 더한 형태가 됩니다.



![img](https://blog.kakaocdn.net/dn/ABpoa/btqGyJiLQPs/fv3YnyUr4POK4fwufNzcOK/img.png)



 

`스프링 MVC`를 이용하면 개발자는 서블릿/JSP의 API를 직접 사용할 필요성이 현저히 줄어듭니다. 스프링은 중간에서 연결 역할을 하기 때문에 이러한 코드를 작성하지 않고도 원하는 기능을 구현할 수 있게 해줍니다.

개발자의 코드는 `스프링 MVC`에서 동작하기 때문에 버전 2.5 미만에서는 특정한 클래스를 상속하거나 인터페이스를 구현하는 방식을 사용했지만, 2.5버전 부터는 어노테이션 방식을 이용해 `XML`이나 어노테이션 등의 설정만으로 개발할 수 있게 되었습니다.

###  

### 모델 2와 스프링 MVC

모델 2방식은 쉽게 말해서 **'로직과 화면을 분리'**하는 스타일의 개발 방식입니다. 모델 2방식은 `MVC` 구조를 사용합니다.



![img](https://blog.kakaocdn.net/dn/UZCTC/btqGvQXuEz6/8IUHzZnfX8OsCZ6OVTgjQK/img.png)



 

모델 2방식에서 사용자의 `Request`는 특별한 상황이 아닌 이상 먼저 `Controller`를 호출합니다.

이렇게 하는 가장 큰 이유는 나중에 `View`를 교체하더라도 사용자가 호출하는 `URL` 자체에 변화가 없게 만들어 주기 때문입니다. 컨트롤러는 데이터를 처리하는 존재를 통해서 데이터`(Model)`를 처리하고 `Response` 할 때 필요한 데이터`(Model)`를 `View` 쪽으로 전달합니다. 서블릿을 이용하는 경우, 개발자들은 서블릿 API의 `RequestDispatcher` 등을 이용해서 이를 직접 처리했지만, `스프링 MVC`는 내부에서 이러한 처리를 해주고, 개발자는 `스프링 MVC`의 `API`를 이용해서 코드를 작성하게 됩니다.

 

`스프링 MVC`의 기본 구조는 다음과 같습니다.



![img](https://blog.kakaocdn.net/dn/cpzN7a/btqGuw6uijk/EbC7yYji8rFmYGBKQgihM1/img.png)



1. 사용자의 `Request`는 `Front-Controller`인 `DispatcherServlet`을 통해서 처리합니다. `web.xml`을 보면 다음과 같이 모든 `Request`들을 `DispatcherServlet`이 받도록 되어 있습니다.

   ```
       <servlet>
           <servlet-name>appServlet</servlet-name>
           <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
           <init-param>
               <param-name>contextConfigLocation</param-name>
               <param-value>/WEB-INF/spring/appServlet/servlet-context.xml</param-value>
           </init-param>
           <load-on-startup>1</load-on-startup>
       </servlet>
   ```

2. 1. `HandlerMapping`은 `Request`의 처리를 담당하는 컨트롤러를 찾기 위해 존재합니다.`HandlerMapping` 인터페이스를 구현한 여러 객체들 중 `RequestMappingHandlerMapping` 같은 경우는 개발자가 `@RequestMapping` 어노테이션이 적용된 것을 기준으로 판단하게 됩니다. 적절한 컨트롤러를 찾았다면 `HandlerAdapter`를 이용해서 해당 컨트롤러를 동작시킵니다.

3. `Controller`는 개발자가 작성하는 클래스로, 실제 `Request`를 처리하는 로직을 작성합니다. 이 때, `View`에 전달해야 하는 데이터는 주로 `Model`이라는 객체에 담아서 전달합니다. `Controller`는 다양한 타입의 결과를 반환하는데, 이에 대한 처리는 `ViewResolver`를 이용합니다.

4. `ViewResolver`는 `Controller`가 반환한 결과를 어떤 `View`를 통해서 처리하는 것이 좋을 지 해석합니다. 가장 흔하게 사용하는 설정은 `servlet-context.xml`에 정의된 `InternalResourceViewResolver` 입니다.

   ```
       <!-- Resolves views selected for rendering by @Controllers to .jsp resources in the /WEB-INF/views directory -->
       <beans:bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
           <beans:property name="prefix" value="/WEB-INF/views/" />
           <beans:property name="suffix" value=".jsp" />
       </beans:bean>
   ```

5. 1. `View`는 실제로 응답을 보내야 하는 데이터를 `JSP` 등을 이용해서 생성하는 역할을 합니다. 만들어진 응답은 `DispatcherServlet`을 통해서 전송됩니다.

위 그림을 보면 모든 `Request`는 `DispatcherServlet`을 통해서 들어오도록 설계되는데, 이런 방식을 `Front-Controller` 패턴이라고 합니다. `Front-Controller` 패턴을 이용하면 전체 흐름을 강제로 제한할 수 있습니다.

예를 들어 `HttpServlet`을 상속해서 만든 클래스를 이용하는 경우 특정 개발자는 이를 활용할 수 있지만, 다른 개발자는 자신이 원래 하던 방식대로 `HttpServlet`을 그대로 상속해서 개발할 수도 있습니다.

`Front-Controller` 패턴을 이용하는 경우에는 모든 `Request`의 처리에 대한 분배가 정해진 방식대로만 동작하기 때문에 좀 더 **엄격한 구조**를 만들 수 있습니다.