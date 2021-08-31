## Section 0. Microservice와 Spring Cloud 소개



### 소프트웨어 아키텍처

- Antifragile
  - Auto scaling - 성수기에는 서버 개수 늘리고, 비수기에는 개수 줄이고 이런 작업을 수기로 하지 않고 CPU, 메모리, 네트워크, DB 사용량에 따라서 자동으로 처리
  - Microservices
  - Chaos engineering - 예견된/되지 않으 불확실성에 대한 대응 필요
  - Continuous deployments - CI/CD 배포 파이프라인

### Cloud Native Architecture

기존의 시스템을 클라우드 환경으로 전환하려면 어떠한 아키텍처를 가져야 할까?

#### cloud native architecture의 특징

- 확장 가능한 아키텍처
  - 시슽메의 수평적 확장에 유연
  - 확장된 서버(스케일 아웃)로 시스템의 부하 분산, 가용성 보장
  - 시스템 또는 서비스 애플리케이션 단위의 패키지 (컨테이너 기반 패지)
  - 모니터링
- 탄력적 아키텍처
  - 서비스 생성 - 통합 배포, 비즈니스 환경 변화에 대응 시간 단축
  - 분할된 서비스 구조
  - 무상태 통신 프로토콜
  - 서비스의 추가/삭제 자동으로 감지
  - 변경된 서비스 요청에 따라 사용자 요청 처리 (동적으로)
- 장애 격리 (fault isolation)
  - 특정 서비스에 오류가 발생해도 다른 서비스에 영향 주지 않음



### Cloud Native Application

#### CI/CD

- 지속적인 통합, Continuous Integration
  - 여러 개발자의 작업물을 하나로 통합하는 형상 관리, 또는 통합된 작업물을 빌드하고 테스트 하는 과정 자체 
- 지속적인 배포, Continuous Delivery/Deployment
  - pipe line

#### DevOps

Development + Operations (+ Quality Assurance)

![파일:Devops-toolchain.svg](https://upload.wikimedia.org/wikipedia/commons/thumb/0/05/Devops-toolchain.svg/512px-Devops-toolchain.svg.png)



#### Container 가상화

![Deployment evolution](https://d33wubrfki0l68.cloudfront.net/26a177ede4d7b032362289c6fccd448fc4a91174/eb693/images/docs/container_evolution.svg)



### Clout Native Application 구축함에 있어 고려해야 할 12 Factors

heroku 에서 제안한 항목들

https://12factor.net/ko/



+3

- API first
- Telementry
- Authentication and authorization



### Monolithic vs. MSA

Monolith Architecture

- 모든 업무 로직이 하나의 애플리케이션 형태로 패키지되어 서비스
- 애플리케이션에서 사용하는 데이터가 한 곳에 모여 참조되어 서비스되는 형태



Microservice

- Small autonomous services that work together - Sam Newman
- 서비스 특성에 맞게 서비스 별로 서로 다른 프로그래밍 언어나 데이터베이스를 사용할 수 있는 구조



### Microservice Architecture

Microservice 의 특징

1. Challenges
2. Small Well Chosen Deployable Units
3. Bounded Context
4. RESTful
5. Configuration Management
6. Cloud Enabled
7. Dynamic Scale Up And Scale Down
8. CI/CD
9. Visibility

 

### SOA vs. MSA

SOA(Service Oriented Architecture) 와 MSA 모두 서비스 지향적

- SOA 
  - 재사용을 통한 비용 절감
  - 공통의 서비스를 ESB에 모아 사업 측면에서 공통 서비스 형식으로 서비스 제공
  - 
- MSA 
  - 서비스 간의 결합도를 낮추어 변화에 능동적으로 대응
  - 각 독립된 서비스가 노출된 REST API를 사용



### Microservice Architecture Structures

![img](https://t1.daumcdn.net/cfile/tistory/99A060455C70137A29)





Service mesh - MSA 를 적용한 시스템의 내부 통신

- service router
- load balancing
- service discovery
- config. store
- Identity provider



MSA 표준 구성 요소

CNCF(Cloud Native Computing Foundation)

https://landscape.cncf.io/ 참고



### Spring Cloud

https://spring.io/projects/spring-cloud

