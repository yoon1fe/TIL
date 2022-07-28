## 회원 관리 웹 애플리케이션 요구사항



``` java
package hello.servlet.domain.member;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MemberRepositoryTest {

  MemberRepository memberRepository = MemberRepository.getInstance();

  @AfterEach
  void afterEach() {
    memberRepository.clearStore();
  }

  @Test
  void save() {
    // given
    Member member = new Member("username", 30);

    // when
    Member savedMember = memberRepository.save(member);

    // then
    Member findMember = memberRepository.findById(savedMember.getId());
    assertThat(findMember).isEqualTo(savedMember);
  }

  @Test
  void findAll() {
    // given
    Member member1 = new Member("username1", 20);
    Member member2 = new Member("username2", 30);

    memberRepository.save(member1);
    memberRepository.save(member2);

    // when
    List<Member> result = memberRepository.findAll();

    // then
    assertThat(result.size()).isEqualTo(2);
    assertThat(result).contains(member1, member2);

  }


}
```





## 서블릿으로 회원 관리 웹 애플리케이션 만들기



- 서블릿과 자바 코드만으로 HTML 만들기 -> 자바 코드 안에 html
- HTML 안에 자바 코드 -> **템플릿 엔진**(JSP, Thymeleaf, Freemarker, Velocity 등.. )



## JSP로 회원 관리 웹 애플리케이션 만들기

JSP를 사용하면 HTML 안에 자바 코드를 넣어서 편하게 사용할 수 있다.

하지만 여전히 비즈니스 로직과 화면단(html)이 혼재되어 있다.



#### MVC(Model, View, Controller) 패턴의 등장

비즈니스 로직은 서블릿처럼 다른 곳에서 처리하고, JSP는 목적에 맞게 HTML로 화면을 그리는 일에 집중하도록 하자.



## MVC 패턴 - 개요

#### 너무 많은 역할

하나의 서블릿이나 JSP만으로 비즈니스 로직과 뷰 렌더링까지 모두 처리하면 너무 많은 역할을 하게 되고, 결과적으로 유지보수가 어려워진다.



#### 변경의 라이프 사이클

비즈니스 로직과 뷰 렌더링의 라이프 사이클이 다르다는 점이 굉장이 중요하다. 변경의 라이프 사이클이 다른 부분을 하나의 하나의 코드로 관리하는 것은 유지보수하기 좋지 않다.



#### 기능 특화

뷰 템플릿은 화면을 렌더링하는 업무만 담당하는 것이 가장 효과적이다.



#### Model View Controller

- **Model**: 뷰에 출력할 데이터를 담아둔다. 뷰가 필요한 데이터를 모두 모델에 담아서 전달해주기 때문에 뷰는 비즈니스 로직이나 데이터 접근을 몰라도 되고, 화면을 렌더링하는 일에 집중할 수 있다.
- **View**: 모델에 담겨있는 데이터를 사용해서 화면을 그리는(렌더링) 일에 집중한다. 여기서는 HTML 생성하는 부분
- **Controller**: HTTP 요청을 받아서 파라미터를 검증하고, 비즈니스 로직을 실행한다. 그리고 뷰에 전달할 결과 데이터를 조회해서 모델에 담는다.
- 비즈니스 로직와 컨트롤러 로직과 분리하는 것이 좋다. -> MVC2 모델
  - 비즈니스 로직: 서비스 계층



## MVC 패턴 - 적용

컨트롤러 -> 서블릿

뷰 -> JSP

모델 -> `HttpServletRequest`



- `dispatcher.forward()` : 다른 서블릿이나 JSP로 이동할 수 있는 기능이다. **서버 내부에서** 다시 호출이 발생한다. 리다이렉트와 다르다.

- `/WEB-INF`: 이 경로안에 JSP가 있으면 외부에서 직접  JSP를 호출할 수 없다. 우리가 기대하는 것은 **항상 컨트롤러를 통해서 JSP를 호출하는 것이다.** WAS 에서 정해져 있는 룰!



- 상대 경로는 `/`로 시작하지 않는다. 상대 경로를 사용하면 폼 전송시 현재 URL이 속한 계층 경로 + `/save`가 호출된다.
  - 현재 계층 경로: `/servlet-mvc/members`
  - 결과: `servlet-mvc/members/save`



## MVC 패턴 - 한계

MVC 패턴을 적용한 덕분에 컨트롤러의 역할과 뷰를 렌더링 하는 역할을 명확하게 구분할 수 있다. 특히 뷰는 화면을 그리는 역할에 충실한 덕분에, 코드가 깔끔하고 직관적이다. 단순하게 모델에서 필요한 데이터를 꺼내고, 화면을 만들면 된다. 그런데 **컨트롤러는 딱 봐도 중복이 많고, 필요하지 않는 코드들도 많다.**



### MVC 컨트롤러의 단점

- `forward` 중복

  ``` java
  RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
  dispatcher.forward(request, response);
  ```

- `viewPath` 중복

  ``` java
  String viewPath = "/WEB-INF/views/new-form.jsp";
  ```

  - 만약 jsp를 thymeleaf와 같은 다른 템플릿 엔진으로 변경한다면 전체 코드를 다 변경해야 한다.

- 사용하지 않는 코드

  다음 코드를 사용하지 않을 때도 있다. 특히 response는 현재 코드에서 사용되지 않는다.

  ``` java
  HttpServletRequest request, HttpServletResponse response
  ```

  얘들을 사용하는 코드는 테스트 케이스를 작성하기도 어렵다.

- 공통 처리가 어렵다

  공통 기능을 메서드로 뽑으면 될 것 같지만, 결과적으로 해당 메서드를 항상 호출해야 한다. 호출하는 것 자체가 중복!



공통 처리가 어렵다는 문제를 해결하려면 컨트롤러 호출 전에 공통 기능을 처리해야 한다. **프론트 컨트롤러 패턴**을 도입하면 이런 문제를 해결할 수 있다!

**스프링 MVC의 핵심이 바로 이 프론트 컨트롤러에 있다.**

