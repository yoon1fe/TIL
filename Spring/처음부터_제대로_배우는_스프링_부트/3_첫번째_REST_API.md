최근엔 다음과 같은 환경을 제외하고는 마이크로 서비스를 많이 사용한다.

- 도메인과 도메인의 경계가 모호할 때
- 제공된 기능이 긴밀히 결합되고, 모듈 간 상호작용에서 유연함보다 성능이 절대적으로 더 중요할 때
- 관련된 모든 기능의 애플리케이션 확장 요구사항이 알려져있고 일관적일 때
- 기능이 변동성이 없을 때



애플리케이션/마이크로 서비스를 연결하는 방식은 많지만, API(개발자가 작성한 사양/인터페이스) 가 짱이다.



### REST API

- **RE**presentational **S**tate **T**ransfer
- 애플리케이션 A와 B가 통신할 때, 통신 시점의 **현재 상태**를 주고받는다. 누적된 프로세스 정보와 같은 상태를 유지할 필요 없음.

- `GET`: 읽기
- `POST`: 생성
- `PUT`, `PATCH`: 업데이트
- `DELETE`: 삭제





**`@RestController`**

- 스프링 MVC는 데이터(Model) / 데이터를 전송하는 부분(Controller) / 데이터를 표현하는 부분(View) 을 분리해서 생성한다. 이러한 부분들을 연결하는데 `@Controller`가 도움을 줌.
  - `@Controller`가 붙은 클래스는 `Model` 객체를 받는다. `Model` 객체로 표현 계층에 모델 기반 데이터를 제공.
  - `ViewResolver`와 함께 작동해 애플리케이션이 렌더링된 뷰를 표시하게 한다.
- `@ResponseBody`: JSON이나 XML같은 데이터 형식처럼 형식화된 응답을 반환하도록 설정
- `@RestController` = `@Controller` + `@ResponseBody`



`PUT` 메서드 응답 시 HTTP 상태 코드 필수. 반환할 객체와 HTTP 상태 코드가 포함된 `ResponseEntity`를 반환하도록 한다.

