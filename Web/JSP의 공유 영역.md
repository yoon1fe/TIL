### JSP의 공유 영역

setAttribute() -> Controller에서 

getAttribute() -> View 에서 (JSP)



**pageContext**

한 번의 클라이언트 요청이 오면, 하나의 JSP 페이지가 응답된다.

page 영역은 이 때 하나의 JSP 페이지 내에서만 객체를 공유하는 영역을 의미한다. 즉, pageContext 객체는 page 영역에서만 유효하다.



**request**

요청을 받아서 응답하기까지 객체가 유효한 영역이다.

보통 서블릿에서 JSP로 객체를 보낼 때 사용하는 방법이다.



**session**

하나의 브라우저 당 한 개의 session 객체가 생성된다. 즉, 같은 브라우저 내에서 요청되는 페이지들은 같은 객체를 공유한다.



**application**

하나의 애플리케이션 당 한 개의 application 객체가 생성된다. 즉, 같은 애플리케이션 내에서 요청되는 페이지들은 같은 객체를 공유한다.



![img](https://t1.daumcdn.net/cfile/tistory/99D9A73D5AAE58C40B)