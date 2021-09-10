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







## HTTP 메서드





## HTTP 메서드 활용





## HTTP 상태 코드







## HTTP 헤더 1 - 일반 헤더





## HTTP 헤더 2 - 캐시와 조건부 요청







