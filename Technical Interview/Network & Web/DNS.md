## DNS (https://naver.com/ 에 들어가기까지의 과정)

####  

#### [DNS (Domain Name System)]

DNS는 특정 컴퓨터 또는 네트워크로 연결된 임의의 장치의 주소를 찾기 위해, 사람이 이해하기 쉬운 도메인 이름을 숫자로 된 IP 주소로 변환해주는 시스템이다.

![img](https://t1.daumcdn.net/cfile/tistory/9991FF375C0FDC242B)





그럼 웹 브라우저의 주소창에 `https://naver.com/`을 검색했을 때 일어나는 일을 알아보자.

####  

#### [DNS 동작 방식]

**1. 웹 브라우저에서 URL을 입력하고 Enter를 클릭한다.**

**2. 웹 브라우저가 입력한 URL을 분석하며 일을 하기 시작한다.**

- URL의 구조

  Scheme: `//:@:/?#`

  여기서 Scheme은 프로토콜로, http, ftp, sftp, pop3, IMAP 등을 말한다.

  e.g) `https://yoon1fe.tistory.com/manage/newpost/?type=post&returnURL=a`

  https 프로토콜을 사용해 `yoon1fe.tistory.com`라는 도메인에 접근한다.

  이 때, Path는 `manage/newpost/` 이고, 쿼리는 `type=post&returnURL=a` 가 된다.

***3. URL을 분석한 다음,\*** 

- URL 구조가 맞지 않은 경우 -> 사용 중인 브라우저의 검색 엔진으로 입력어를 검색한다.
- URL 구조가 맞는 경우 -> HSTS (HTTP Strict Transport Security) 목록을 받아와 URL 내의 주소와 목록의 주소가 일치하면 https로, 그렇지 않으면 http로 첫 요청을 보낸다.

**4. DNS (Domain Name System) Server에 내가 접근하려는 도메인의 IP 주소를 요청한다.**

1. DNS에 요청을 보내기 전에 먼저 **브라우저에 해당 도메인이 캐시**되어 있는지 먼저 확인한다. 있으면 그 IP 주소를 리턴한다.
2. 없을 경우 **로컬에 저장되어 있는 hosts 파일**에서 참조할 수 있는 도메인이 있는지 확인한다.
3. 1. , 2. 모두 실패했을 경우 시스템에 설정된 DNS 서버인 **로컬 DNS 서버**에 `naver.com`의 IP 주소를 요청한다. 로컬 DNS 서버에 도메인의 IP 주소가 있다면 바로 반환하고, 그렇지 않다면 다음과 같은 작업을 수행한다.
4. **Root DNS 서버**에 `naver.com`을 요청한다(DNS Query). Root DNS 서버에는 `naver.com`에 대한 정보는 없지만, `.com` **도메인을 관리하는 서버에 대한 정보**를 알려준다.
5. **`.com` DNS 서버(TLD, Top Level Domain)**에 `naver.com`을 요청한다. `.com` DNS 서버는 **`naver` 도메인을 관리하고 있는 네임 서버에 대한 정보**를 알려준다.
6. `naver.com` 네임 서버에 `naver.com` 를 요청한다. 네임 서버는 `naver.com` 의 IP 주소를 반환한다.
7. 로컬 DNS 서버가 클라이언트에게 `naver.com` 의 IP 주소를 전달해준다.

- DNS 서버는 한 번 검색한 결과는 로컬 메모리에 캐싱되며, 같은 정보가 요청되면 캐시에 있는 정보를 전송한다. 이 때, 캐시에는 유효 기간(TTL, Time To Live)이 정해져 있으므로 유효 기간이 지난 정보는 캐시에서 삭제된다. 일반적으로 로컬 DNS 서버에 TLD 서버들이 캐싱된다. 따라서 Root DNS 서버는 자주 방문하지 않는다.



*** 로컬 DNS 서버란 ?**

각 ISP (Internet Service Provider, KT, SKT 같은 ..)는 로컬 DNS 서버를 가진다. Default Name Server라고도 한다.

호스트가 DNS 질의를 하면 로컬 DNS 서버로 질의가 전송된다.



#### [DNS에서 TCP와 UDP 동작 차이점]

DNS는 기본적으로 UDP를 이용하지만, 특수한 상황에선 TCP를 이용해서 조금 더 안정성을 보장하기도 한다.

- DNS 포트

  기본은 UDP/53, 특수한 상황에서는 TCP/53을 사용한다.

- UDP가 사용되는 경우

  **일반적인 DNS 질의 및 응답**에 사용된다. 이는 TCP에 비해 프로토콜 오버 헤드가 작다는 이점 때문이다. TCP에서는 실제 데이터를 송신할 때까지 3-way handshake와 같은 일련의 처리 과정을 거쳐야 하기 때문에 비효율적이다. 이에 비해 UDP는 신뢰성 확보를 위한 처리가 없어 DNS 질의와 같은 적은 데이터를 주고받을 때 TCP 보다 유리하다.

- TCP가 사용되는 경우

  1. Zone transfer (DNS 트랜잭션 유형 중 하나로, 다수의 DNS 서버 간 데이터베이스를 복제하는데 사용되는 방법)

     안정성을 위해 두 개 이상의 DNS 서버를 이용할 경우 Master에서 Slave로 zone 정보를 보낼 경우, 안정성을 위해 TCP를 사용한다.

  2. 메시지 사이즈가 512Byte를 넘는 경우

     www로 매핑된 서버가 많이 있는 경우, 메시지 사이즈가 512 Byte를 넘을 수 있는데, 그럴 경우에 TCP로 재질의하여 응답을 받는다.











Reference

[https://velog.io/@directorhwan59/%EC%9B%B9-%EB%B8%8C%EB%9D%BC%EC%9A%B0%EC%A0%80%EC%97%90-URL%EC%9D%84-%EC%9E%85%EB%A0%A5%ED%95%98%EA%B3%A0-%EC%B2%AB-%ED%99%94%EB%A9%B4%EC%9D%B4-%EC%B6%9C%EB%A0%A5%EB%90%98%EA%B8%B0%EA%B9%8C%EC%A7%80](https://velog.io/@directorhwan59/웹-브라우저에-URL을-입력하고-첫-화면이-출력되기까지)

https://m.blog.naver.com/PostView.nhn?blogId=junhyung17&logNo=220506163514&proxyReferer=https:%2F%2Fwww.google.com%2F

https://jacking.tistory.com/1356