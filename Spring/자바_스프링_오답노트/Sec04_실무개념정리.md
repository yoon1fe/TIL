## 네트워크 용어

**도메인**

- 네트워크 상에서 컴퓨터를 식별하는 호스트명



**DNS(Domain Name System)**

- 호스트의 도메인 이름을 호스트의 네트워크 주소로 바꾸거나, 그 반대의 변환을 수행할 수 있도록 하기 위해 개발됨
- 이 도메인(example.com)으로 요청이 들어오면 이 서버로 연결해줘



**VIP(Virtual IP)**

- 클라우드 환경에서는 서버가 몇백대를 넘어가는 경우도 많음.
- 대표 IP로 생각하면 됨.
- 여러 개의 서버들의 대표 IP로, DNS 서버에 VIP 하나만 등록



**LB 로드 밸런싱**

- 부하 분산
- 둘, 셋 이상의 중앙처리장치 혹은 저장 장치와 같은 컴퓨터 자원들에게 작업을 나누는 것.

- == SLB(Server LB)
- L4 로드밸런싱 - 트랜스포트 레이어
- L7 로드밸런싱: 애플리케이션 레이어. URI나 HTTP 값 같은걸 기준으로 로드밸런싱



**GSLB(Global Server Load Balancing)**

- DNS 기반의 LB라서 IP가 아닌 도메인을 가짐.
- 로드밸런싱할 때 서버의 위치를 고려해서 효율적임



**FQDN(Fully Qualified Domain Name)**

- 풀 도메인
- 서브 도메인을 포함한 도메인



**CDN(Content Delivery Network)**

- 컨텐츠를 효율적으로 전달하기 위해 여러 노드를 가진 네트워크에 데이터를 저장하여 제공하는 시스템
- 네트워크 캐시처럼 동작



**ACL(Access Control List)**

- 접근 제어 목록. 개체나 개체 속성에 적용되어 있는 허가 목록
- 보통 화이트 리스트로 관리



**Proxy 서버**

- 클라이언트가 자신을 통해서 다른 네트워크 서비스에 간접적으로 접속할 수 있게 해주는 컴퓨터 시스템이나 응용 프로그램
- 역할
  - 캐시
  - 액세스 컨트롤
  - 보안
  - 사용률 파악 ..



**SSL**

- HTTP 를 HTTPS 로 만들어주는 보안 프로토콜



## 인프라 용어

**Container**

- OS 레벨에서 애플리케이션 실행 환경을 격리함으로써 마치 다른 OS에서 동작하는 것과 같은 가상 실행 환경을 제공하는 기술

- VM 보다 조금 더 가벼운 방식의 가상화 기술



**Docker**

- 리눅스 응용 프로그램들을 프로세스 격리 기술들을 사용해 컨테이너로 실행하고 관리하는 오픈 소스 프로젝트



**VPN(Vitual Private Network)**

- 공중 네트워크를 통해 내부 망에 통신할 목적으로 쓰이는 사설 통신망



**VDI(Virtual Desktop Infrastructure)**

- 가상 데스크탑 인프라 시스템 기술
- 가상 테스크탑 생성 블록 및 연결 블록, 자원 관련 블록 등으로 구분돼 각 블록은 독립적으로 수행하는 형태로 동작함



## 운영 용어

**HA(High Availability)**

- 고가용성. == 절대 고장 나지 않음
- 이중화를 통해 HA를 높일 수 있다.
  - Active - Standby: 시스템 하나만 사용
  - Active - Active: 시스템 둘 다 사용. LB
  - Master - Slave
- Failover: 시스템에 이상이 생겼을 때 예비 시스템으로 자동 전환되는 기능
- Switchover: 수동 전환되는 기능



**CI / CD**

- Continuous Integration / Continuous Deployment
- 사실상 현업에서는 자동 빌드 / 자동 배포에 가까움



## k8s

**컨테이너 오케스트레이션**

- 컨테이너를 자동으로 배포 / 관리 / 스케일링할 수 있도록 도와주는 도구
- 컨테이너 몇 개 띄우고, 응답이 없는 컨테이너는 죽이고, 컨테이너가 죽었으면 설정한 개수에 맞춰서 새로 띄우고 등등.. 하는 작업

- ingress -> service -> pod
- 사용자가 요청을 보내면 맨 앞단의 Ingress-nginx가 받고, 요청에 맞는 service로 요청보냄. service -> pod(실제 우리 애플리케이션)



## MySQL 팁

**실행 계획**

- SQL 쿼리를 옵티마이저가 어떻게 처리할지 보는 방법

- `EXPLAIN SELECT * FROM tbl WHERE c1 = '1'`

- | 칼럼        | 설명                                         | OK                                    | WARNING                                                      |
  | ----------- | -------------------------------------------- | ------------------------------------- | ------------------------------------------------------------ |
  | select_type | 일반 쿼리인지, 서브 쿼리인지, Union 쿼리인지 |                                       | DEPENDENT<br />DERIVED                                       |
  | type        | 어떤 타입의 인덱스를 참조했는지              | const<br />eq_ref<br />ref<br />range | index - 인덱스 풀 스캔<br />fulltext - 전문 검색<br />all - 모든 데이터 풀스캔 |
  | key         | 어떤 인덱스를 사용했는지                     |                                       |                                                              |
  | Extra       | 쿼리를 어떤 식으로 풀었는지                  | Using index                           | Full scan ~<br />Impossible ~<br />No matching ~ <br />Using filesort<br />Using temporary<br />Using join buffer |



**트랜잭션 격리 레벨**

|                  | Dirty Read<br />커밋되지 않은 값인데 다른 곳에서 읽힘 | Non-repeatable read<br />하나의 트랜잭션에서 똑같은 쿼리의 결과가 다를 수 있다 | Phantom read<br />하나의 트랜잭션에서 똑같은 쿼리의 결과 레코드가 새로 생성되거나 삭제될 수 있다 |
| ---------------- | ----------------------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| READ UNCOMMITTED | O                                                     | O                                                            | O                                                            |
| READ COMMITTED   | X                                                     | O                                                            | O                                                            |
| REPEATABLE READ  | X                                                     | X                                                            | O(InnoDB에서는 X)                                            |
| SERIALIZABLE     | X                                                     | X                                                            | X                                                            |

- SERIALIZABLE은 ACID 를 만족하지만 동시성이 너무 떨어지기 때문에 사용하지 않음



**실무상의 조언**

- 외래키는 공짜가 아니다. 외래키 사용은 지양하자.
- 한방 쿼리 지양하자. ex) case, if then, 서브 쿼리
  - 애플리케이션단에 맡기자
- Fulltext index 사용 금지.
- In 쿼리에 개수가 많아지면 풀 스캔을 한다. 쿼리를 메모리에 올리는 것도 메모리 제한이 있기 때문.
- like 검색에서 %는 뒤에만. 인덱스가 정렬되어 있기 때문에 인덱스를 그나마 태울 수 있다.
- with Redis - write-back 형 캐시 메모리 도입도 좋은 방안.