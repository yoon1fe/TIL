## Chapter 5. 서버측의 LAN에는 무엇이 있는가?

_방화벽과 캐시 서버의 탐험



패킷이 최종적으로 웹 서버 측의 LAN에 도착하면, 방화벽이 들어오는 패킷을 검사한다. 그리고 페이지의 데이터 중 다시 쓸 수 있는 데이터들은 캐시 서버에 들어가게 된다.





### 1. 웹 서버의 설치 장소

보통 클라이언트 PC는가정이나 회사의 LAN에 설치되어 있지만, 서버는 그렇지 않다. 사내에 설치할 수도 있고, 데이터센터에 설치할 수도 있다.

예전에는 사내에 웹 서버를 설치하는 경우가 많았지만, 현재는 이 방법을 많이 사용하지 않는다. 그 이유 중 하나는 IP 주소의 부족때문이다. 다른 이유는 보안 상의 이유이다. 최근에는 바화벽을 두어 사내 LAN과 웹 서버를 분리하는 방법도 채택되고 있다. 방화벽은 관문 역할을 하여 특정 서버에서 동작하는 특정 애플리케이션에 액세스하는 패킷만 통과시키고, 그 외의 패킷은 차단하는 역할을 한다.



데이터센터는 보통 내진 구조, 자가 바전 장치 비치 등 물리적으로도 안정적인 공간에 설치한다. 따라서 일반 회사 내부보다 안전성이 높다. 또한, 데이터센터는 서버의 설치 장소만 제공할 뿐만 아니라 기기의 가동 상태 감시, 방화벽의 설치 운영, 부정 침입 감시라는 부가 서비스를 제공하는 경우가 많다.



### 2. 방화벽의 원리와 동작

최근에는 서버의 설치 장소와 관계없이 서버 바로 앞에 방화벽이 있다. 특정 서버나 애플리케이션의 패킷만 통과시키는 것이 방화벽의 역할인데, 네트워크에는 다양한 종류의 패킷이 많이 흐르고 있다. 따라서 이 중 통과시킬 패킷과 차단할 패킷을 고르는 것은 매우 어려운 일이기 때문에 다양한 방법이 고안되어왔는데, 가장 많이 보급된 방법은 **패킷 필터링형**이다. 



패킷 헤더에는 통신 동작을 제어하는 제어 정보가 들어있어서 이를 통해 여러 사항을 확인할 수 있다. 그 중 다음 항목들은 패킷 필터링의 조건 설정에서 자주 사용되는 것이다.

- MAC 헤더
- IP 헤더
- TCP / UDP 헤더

추가로, 헤더가 아니라 ICMP 메시지의 내용을 사용하기도 한다.







### 3. 복수 서버에 리퀘스트를 분배한 서버의 부하 분산



### 4. 캐시 서버를 이용한 서버의 부하 분산



### 5. 콘텐츠 배포 서비스



