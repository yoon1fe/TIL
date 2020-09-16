## AJAX(Asynchronous Javascript And XML)

Ajax는 언어나 프레임워크가 아닌 구현하는 방식을 의미한다.

웹에서 화면을 갱신하지 않고, 데이터를 서버로부터 가져와 처리하는 방법을 의미힌다.

JavaScript의 XMLHttpRequest(XHR) 객체로 데이터를 전달하고 비동기 방식으로 결과를 조회한다.

화면 갱신이 없으므로 사용자 입장에서는 편리하지만, 동적으로 DOM을 구성해야 하므로 구현이 복잡하다.



#### 일반 요청에 대한 응답

- 데이터를 입력 후 event 발생
- Ajax를 적용하지 않은 요청은 서버에서 data를 이용하여 로직을 처리한다.
- logic 처리에 대한 결과에 따라 응답 page를 생성하고 클라이언트에게 전송(화면 전환)

#### Ajax 요청에 대한 응답

- 데이터를 입력 후 event 발생
- Ajax를 적용하면 event 발생 시 서버에서 요청을 처리한 후 Text, XML 또는 JSON으로 응답한다.
- 클라이언트(브라우저)에서는 이 응답 데이터를 이용하여 화면 전환없이 현재 페이지에서 동적으로 화면을 재구성한다.



### Javascript AJAX

XMLHttpRequest는 자바스크립트가 Ajax 방식으로 통신할 때 사용하는 객체이다.

XMLHttpRequest 객체는 Ajax 통신 시 전송 방식, 경로 등 전송 정보를 싣는 역할을 한다.

실제 서버와의 통신은 브라우저의 Ajax 엔진에서 수행한다.

직접 자바스크립트로 Ajax를 프로그래밍할 경우 브라우저 별로 통신 방식이 달라 코드가 복잡해진다.



#### httpRequest의 속성 값

**readyState**

| 값   | 의미          | 설명                                                     |
| ---- | ------------- | -------------------------------------------------------- |
| 0    | Uninitialized | 객체만 생성(open 메소드 호출 전)                         |
| 1    | Loading       | open 메소드 호출                                         |
| 2    | Loaded        | send 메소드 호출. status의 헤더가 아직 도착하기 전 상태. |
| 3    | Interactive   | 데이터 일부를 받은 상태                                  |
| 4    | Completed     | 데이터 전부를 받은 상태                                  |

**status**

| 값   | 텍스트(status Text)   | 설명           |
| ---- | --------------------- | -------------- |
| 200  | OK                    | 요청 성공      |
| 403  | Forbidden             | 접근 거부      |
| 404  | Not Found             | 페이지 없음    |
| 500  | Internal Server Error | 서버 오류 발생 |





### jQuery Ajax 함수 - $.ajax()

