### 스프링 컨테이너 생성

`ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);`



- `Application Context`를 스프링 컨테이너라고도 한다.
- `Application Context`는 인터페이스이다. 다형성!
- 스프링 컨테이너는 XML이나 어노테이션 기반의 자바 설정 클래스로 만들 수 있다. 여기서는 구현체 이름 그대로 어노테이션 설정



> 참고: 더 정확히는 스프링 컨테이너를 부를때 `BeanFactory`, `Application Context` 로 구분해서 이야기한다.

 

* 주의: **빈 이름은 항상 다른 이름을 부여**해야 한다!!
* 스프링은 빈을 생성된 이후에 의존관계를 주입한다. 그런데 자바 코드로 스프링 빈을 등록하면 생성자를 호출하면서 의존관계 주입도 한 번에 처리된다.



## 스프링 빈 조회 

스프링 컨테이너에서 스프링 빈을 찾는 가장 기본적인 방법

- `applicationContext.getBean(빈 이름, 타입);`
- `applicationContext.getBean(타입);`
- 조회 대상 스프링 빈이 없으면 예외가 발생한다.
  - `NoSuchBeanDefinitionException`



##### 동일한 타입이 둘 이상일 경우

타입으로 조회할 시 같은 타입의 스프링 빈이 두 개 이상 있다면 오류가 발생한다. 이 때는 빈 이름을 지정하자.

- 오류
  - `org.springframework.beans.factory.NoUniqueBeanDefinitionException: No qualifying bean of type 'hello.core.member.MemberRepository' available: expected single matching bean but found 2: memberRepository1,memberRepository2`

`ac.getBeanOfType()` 을 사용하면 해당 타입의 모든 빈을 조회할 수 있다.



``` java
    Map<String, MemberRepository> beansOfType = ac.getBeansOfType(MemberRepository.class);
```



##### 상속 관계

- **부모 타입으로 조회하면, 자식 타입도 함께 조회된다!!**
- 그래서 모든 자바 객체의 최고 부모인 `Object` 타입으로 조회하면, 모든 스프링 빈이 조회된다.



사실 `getBean()` 을 직접 사용할 일은 없다~!



## BeanFactory와 ApplicationContext

![BeanFactory와 ApplicationContext - dodeon](https://3513843782-files.gitbook.io/~/files/v0/b/gitbook-legacy-files/o/assets%2F-LxjHkZu4T9MzJ5fEMNe%2Fsync%2F21012b333f698d2d366ad35304db7e559cd641d9.png?generation=1618052456312074&alt=media)



##### BeanFactory

- 스프링 컨테이너의 최상위 인터페이스이다.
- 스프링 빈을 관리하고 조회하는 역할을 담당한다.
- `getBean()` 메서드를 제공한다.
- 지금까지 우리가 사용했던 대부분의 기능은 BeanFactory가 제공하는 기능이다.



##### ApplicationContext

- BeanFactory 기능을 모두 상속받아서 제공한다.
- 애플리케이션을 개발할 때는 빈은 관리하고 조회하는 기능은 물론이고, 수많은 부가 기능이 필요하다.
- BeanFactory를 직접 사용할 일은 거의 없고, ApplicationContext가 제공해주는 부가 기능을 많이 사용한다.





![Image](https://lh4.googleusercontent.com/L503ZbZNJl6u1HFDyJf-ZQtthPpNOfvm97eg7_ufQHOrwgPqkbnADOqJKv3rx2IFE06XI3fLFZH1I-6_rauWmmO9YNfG8GbgnghTIyQ_EnnXMyxJdbOulDJRUbBbMxGga0Dav8c2=w1200-h630-p-k-no-nu)

**ApplicationContext가 제공하는 부가 기능**

- 메시지 소스를 활용한 국제화 기능
- 환경 변수
- 애플리케이션 이벤트
- 편리한 리소스 조회



## 스프링 빈 설정 메타 정보 - BeanDefinition

- 스프링은 `BeanDefinition` 추상화를 통해 다양한 설정 형식을 지원한다.
- **역할과 구현을 개념적으로 나눈 것!**
  - XML 파일이나 자바 코드를 읽어 들여서 `BeanDefinition` 을 만들면 된다.
  -  스프링 컨테이너는 `BeanDefinition`만 알면 설정 파일이 자바 코드인지 XML인지 몰라도 된다.
- `BeanDefinition`을 빈 설정 메타 정보라고 한다.
  - `@Bean`, `<bean>` 당 하나씩 메타 정보가 생성된다.
- 스프링 컨테이너는 이 메타 정보를 기반으로 스프링 빈을 생성한다.