## 빈 후처리기

`@Bean` 이나 컴포넌트 스캔으로 스프링 빈을 등록하면 스프링은 대상 객체를 생성하고 스프링 컨테이너 내부의 빈 저장소에 등록한다.



**빈 후처리기 - BeanPostProcessor**

빈 후처리기는 스프링이 빈을 생성한 이후 무언가를 처리하는 용도로 사용한다. 객체를 조작할 수도 있고, 다른 객체로 바꿔치기도 가능



**빈 등록 과정 with 빈 후처리기**

1. 생성: 스프링 빈 대상이 되는 객체를 생성
2. 전달: 생성된 객체를 빈 저장소에 등록하기 전에 빈 후처리기에 전달
3. 후 처리 작업: 전달된 빈 객체를 조작하거나 다른 객체로 바꿔치기
4. 등록: 빈 후처리기가 조작한 스프링 빈 객체를 빈 저장소에 등록



**BeanPostProcessor 인터페이스 - 스프링 제공**

```java
public interface BeanPostProcessor {
  Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException
  Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException
}
```

- 빈 후처리기를 사용하려면 `BeanPostProcessor` 인터페이스를 구현하고 스프링 빈으로 등록하면 된다.
- `postProcessBefore/AfterInitialization()`: 객체 생성 이후 `@PostConstruct` 같은 초기화 발생하기 전/후 호출되는 포스트 프로세서







```java
public class BeanPostProcessorTest {

  @Test
  void postProcessor() {
    ApplicationContext applicationContext = new AnnotationConfigApplicationContext(BeanPostProcessorConfig.class);

    //beanA 이름으로 B 객체가 빈으로 등록된다.
    B b = applicationContext.getBean("beanA", B.class);
    b.helloB();

    //A는 빈으로 등록되지 않는다.
    Assertions.assertThrows(NoSuchBeanDefinitionException.class,
        () -> applicationContext.getBean(A.class));
  }

  @Slf4j
  @Configuration
  static class BeanPostProcessorConfig {

    @Bean(name = "beanA")
    public A a() {
      return new A();
    }

    @Bean
    public AToBPostProcessor helloPostProcessor() {
      return new AToBPostProcessor();
    }

  }

  @Slf4j
  static class A {
    public void helloA() {
      log.info("hello A");
    }
  }

  @Slf4j
  static class B {
    public void helloB() {
      log.info("hello B");
    }
  }

  @Slf4j
  static class AToBPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
      log.info("beanName={} bean={}", beanName, bean);
      if (bean instanceof A) {
        return new B();
      }
      return bean;
    }
  }

}
```



**`AToBPostProcessor`**

- A를 B로 바꿔치기하는 빈 후처리기.



빈 후처리기는 빈을 조작하고 변경할 수 있는 후킹 포인트이다. -> **빈 객체를 프록시로 교체**하는 것도 가능!

설정 파일에 있는 수 많은 프록시 생성 코드도 모두 제거 가능!!



**`PackageLogTraceProxyPostProcessor.java`**

```java
package hello.proxy.config.v4_postprocessor.postprocessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

@Slf4j
@RequiredArgsConstructor
public class PackageLogTraceProxyPostProcessor implements BeanPostProcessor {

  private final String basePackage;
  private final Advisor advisor;

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    log.info("param beanName={} bean={}", beanName, bean.getClass());

    //프록시 적용 대상 여부 체크
    //프록시 적용 대상이 아니면 원본을 그대로 반환
    String packageName = bean.getClass().getPackageName();
    if (!packageName.startsWith(basePackage)) {
      return bean;
    }

    //프록시 대상이면 프록시를 만들어서 반환
    ProxyFactory proxyFactory = new ProxyFactory(bean);
    proxyFactory.addAdvisor(advisor);
    Object proxy = proxyFactory.getProxy();

    log.info("create proxy: target={} proxy={}", bean.getClass(), proxy.getClass());
    return proxy;
  }
}
```

- 원본 객체를 프록시 객체로 변환해주는 빈 후처리기. 프록시 팩토리를 사용하는데 `advisor`가 필요하기 때문에 외부에서 주입받는다.
- `basePackage`: 특정 패키지의 객체에만 적용
- 스프링 부트가 기본으로 등록하는 빈들이 굉장히 많다. 이 중에는 프록시 객체를 만들 수 없는 빈들도 있어서 모든 객체를 프록시로 만들 경우 오류가 발생한다.



**스프링은 프록시를 생성하기 위한 빈 후처리기를 이미 만들어서 제공하고 있다!**

스프링 AOP는 포인트컷을 사용해서 프록시 적용 대상 여부를 체크한다.



포인트컷은 다음 두 곳에 사용됨

1. 프록시 적용 대상 여부 체크 (빈 후처리기 - 자동 프록시 생성)
2. 프록시의 어떤 메서드가 호출되었을 때 어드바이스를 적용할지 판단 (프록시 내부)



## 스프링이 제공하는 빈 후처리기









### 하나의 프록시에 여러 Advisor 적용