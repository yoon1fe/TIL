## 웹 서버, 웹 애플리케이션 서버

웹이라는 것은 HTTP 을 기반으로 통신한다.



- 최근 모든 데이터는 HTTP 프로토콜 기반으로 데이터를 주고 받는다.
- 거의 모든 형태의 데이터를 전송할 수 있다.
- 서버간에 데이터를 주고 받을 때도!



### 웹 서버(Web Server)

- HTTP 기반으로 동작
- 정적 리소스 재공, 기타 부가 기능
- 정적 HTML, CSS, JS, 이미지, 영상
- 예) NGINX, APACHE



### 웹 애플리케이션 서버(WAS - Web Application Server)

- HTTP 기반으로 동작

- 웹 서버 기능 포함

- 프로그램 코드를 실행해서 애플리케이션 로직 수행

  - 동적 HTML, HTTP API(JSON)

  - 서블릿, JSP, 스프링 MVC

- 예) 톰캣, Jetty, Undertow



### 웹 서버, WAS의 차이는?

- 웹 서버는 정적 리소스(파일), WAS는 애플리케이션 로직을 담고 있다.
- 하지만 웹 서버도 프로그램을 실행하는 기능을 포함하기도 하고, WAS도 웹 서버의 기능을 제공한다.
- 자바에서는 보통 **서블릿 컨테이너** 기능을 제공하면 WAS라고 부른다.



결론: WAS는 애플리케이션 코드를 실행하는데 더 특화되어 있다!



### 웹 시스템 구성

- 웹 시스템의 구성은 기본적으로 WAS, DB만으로 구성할 수 있다.

  - 하지만 WAS 하나로만 운영하면 WAS가 너무 많은 역할을 담당하게 된다.
  - 값비싼 애플리케이션 로직이 (이에 반해서 싼) 정적 리소스때문에 수행이 어려워질 수 있다.
  - WAS가 장애날 시에 오류 화면도 노출할 수 없게 된다.

- 그래서 일반적으로 웹 서버, WAS, DB 로 구성한다.

  - 정적 리소스는 웹 서버가 처리한다.
  - 웹 서버는 애플리케이션 로직같은 동적인 처리가 필요하면 WAS에 요청을 위임한다.
  - WAS는 중요한 애플리케이션 로직 처리를 전담할 수 있게 된다.

  -> 효율적인 리소스 관리를 할 수 있게 된다!

  - 정적 리소스만 제공하는 웹 서버는 잘 죽지 않는다.
  - 따라서 WAS나 DB 장애 시 웹 서버가 오류 화면을 제공해줄 수 있다.



## 서블릿

서블릿이 없다면 비즈니스 로직 실행 전후로 해야할 일이 너무 많다.



### 서블릿(Servlet)

``` java
@WebServlet(name = "helloServlet", urlPatterns = "/hello")
public class HelloServlet extends HttpServlet {
 @Override
 protected void service(HttpServletRequest request, HttpServletResponse response){
 //애플리케이션 로직
 }
}
```



- urlPatterns (`/hello`)의 URL이 호출되면 서블릿 코드가 실행된다.

- HTTP 요청 정보를 편리하게 사용할 수 있는 `HttpServletRequest`
- HTTP 응답 정보를 편리하게 제공할 수 있는 `HttpServletResponse` 
- 개발자는 HTTP 스펙을 매우 편리하게 사용할 수 있다.



![img](https://s1.md5.ltd/image/10413c72cf011f66c6601991bfa5601f.png)



### HTTP 요청 - 응답 흐름

- HTTP 요청 시
  - WAS는 Request, Response 객체를 새로 만들어서 서블릿 객체를 호출한다.
  - 개발자는 Request/Response 객체에서 HTTP 요청/응답 정보를 편리하게 꺼내서 사용/입력한다.
  - WAS는 Response 객체에 담겨있는 내용으로 HTTP 응답 정보를 생성한다.



### 서블릿 컨테이너

- 톰캣처럼 서블릿을 지원하는 WAS를 서블릿 컨테이너라고 한다.
- 서블릿 컨테이너는 서블릿 객체를 생성, 초기화, 호출, 종료하는 생명 주기를 관리한다.
- 서블릿 객체는 싱글톤으로 관리된다.
  - 고객의 요청이 올 때마다 객체를 생성하는 것은 비효율적
  - 최초 로딩 시점에 서블릿 객체를 미리 만들어두고 재활용
  - **공유변수 사용에 주의해야 한다**
  - 서블릿 컨테이너 종료 시 함께 종료
- JSP도 서블릿으로 변환되어서 사용한다.
- 동시 요청을 위한 멀티 쓰레드 처리를 지원한다.





## 동시 요청 - 멀티 쓰레드



### 쓰레드

- 쓰레드가 애플리케이션 코드를 하나하나 순차적으로 실행한다.
- 쓰레드는 한 번에 하나의 코드 라인만 수행한다.
- 동시 처리가 필요하면 쓰레드를 추가로 생성하면 된다.



### 요청마다 쓰레드를 생성한다면?

- 장점
  - 동시 요청 가능
  - 리소스(CPU, 메모리)가 허용할 때까지 처리 가능
  - 하나의 쓰레드가 지연되어도 나머지 쓰레드는 정상 동작
- 단점
  - 생성 비용이 매우 비싸다. 고객의 요청이 올 때마다 쓰레드를 생성하면 응답 속도가 늦어진다.
  - 쓰레드는 컨텍스트 스위칭 비용이 발생한다.
  - 쓰레드 생성에 제한이 없다. -> 고객 요청이 너무 많이 오면 CPU, 메모리 임계점을 넘어서 서버가 죽을 수도 있다.



### 쓰레드 풀

- 요청마다 쓰레드 생성의 단점 보완
- 필요한 쓰레드를 쓰레드 풀에 보관하고 관리한다.
- 쓰레드 풀에 생성 가능한 쓰레드의 최대치를 관리한다. 톰캣은 기본 설정이 최대 200개

- 장점
  - 쓰레드가 미리 생성되어 있으므로 생성 및 종료 비용이 절약되고, 응답 시간이 빠르다
  - 생성 가능한 최대치가 있으므로 너무 많은 요청이 들어와도 기존 요청을 안전하게 처리할 수 있다.

#### 실무 팁

- WAS의 주요 튜닝 포인트는 최대 쓰레드(max thread) 수이다.
- 이 값을 너무 낮게 설정하면?
  - 동시 요청이 많으면 서버 리소스는 여유롭지만, 클라이언트에게는 금방 응답 지연이 돌아간다.
- 반대로 너무 높게 설정하면?
  - 동시 요청이 많으면 CPU, 메모리 리소스 임계점 초과로 서버가 다운될 수 있다.
- 장애 발생시?
  - 클라우드면 일단 서버부터 늘리고, 이후에 튜닝
  - 아니면 열심히 튜닝;



### WAS의 멀티 쓰레드 지원

- 멀티 쓰레드에 대한 부분은 WAS가 처리해준다.
- **개발자가 멀티 쓰레드 관련 코드를 신경쓰지 않아도 된다!**
- 개발자는 마치 **싱글 쓰레드 프로그래밍을 하듯이 편리하게 소스 코드를 개발할 수 있다.**
- 멀티 쓰레드 환경이므로 싱글톤 객체(서블릿, 스프링 빈)는 주의해서 사용하자



## HTML, HTTP API, SSR, CSR

### 정적 리소스

- 고정된 HTML 파일, CSS, JS, 이미지, 영상 등을 제공



### HTTP API

- HTML이 아닌 데이터를 전달
- 주로 JSON 형식을 사용한다



### SSR (Server Side Rendering)

- HTML 최종 결과를 서버에서 만들어서 웹 브라우저에 전달
- 주로 정적인 화면에 사용된다.
- JSP, Thymeleaf -> 백엔드 개발자



### CSR (Client Side Rendering)

- HTML 결과를 자바스크립트를 사용해 웹 브라우저에서 동적으로 생성해서 사용한다.
- 주로 동적인 화면에 사용되며, 웹 환경을 마치 앱처럼 필요한 부분만 변경 가능하다
- 예) 구글 지도, Gmail, 구글 캘린더 등...
- React, Vue.js -> 프론트엔드 개발자



## 자바 백엔드 웹 기술 역사



### Web Reactive - Spring WebFlux

- 완전 최신 기술
- 비동기 논블로킹 처리를 한다
- 최소 쓰레드로 최대 성능을 낸다. 쓰레드 컨텍스트 스위칭 비용 효율화
- 함수형 스타일로 개발 - 동시 처리 코드 효율화
- 서블릿 기술 사용하지 않는다!

- 그런데..
  - WebFlux는 기술적 난이도가 매우 높다
  - 아직은 RDB에 대한 지원이 부족하다
  - 일반 MVC의 쓰레드 모델도 충분히 빠르다
  - 실무에서 아직 많이 사용하진 않는다.

