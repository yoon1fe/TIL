## 예제

```java
@Slf4j
@Configuration
public class DbConfig {

  @Bean
  public DataSource dataSource() {
    log.info("dataSource 빈 등록");
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setDriverClassName("org.h2.Driver");
    dataSource.setJdbcUrl("jdbc:h2:mem:test");
    dataSource.setUsername("sa");
    dataSource.setPassword("");

    return dataSource;
  }

  @Bean
  public TransactionManager transactionManager() {
    log.info("transactionManager 빈 등록");
    return new JdbcTransactionManager(dataSource());
  }

  @Bean
  public JdbcTemplate jdbcTemplate() {
    log.info("jdbTemplate 빈 등록");
    return new JdbcTemplate(dataSource());
  }
}
```

- `JdbcTemplate` 사용해서 회원 데이터를 DB 에 보관하고 관리하는 기능
- `DataSource`, `TransactionManager`, `JdbcTemplate` 을 스프링 빈으로 직접 등록한다.



```java
@Repository
@RequiredArgsConstructor
public class MemberRepository {

  public final JdbcTemplate template;

  public void initTable() {
    template.execute("create table member(member_id varchar primary key, name varchar)");
  }

  public void save(Member member) {
    template.update("insert into member(member_id, name) values(?,?)",
        member.getMemberId(),
        member.getName());
  }

  public Member find(String memberId) {
    return template.queryForObject("select member_id, name from member where member_id=?",
        BeanPropertyRowMapper.newInstance(Member.class),
        memberId);
  }

  public List<Member> findAll() {
    return template.query("select member_id, name from member",
        BeanPropertyRowMapper.newInstance(Member.class));
  }
}
```



```java
@SpringBootTest
class MemberRepositoryTest {

  @Autowired
  MemberRepository memberRepository;

  @Transactional
  @Test
  void memberTest() {
    Member member = new Member("idA", "memberA");

    memberRepository.initTable();
    memberRepository.save(member);
    Member findMember = memberRepository.find(member.getMemberId());

    assertThat(findMember.getMemberId()).isEqualTo(member.getMemberId());
    assertThat(findMember.getName()).isEqualTo(member.getName());
  }

}
```

- @Transactional 사용해서 트랜잭션 기능 적용
  - 이 애너테이션 사용하려면 `TransactionManager`가 스프링 빈으로 등록되어 있어야 한다.



DB에 데이터를 보관하고 관리하기 위해 `JdbcTemplate`, `DataSource`, `TransactionManager` 와 같은 객체들을 항상 스프링 빈으로 등록해야 하는 번거로움이 있다. 스프링 부트에서는 이런 번거로움을 없애기 위해 자동 구성이란 기능이 있다!!







## 자동 구성 확인

`DbConfig` 를 설정하지 않아도 `DataSource`, `JdbcTemplate`, `TransactionManager` 객체가 스프링 빈으로 등록되어 있는 것을 알 수 있다!



## 스프링 부트의 자동 구성

스프링 부트는 일반적으로 자주 사용하는 빈들을 자동으로 등록해주는 자동 구성(Auto Configuration) 기능을 제공한다.



**자동 구성 살짝 알아보기**

스프링 부트는 `spring-boot-autoconfigure` 라는 프로젝트 안에서 많은 자동 구성을 제공한다.

`spring-boot-starter-*` > `spring-boot-starter` > `spring-boot-autoconfigure`



**ex. JdbcTemplate 자동 구성 - JdbcTemplateAutoConfiguration**

```java
package org.springframework.boot.autoconfigure.jdbc;

@AutoConfiguration(
  after = {DataSourceAutoConfiguration.class}
)
@ConditionalOnClass({DataSource.class, JdbcTemplate.class})
@ConditionalOnSingleCandidate(DataSource.class)
@EnableConfigurationProperties({JdbcProperties.class})
@Import({DatabaseInitializationDependencyConfigurer.class, JdbcTemplateConfiguration.class, NamedParameterJdbcTemplateConfiguration.class})
public class JdbcTemplateAutoConfiguration {
  public JdbcTemplateAutoConfiguration() {
  }
}
```

- `@AutoConfiguration`: 자동 구성을 사용하려면 이 애너테이션을 등록해야 함.
  - 이 애너테이션 내부에도 `@Configuration` 이 있어서 빈을 등록하는 자바 설정 파일로 사용 가능.
- `@ConditionalOnClass({ DataSource.class, JdbcTemplate.class })`
  - if 문과 유사. 해당 클래스가 있는 경우에만 설정이 동작.



```java
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.boot.autoconfigure.jdbc;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration(
  proxyBeanMethods = false
)
@ConditionalOnSingleCandidate(JdbcTemplate.class)
@ConditionalOnMissingBean({NamedParameterJdbcOperations.class})
class NamedParameterJdbcTemplateConfiguration {
  NamedParameterJdbcTemplateConfiguration() {
  }

  @Bean
  @Primary
  NamedParameterJdbcTemplate namedParameterJdbcTemplate(JdbcTemplate jdbcTemplate) {
    return new NamedParameterJdbcTemplate(jdbcTemplate);
  }
}
```

- `@ConditionalOnMissingBean(JdbcOperations.class)`
  - `JdbcOperations`(JdbcTemplate 부모 인터페이스) 빈이 없을 때 동작하도록 설정
  - 이런 기능이 없으면 개발자가 직접 등록한 빈과 자동 구성이 등록하는 빈이 중복 등록되는 문제가 발생할 수 있다.



**Auto Configuration - 자동 설정 ? 자동 구성 ?**

둘 다 맞다고 본다.



## 자동 구성 직접 만들기

실시간으로 자바 메모리 사용량을 웹으로 확인하는 예제



```java
package memory;

import lombok.Data;

public class Memory {

  private long used;
  private long max;

  public Memory(long used, long max) {
    this.used = used;
    this.max = max;
  }

  public long getUsed() {
    return used;
  }

  public long getMax() {
    return max;
  }

  @Override
  public String toString() {
    return "Memory{" +
        "used=" + used +
        ", max=" + max +
        '}';
  }
}
```



```java
package memory;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemoryFinder {

  public Memory get() {
    long max = Runtime.getRuntime().maxMemory();
    long total = Runtime.getRuntime().totalMemory();
    long free = Runtime.getRuntime().freeMemory();
    long used = total - free;

    return new Memory(used, max);
  }

  @PostConstruct
  public void init() {
    log.info("init memoryFinder");
  }

}
```



```java
package memory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MemoryController {

  private final MemoryFinder memoryFinder;

  @GetMapping("/memory")
  public Memory system() {
    Memory memory = memoryFinder.get();
    log.info("memory = {}", memory);

    return memory;
  }

}
```



```java
package hello.config;

import memory.MemoryController;
import memory.MemoryFinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemoryConfig {

  @Bean
  public MemoryController memoryController() {
    return new MemoryController(memoryFinder());
  }

  @Bean
  public MemoryFinder memoryFinder() {
    return new MemoryFinder();
  }

}
```



## @Conditional

위에서 만든 메모리 조회 기능을 특정 조건일 때만 활성화되도록 해보자.



자바 시스템 속성에 `-Dmemory=on` 인 경우에만 위 자동 설정이 동작하도록 해보자.

조건을 만드려면 스프링에서 제공하는 `Condition` 인터페이스를 구현한 구현체가 있어야 함.



```java
package memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Slf4j
public class MemoryCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    // -Dmemory=on
    String memory = context.getEnvironment().getProperty("memory");
    log.info("memory = {}", memory);
    
    return "on".equals(memory);
  }
}
```



```java
package hello.config;

import memory.MemoryCondition;
import memory.MemoryController;
import memory.MemoryFinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(MemoryCondition.class)
public class MemoryConfig {

  @Bean
  public MemoryController memoryController() {
    return new MemoryController(memoryFinder());
  }

  @Bean
  public MemoryFinder memoryFinder() {
    return new MemoryFinder();
  }

}
```

- @Conditional 에 설정된 MemoryCondition.matches 가 true 를 반환하면 해당 빈들을 등록한다!!



### @Conditional의 다양한 기능

스프링은 이미 필요한 대부분의 구현체를 만들어두었다.

`@Conditional(MemoryCondition.class)` == `@ConditionalOnProperty(name = "memory", havingValue = "on")`



**@ConditionalOnXxx**

- @ConditionalOnClass, @ConditionalOnMissingClass
  - 클래스가 있는/없는 경우 동작
- @ConditionalOnBean, @ConditionalOnMissingBean
  - 빈이 등록되어 있는/없는 경우 동작
- @ConditionalOnProperty
  - 환경 정보가 있는 경우 동작
- @ConditionalOnResource
  - 리소스가 있는 경우 동작
- @ConditionalOnWebApplication, @ConditionalOnNotWebApplication
  - 웹 애플리케이션인/아닌 경우 동작
- @ConditionalOnExpression
  - SpEL 표현식에 만족하는 경우 동작

- https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-auto-configuration.condition-annotations



위 애너테이션은 주로 스프링 자동 구성에 사용된다.

참고로 @Conditional 애너테이션은 스프링 프레임워크, 이를 확장한 @ConditionalOnXxx 는 부트가 제공.



## 순수 라이브러리

### 만들기

순수 라이브러리 jar 만들어보자.



**빌드**

- `./gradlew clean build`
- 압출 풀기: `jar -xvf memory-v1.jar`



### 사용하기

``` groovy
dependencies {
  implementation files('libs/memory-v1.jar')
}
```

- 라이브러리를 jar 파일로 직접 가지고 있으면 files 로 지정.



**라이브러리 설정**

``` java
@Configuration
public class MemoryConfig {

 @Bean
 public MemoryFinder memoryFinder() {
 return new MemoryFinder();
 }

 @Bean
 public MemoryController memoryController() {
 return new MemoryController(memoryFinder());
 }
}
```

- 스프링 부트 자동 구성을 사용하는 것이 아니기 때문에 빈을 직접 등록해주어야 함.

- 외부 라이브러리를 사용하는 클라이언트 개발자 입장에서 생각해보면 라이브러리 내부에 있는 어떤 빈을 등록해야 하는지 알아야 하고, 그걸 다 일일히 등록해야 한다...
- 이런 부분을 자동으로 처리해주는 것이 **스프링 부트 자동 구성**!



## 자동 구성 라이브러리

### 만들기

```java
package memory;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(name = "memory", havingValue = "on")
public class MemoryAutoConfig {

  @Bean
  public MemoryController memoryController() {
    return new MemoryController(memoryFinder());
  }

  @Bean
  public MemoryFinder memoryFinder() {
    return new MemoryFinder();
  }

}
```

- `@AutoConfiguration`: 스프링 부트가 제공하는 자동 구성 기능 적용할 때 사용하는 애너테이션
- `@ConditionalOnProperty`
  - `memory=on` 이라는 환경 정보가 있을 때 라이브러리 적용(== 스프링 빈 등록)
  - 라이브러리를 갖고 있더라도 상황에 따라서 해당 기능을 끄고 켤 수 있도록 유연한 기능 제공



**자동 구성 대상 지정**

- 스프링 부트 자동 구성을 적용하려면 다음 파일에 자동 구성 대상을 지정해주어야 한다.
  - `src/main/resources/META-INF/spring/org.springframework.book.autoconfigure.AutoConfiguration.imports`
  - 앞서 만든 자동 구성인 memory.MemoryAutoConfig 를 패키지를 포함해서 지정해준다
- 스프링 부트는 위 위치에 있는 파일을 읽어서 자동 구성으로 사용. 따라서 내부에 있는 `MemoryAutoConfig` 가 자동으로 실행된다.



### 사용하기

``` groovy
dependencies {
  implementation files('libs/memory-v2.jar')
}
```

- project-v2 에서 사용하는 memory-v2 라이브러리에는 스프링 부트 자동 구성이 적용되어 있으므로 별도의 빈 등록 설정을 하지 않아도 된다!



## 자동 구성 이해

### 스프링 부트의 동작

`@SpringBootApplication` > `@EnableAutoConfiguration`



```java
package org.springframework.boot.autoconfigure;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import({AutoConfigurationImportSelector.class})
public @interface EnableAutoConfiguration {
  String ENABLED_OVERRIDE_PROPERTY = "spring.boot.enableautoconfiguration";

  Class<?>[] exclude() default {};

  String[] excludeName() default {};
}
```

- 이름 그대로 자동 구성을 활성화하는 애너테이션
- `@Import({AutoConfigurationImportSelector.class})`
  - `@Import` 는 주로 스프링 설정 정보를 포함할 때 사용..
  - 근데 `AutoConfigurationImportSelector`는 설정 파일(`@Configuration`) 이 아니다.



### ImportSelector

`@Import` 에 설정 정보를 추가하는 방법은 두 가지

- 정적인 방법: `@Import(클래스)` . 코드에 대상이 딱 박혀 있음.

- 동적인 방법: `@Import( ImportSelector )`. ImportSelector라는 인터페이스의 구현체를 넣어서 사용할 설정 정보를 동적으로 선택.



```java
package hello.selector;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class HelloImportSelector implements ImportSelector {

  @Override
  public String[] selectImports(AnnotationMetadata importingClassMetadata) {
    return new String[]{"hello.selector.HelloConfig"};
  }
}
```

- 설정 정보를 동적으로 선택할 수 있게 하는 `ImportSelector` 구현체
- 단순히 `hello.selector.HelloConfig` 설정 정보를 반환

- SelectorConfig > @Import(HelloImportSelector.class) > HelloImportSelector > "hello.selector.HelloConfig"
  - 스프링은 이 문자에 맞는 대상을 설정 정보로 사용한다.



**@EnableAutoConfiguration 동작 방식**

- 임포트하고 있는 `AutoConfigurationImportSelector`

  ```java
  public class AutoConfigurationImportSelector implements DeferredImportSelector, BeanClassLoaderAware, ResourceLoaderAware, BeanFactoryAware, EnvironmentAware, Ordered {
    ...
  
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
      if (!this.isEnabled(annotationMetadata)) {
        return NO_IMPORTS;
      } else {
        AutoConfigurationEntry autoConfigurationEntry = this.getAutoConfigurationEntry(annotationMetadata);
        return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());
      }
    }
  }
  ```



**남은 문제**

- 빈 등록할 때 사용하는 설정 정보는 어떻게 변경해야 할까?? DB 접속 url, id, pw 등등..
