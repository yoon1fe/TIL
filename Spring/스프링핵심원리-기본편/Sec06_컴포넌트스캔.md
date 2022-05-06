## 컴포넌트 스캔



#### 컴포넌트 스캔과 의존관계 자동 주입

- 지금까지 스프링 빈을 등록할 때는 자바 코드에서 `@Bean` 이나 XML의 `<bean>` 등을 통해서 설정 정보에 직접 등록할 스프링 빈을 나열했다.
- 등록해야 할 스프링 빈이 많아질 경우, 일일이 등록하기도 귀찮고, 설정 정보도 커지고, 실수할 확률도 높아진다.
- 그래서 스프링은 설정 정보 없이도 자동으로 스프링 빈을 등록하는 **컴포넌트 스캔**이라는 기능을 제공한다.
- 또한, 의존 관계도 자동으로 주입하는 `Autowired` 기능도 제공한다.



```Java
package hello.core;

...
  
@Configuration
@ComponentScan( excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = Configuration.class))
public class AutoAppConfig {

}
```



- `@ComponentScan`
  - `excludeFilters` - 뺄 애들 지정. 여기선 `@Configuration` 어노테이션이 붙은 애는 스캔 안해겠다~
  - 컴포넌트 스캔은 이름 그대로 `@Component` 어노테이션이 붙은 클래스를 스캔해서 스프링 빈으로 등록한다. 따라서 빈으로 등록할 클래스에 `@Component` 를 붙여주자.
  - 이때, 의존 관계 주입을 클래스 안에서 해결해주어야 한다.



> 참고: 컴포넌트 스캔을 사용하면 `@Configuration` 붙은 설정 정보도 자동으로 등록되기 때문에, `AppConfig`, `TestConfig` 와 같은 설정 정보도 함께 등록된다. 그래서 `excludeFilters` 를 이용해 컴포넌트 스캔 대상에서 제외해준다. 다만 보통 실무에서는 설정 정보를 컴포넌트 스캔 대상에서 제외하진 않는다.



**자동 의존관계 주입**

- `@Autowired` - 타입에 맞는 빈을 스프링이 자동으로 주입해준다.



```java
package hello.core.member;

...
  
@Component
public class MemberServiceImpl implements MemberService {

  private final MemberRepository memberRepository;

  @Autowired	// ac.geetBean(MemberRepository.class) 처럼 동작
  public MemberServiceImpl(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }
  ...
}
```



**@ComponentScan 의 동작**

- `@Component` 가 붙은 모든 클래스를 스프링 빈으로 등록한다.
- 이때, 스프링 빈의 기본 이름은 클래스명을 사용하되 맨 앞글자만 소문자로 바꾸어서 등록한다.
  - `MemberSeviceImpl` -> `memberServiceImpl`
  - 빈 이름을 직접 지정하고 싶으면 `@Component("memberService")`



**@Autowired 의존관계 자동 주입**

- 생성자에 `@Autowired` 를 지정하면 스프링 컨테이너가 자동으로 해당 스프링 빈을 찾아서 주입한다.
- 이때 기본 조회 전략은 타입이 같은 빈을 찾아서 주입해준다.
  - `getBean(MemberRepository.class)` 와 동일하다고 이해하면 된다.



#### 탐색 위치와 기본 스캔 대상

- 탐색할 패키지의 시작 위치를 지정할 수 있다.

  - 모든 자바 클래스를 다 컴포넌트 스캔하면 시간이 오래 걸린다. 그래서 꼭 필요한 위치부터 탐색하도록 시작 위치를 지정할 수 있다.

    ``` java
    @ComponentScan(
      basePackages = "hello.core",
      ...
    )
    ```

    - `basePackages`: 탐색할 패키지의 시작 위치를 지정한다. 이 패키지 포함해서 하위 패키지 모두 탐색한다.
    - 다음과 같이 시작 위치를 여러 개 지정할  수 있다. `basePackages = {"hello.core", "hello.service"}` 
    - `basePackageClasses`: 지정한 클래스의 패키지를 탐색 시작 위치로 지정한다.
    - 디폴트는  `@ComponentScan` 이 붙은 설정 정보 클래스의 패키지가 시작 위치가 된다.

- 권장하는 방법은 패키지 위치를 지정하지 않고, 설정 정보 클래스의 위치를 프로젝트 최상단에 두는 것이다. 최근 스프링 부트도 이 방법을 기본으로 제공한다. `SpringBootApplication` 어노테이션 안에 `@ComponentScan` 이 붙어있다.



#### 컴포넌트 스캔 기본 대상

- `Component`
- `Controller`: 스프링 MVC 컨트롤러에서 사용
- `Service`: 스프링 비즈니스 로직에서 사용. 특별한 처리는 하지 않고, 개발자로 하여금 비즈니스 로직이 여기 있구나라고 인식하는데 도움이 된다.
- `Repository`: 스프링 데이터 접근 계층에서 사용. 데이터 계층의 예외를 스프링 예외로 변환해준다.
- `Configuration`: 스프링 설정 정보에서 사용. 스프링이 설정 정보로 인식하고, 스프링 빈이 싱글톤으로 유지되도록 처리해준다.



위 어노테이션 모두 내부적으로 `@Component` 어노테이션을 포함하고 있다.



> 참고: 어노테이션에는 상속 관계가 없다. 어노테이션이 특정 어노테이션을 들고 있는 것을 인식하는 것은 자바 언어가 지원하는 기능이 아니라 스프링이 지원하는 기능이다.



#### 필터

- `includeFilters`: 컴포넌트 스캔 대상을 추가로 지정한다.
- `excludeFilters`: 컴포넌트 스캔에서 제외할 대상을 지정한다.



**FilterType 옵션**

5가지 옵션이 있다.

- ANNOTATION: 기본값, 어노테이션을 인식해서 동작
- ASSIGNABLE_TYPE: 지정한 타입과 자식 타입을 인식해서 동작
- ASPECTJ: AspectJ 패턴 사용
- REGEX: 정규 표현식
- CUSTOM: `TypeFilter`라는 인터페이스를 구현해서 처리



#### 중복 등록과 충돌

컴포넌트 스캔에서 같은 빈 이름을 등록하면 어떻게 될까?

1. 자동 빈 등록 vs 자동 빈 등록
2. 수동 빈 등록 vs 자동 빈 등록



**자동 빈 등록 vs 자동 빈 등록**

- 컴포넌트 스캔에 의해 자동으로 스프링 빈이 등록되는데, 이름이 같은 경우 스프링은 예외를 발생시킨다.
  - `ConflictBeanDefinitionException` 예외 발생



**수동 빈 등록 vs 자동 빈 등록**

- 수동 빈 등록이 우선권을 가진다. (수동 빈이 자동 빈을 오버라이딩한다.)
- 최근 스프링 부트에서는 수동 빈 등록과 자동 빈 등록이 충돌나면 오류가 발생하도록 기본 값을 바꾸었다.