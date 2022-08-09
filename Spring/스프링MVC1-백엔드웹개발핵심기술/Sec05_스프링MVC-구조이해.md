## 스프링 MVC 전체 구조

- 직접 만든 MVC 프레임워크 구조

  ![img](https://oopy.lazyrockets.com/api/v2/notion/image?src=https%3A%2F%2Fs3-us-west-2.amazonaws.com%2Fsecure.notion-static.com%2F27b5c274-b3f1-4ae8-90c4-accc4439f1bd%2FUntitled.png&blockId=c9913e2f-a5f3-4e6e-9e03-61b1d5684c8a)

  

- Spring MVC 구조

  ![img](https://oopy.lazyrockets.com/api/v2/notion/image?src=https%3A%2F%2Fs3-us-west-2.amazonaws.com%2Fsecure.notion-static.com%2F183bb42c-2998-4362-ade1-b7d75f75a851%2FUntitled.png&blockId=209ecc2e-d659-4a44-a519-675c16309d89)



비교

- FrontController -> DispatcherServlet
- handlerMappingMap -> HandlerMapping
- MyHandlerAdapter -> HandlerAdapter
- ModelView -> ModelAndView
- viewResolver -> ViewResolver
- MyView -> View



스프링 MVC에서의 프론트 컨트롤러는 `DispatcherServlet`이다.

`DispatcherServlet`도 부모 클래스에서 `HttpServlet`을 상속받아서 사용하고, 서블릿으로 동작한다.

- DispatchrServlet -> FrameworkServlet -> HttpServletBean -> HttppServlet

스프링 부트는 `DispatcherServlet`을 서블릿으로 자동으로 등록하면서 모든 경로(`urlPattern="/"`)에 대해서 매핑한다. 참고로 더 자세한 경로가 우선순위가 높다. 그래서 기존에 등록한 서블릿도 함께 동작한다.



#### 요청 흐름

- 서블릿이 호출되면 `HttpServlet`이 제공하는 `service()`호출
- 스프링 MVC는 `DispatcherServlet`의 부모인 `FrameworkServlet`에서 `service()` 를 오버라이드 해두고 있음
- `FrameworkServlet.service()`를 시작으로 여러 메서드가 호출되면서 최종적으로 `DispatcherServlet.doDispatch()`가 호출됨



``` java
protected void doDispatch (HttpServletRequest request, HttpServletResponse response) throws Exception {
      HttpServletRequest processedRequest = request;
      HandlerExecutionChain mappedHandler = null;
      ModelAndView mv = null;
      // 1. 핸들러 조회
      mappedHandler = getHandler(processedRequest);
      if (mappedHandler == null) {
        noHandlerFound(processedRequest, response);
        return;
      }
      
      // 2. 핸들러 어댑터 조회 - 핸들러를 처리할 수 있는 어댑터
      HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
      
      // 3. 핸들러 어댑터 실행 -> 4. 핸들러 어댑터를 통해 핸들러 실행 -> 5. ModelAndView 반환
      mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
      processDispatchResult(processedRequest, response, mappedHandler, mv,
          dispatchException);
    }
    
    private void processDispatchResult (HttpServletRequest request,
        HttpServletResponse response, HandlerExecutionChain mappedHandler, ModelAndView mv, Exception exception) throws Exception {
      
      // 뷰 렌더링 호출
      render(mv, request, response);
    }
    protected void render (ModelAndView mv, HttpServletRequest request, HttpServletResponse response) throws Exception {
      View view;
      String viewName = mv.getViewName();
      
      // 6. 뷰 리졸버를 통해서 뷰 찾기, 7. View 반환
      view = resolveViewName(viewName, mv.getModelInternal(), locale, request);
      
      // 8. 뷰 렌더링
      view.render(mv.getModelInternal(), request, response);
    }
```





#### Spring MVC 동작 순서

1. **핸들러 조회**: 핸들러 매핑을 통해 요청 URL에 매핑된 핸들러(컨트롤러)를 조회한다.
2. **핸들러 어댑터 조회**: 핸들러를 실행할 수 있는 핸들러 어댑터를 조회한다.
3. **핸들러 어댑터 실행**: 핸들러 어댑터를 실행한다.
4. **핸들러 실행**: 핸들러 어댑터가 실제 핸들러를 호출한다.
5. **ModelAndView 반환**: 핸들러 어댑터가 핸들러가 반환하는 정보를 `ModelAndView`로 **변환**해서 반환한다.
6. **ViewResolver 호출**: 뷰 리졸버를 찾고 실행한다.
   - JSP의 경우 `InternalResourceViewResolver`가 자동 등록되고 사용된다.
7. **View 반환**: 뷰 리졸버는 뷰의 논리 이름을 물리 이름으로 바꾸고, 렌더링 역할을 담당하는 뷰 객체를 반환한다.
   - JSP의 경우 `InternalResourceView(JstlView)`를 반환하는데, 내부에 `foward()` 로직이 있다.
8. **뷰 렌더링**: 뷰를 통해서 렌더링한다.



스프링 MVC의 큰 장점은 `DispatcherServlet`코드의 변경없이 원하는 기능을 변경하거나 확장할 수 있다는 점이다. 대부분이 인터페이스로 제공된다.



#### 주요 인터페이스 목록

- HandlerMapping
- HandlerAdapter
- ViewResolver
- View



## 핸들러 매핑과 핸들러 어댑터



## 핸들러 매핑과 핸들러 어댑터

컨트롤러가 호출되려면 두 가지가 필요하다.

- HandlerMapping
  - 핸들러 매핑에서 이 컨트롤러를 찾을 수 있어야 한다.
- HandlerAdapter
  - 핸들러 매핑을 통해 찾은 핸들러를 실행할 수 있는 핸들러 어댑터가 필요하다.



#### HandlerMapping

핸들러 찾는 우선순위

``` java
0 = RequestMappingHandlerMapping : 애노테이션 기반의 컨트롤러인 @RequestMapping에서
사용
1 = BeanNameUrlHandlerMapping : 스프링 빈의 이름으로 핸들러를 찾는다.
```



#### HandlerAdapter

``` java
0 = RequestMappingHandlerAdapter : 애노테이션 기반의 컨트롤러인 @RequestMapping에서
사용
1 = HttpRequestHandlerAdapter : HttpRequestHandler 처리
2 = SimpleControllerHandlerAdapter : Controller 인터페이스(애노테이션X, 과거에 사용)
처리
```



#### @RequestMapping

가장 우선순위가 높은 핸들러 매핑과 핸들러 어댑터는 `RequestMappingHandlerMapping`, `RequestMappingHandlerApdater`이다. 얘들이 바로 지금 스프링에서 주로 사용하는 애너테이션 기반의 컨트롤러를 지원하는 매핑과 어댑터이다.



## 뷰 리졸버



#### 뷰 리졸버 - InternalResourceViewResolver

스프링 부트는 `InternalResourceViewResolver`라는 뷰 리졸버를 자동으로 등록하는데, 이때 `application.properties`에 등록한 `spring.mvc.view.prefix`, `spring.mvc.view.suffix` 설정 정보를 사용해서 등록한다.



#### 스프링 부트가 자동 등록하는 뷰 리졸버 (실제로는 더 많음)

``` java
1 = BeanNameViewResolver : 빈 이름으로 뷰를 찾아서 반환한다. (예: 엑셀 파일 생성
기능에 사용)
2 = InternalResourceViewResolver : JSP를 처리할 수 있는 뷰를 반환한다.
```



## 스프링 MVC - 시작하기

스프링이 제공하는 컨트롤러는 애너테이션 기반으로 동작해서 매우 유연하고 실용적이다.



#### `RequestMapping`

- `RequestMappingHandlerMapping`
- `RequestMappingHandlerAdapter`



```java
package hello.servlet.web.springmvc.v1;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SpringMemberFormControllerV1 {

  @RequestMapping("/springmvc/v1/members/new-form")
  public ModelAndView process() {
    return new ModelAndView("new-form");
  }
}
```

- `@Controller`
  - 스프링이 자동으로 스플이 빈으로 등록한다.
  - 스프링 MVC에서 애너테이션 기반 컨트롤러로 인식한다.
- `@RequestMapping`
  - 요청 정보를 매핑한다. 해당 URL이 호출되면 이 메서드가 호출된다. 애너테이션을 기반으로 동작하기 때문에 메서드의 이름은 임의로 지어도 된다.
- `ModelAndView`: 모델과 뷰 정보를 담아서 반환한다.



`RequestMappingHandlerMapping`은 스프링 빈 중에서 `@RequestMapping` 또는 `@Controller` 가 클래스 레벨에 붙어 있는 경우 매핑 정보로 인식한다.



## 스프링 MVC - 컨트롤러 통합

`RequestMapping`가 메서드 단위에 적용되어 있는데, 클래스 단위에 적용하면 클래스를 유연하게 통합할 수 있다.



클래스 레벨 매핑 + 메서드 레벨 매핑 가능!



## 스프링 MVC - 실용적인 방식

스프링 MVC는 개발자가 편리하게 개발할 수 있도록 다양한 편의 기능을 제공한다.



- ModelAndView 반환 대신 String 반환

- `@RequestParam` 사용

  스프링은 HTTP 요청 파라미터를 `@RequestParam` 으로 받을 수 있다. `@RequestParam("username")` 은 `request.getParameter("username")` 와 거의 같은 코드라 생각하면 된다. 물론 GET 쿼리 파라미터, POST Form 방식을 모두 지원한다.

- `@RequestMapping` -> `@GetMapping`, `@PostMapping`

  URL만 매칭하는 것이 아니라, HTTP Method도 함께 구분할 수 있다.
