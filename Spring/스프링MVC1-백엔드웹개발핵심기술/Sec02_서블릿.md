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



#### Header 정보 갖고 오는 두 가지 방법

- `Enumeration<STring> headerNames = request.getHeaderNames()`: 옛날 방식
- `request.getHeaderNames().asIterator().forEachRemaining(headerName -> System.out.println(headerName + ": " + ~~))`



#### Header 편리한 조회

Host, Locale, Cookie, Content 정보 조회 가능



#### 기타 정보

기타 정보는 HTTP 메시지의 정보는 아니다.



## HTTP 요청 데이터 - 개요

HTTP 요청 메시지를 통해 클라이언트에서 서버로 데이터를 전달하는 방법



주로 세 가지 방법을 사용한다.

#### GET - 쿼리 파라미터

- /url?username=wow?age=20
- 메시지 바디 없이 url의 쿼리 파라미터에 데이터를 포함해서 전달
- 검색, 필터, 페이징 등에서 많이 사용하는 방식



#### POST - HTML Form

- content-type: application/x-www-form-urlencoded

- 메시지 바디에 쿼리 파라미터 형식으로 전달

  `username=hello&age=20`

- 회원 가입, 상품 주문, HTML Form 사용

- HTTP 스펙 상 PUT, PATCH 방식으로 보낼 수 없다.



#### HTTP message body에 데이터를 직접 담아서 요청

- HTTP API에 주로 사용. JSON, XML, TEXT
- 데이터 형식은 주로 JSON 사용
- POST, PUT, PATCH



## HTTP 요청 데이터 - GET 쿼리 파라미터



쿼리 파라미터는 URL에 `?` 를 시작으로 보낼 수 있다. 추가 파라미터는 `&`로 구분한다.

서버에서는 `HttpServletRequest`가 제공하는 `getParameter()` 메서드로 쿼리 파라미터를 편리하게 조회할 수 있다.



#### 이름이 중복된 파라미터 조회

`request.getParameterValues()`



## HTTP 요청 데이터 - POST HTML Form

- 메시지 바디에 들어가기 때문에 `content-type: application/x-www-urlencoded`
- 메시지 바디에 쿼리 파라미터 형식으로 데이터를 전달한다.
- application/x-www-form-urlencoded 형식은 앞서 GET에서 살펴본 쿼리 파라미터 형식과 같다. 따라서 쿼리 파라미터 조회 메서드를 그대로 사용하면 된다. 
- 클라이언트(웹 브라우저) 입장에서는 두 방식에 차이가 있지만, 서버 입장에서는 둘의 형식이 동일하므로, request.getParameter() 로 편리하게 구분없이 조회할 수 있다.



content-type은 HTTP 메시지 바디의 데이터 형식을 지정한다.

**GET URL 쿼리 파라미터 방식**으로 클라이언트에서 서버로 데이터를 전달할 때는 HTTP 메시지 바디를 사용하지 않기 때문에 content-type이 없다.

**POST HTML FORM 방식**으로 전달할 때는 content-type을 반드시 지정해야 한다.



## HTTP 요청 데이터 - API 메시지 바디 - 단순 텍스트

- 단순 텍스트 메시지를 HTTP 메시지 바디에 담아서 전송/읽어보자
- HTTP 메시지 바디의 데이터를 `InputStream`을 이용해서 직접 읽을 수 있다.
- `content-type=text/plain`
- `message body: hello`



## HTTP 요청 데이터 - API 메시지 바디 - JSON

- `content-type=application/json`
- message body: `{"username": "hello", "age": 20}`



JSON 라이브러리를 통해 JSON 형식으로 파싱할 수 있다.

`ObjectMapper.readValue()`



## HttpServletResponse - 기본 사용법

### HttpServletResponse의 역할

#### HTTP 응답 메시지 생성

- HTTP 응답 코드 지정
- 헤더 생성
- 바디 생성



#### 편의 기능 제공

- Content-type, cookie, redirect

- `setContentType()`, `setCharacterEncoding()`

- ``` java
  Cookie cookie = new Cookie("myCookie", "good");
  response.addCookie(cookie);
  ```

- `response.sendRedirect("/basic/hello-form.html");`



## HTTP 응답 데이터 - 단순 텍스트, HTML

HTTP 응답 메시지는 주로 다음 내용을 담아서 전달한다.

- 단순 텍스트 응답
- HTML 응답
- HTTP API - MessageBody JSON 응답



### HTML 응답

- HTTP 응답으로 HTML을 반환할 때는 content-type을 text/html 로 지정해야 한다.



## HTTP 응답 데이터 - API JSON



### HTTP API 응답

- HTTP 응답으로 JSON을 반환할 때는 content-type을 application/json 로 지정해야 한다. 
- Jackson 라이브러리가 제공하는 objectMapper.writeValueAsString() 를 사용하면 객체를 JSON 문자로 변경할 수 있다.
- application/json 은 스펙상 utf-8 형식을 사용하도록 정의되어 있다. 따라서`application/json;charset=utf-8` 로 전달하면 아무 의미가 없다.
- response.getWriter()를 사용하면 추가 파라미터를 자동으로 추가해버린다. 이때는 response.getOutputStream()으로 출력하면 그런 문제가 없다.

