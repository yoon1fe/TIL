## 프론트 컨트롤러 패턴

프론트 컨트롤러가 도입되기 전까진 모든 컨트롤러 앞단에 공통된 로직이 붙었어야 했다. 



#### 프론트 컨트롤러 특징

- 프론트 컨트롤러 서블릿 하나로 클라이언트의 요청을 받는다.
- 프론트 컨트롤러가 요청에 맞는 컨트롤러를 찾아서 호출한다.
- 입구를 하나로 만들어서 공통 로직 처리 가능
- 프론트 컨트롤러를 제외한 나머지 컨트롤러는 서블릿을 사용하지 않아도 된다.



**스프링 웹 MVC의 핵심도 바로 FrontController이다.** 스프링 웹 MVC의 `DispatcherServlet`이 FrontController 패턴으로 구현되어 있다.!



## 도입 - v1

프론트 컨트롤러 패턴을 단계적으로 적용시켜보자.



서블릿과 비슷한 모양의 컨트롤러 인터페이스를 도입한다. 각 컨트롤러는 이 인터페이스를 구현한다. 프론트 컨트롤러는 이 인터페이스를 호출해서 구현과 관계없이 로직의 일관성을 가질 수 있다.



## View 분리 - v2

모든 컨트롤러에서 뷰로 이동하는 부분에 중복이 있고 깔끔하지 않다.

viewPath, dispatcher.forward() 등등..

이부분을 따로 처리해주는 객체를 만들자



```java
package hello.servlet.web.frontcontroller;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyView {

  private String viewPath;

  public MyView(String viewPath) {
    this.viewPath = viewPath;
  }

  public void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
    dispatcher.forward(request, response);
  }
}
```



viewPath를 받아와서 forward!



HTTP 요청이 들어오면

1. URL 매핑 정보에서 컨트롤러 조회
2. FrontController가 해당하는 컨트롤러 호출
3. 각 컨트롤러가 **MyView** 반환
4. FrontController가 MyView.render() 호출
5. JSP forward
6. HTML 응답



프론트 컨트롤러의 도입으로 MyView 객체의 render()를 호출하는 부분을 모두 일관되게 처리할 수 있게 되었다. 각각의 컨트롤러는 MyView 객체를 생성만 해서 반환해주면 된다.



## Model 추가 - v3

##### 서블릿 종속성 제거

컨트롤러는 HttpServletRequest, HttpServletResponse가 필요없  다.

요청 파라미터 정보는 자바의 Map으로 대신 넘기면 지금 구조에서는 컨트롤러가 서블릿 기술을 몰라도 동작할 수 있다.

그리고 request 객체를 Model로 사용하는 대신, 변도의 Model 객체를 만들어서 반환하면 된다. 이렇게 수정하면 구현 코드도 매우 단순해지고, 테스트 코드 작성도 쉬워진다.



##### 뷰 이름 중복 제거

뷰 이름에 중복이 많다. `/WEB-INF/view/...` 

컨트롤러는 **뷰의 논리 이름**을 반환하고, 실제 물리 위치의 이름은 프론트 컨트롤러에서 처리하도록 단순화하자. 이렇게 해두면 나중에 뷰의 폴더 위치가 바뀌어도 프론트 컨트롤러만 고치면 된다. 



```java
package hello.servlet.web.frontcontroller;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelView {

  private String viewName;
  private Map<String, Object> model = new HashMap<>();

  public ModelView(String viewName) {
    this.viewName = viewName;
  }

}
```



##### ViewResolver

컨트롤러가 반환한 논리 뷰 이름을 실제 물리 뷰 경로로 변경하는 역할을 한다. 경로가 바뀌어도 컨트롤러의 코드는 전혀 건들 필요가 없다~!

JSP는 `request.getAttribute()`로 데이터를 조회하기 때문에, 모델의 데이터를 꺼내서 `request.setAttribute()`로 담아두어야 한다.



## 단순하고 실용적인 컨트롤러 - v4

v3 컨트롤러는 항상 `ModelView`객체를 생성하고 반환해야 하는 부분이 있다. 실제 구현하는 개발자들이 편리하게 개발할 수 있는 v4 버전을 만들어보자.



구조는 동일하지만, 컨트롤러가 `ModelView`가 아닌 `ViewName`을 반환한다.



```java
package hello.servlet.web.frontcontroller.v4;

import java.util.Map;

public interface ControllerV4 {

  String process(Map<String, String> paramMap, Map<String, Object> model);

}
```



```java
package hello.servlet.web.frontcontroller.v4;

import hello.servlet.web.frontcontroller.ModelView;
import hello.servlet.web.frontcontroller.MyView;
import hello.servlet.web.frontcontroller.v4.controller.MemberFormControllerV4;
import hello.servlet.web.frontcontroller.v4.controller.MemberListControllerV4;
import hello.servlet.web.frontcontroller.v4.controller.MemberSaveControllerV4;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "frontControllerServletV4", urlPatterns = "/front-controller/v4/*")
public class FrontControllerServletV4 extends HttpServlet {

  private Map<String, ControllerV4> controllerMap = new HashMap<>();

  public FrontControllerServletV4() {
    controllerMap.put("/front-controller/v4/members/new-form", new MemberFormControllerV4());
    controllerMap.put("/front-controller/v4/members/save", new MemberSaveControllerV4());
    controllerMap.put("/front-controller/v4/members", new MemberListControllerV4());
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String requestURI = request.getRequestURI();
    
    ControllerV4 controller = controllerMap.get(requestURI);
    if (controller == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    Map<String, String> paramMap = createParamMap(request);
    Map<String, Object> model = new HashMap<>();
    String viewName = controller.process(paramMap, model);

    MyView view = viewResolver(viewName);
    view.render(model, request, response);
  }

  private static Map<String, String> createParamMap(HttpServletRequest request) {
    Map<String, String> paramMap = new HashMap<>();
    request.getParameterNames().asIterator()
        .forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
    return paramMap;
  }

  private MyView viewResolver(String viewName) {
    return new MyView("/WEB-INF/views/" + viewName + ".jsp");
  }
}
```



아이디어는 간단하다.

기존 구조에서

- 모델을 파라미터로 넘긴다
- 뷰의 논리 이름(String)을 반환한다



## 유연한 컨트롤러1 - v5





## 유연한 컨트롤러2 - v5

