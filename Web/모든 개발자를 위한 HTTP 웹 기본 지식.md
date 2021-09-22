# 모든 개발자를 위한 HTTP 웹 기본 지식



## 인터넷 네트워크

### 인터넷 통신

인터넷에서 컴퓨터 둘(서버 - 클라)은 어떻게 통신할까? 

중간에 인터넷이 있다.

IP(Internet Protocol) 클라와 서버가 IP 주소를 부여 받는다.

- 지정한 IP 주소에 데이터 전달
- 패킷이라는 통신 단위로 전달한다.

IP 프로토콜의 한계

- 비연결성

  패킷을 받을 대상이 없거나 서비스 불능 상태여도 패킷을 전송한다.

- 비신뢰성

  중간에 패킷이 사라지면?

  패킷이 순서대로 안오면??

- 프로그램 구분

  같은 IP를 사용하는 서버에서 통신하는 애플리케이션이 둘 이상이라면???



### TCP, UDP

인터넷 프로토콜 스택의 4계층

- 애플리케이션 계층 - HTTP, FTP
- 전송 계층 - TCP, UDP
- 인터넷 계층 - IP
- 네트워크 인터페이스 계층



- 전송 제어 프로토콜 (TCP, Transmission Control Protocol)

  - 연결 지향 - TCP 3-way handshake (가상(논리적) 연결 -진짜로 연결된 것이 아니다.)

    요즘엔 3. ACK 보낼 때 데이터도 전송한댄다...

  - 데이터 전달 보증

  - 순서 보장

  - 신뢰할 수 있는 프로토콜

  - 현재는 대부분 TCP 사용한다.

- 사용자 데이터그램 프로토콜(UDP, User Datagram Protocol)

  - 하얀 도화지에 비유할 수 있다. (기능이 거의 없음)
  - 연결 지향 - TCP 3-way handshake X
  - 데이터 전달 보증 X
  - 순서 보장 X
  - 데이터 전달 및 순서가 보장되지 않지만, 단순하고 빠름 -> 스트리밍..?
  - 정리
    - IP와 거의 같다. + PORT + CHECKSUm 정도만 추가
    - 애플리케이션에서 추가 작업이 필요하다!



### PORT

한 번에 둘 이상 연결해야 한다면?? 

- port - 같은 IP 내에서 프로세스 구분

  port 정보는 패킷 안에 있다

  ex) IP - 아파트, port - 동/호수..



### DNS

IP 기억하기 어렵다.. 변경될 수도 있다.

- 도메인 네임 시스템 (DNS, Domain Name System)

  전화번호부

  도메인 명을 IP 주소로 변환



## URI와 웹 브라우저 요청 흐름

### URI(Uniform Resource Identifier)

리소스를 식별..

URI / URL / URN

"URI는 로케이터(Locator), 이름(Name) 또는 둘 다 추가로 분류될 수 있다."

![URI, URL, URN](https://t1.daumcdn.net/cfile/tistory/2416C94158D62B9E11)

URI 단어 뜻

Uniform - 리소스를 식별하는 통일된 방식

Resource - 자원, URI로 식별할 수 있는 모든 것

Identifier - 다른 항목과 구분하는데 필요한 정보



Locator - 리소스가 있는 위치를 지정

Name - 리소스에 이름을 부여

위치는 변할 수 있지만, 이름은 변하지 않는다.

URN 만으로 실제 리소스를 찾을 수 있는 방법은 보편화되지 않았다.



### 웹 브라우저 요청 흐름

`https://www.google.com/search?q=hello&h=ko` 라고 웹 브라우저에 입력한다면

`www.google.com` DNS 조회 -> IP: 200.200.200.2

`:443` -> port 생략



HTTP 요청 메시지

``` http
GET /search?q=hello&hl=ko HTTP/1.1
Host: www.google.com
```



### HTTP 메시지 전송

1. 웹 브라우저가 HTTP 메시지 생성

2. SOCKET 라이브러리를 통해 전달

   A: TCP/IP 연결(IP, PORT)

   B: 데이터 전달

3. TCP/IP 패킷 생성, HTTP 메시지 포함



이 메시지가 서버에 도착하면 HTTP 메시지를 해석한 후 클라이언트에 HTTP 응답 메시지를 보낸다.

HTTP 응답 메시지

``` http
HTTP/1.1 200 OK
Content-Type: text/html;charset=UTF-8
Content-Length: 3424

<html>
	<body>...</body>
</html>
```



클라이언트의 웹 브라우저가 얘를 받으면 HTML 렌더링해서 브라우저 상에 보여준다.



## HTTP 기본

### 모든 것이 HTTP

HyperText Transfer Protocol

지금은 모든 것을 HTTP 프로토콜에 담아서 통신한다!

HTML, TEXT, 이미지, 음성, 영상, 파일, JSON, XML, 등등.. 거의 모든 형태의 데이터 전송 가능하다

서버간 데이터를 주고 받을 때도 대부분 HTTP 사용한다. 게임이나.. 특별한 경우에나 TCP 통신하고..



#### HTTP의 역사

- HTTP/0.9 - GET 메서드만 지원, 헤더 X
- HTTP/1.0 - 메서드, 헤더 추가
- **HTTP/1.1** - 가장 많이 사용, 가장 중요한 버전!
  - RFC2068 -> RFC2616 -> RFC7230~7235
- HTTP/2 - 성능 개선
- HTTP/3 - TCP 대신 UDP 사용, 성능 개선



#### 기반 프로토콜

- TCP - HTTP/1.1, HTTP/2

  -> 기본 매커니즘 자체가 성능이 좋은 프로토콜이 아니다..

- UDP - HTTP/3

  -> 고로 성능 개선!

- 현재 HTTP/1.1 주로 사용한다.

  - /2, /3도 증가하는 추세



#### HTTP 특징

- 클라이언트-서버 구조로 동작한다.
- 무상태 프로토콜 지향, 비연결성
- HTTP 메시지를 통해 통신한다.
- 단순함, 확장 가능



### 클라이언트 서버 구조

- Request - Response 구조
- 클라이언트는 서버에 요청을 보내고, 응답을 대기
- 서버가 요청에 대한 결과를 만들어서 응답



### Stateful, Stateless

- 무상태(**Stateless**) 프로토콜 지향..

- 서버가 클라이언트의 상태를 보존하지 않는다는 의미!

  -> 응답하는 서버가 항상 같은 서버이지 않아도 된다.

  클라이언트가 요청에 필요한 정보를 모두 담아서 요청보낸다.

- 무상태는 스케일 아웃에 유리하다!



#### Stateless 실무 한계

- 모든 것을 무상태로 설계할 수 있는 경우도 있고 없는 경우도 있다.

- 무상태

  예) 로그인이 필요없는 단순한 서비스 소개 화면

- 상태 유지

  예) 로그인

- 로그인한 사용자의 경우 로그인했다는 상태를 서버에 유지

  - 일반적으로 브라우저 쿠키와 서버 세션 등을 사용해서 상태 유지한다!

- 상태 유지는 최소한만 사용





### 비연결성(Connectionless)

#### 연결을 유지하는 모델의 경우...

여러 클라이언트가 요청해서 연결이 유지되는 경우, 서버는 여러 연결을 계속 유지하고, 서버의 자원이 낭비된다

**반면에, ** 연결을 유지하지 않는 모델의 경우 요청이 온 경우에만 응답하면 서버는 최소한의 자원만으로 서버를 유지할 수 있다!!



#### 비연결성

- HTTP 는 기본적으로 연결을 유지하지 않는 모델
- 일반적으로 초 단위 이하의 빠른 속도로 응답
- 한시간동안 수천명이 서비스를 사용해도 실제 서버에서 동시에 처리하는 요청은 수십개 이하로 매우 적다
- 서버 자원을 매우 효율적으로 사용할 수 있다



#### 비연결성의 한계와 극복

- TCP/IP 연결을 계속 새로 맺어야 한다. - 3-way handshake 시간 추가

- 웹 브라우저로 사이트를 요청하면 HTML 뿐만 아니라 자바스크립트, css, 추가 이미지 등 수많은 자원이 함께 다운로드 된다.

- 지금은 HTTP 지속 연결(Persistent Connections)로 문제 해결

  Keep-Alive.. 랑 같은 맥락

- HTTP/2, HTTP/3 에서 더 많은 최적화가 이루어졌다.



### HTTP 메시지

#### HTTP 메시지 구조

``` http
start-line

header

empty line

message body
```



#### HTTP 요청 메시지

``` http
GET /search?q=hello&hl=ko HTTP/1.1		(start-line)
Host: www.google.com									(header)
																			(empty line)
```

- 요청 메시지도 message body 가질 수 있다.



#### HTTP 응답 메시지

``` http
HTTP/1.1 200 OK												(start-line)
Content-Type: text/html;charset=UTF-8	(header)
Content-Length: 3424
																			(empty line)
<html>																(message body)
	<body>...</body>
</html>
```



#### 시작 라인

- 요청 메시지
  - start-line = **request-line** / status-line
  - request-line = method SP(공백) request-target SP HTTP-version CRLF(엔터)
  - HTTP 메서드 (GET: 조회)
    - GET, POST, PUT, DELETE ...
    - 서버가 수행해야 할 동작 지정
      - GET - 리소스 조회
      - POST - 요청 내역 처리
  - 요청 대상 (/search?q=hello&hl=ko)
    - absolute-path[?query]
    - 절대 경로 = "/"로 시작하는 경로
  - HTTP 버전
- 응답 메시지
  - start-line = **status-line**
  - status-line = HTTP-version SP status-code SP reason-phrase CRLF
  - HTTP 버전
  - HTTP 상태 코드: 요청 성공/실패 나타냄
    - 200: 성공
    - 400: 클라이언트 요청 오류
    - 500: 서버 내부 오류
  - 이유 문구: 사람이 이해할 수 있는 짧은 상태 코드 설명 글

#### HTTP 헤더

- header-field = field-name ":" OWS field-value OWS (OWS: 띄어쓰기 허용)

- field-name 은 대소문자 구분 없음

- 용도

  - HTTP 전송에 필요한 모든 부가 정보

    예) 메시지 바디의 내용, 메시지 바디의 크기, 압축, 인증, 요청 클라이언트 정보, 서버 애플리케이션 정보 .. 등등

  - 표준 헤더가 너무 많음

  - 필요시 임의의 헤더 추가 가능



#### HTTP 메시지 바디

- 용도

  - 실제 전송할 데이터

    HTML 문서, 이미지, 영상, JSON 등등 byte 로 표현할 수 있는 모든 데이터 전송 가능하다



#### 단순함! 확장 가능!!





## HTTP 메서드

### HTTP API를 만들어보자!

#### 요구사항 - 회원 정보 관리 API 

- 회원 목록 조회
  - URI - `/read-member-list`
- 회원 조회
  - 
- 회원 등록
- 회원 수정
- 회원 삭제



**URI 설계해보자**

- `/read-member-list` 
- `read-member-by-id`
- ... ㅇ

이게 좋은 설계일까??

가장 중요한 것은 **리소스 식별**!!



- 리소스의 의미는 뭘까?
  - 회원을 등록하고 수정하고 조회하는게 리소스가 아니다!
  - **회원이라는 개념 자체가 리소스**
- 리소스를 어떻게 식별하는 것이 좋을까?
  - 회원을 등록하고 수정하고 조회하는 것을 모두 배제
  - **회원이라는 리소스만 식별하면 된다. -> 회원 리소스를 URI에 매핑**



**다시 설계해보자**

- 회원 목록 조회 - `/members`
- 회원 조회 - `/members/{id}`
- 회원 등록 - `/members/{id}`
- 회원 수정 - `/members/{id}`
- 회원 삭제 - `/members/{id}`

** 참고: 계층 구조상 상위를 컬렉션으로 보고 복수 단어 사용을 권장!



**그럼 조회/등록/수정/삭제를 어떻게 구분하지??**



#### 리소스와 행위를 분리 - 가장 중요한 것은 리소스를 식별하는 것

- URI 는 리소스만 식별한다!

- 리소스와 해당 리소스를 대상으로 하는 **행위**를 분리하자

  - 리소스: 회원
  - 행위: 조회, 등록, 삭제, 변경

- 리소스는 명사, 행위는 동사

- 행위(메서드)는 어떻게 구분하지?

  -> HTTP 메서드로!!!



### HTTP 메서드 - GET, POST

#### 주요 메서드

- GET: 리소스 조회
- POST: 요청 데이터 처리, 주로 등록에 사용
- PUT: 리소스를 대체, 해당 리소스가 없으면 생성
- PATCH: 리소스 부분 변경
- DELETE: 리소스 삭제

** 최근엔 리소스 -> Representation

#### 기타 메서드

- HEAD
- OPTIONS
- CONNECT
- TRACE



#### GET

- 리소스 조회

- 서버에 전달하고 싶은 데이터를 query(쿼리 파라미터, 쿼리 스트링)를 통해서 전달

- 메시지 바디를 사용해서 데이터를 전달할 수 있지만, 지원하지 않는 곳이 많아서 권장하지 않는다!



#### POST

- 요청 데이터 처리

- **메시지 바디를 통해 서버로 요청 데이터 전달**

- 서버는 요청 데이터를 **처리**

  메시지 바디를 통해 들어온 데이터를 처리하는 모든 기능을 수행한다.

- 주로 전달된 데이터로 신규 리소스 등록, 프로세스 처리에 사용

- 스펙 상 - POST 메서드는 **대상 리소스가 리소스의 고유한 의미 체계에 따라 요청에 포함된 표현을 처리하도록 요청**한다
- **새 리소스 생성(등록)**
- **요청 데이터 처리**
- **다른 메서드로 저리하기 애매한 경우**



### HTTP 메서드 - PUT, PATCH, DELETE

#### PUT

- 리소스를 대체

  있으면 대체

  없으면 생성

  덮어씌우기

- **중요! 클라이언트가 리소스를 식별**하고 있어야 한다!

  클라이언트가 리소스 위치를 알고 URI를 지정한다.

  POST와의 큰 차이점이다.

- PUT 은 기존 리소스를 **수정**하는 메서드가 아니다!



그럼 부분 변경은??



#### PATCH

- 리소스 부분 변경

#### DELETE

- 리소스 제거



### HTTP 메서드의 속성

| HTTP 메소드 |                       RFC                       | 요청에 Body가 있음 | 응답에 Body가 있음 |  안전  | 멱등(Idempotent) | 캐시 가능 |
| :---------: | :---------------------------------------------: | :----------------: | :----------------: | :----: | :--------------: | :-------: |
|     GET     | [RFC 7231](https://tools.ietf.org/html/rfc7231) |       아니요       |         예         |   예   |        예        |    예     |
|    HEAD     | [RFC 7231](https://tools.ietf.org/html/rfc7231) |       아니요       |       아니요       |   예   |        예        |    예     |
|    POST     | [RFC 7231](https://tools.ietf.org/html/rfc7231) |         예         |         예         | 아니요 |      아니요      |    예     |
|     PUT     | [RFC 7231](https://tools.ietf.org/html/rfc7231) |         예         |         예         | 아니요 |        예        |  아니요   |
|   DELETE    | [RFC 7231](https://tools.ietf.org/html/rfc7231) |       아니요       |         예         | 아니요 |        예        |  아니요   |
|   CONNECT   | [RFC 7231](https://tools.ietf.org/html/rfc7231) |         예         |         예         | 아니요 |      아니요      |  아니요   |
|   OPTIONS   | [RFC 7231](https://tools.ietf.org/html/rfc7231) |     선택 사항      |         예         |   예   |        예        |  아니요   |
|    TRACE    | [RFC 7231](https://tools.ietf.org/html/rfc7231) |       아니요       |         예         |   예   |        예        |  아니요   |
|    PATCH    | [RFC 5789](https://tools.ietf.org/html/rfc5789) |         예         |         예         | 아니요 |      아니요      |    예     |





#### 안전(Safe Methods)

- 호출해도 **리소스를 변경하지 않는 속성**

  Q: 그래도 계속 호출해서 로그 같은게 쌓여서 장애가 발생하면??

  A: 안전은 해당 **리소스**만 고려한다. 그런 부분까지 고려하지 않는다!



#### 멱등(Idempotent Methods)

- f(f(x)) = f(x)

- **한 번 호출하든 두 번 호출하든 백 번 호출하든 결과가 똑같은 성질**

- 멱등 메서드

  - GET: 한 번 조회하든, 두 번 조회하든 같은 결과가 조회된다.
  - PUT: 결과를 대체한다. 따라서 같은 요청을 여러 번 해도 최종 결과는 같다.
  - DELETE: 결과를 삭제한다. 같은 요청을 여러 번 해도 삭제된 결과는 같다.
  - ~~POST: 멱등 아님!! 두 번 호출하면 같은 결제가 중복해서 발생할 수 있다!~~

- 언제 쓰냐??

  - 자동 복구 메커니즘
  - **서버가 TIMEOUT 등으로 정상 응답을 못 주었을 때, 클라이언트가 같은 요청을 다시 해도 되는가? 에 대한 판단 근거가 될 수 있다.**

  Q: 재요청 중간에 다른 곳에서 리소스를 변경해버리면??

  - 사용자1: GET -> username: A, age: 20
  - 사용자2: PUT -> username: A, age: 30
  - 사용자1: GET -> username: A, age: 30 -> 사용자2의 영향으로 바뀐 데이터 조회

  **A: 멱등은 외부 요인으로 중간에 리소스가 변경되는 것까지는 고려하지 않는다!**



#### 캐시 가능(Cacheable Methods)

- 응답 결과 리소스를 캐시해서 사용해도 되는가?
- GET, HEAD, POST, PATCH 캐시 가능
- 실제로는 GET, HEAD 정도만 캐시로 사용한다.
  - POST, PATCH 는 본문 내용까지 캐시 키로 고려해야 하는데, 구현이 쉽지 않다



## HTTP 메서드 활용

### 클라이언트 -> 서버 데이터 전송

**데이터 전달 방식은 크게 두 가지**

- 쿼리 파라미터를 통한 데이터 전송
  - GET
  - 주로 정렬 필터(검색어)
- 메시지 바디를 통한 데이터 전송
  - POST, PUT, PATCH
  - 회원 가입, 상품 주문, 리소스 등록, 리소스 변경



#### 주로 네 가지 상황이 있다

- 정적 데이터 조회

  - 이미지, 정적 텍스트 문서

- 동적 데이터 조회

  - 주로 검색, 게시판 목록에서 정렬 필터(검색어)

- HTML Form을 통한 데이터 전송

  - 회원 가입, 상품 주문, 데이터 변경
  - 디폴트 컨텐트 타입 - application/x-www-form-urlencoded <- form 의 내용을 메시지 바디를 통해 전송.
  - Content-Type: multipart/form-data - 메시지 바디에 바이너리 데이터 들어감
  - POST - HTTP 메시지 바디에 정보가 들어가고,
  - GET - 쿼리 스트링에 들어간다. <- 조회에만 사용하자!
  - 참고: HTML Form 전송은 POST, GET 만 지원!!

- HTTP API를 통한 데이터 전송

  - HTTP 요청 메시지를 아예 만들어서 전송. Content-Type 다 지정해서..

  - 회원 가입, 상품 주문, 데이터 변경
  - 서버 to 서버, 앱 클라이언트, 웹 클라이언트 (ajax)



### HTTP API 설계 예시

### 회원 관리 시스템

#### API 설계 - POST 기반 등록

- 회원 목록 - GET `/members`
- 회원 등록 - POST `/members`
- 회원 조회 - GET `/members/{id}`
- 회원 수정 - PATCH, PUT, POST `/members/{id}`
- 회원 삭제 - DELETE `/members/{id}`



#### POST - 신규 자원 등록 특징

- 클라이언트는 등록될 리소스의 URI를 모른다.

  - 회원 등록 -  `POST /members`

- 서버가 새로 등록된 리소스 URI를 생성해준다.

  - HTTP/1.1 201 Created

    Locatiopn: /members/100

- 컬렉션

  - 서버가 관리하는 리소스 디렉토리
  - 서버가 리소스의 URI 를 생성하고 관리
  - 여기서 컬렉션은 `/members`



### 파일 관리 시스템

#### API 설계 - PUT 기반 등록

- 파일 목록 - GET `/files`
- 파일 조회 - GET `/files/{filename}`
- 파일 등록 - PUT `/files/{filename}`
- 파일 삭제 - DELETE `/files/{filename}`
- 파일 대량 등록 - POST `/files`



#### PUT - 신규 자원 등록 특징

- 클라이언트가 리소스 URI를 알고 있어야 한다.
  - 파일 등록 PUT `/files/{filename}`
  - PUT `/files/star.jpg`
- 클라이언트가 직접 리소스의 URI를 지정한다.
- 스토어
  - 클라이언트가 관리하는 리소스 저장소
  - 클라이언트가 리소스의 URI를 알고 관리
  - 여기서 스토어는 `/files`



### HTML FORM 사용

- HTML form 은 GET, POST 만 지원한다..
- 이는 ajax 같은기술을 사용해서 해결 가능!
- 여기서는 순수 html, html form 이야기만 해보자



- 회원 목록 - GET `/members`
- 회원 등록 폼 - GET `/members/new`
- 회원 등록 - POST `/members/new, /members`  등록 폼과 등록 url을 통일하는 것 추천..!
- 회원 조회 - GET `/members/{id}`
- 회원 수정 폼 - GET `/members/{id}/edit`
- 회원 수정 - POST `/members/{id}/edit,/members/{id}`
- 회원 삭제 - POST `/members/{id}/delete`



-> html form은 GET, POST만 지원하므로 제약이 많다. 따라서 동사로 된 리소스 경로(컨트롤 URI)를 사용한다.

HTTP 메서드로 해결하기 애매한 경우 사용한다.



#### 참고하면 좋은 URI 설계 개념

- 문서(Document)
  - 단일 개념(파일 하나, 객체 인스턴스, 데이터베이스 row)
  - `/members/100`, `/files/star.jpg`
- 컬렉션(Collection)
  - 서버가 관리하는 리소스 디렉터리
  - 서버가 리소스의 URI를 생성하고 관리
  - `/members`
- 스토어(Store)
  - 클라이언트가 관리하는 자원 저장소
  - 클라이언트가 리소스의 URI를 알고 관리
  - `/files`
- 컨트롤러(Controller), 컨트롤 URI
  - 문서, 컬렉션, 스토어가 해결하기 어려운 추가 프로세스 실행
  - 동사를 직접 사용
  - `/members/{id}/delete`







## HTTP 상태 코드







## HTTP 헤더 1 - 일반 헤더





## HTTP 헤더 2 - 캐시와 조건부 요청







