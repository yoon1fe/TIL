## 라이브러리 직접 관리

프로젝트를 처음 시작할 때 어떤 라이브러리를 사용할지 결정해야 함. + 버전까지. 호환성 체크 필요하기 때문.. 부트가 나오기 전에는  이런 문제들 때문에 초기 세팅하는데 상당히 많은 시간이 소비됐었다.



스프링 부트는 개발자가 라이브러리들을 편리하게 사용할 수 있는 다양한 기능을 제공.

- 외부 라이브러리 버전 관리
- 스프링 부트 스타터 제공



#### 라이브러리 직접 관리

**build.gradle**

```groovy
dependencies {

    //1. 라이브러리 직접 지정
    //스프링 웹 MVC
    implementation 'org.springframework:spring-webmvc:6.0.4'
    //내장 톰캣
    implementation 'org.apache.tomcat.embed:tomcat-embed-core:10.1.5'
    //JSON 처리
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.14.1'
    //스프링 부트 관련
    implementation 'org.springframework.boot:spring-boot:3.0.2'
    implementation 'org.springframework.boot:spring-boot-autoconfigure:3.0.2'
    //LOG 관련
    implementation 'ch.qos.logback:logback-classic:1.4.5'
    implementation 'org.apache.logging.log4j:log4j-to-slf4j:2.19.0'
    implementation 'org.slf4j:jul-to-slf4j:2.0.6'
    //YML 관련
    implementation 'org.yaml:snakeyaml:1.33'
}
```

- 스프링 웹 MVC, 내장 톰캣, JSON 처리, 스프링 부트 관련, LOG, YML 등등 다양한 라이브러리가 사용된 다.

- 스프링 웹 MVC, 내장 톰캣, JSON 처리, 스프링 부트 관련, LOG, YML 등등 다양한 라이브러리가 사용된 다.



**라이브러리 직접 선택시 발생하는 문제**

웹 프로젝트를 하나 설정하기 위해서는 수 많은 라이브러리를 알아야 한다. 여기에 추가로 각각의 라이브러리의 버전까 지 골라서 선택해야 한다. 여기서 눈에 보이지 않는 가장 어려운 문제는 각 라이브러리들 간에 서로 호환이 잘 되는 버전 도 있지만 **호환이 잘 안되는 버전도 있다는 점**이다. 개발자가 라이브러리의 버전을 선택할 때 이런 부분까지 고려하는 것은 매우 어렵다.



## 스프링 부트 라이브러리 버전 관리

스프링 부트는 외부 라이브러리 버전을 직접 관리한다. 따라서 버전은 생략 가능! 

스프링 부트가 부트 버전에 맞춘 최적의 버전을 갖고온다.



**버전 관리 기능을 사용하려면 `io.spring.dependency-management` 플러그인을 사용해야 한다.**

- `spring-boot-dependencies` 는 스프링 부트 gradle 플러그인에서 사용하기 때문에 개발자의 눈에 의존 관계로 보이진 않는다.
- 현재 프로젝트에서 지정한 스프링 부트 버전을 참고한다.

- 스프링 부트가 관리하지 않는 외부 라이브러리는 버전을 직접 적어주어야 함.



## 스프링 부트 스타터

스프링 부트는 대중적인 라이브러리들을 모아둔 spring boot starter 를 제공한다.



```groovy
dependencies {
  implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

- 이거 하나만 추가하면 web 애플리케이션 개발에 필요한 라이브러리들이 다 포함된다.



**이름 패턴**

`spring-boot-starter-*`

- spring-boot-starter-data-jpa
- spring-boot-starter-web
- ..



**자주 사용하는 것들**

- spring-boot-starter : 핵심 스타터, 자동 구성, 로깅, YAML
- spring-boot-starter-jdbc : JDBC, HikariCP 커넥션풀 
- spring-boot-starter-data-jpa : 스프링 데이터 JPA, 하이버네이트 
- spring-boot-starter-data-mongodb : 스프링 데이터 몽고 
- spring-boot-starter-data-redis : 스프링 데이터 Redis, Lettuce 클라이언트 
- spring-boot-starter-thymeleaf : 타임리프 뷰와 웹 MVC 
- spring-boot-starter-web : 웹 구축을 위한 스타터, RESTful, 스프링 MVC, 내장 톰캣 
- spring-boot-starter-validation : 자바 빈 검증기(하이버네이트 Validator) 
- spring-boot-starter-batch : 스프링 배치를 위한 스타터



전체 목록: https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.build-systems.starters



**라이브러리 버전 변경** 

외부 라이브러리의 버전을 변경하고 싶을 때 다음과 같은 형식으로 편리하게 변경할 수 있다.

 ```groovy
 ext['tomcat.version'] = '10.1.4'
 ```

- 스프링 부트가 관리하는 외부 라이브러리의 버전을 변경하는 일은 거의 없긴 하다.