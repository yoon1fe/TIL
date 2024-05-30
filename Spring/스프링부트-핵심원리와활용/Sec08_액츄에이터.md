## 프로덕션 준비 기능

- 운영환경에서 모니터링은 매우 중요!

- 비즈니스적인 요구사항만 개발하는 것이 아니라, 서비스에 문제가 없는지 모니터링하고 지표들을 심어서 감시하는 등의 업무가 필요함. 이러한 기능을 프로덕션 준비 기능이라 한다. 즉, 프로덕션을 운영에 배포할 때 준비해야 하는 비기능적 요소를 뜻함.
  - 지표(metric), 추적(trace), 감사(auditing)
  - 모니터링

- 애플리케이션이 살아있는지, 로그 정보는 정상 설정되어있는지, 커넥션 풀은 얼마나 사용되고 있는지 등..
- 스프링 부트가 제공하는 액츄에이터(actuator, 시스템을 움직이거나 제어하는 데 쓰이는 기계 장치)는 이런 프로덕션 준비 기능을 매우 편리하게 사용할 수 있는 편의 기능들을 제공한다. + 마이크로미터, 프로메테우스, 그라파나 같은 최근 유행하는 모니터링 시스템과 매우 쉽게 연동 가능



## 액츄에이터 시작

- 스프링 부트 액츄에이터 라이브러리 추가

  ``` groovy
  implementation 'org.springframework.boot:spring-boot-starter-actuator' // actuator 추가
  ```

- 액츄에이터는 `/actuator` 경로를 통해 기능 제공한다. 

  `http://localhost:8080/actuator` 

  ``` json
  {
    "_links": {
      "self": {
        "href": "http://localhost:8080/actuator",
        "templated": false
      },
      "health": {
        "href": "http://localhost:8080/actuator/health",
        "templated": false
      },
      "health-path": {
        "href": "http://localhost:8080/actuator/health/{*path}",
        "templated": true
      }
    }
  }
  ```

  - 액추에이터가 제공해주는 기능들

- `/health`: 현재 서버가 잘 동작하고 있는지 애플리케이션의 헬스 상태

- 여러 기능들을 웹 환경에 노출시키기 위해서는 설정 추가해야 함.

  ``` yaml
  management:
    endpoints:
      web:
        exposure:
          include: "*""
  ```

  - 모든 기능을 웹 환경에 노출. 기능 엄청 많다.

- 각각 엔드포인트는 `/actuator/{end-point}` 형식으로 접근 가능.



## 엔드포인트 설정

엔드포인트 사용하려면 활성화 + 노출이 필요함

- 활성화: 기능 자체를 사용할지 말지
- 노출: 활성화된 엔드포인트를 HTTP에 노출할지 JMX에 노출할지

- 엔드포인트는 대부분 기본으로 활성화되어 있다. (`shutdown` 제외) 노출만 안되어 있는 것.
  - 활성화: `management.endpoint.shutdown.enabled=true`
  - `shutdown` 은 POST 요청보내야 함.



## 다양한 엔드포인트

- beans : 스프링 컨테이너에 등록된 스프링 빈을 보여준다. 
- conditions : condition 을 통해서 빈을 등록할 때 평가 조건과 일치하거나 일치하지 않는 이유를 표시한다. 
- configprops : @ConfigurationProperties 를 보여준다. 
- env : Environment 정보를 보여준다. 
- health : 애플리케이션 헬스 정보를 보여준다. 
- httpexchanges : HTTP 호출 응답 정보를 보여준다. HttpExchangeRepository 를 구현한 빈을 별 도로 등록해야 한다. 
- info : 애플리케이션 정보를 보여준다. 
- loggers : 애플리케이션 로거 설정을 보여주고 변경도 할 수 있다. 
- metrics : 애플리케이션의 메트릭 정보를 보여준다. 
- mappings : @RequestMapping 정보를 보여준다. 
- threaddump : 쓰레드 덤프를 실행해서 보여준다. 
- shutdown : 애플리케이션을 종료한다. 이 기능은 기본으로 비활성화 되어 있다.



## `health`

**기본 동작**

``` json
{
  "status": "UP"
}
```



헬스 정보는 단순히 애플리케이션이 요청에 응답할 수 있는지 판단하는 것을 넘어서, **애플리케이션이 사용하는 데이터베이스가 응답하는지, 디스크 사용량에는 문제 없는지**와 같은 다양한 정보들을 포함해서 만들어진다.



헬스 정보를 더 자세히 보려면 다음 옵션 지정

```yaml
management:
  endpoint:
  	health:
    	show-details: always
```

- 결과

  ``` json
  {
    "status": "UP",
    "components": {
      "db": {
        "status": "UP",
        "details": {
          "database": "H2",
          "validationQuery": "isValid()"
        }
      },
      "diskSpace": {
        "status": "UP",
        "details": {
          "total": 494384795648,
          "free": 361082380288,
          "threshold": 10485760,
          "path": "/~~/actuator/.",
          "exists": true
        }
      },
      "ping": {
        "status": "UP"
      }
    }
  }
  ```

- 액츄에이터는 db , mongo , redis , diskspace , ping 과 같은 수많은 헬스 기능을 기본으로 제공한다.



## 애플리케이션 정보: `info`

애플리케이션의 기본 정보 노출

- java : 자바 런타임 정보 
- os : OS 정보 
- env : Environment 에서 info. 로 시작하는 정보 
- build : 빌드 정보, META-INF/build-info.properties 파일이 필요하다. 
- git : git 정보, 어느 브랜치인지 등등.. git.properties 파일이 필요하다.

env , java , os 는 기본으로 비활성화 되어 있음.



``` yaml
management:
  info:
    java:
      enabled: true
    os:
      enabled: true
```

- endpoint 하위 아님;



**build**

- build.gradle에 `springBoot { buildInfo() }` 붙여주면 만들어준다.



**git**

- git.properties 생성

``` groovy
plugins {
 ...
 id "com.gorylenko.gradle-git-properties" version "2.4.1" //git info
}
```



## `loggers`

로깅과 관련된 정보 확인 + 실시간으로 변경 가능.



``` yaml
logging:
 level:
 hello.controller: debug
```

- debug 레벨부터 로그 찍힘.



``` json
{
  "levels": [
    "OFF",
    "ERROR",
    "WARN",
    "INFO",
    "DEBUG",
    "TRACE"
  ],
  "loggers": {
    "ROOT": {
      "configuredLevel": "INFO",
      "effectiveLevel": "INFO"
    },
    "_org.springframework": {
      "effectiveLevel": "INFO"
    },
    "hello": {
      "effectiveLevel": "INFO"
    },
    "hello.ActuatorApplication": {
      "effectiveLevel": "INFO"
    },
    "hello.controller": {
      "configuredLevel": "DEBUG",
      "effectiveLevel": "DEBUG"
    },
    "hello.controller.LogController": {
      "effectiveLevel": "DEBUG"
    }
  }
}
```

- 로그를 별도로 설정하지 않으면 스프링 부트는 기본으로 `INFO` 사용



**실시간 로그 레벨 변경**

- 보통 운영서버는 중요하다고 판단되는 `INFO` 로그 레벨 사용
- `loggers` 엔드포인트를 사용하면 애플리케이션을 다시 시작하지 않고 실시간으로 로그 레벨 변경 가능. `POST` 요청!



```
POST /actuator/loggers/hello.controller
Content-Type: application/json

{
	"configuredLevel" : "TRACE"
}
```



## HTTP 요청 응답 기록: `httpexchanges`

- HTTP 요청과 응답의 과거 기록 확인 가능
- `HttpExchangeRepository` 인터페이스의 구현체를 빈으로 등록해야 함
- 스프링 부트는 기본으로 `InMemoryHttpExchangeRepository` 구현체를 제공한다.
  - 얘는 최대 과거 100개 저장

- 이 기능은 매우 단순하기 때문에 개발 환경에서 사용하는걸 추천.



## 액츄에이터와 보안

**보안 주의**

- 액츄에이터가 제공하는 기능들은 애플리케이션의 내부 정보를 너무 많이 노출하므로 내부망에서만 접근할 수 있도록 하는 것이 좋다.
- 액츄에이터 포트 설정: `management.server.port=9292`



**액츄에이터 URL 경로에 인증 설정**

- 포트 분리나 내부망에서만 사용하는 것이 어렵다면, `/actuator` 경로에 서블릿 필터나 스프링 시큐리티를 통해 인증된 사용자만 접근 가능하도록 추가 개발 필요함.



**엔드포인트 경로 변경**

``` yaml
management:
	endpoints:
		web:
			base-path: "/manage"
```

- `/actuator/**` -> `/manage/**`