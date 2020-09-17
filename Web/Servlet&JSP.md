## Servlet



### Servlet 이란?

Java Servlet이란 자바를 이용하여 웹 페이지를 동적으로 생성하는 서버단 프로그램, 혹은 그 사양을 말하며, 흔히 "서블릿"이라고 한다. 자바 서블릿은 웹 서버의 성능을 향상하기 위해 사용되는 자바 클래스의 일종이다.

![](https://t1.daumcdn.net/cfile/tistory/2445804457382D9145)



### Servlet LifeCycle

Servlet 클래스는 JAVASE에서의 클래스와 다르게 main 메소드가 없다. 즉, 객체의 생성부터 사용(method call)의 주체가 사용자가 아닌 Servlet Container에게 있는 것이다.

클라이언트가 요청(request)를 하게 되면 Servlet Container는 Servlet 객체를 생성(한 번)하고, 초기화(한 번)하며, 요청에 대한 처리(요청시마다 반복)를 하게 된다. 또한, Servlet 객체가 필요없어지면 제거되는 일까지 Container가 담당한다.



![](https://t1.daumcdn.net/cfile/tistory/991870335A04292F0B)



- init() - 서블릿이 메모리에 로드될 때 한 번 호출된다. (코드 수정으로 인해 다시 로드되면 다시 호출된다.)
- doGet() - GET 방식으로 데이터를 전송할 때 호출된다.
- doPost() - POST 방식으로 데이터를 전송할 때 호출된다.
- service() - 모든 요청은 service() 메소드를 통해서 doXXX() 메소드로 이동한다.
- destroy() - 서블릿이 메모리에서 해제되면 호출된다.(코드가 수정되면 호출된다.)



### Parameter 전송 방식

- GET 
  - 전송되는 데이터가 URL뒤에 QueryString으로 전달된다. 입력 값이 적은 경우나 데이터가 노출이 되어도 문제가 없는 경우 사용된다.
  - 간단한 데이터를 빠르게 전송할 수 있다.
  - form tag 뿐만 아니라 직접 URL에 입력하여 전송할 수도 있다.
  - 데이터 양에 제한이 있다.
- POST
  - URL과 별도로 전송된다.
  - HTTP header 뒤 body에 입력 스트림 데이터로 전달된다.
  - 데이터의 제한이 없다.
  - 최소한의 보안 유지 효과를 볼 수 있다.
  - 전달 데이터의 양이 같을 경우, 전송 패킷을 body에 데이터를 구성해야 하므로 GET 방식보다 느리다.



## JSP(Java Server Pages)

자바 서버 페이지는 HTML내에 자바 코드를 삽입하여 웹 서버에서 동적으로 웹 페이지를 생성하여 웹 브라우저에 돌려주는 언어이다. JAVA EE 스펙 중 일부로, 웹 애플리케이션 서버에서 동작한다.

JSP는 실행 시에는 Servlet으로 변환된 후 실행되므로 Servlet과 거의 유사하다고 볼 수 있지만, Servlet과 달리 HTML 표준에 따라 작성되므로 웹 디자인하기에 편리하다.





### JSP 스크립팅 요소(Scripting Element)

1. Declaration - 멤버 변수 선언이나 메소드를 선언하는 영역이다.

   ````jsp
   <%!
   String name;
   
   public void init(){
   	name = "우왕";
   }
   %>
   ````

2. Scriptlet - 클라이언트 요청 시 매번 호출되는 영역으로, Servlet으로 변환 시 service() method에 해당되는 영역이다. request, response에 관련된 코드를 구현한다.

   ```jsp
   <%
   	// 로직
   %>
   ```

3. Expression - 데이터를 브라우저에 출력할 때 사용한다.

   ```html
   내 이름은 <%= name %> 입니다.
   <!-- <% out.print(name); %> 과 같다. -->
   ```

   * 문자열 뒤에 세미콜론을 붙이면 안된다.

   

### JSP 기본 객체

- request - HTML 폼 요소의 선택 값 등 사용자 입력 정보를 읽어올 때 사용

- response - 사용자 요청에 대한 응답을 처리하기 위해 사용

- pageContext - 각종 기본 객체를 얻거나 forward 및 include 기능을 활용할 때 사용

- session - 클라이언트에 대한 세션 정보를 처리하기 위해 사용.

  page directive의 session 속성을 false로 하면 내장 객체는 생성이 되지 않는다.

- application - 웹 서버의 애플리케이션 처리와 관련된 정보를 레퍼런스하기 위해 사용

- out - 사용자에게 전달하기 위한 output 스트림을 처리할 때 사용

- config - 현재 JSP에 대한 초기화 환경을 처리하기 위해 사용

- page - 현재 JSP에 대한 참조 변수에 해당된다.





### JSP 기본 객체의 영역(scope)

- pageContext - 하나의 JSP 페이지를 처리할 때 사용되는 영역이다. 한 번의 클라이언트 요청에 대하여 하나의 JSP 페이지가 호출되며, 이 때 단 한 개의 page 객체만 대응이 된다. 페이지 영역에 저장한 값은 페이지를 벗어나면 사라진다.
- request - 하나의 HTTP 요청을 처리할 때 사용되는 영역이다. 웹 브라우저가 요청을 할 때마다 새로운 request 객체가 생성된다. request 영역에 저장한 속성은 그 요청에 대한 응답이 완료되면 사라진다.
- session - 하나의 웹 브라우저와 관련된 영역이다. 같은 웹 브라우저 내에서 요청되는 페이지들은 같은 session들을 공유하게 된다.
- application - 하나의 웹 애플리케이션과 관련된 영역이다. 웹 애플리케이션당 한 개의 application 객체가 생성된다. 같은 웹 애플리케이션에서 요청되는 페이지들은 같은 application 객체를 공유한다.



### 공통 method

- void setAttribute(String name, Object value)

  문자열 name 이름으로 Object형 데이터를 저장한다. Object형이기 때문에 어떠한 자바 객체도 저장할 수 있다.

- Object getAttribute(String name)

  문자열 name에 해당하는 attribute 값이 있다면 Object 형태로 반환하고, 없으면 null을 리턴한다. 리턴 값에 대한 적절한 형 변환이 필요하다.

- Enumeration getAttributeNames() 

  현재 객체에 저장된 속성들의 이름들을 Enumeration 형태로 가져온다.

- void removeAttribute(String name)

  문자열 name에 해당하는 속성을 삭제한다.





### 페이지 이동 - forward 와 redirect

#### forward(request, response)

```jsp
RequestDispatcher dispatcher = request.getRequestDispatcher(path);
dispatcher.forward(request, response);
```

동일한 서버 내의 경로로 이동할 수 있다.

기존의 URL을 유지한다. 따라서 실제로 이동되는 주소를 확인할 수 없다.

기존의 request, response 객체가그대로 전달된다.

비교적 빠르다.

request의 setAttribute(name, value)를 통해 데이터를 전달한다.



#### sendRedirect(location)

```jsp
response.sendRidirect(location);
```

동일한 서버 포함 다른 URL이 가능하다.

이동하는 페이지로 URL이 변경된다.

기존의 request와 response는 소멸되고, 새로운 request와 response가 생성된다.

forward()에 비해 느리다.

데이터는 request로는 저장할 수 없다. session이나 cookie를 이용한다.