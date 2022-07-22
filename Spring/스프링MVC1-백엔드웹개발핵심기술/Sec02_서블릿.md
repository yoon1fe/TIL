## Hello Servlet



`@ServletComponentScan`: 스프링 부트는 **서블릿을 직접 등록해서 사용할 수 있도록** 해당 어노테이션을 지원한다.



HTTP 요청을 통해 매핑된 URL이 호출되면 서블릿 컨테이너는 다음 메서드를 실행한다. 

``` java
protected void service(HttpServletRequest request, HttpServletResponse response)
```



```java
String username = request.getParameter("username");	// 쿼리 파라미터 갖고오기
```



```java
// 응답
response.setContentType("text/plain");
response.setCharacterEncoding("utf-8");

response.getWriter().write("hello " + username);
```



### 서블릿 컨테이너 동작 방식

- 스프링 부트를 실행하면 스프링 부트가 내장 톰캣 서버(서블릿 컨테이너 내장) 실행
- 내장 톰캣 서버가 서블릿 컨테이너에 `helloServlet` 객체 생성
- HTTP 요청이 들어오면 요청 메시지를 기반으로 HttpServletRequest 객체를 생 -> helloServlet(service()) 호출
- 종료되면서 WAS 서버가 response 정보를 가지고 HTTP 응답 메시지를 생성해서 웹 브라우저에 전달



## HttpServletRequest - 개요

- HTTP 요청 메시지를 개발자가 직접 파싱해서 사용해도 되지만, 매우 불편할 것이다. 

- 서블릿은 개발자가 HTTP 요청 메시지를 편리하게 사용할 수 있도록 개발자 대신에 HTTP 요청 메시지를 파싱한다.

- 그리고 그 결과를 `HttpServletRequest` 객체에 담아서 제공한다. 

- `HttpServletRequest`를 사용하면 다음과 같은 HTTP 요청 메시지를 편리하게 조회할 수 있다.

  ```
  POST /save HTTP/1.1
  Host: localhost:8080
  Content-Type: application/x-www-form-urlencoded
  username=kim&age=20
  ```



`HttpServletRequest` 객체는 추가로 여러가지 부가기능도 함께 제공한다. 

#### 임시 저장소 기능 

해당 HTTP 요청이 시작부터 끝날 때 까지 유지되는 임시 저장소 기능 

- 저장: `request.setAttribute(name, value)`
- 조회: `request.getAttribute(name)`



#### 세션 관리 기능 

- `request.getSession(create: true)`



#### 중요!

`HttpServletRequest`, `HttpServletResponse`를 사용할 때 가장 중요한 점은 이 객체들이 HTTP 요청 메시지, HTTP 응답 메시지를 편리하게 사용하도록 도와주는 객체라는 점이다. 따라서 이 기능에 대해서 깊이있는 이해를 하려면 HTTP 스펙이 제공하는 요청, 응답 메시지 자체를 이해해야 한다!



## HttpServletRequest - 기본 사용법







## HTTP 요청 데이터 - 개요





## HTTP 요청 데이터 - GET 쿼리 파라미터





## HTTP 요청 데이터 - POST HTML Form





## HTTP 요청 데이터 - API 메시지 바디 - 단순 텍스트





## HTTP 요청 데이터 - API 메시지 바디 - JSON





## HttpServletResponse - 기본 사용법





## HTTP 응답 데이터 - 단순 텍스트, HTML





## HTTP 응답 데이터 - API JSON



