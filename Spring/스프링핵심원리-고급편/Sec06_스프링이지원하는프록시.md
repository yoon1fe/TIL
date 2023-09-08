## 프록시 팩토리

인터페이스가 있으면 JDK 동적 프록시, 그렇지 않은 경우 CGLIB 적용 어떻게 해야할까?

스프링은 유사한 구체적인 기술들이 있을 때 그것들을 통합해서 일관성 있게 접근할 수 있도록 추상화된 기술을 제공한다.

스프링은 동적 프록시를 통합해서 편리하게 만들어주는 **프록시 팩토리**라는 기능을 제공. 내부에서 JDK 동적 프록시, CGLIB를 사용한다.



1. 클라이언트가 프록시 요청
2. ProxyFactory가 프록시 기술 선택
3. 프록시 생성(JDK 동적 프록시 or CGLIB)



JDK 동적 프록시의 InvocationHandler와 CGLIB가 제공하는 MethodInterceptor를 중복으로 따로 만들어야 하나??

-> 스프링은 이 문제를 해결하기 위해 부가 기능을 적용할 때 `Advice`라는 개념을 도입. 프록시 팩토리가 알아서 advice를 호출해준다.



특정 조건에만 로직을 적용하려면 ? -> `Pointcut`



### 예제

**Advice 만들기**

`MethodInterceptor` 인터페이스 구현하면 됨.



```java
package org.aopalliance.intercept;

public interface MethodInterceptor extends Interceptor {
  Object invoke(MethodInvocation var1) throws Throwable;
}
```

- `MethodInvocation invocation`: 다음 메서드 호출하는 방법, 현재 프록시 객체 인스턴스 등 파라미터로 제공되는 부분들 다 여기 있다.



**TimeAdvice.java**

```java
package hello.proxy.common.advice;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

@Slf4j
public class TimeAdvice implements MethodInterceptor {

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    log.info("TimeProxy 실행");
    long startTime = System.currentTimeMillis();

    Object result = invocation.proceed();

    long endTime = System.currentTimeMillis();
    long resultTime = endTime - startTime;
    log.info("TimeProxy 종료 resultTime = {}", resultTime);

    return resultTime;
  }
}
```

- `invocation.proceed()`: target 클래스를 호출하고 그 결과를 받음. `target` 클래스의 정보는 `MethodInvocation` 에 다 들어있다. 프록시 팩토리로 프록시를 생성하는 단계에서 `target`의 정보를 파라미터로 전달함 



```java
@Test
@DisplayName("인터페이스가 있으면 JDK 동적 프록시 사용")
void interfaceProxy() {
  ServiceInterface target = new ServiceImpl();

  ProxyFactory proxyFactory = new ProxyFactory(target);
  proxyFactory.addAdvice(new TimeAdvice());

  ServiceInterface proxy = (ServiceInterface) proxyFactory.getProxy();
  log.info("targetClass = {}", target.getClass());
  log.info("proxyClass = {}", proxy.getClass());

  proxy.save();

  assertThat(AopUtils.isAopProxy(proxy)).isTrue();
  assertThat(AopUtils.isJdkDynamicProxy(proxy)).isTrue();
  assertThat(AopUtils.isCglibProxy(proxy)).isFalse();
}
```

- 프록시 팩토리 생성할 때 생성자에 프록시의 호출 대상(target)을 함께 넘겨준다.



**결과**

```
INFO hello.proxy.proxyfactory.ProxyFactoryTest - targetClass = class hello.proxy.common.service.ServiceImpl
INFO hello.proxy.proxyfactory.ProxyFactoryTest - proxyClass = class com.sun.proxy.$Proxy13
INFO hello.proxy.common.advice.TimeAdvice - TimeProxy 실행
INFO hello.proxy.common.service.ServiceImpl - save 호출
INFO hello.proxy.common.advice.TimeAdvice - TimeProxy 종료 resultTime = 1
```



**구체 클래스 프록시**

```java
@Test
@DisplayName("구체 클래스만 있으면 CGLIB 사용")
void concreteProxy() {
  ConcreteService target = new ConcreteService();
  ProxyFactory proxyFactory = new ProxyFactory(target);
  proxyFactory.addAdvice(new TimeAdvice());

  ConcreteService proxy = (ConcreteService) proxyFactory.getProxy();

  log.info("targetClass={}", target.getClass());
  log.info("proxyClass={}", proxy.getClass());

  proxy.call();
  assertThat(AopUtils.isAopProxy(proxy)).isTrue();
  assertThat(AopUtils.isJdkDynamicProxy(proxy)).isFalse();
  assertThat(AopUtils.isCglibProxy(proxy)).isTrue();
}
```



**proxyTargetClass 옵션**

```java
@Test
@DisplayName("ProxyTargetClass 옵션을 사용하면 인터페이스가 있어도 CGLIB를 사용하고, 클래스 기반 프록시 사용")
void proxyTargetClass() {
  ServiceInterface target = new ServiceImpl();
  ProxyFactory proxyFactory = new ProxyFactory(target);
  proxyFactory.setProxyTargetClass(true); //중요
  proxyFactory.addAdvice(new TimeAdvice());
  ServiceInterface proxy = (ServiceInterface) proxyFactory.getProxy();
  
  log.info("targetClass={}", target.getClass());
  log.info("proxyClass={}", proxy.getClass());
  
  proxy.save();
  
  assertThat(AopUtils.isAopProxy(proxy)).isTrue();
  assertThat(AopUtils.isJdkDynamicProxy(proxy)).isFalse();
  assertThat(AopUtils.isCglibProxy(proxy)).isTrue();
}
```

- 인터페이스가 있지만 CGLIB를 사용해서 클래스 기반으로 동적 프록시 만들 수 있다.
- `proxyTargetClass` 옵션에 true를 넣으면 인터페이스가 있어도 강제로 CGLIB를 사용한다.



## 포인트컷, 어드바이스, 어드바이저

- **포인트컷**: 어디에 부가 기능을 적용할지말지 판단하는 **필터링 로직.** 주로 클래스와 메서드 이름으로 필터링한다. 어떤 포인트에 기능을 적용할지 말지 잘라서 구분!
- **어드바이스:** 프록시가 호출하는 **부가 기능**.
- **어드바이저:** 하나의 포인트컷과 하나의 어드바이스를 가지고 있는 것. **포인트컷1 + 어드바이스1**



### 예제

어드바이저는 하나의 포인트컷와 하나의 어드바이스를 갖고 있다.

프록시 팩토리를 통해서 프록시를 생성할 때 어드바이저를 제공하면 어디에 어떤 기능을 제공할 지 선택 가능



```java
@Test
void advisorTest1() {
  ServiceInterface target = new ServiceImpl();
  ProxyFactory proxyFactory = new ProxyFactory(target);

  DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(Pointcut.TRUE, new TimeAdvice());
  proxyFactory.addAdvisor(advisor);

  ServiceInterface proxy = (ServiceInterface) proxyFactory.getProxy();
  
  proxy.save();
  proxy.find();
}
```

- `new DefaultPointcutAdvisor`: `Advisor` 인터페이스의 가장 일반적인 구현체. 어드바이저는 하나의 포인트컷과 하나의 어드바이스로 구성된다.

- `ProxyFactory` -> `Advisor` (`Pointcut`, `Advice`) 



**직접 만든 포인트컷**

`save()`에만 적용하고 `find()` 에는 적용 안하도록.



```java
@Test
@DisplayName("직접 만든 포인트컷")
void advisorTest2() {
  ServiceImpl target = new ServiceImpl();
  ProxyFactory proxyFactory = new ProxyFactory(target);

  DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(new MyPointcut(), new TimeAdvice());
  proxyFactory.addAdvisor(advisor);

  ServiceInterface proxy = (ServiceInterface) proxyFactory.getProxy();

  proxy.save();
  proxy.find();
}

static class MyPointcut implements Pointcut {

  @Override
  public ClassFilter getClassFilter() {
    return ClassFilter.TRUE;
  }

  @Override
  public MethodMatcher getMethodMatcher() {
    return new MyMethodMatcher();
  }
}

static class MyMethodMatcher implements MethodMatcher {
  private String matchName = "save";

  @Override
  public boolean matches(Method method, Class<?> targetClass) {
    boolean result = method.getName().equals(matchName);
    log.info("포인트컷 호출 method={} targetClass={}", method.getName(), targetClass);
    log.info("포인트컷 결과 result={}", result);
    return result;
  }

  @Override
  public boolean isRuntime() {
    return false;
  }

  @Override
  public boolean matches(Method method, Class<?> targetClass, Object... args) {
    throw new UnsupportedOperationException();
  }
}
```



**스프링이 제공하는 포인트컷**

스프링은 우리가 필요한 포인트컷을 대부분 제공한다. 위 기능-> `NameMatchMethodPointcut`



```java
@Test
@DisplayName("스프링이 제공하는 포인트컷")
void advisorTest3() {
  ServiceImpl target = new ServiceImpl();
  ProxyFactory proxyFactory = new ProxyFactory(target);
  NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
  pointcut.setMappedNames("save");

  DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, new TimeAdvice());
  proxyFactory.addAdvisor(advisor);
  
  ServiceInterface proxy = (ServiceInterface) proxyFactory.getProxy();
  proxy.save();
  proxy.find();
}
```



**스프링이 제공하는 포인트컷**

- `NameMatchMethodPointcut`: 메서드 이름 기반으로 매칭
- `JdkRegexMEthodPointcut`: JDK 정규 표현식 기반으로 매칭
- `TruePointcut`: 항상 참을 반환
- `AnnotationMatchingPointcut`: 애너테이션으로 매칭
- `AspectJExpressionPointcut`: aspectJ 표현식으로 매칭

**가장 중요한 것은 aspectJ 표현식!**



**여러 어드바이저 함께 적용**

여러 어드바이저를 하나의 `target`에 적용하려면 어떻게 해야 할까?

프록시 두 번 생성?? 적용해야 하는 어드바이저의 수만큼 프록시를 생성해야 하는 문제가 있다.



스프링은 이 문제를 해결하기 위해 하나의 프록시에 여러 어드바이저를 적용할 수 있게 만들어 두었다.



```java
@Test
@DisplayName("하나의 프록시, 여러 어드바이저")
void multiAdvisorTest2() {
  //proxy -> advisor2 -> advisor1 -> target
  DefaultPointcutAdvisor advisor2 = new DefaultPointcutAdvisor(Pointcut.TRUE, new Advice2());
  DefaultPointcutAdvisor advisor1 = new DefaultPointcutAdvisor(Pointcut.TRUE, new Advice1());

  ServiceInterface target = new ServiceImpl();

  ProxyFactory proxyFactory1 = new ProxyFactory(target);
  proxyFactory1.addAdvisor(advisor2);
  proxyFactory1.addAdvisor(advisor1);

  ServiceInterface proxy = (ServiceInterface) proxyFactory1.getProxy();

  //실행
  proxy.save();
}
```

- ProxyFactory에 원하는 만큼 `addAdvisor()`를 통해 어드바이저를 등록하면 된다.
- 등록하는 순서대로 `advisor`가 호출됨







**ProxyFactoryConfigV1.java**

``` java
  @Bean
  public OrderRepositoryV1 orderRepositoryV1(LogTrace logTrace) {
    OrderRepositoryV1 orderRepository = new OrderRepositoryV1Impl();
    ProxyFactory factory = new ProxyFactory(orderRepository);
    factory.addAdvisor(getAdvisor(logTrace));
    OrderRepositoryV1 proxy = (OrderRepositoryV1) factory.getProxy();
    log.info("ProxyFactory proxy={}, target={}", proxy.getClass(), orderRepository.getClass());
    return proxy;
  }

  private Advisor getAdvisor(LogTrace logTrace) {
    //pointcut
    NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
    pointcut.setMappedNames("request*", "order*", "save*");
    //advice
    LogTraceAdvice advice = new LogTraceAdvice(logTrace);
    //advisor = pointcut + advice
    return new DefaultPointcutAdvisor(pointcut, advice);
  }
```



**ProxyFactoryConfigV2.java**

인터페이스가 없고 구체 클래스만 있는 v2 애플리케이션에 `LogTrace` 기능을 프록시 팩토리를 통해서 프록시를 만들고 적용



```java
package hello.proxy.config.v3_proxyfactory;

@Slf4j
@Configuration
public class ProxyFactoryConfigV2 {

  ...
    
  @Bean
  public OrderRepositoryV2 orderRepositoryV2(LogTrace logTrace) {
    OrderRepositoryV2 orderRepository = new OrderRepositoryV2();
    ProxyFactory factory = new ProxyFactory(orderRepository);
    factory.addAdvisor(getAdvisor(logTrace));
    OrderRepositoryV2 proxy = (OrderRepositoryV2) factory.getProxy();
    log.info("ProxyFactory proxy={}, target={}", proxy.getClass(), orderRepository.getClass());
    return proxy;
  }

  private Advisor getAdvisor(LogTrace logTrace) {
    //pointcut
    NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
    pointcut.setMappedNames("request*", "order*", "save*");
    //advice
    LogTraceAdvice advice = new LogTraceAdvice(logTrace);
    //advisor = pointcut + advice
    return new DefaultPointcutAdvisor(pointcut, advice);
  }
}
```



**남은 문제**

1. 너무 많은 설정: 스프링 빈의 개수만큼 동적 프록시 생성 코드를 작성해야 한다.
2. 컴포넌트 스캔



-> 두 문제를 한 번에 해결하는 방법: **빈 후처리기**