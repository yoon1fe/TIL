## TCP & UDP



**TCP와 UDP**는 모두 OSI 7계층 중에서 **전송(Transport) 계층**에서 사용하는 프로토콜이다. 전송 계층은 송신자와 수신자를 연결하는 통신 서비스를 제공하는 계층으로, 쉽게 말해 데이터의 전달을 담당한다. 즉, TCP와 UDP는 **데이터를 보내기 위해 사용하는 프로토콜**이다.



### UDP (User Datagram Protocol)

UDP를 문자 그대로 해석하면 데이터를 **데이터그램 단위로 처리하는 프로토콜**이라고 할 수 있다. 여기서 데이터그램이란, 독립적인 관계를 지니는 패킷이라는 뜻이다. UDP는 TCP와 달리 비연결형 프로토콜이다. 즉, 연결을 위해 할당되는 논리적인 경로가 없는데, 그렇기 때문에 각각의 패킷은 다른 경로로 전송되고, 각각의 패킷은 독립적인 관계를 지니게 되는데 이렇게 데이터를 서로 다른 경로로 독립적으로 처리하게 된다. UDP는 소켓 대신 IP 를 기반으로 데이터를 전송한다. 데이터그램 단위로 데이터를 전송하고, 그 크기는 65525 (2의 16제곱) 바이트로, 크기가 초과하면 잘라서 보낸다. 파일 전송같은 신뢰성이 필요한 서비스보단 성능이 중요시 되는 경우에 사용된다.

UDP를 사용하는 대표적인 예는 DNS가 있다. 어떤 호스트 네임의 IP 주소를 찾을 필요가 있는 프로그램은 DNS 서버로 호스트 네임을 포함한 UDP 패킷을 보낸다. 이 서버는 호스트의 IP 주소를 포함한 UDP 패킷으로 응답한다.

![img](https://t1.daumcdn.net/cfile/tistory/9969973359FEB59309)



### TCP (Transmission Control Protocol)

대부분의 인터넷 응용 분야들은 **신뢰성**과 **순차적인 전달**을 필요로 한다. UDP로는 이를 만족시킬 수 없기 때문에 탄생한 프로토콜이 바로 TCP 이다. 일반적으로 TCP는 IP와 함께 사용하는데, IP가 데이터의 배달을 처리한다면 TCP는 패킷을 추적 및 관리한다. TCP는 신뢰성이 없는 인터넷을 통해 종단간에 신뢰성 있는 **바이트 스트림을 전송**하도록 특별히 설계되었다. TCP 서비스는 송신자와 수신자 모두가 소켓이라고 부르는 종단점을 생성함으로써 이루어진다. TCP에서 연결 설정(Connection Establishment) 및 해제(Termination)는 각각 **3-way handshake와 4-way handshake**로 이루어진다. 신뢰성을 보장하기 위해 패킷에 대한 응답을 해야하기 때문에 UDP보다 속도가 느리다. 패킷이 손실된 경우 재전송 요청을 하기 때문에 스트리밍 서비스에 불리하다. 

모든 TCP 연결은 전이중(full-duplex), 점대점(point to point) 방식이다. 전이중이란 전송이 양방향으로 동시에 일어날 수 있음을 의미하며, 점대점이란 각 연결이 정확히 2개의 종단점을 가지고 있음을 의미한다. TCP는 multicasting이나 broadcasting을 지원하지 않는다.

![img](https://t1.daumcdn.net/cfile/tistory/991BEB3359FEB5712F)



#### [3 & 4 way handshake]

**3 way handshake - 연결 성립**

TCP는 정확한 전송을 보장해야 한다. 따라서 데이터를 주고받기에 앞서, 논리적인 접속을 성립하기 위해 3 way handshakr 과정을 진행한다.

![img](https://media.geeksforgeeks.org/wp-content/uploads/TCP-connection-1.png)

1. 클라이언트가 서버에게 SYN(SYNchronization) 플래그를 보낸다. (sequence: x)

   **P: Q야 통신하자(SYN)**

2. 서버가 SYN(x)을 받고, 클라이언트로 받았다는 신호인 ACK(ACKnowledgement) 와 SYN 플래그를 보낸다. (sequence: y, ACK: x + 1)

   **Q: 잘 받았어(ACK) 통신하자(SYN)**

3. 클라이언트는 서버의 응답으로 ACK(x + 1)와 SYN(y) 플래그를 받고, ACK(y+1)를 서버로 보낸다.

   **P: 잘 받아써**



이렇게 세 번의 통신이 완료되면 연결이 성립된다.



**4 way handshake - 연결 해제**

연결 성립 후, 모든 통신이 끝났다면 해제해야 한다.

![img](https://media.geeksforgeeks.org/wp-content/uploads/CN.png)



1. 클라이언트는 서버에게 연결을 종료한다는 FIN(FINish) 플래그를 보낸다.

   **P: 그만하자(FIN)**

2. 서버는 FIN을 받고, 확인했다는 ACK를 클라이언트에게 보낸다. 이 때 모든 데이터를 보내기 위해 TIME OUT 상태가 된다.

   **Q: 알겠어(ACK) 잠깐만 있어봐 (TIME OUT)**

3. 데이터를 모두 보냈다면, 연결이 종료되었다는 FIN 플래그를 클라이언트에게 보낸다.

   **Q: 마저 다 보냈어. 그만하자(FIN)**

4. 클라이언트는 FIN을 받고, 확인했다는 ACK를 서버에게 보낸다. 아직 서버로부터 받지 못한 데이터가 있을 수 있으므로 TIME_WAIT을 통해 기다린다.

   **P: 알게써(ACK) 잠만 있어바(TIME OUT)**

5. 서버는 ACK를 받은 이후 소켓을 닫는다. (closed)

6. TIME_WAIT 시간이 끝나면 클라이언트도 소켓을 닫는다. (closed)



이처럼 네 번의 통신이 완료되면 연결이 해제된다.







##### Reference

https://mangkyu.tistory.com/15

https://gyoogle.dev/blog/computer-science/network/TCP%203%20way%20handshake%20&%204%20way%20handshake.html

https://asfirstalways.tistory.com/356

[패킷의 플래그](https://mindgear.tistory.com/206)

