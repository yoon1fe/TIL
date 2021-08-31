## Section 1. Service Discovery

Service Discovery - 외부에서 다른 서비스들이 마이크로 서비스들을 검색하기 위해 사용되는 개념. 일종의 전화번호부

key - value

A 서버 - 어디

B 서버 - 어디

Netflix eureka 제품 사용할 거다.

 

### Spring Cloud Netflix Eureka

한 PC 내에서 여러 인스턴스 - 포트 번호로 구분

여러 PC 있다면? server1:8080, server2:8080 ,,,





### User Service - 등록

- VM options
  - -D : 자바 클래스 실행할 때 주는 옵션

![image-20210831101428973](/Users/nhn/Library/Application Support/typora-user-images/image-20210831101428973.png)



### User Service - Load Balancer

- application.yml -> server.port: 0 => 랜덤 포트 사용

  ![image-20210831105759748](/Users/nhn/Library/Application Support/typora-user-images/image-20210831105759748.png)

  