## 포인트컷 지시자

AspectJ는 포인트컷을 편리하게 표현하기 위한 표현식을 제공한다. ex) `@Pointcut("execution(* hello.aop.order..*(..))")`



**포인트컷 지시자**

포인트컷 표현식은 `execution`같은 포인트컷 지시자(Pointcut Designator)로 시작한다. 줄여서 PCD라고 함.



- `execution`: 메서드 실행 조인 포인트 매칭. 스프링 AOP에서 가장 많이 사용하고 기능도 복잡하다.
- `within`: 특정 타입 내의 조인 포인트 매칭
- `args`: 인자가 주어진 타입의 인스턴스인 조인 포인트
- `this`: 스프링 빈 객체(스프링 AOP 프록시)를 대상으로 하는 조인 포인트
- `target`: Target 객체(스프링 AOP 프록시가 가리키는 실제 대상)를 대상으로 하는 조인 포인트
- `@target`: 실행 객체의 클래스에 주어진 타입의 애너테이션이 있는 조인 포인트
- `@within`: 주어진 애너테이션이 있는 타입 내 조인 포인트
- `@annotation`: 메서드가 주어진 애너테이션을 갖고 있는 조인 포인트 매칭
- `@args`: 전달된 실제 인수의 런타임 타입이 주어진 타입의 애너테이션을 갖는 조인 포인트
- `bean`: 스프링 전용 포인트컷 지시자. 빈의 이름으로 포인트컷 지정



**`ClassAop.java`**

```java
package hello.aop.member.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClassAop {

}
```



**`MethodAop.java`**

```java
package hello.aop.member.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodAop {

  String value();

}
```



**`MemberServiceImpl.java`**

```java
package hello.aop.member;

import hello.aop.order.aop.member.annotation.ClassAop;
import hello.aop.order.aop.member.annotation.MethodAop;
import org.springframework.stereotype.Component;

@ClassAop
@Component
public class MemberServiceImpl implements MemberService {

  @Override
  @MethodAop("test value")
  public String hello(String param) {
    return "ok";
  }

  public String internal(String param) {
    return "ok";
  }
}
```



```java
package hello.aop.pointcut;

import hello.aop.member.MemberServiceImpl;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

@Slf4j
public class ExecutionTest {

  AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
  Method helloMethod;

  @BeforeEach
  public void init() throws NoSuchMethodException {
    helloMethod = MemberServiceImpl.class.getMethod("hello", String.class);
  }

  @Test
  public void printMethod() {
    // helloMethod=public java.lang.String hello.aop.member.MemberServiceImpl.hello(java.lang.String)
    log.info("helloMethod={}", helloMethod);
  }

}
```

- `AspectJExpressionPointcut`: 포인트컷 표현식을 처리해주는 클래스. 상위에 `Pointcut` 인터페이스가 있음
- 위에 출력한 메서드의 정보를 매칭해서 `execution` 포인트컷 대상을 찾아낸다.



### execution

**문법**

```
execution(modifiers-pattern? ret-type-pattern declaring-type-pattern?namepattern(param-pattern) throws-pattern?)
execution(접근제어자? 반환타입 선언타입?메서드이름(파라미터) 예외?)
```

- 메서드 실행 조인 포인트 매칭
- `?`: 생략 가능
- `*` 같은 패턴 지정 가능



**`MemberServiceImpl.hello(String)` 메서드와 가장 정확하게 매칭되는 표현식**

```java
  @Test
  void exactMatch() {
    //public java.lang.String hello.aop.member.MemberServiceImpl.hello(java.lang.String)
    pointcut.setExpression("execution(public String hello.aop.member.MemberServiceImpl.hello(String)) ");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }
```

- `AspectJExpressionPointcut`에 `setExpression()`으로 포인트컷 표현식 적용
- `pointcut.matches(메서드, 대상 클래스)` 를 실행하면 지정한 포인트컷 표현식의 매칭 여부를 반환



**매칭 조건**

- 접근제어자?: `public`
- 반환 타입: `String`
- 선언 타입?: `hello.aop.member.MemberServiceImpl`
- 메서드 이름: `hello`
- 파라미터: `(String)`
- 예외?: 생략



**가장 생략된 표현식**

```java
@Test
void allMatch() {
  pointcut.setExpression("execution(* *(..))");
  assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}
```

- 접근제어자?: 생략
- 반환 타입: `*`
- 선언 타입?: 생략
- 메서드 이름: `*`
- 파라미터: `(..)` - 파라미터의 타입과 개수가 상관없다는 의미
- 예외?: 생략



**메서드 이름 매칭**

```java
@Test
void nameMatch() {
  pointcut.setExpression("execution(* hello(..))");
  assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}
@Test
void nameMatchStar1() {
  pointcut.setExpression("execution(* hel*(..))");
  assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}
@Test
void nameMatchStar2() {
  pointcut.setExpression("execution(* *el*(..))");
  assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}
@Test
void nameMatchFalse() {
  pointcut.setExpression("execution(* nono(..))");
  assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isFalse();
}
```



**패키지명 매칭**

```java
@Test
void packageExactMatch1() {
  pointcut.setExpression("execution(* hello.aop.member.MemberServiceImpl.hello(..))");
  assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}
@Test
void packageExactMatch2() {
  pointcut.setExpression("execution(* hello.aop.member.*.*(..))");
  assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}
@Test
void packageExactMatchFalse() {
  pointcut.setExpression("execution(* hello.aop.*.*(..))");
  assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isFalse();
}
@Test
void packageMatchSubPackage1() {
  pointcut.setExpression("execution(* hello.aop.member..*.*(..))");
  assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}
@Test
void packageMatchSubPackage2() {
  pointcut.setExpression("execution(* hello.aop..*.*(..))");
  assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}
```



`hello.aop.member.*(1).*(2)`

- (1): 타입
- (2): 메서드 이름



패키지에서

- `.`: 해당 위치의 패키지
- `..`: 해당 위치 패키지 + 하위 패키지



**타입 매칭**

```java
@Test
void typeExactMatch() {
  pointcut.setExpression("execution(* hello.aop.member.MemberServiceImpl.*(..))");
  assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}
@Test
void typeMatchSuperType() {
  pointcut.setExpression("execution(* hello.aop.member.MemberService.*(..))");
  assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}

@Test
void typeMatchInternal() throws NoSuchMethodException {
  pointcut.setExpression("execution(* hello.aop.member.MemberServiceImpl.*(..))");
  Method internalMethod = MemberServiceImpl.class.getMethod("internal", String.class);
  assertThat(pointcut.matches(internalMethod, MemberServiceImpl.class)).isTrue();
}

//포인트컷으로 지정한 MemberService 는 internal 이라는 이름의 메서드가 없다.
@Test
void typeMatchNoSuperTypeMethodFalse() throws NoSuchMethodException {
  pointcut.setExpression("execution(* hello.aop.member.MemberService.*(..))");
  Method internalMethod = MemberServiceImpl.class.getMethod("internal", String.class);
  assertThat(pointcut.matches(internalMethod, MemberServiceImpl.class)).isFalse();
}
```

- `MemberService` 매칭: 부모 타입으로 선언해도 그 자식 타입까지 매칭됨.
- `internal` 메서드 매칭X: 부모타입으로 매칭할 경우 부모 타입에서 선언한 메서드가 자식 탕비에 있어야 매칭 성공.



**파라미터 매칭**

```java
//String 타입의 파라미터 허용
//(String)
@Test
void argsMatch() {
  pointcut.setExpression("execution(* *(String))");
  assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}

//파라미터가 없어야 함
//()
@Test
void argsMatchNoArgs() {
  pointcut.setExpression("execution(* *())");
  assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isFalse();
}

//정확히 하나의 파라미터 허용, 모든 타입 허용
//(Xxx)
@Test
void argsMatchStar() {
  pointcut.setExpression("execution(* *(*))");
  assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}

//숫자와 무관하게 모든 파라미터, 모든 타입 허용
//파라미터가 없어도 됨
//(), (Xxx), (Xxx, Xxx)
@Test
void argsMatchAll() {
  pointcut.setExpression("execution(* *(..))");
  assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}

//String 타입으로 시작, 숫자와 무관하게 모든 파라미터, 모든 타입 허용
//(String), (String, Xxx), (String, Xxx, Xxx) 허용
@Test
void argsMatchComplex() {
  pointcut.setExpression("execution(* *(String, ..))");
  assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}
```

- `(String)`: 정확하게 String 타입 파라미터
- `()`: 파라미터가 없어야 함
- `(*)`: 정확히 한 개의 파라미터, 모든 타입 허용
- `(*, *)`: 정확히 두 개의 파라미터, 모든 타입 허용
- `(..)`: 개수와 무관하게 모든 파라미터, 모든 타입을 허용. 파라미터가 없어도 된다. `0..*`
- `(String, ..)`: String 타입으로 시작, 개수와 무관하게 모든 파라미터, 모든 타입 허용
  - ex) `(String)`, `(String, xxx)`, `(String, xxx, xxx)`



### within

특정 타입 내의 조인 포인트에 대한 매칭을 제한. 즉, 해당 타입이 매칭되면 그 안의 메서드들이 자동으로 매칭된다.

`execution`에서 타입 부분만 사용하는 택.



```java
public class WithinTest {

  AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
  Method helloMethod;

  @BeforeEach
  public void init() throws NoSuchMethodException {
    helloMethod = MemberServiceImpl.class.getMethod("hello", String.class);
  }

  @Test
  void withinExact() {
    pointcut.setExpression("within(hello.aop.member.MemberServiceImpl)");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void withinStar() {
    pointcut.setExpression("within(hello.aop.member.*Service*)");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void withinSubPackage() {
    pointcut.setExpression("within(hello.aop..*)");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }
  
  @Test
  @DisplayName("타켓의 타입에만 직접 적용, 인터페이스를 선정하면 안된다.")
  void withinSuperTypeFalse() {
    pointcut.setExpression("within(hello.aop.member.MemberService)");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isFalse();
  }
  
  @Test
  @DisplayName("execution은 타입 기반, 인터페이스를 선정 가능.")
  void executionSuperTypeTrue() {
    pointcut.setExpression("execution(* hello.aop.member.MemberService.*(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }
}
```



**주의!!**

**표현식에 부모 타입을 지정하면 안된다**! 정확하게 타입이 맞아야 한다. 인터페이스 지정도 못함.

잘 안쓴다.



### args

인자가 주어진 타입의 인스턴스인 조인 포인트로 매칭

기본 문법은 역시 `execution`의 `args` 부분과 동일하다.



**차이점**

- `execution`: 파라미터 타입이 정확히 매칭되어야 함. 클래스에 선언된 정보를 기반으로 판단.
- `args`: 부모 타입 허용. 실제 넘어온 파라미터 객체 인스턴스를 보고 판단.



```java
package hello.aop.pointcut;

import hello.aop.member.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class ArgsTest {
  
  Method helloMethod;
  
  @BeforeEach
  public void init() throws NoSuchMethodException {
    helloMethod = MemberServiceImpl.class.getMethod("hello", String.class);
  }

  private AspectJExpressionPointcut pointcut(String expression) {
    AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
    pointcut.setExpression(expression);
    return pointcut;
  }

  @Test
  void args() {
    //hello(String)과 매칭
    assertThat(pointcut("args(String)")
        .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    assertThat(pointcut("args(Object)")
        .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    assertThat(pointcut("args()")
        .matches(helloMethod, MemberServiceImpl.class)).isFalse();
    assertThat(pointcut("args(..)")
        .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    assertThat(pointcut("args(*)")
        .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    assertThat(pointcut("args(String,..)")
        .matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }
  /**
   * execution(* *(java.io.Serializable)): 메서드의 시그니처로 판단 (정적)
   * args(java.io.Serializable): 런타임에 전달된 인수로 판단 (동적)
   */
  @Test
  void argsVsExecution() {
    //Args
    assertThat(pointcut("args(String)")
        .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    assertThat(pointcut("args(java.io.Serializable)")
        .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    assertThat(pointcut("args(Object)")
        .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    //Execution
    assertThat(pointcut("execution(* *(String))")
        .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    assertThat(pointcut("execution(* *(java.io.Serializable))") //매칭 실패
        .matches(helloMethod, MemberServiceImpl.class)).isFalse();
    assertThat(pointcut("execution(* *(Object))") //매칭 실패
        .matches(helloMethod, MemberServiceImpl.class)).isFalse();
  }
}
```

- 정적으로 클래스에 선언된 정보만 보고 판단하는 `execution(* *(Object))`는 매칭 실패
- 동적으로 실제 파라미터로 넘어온 객체 인스턴스로 판단하는 `args(Object)`는는 매칭 성공(부모 타입 허용)



참고: args 지시자는 단독으로 사용되기 보다는 파라미터 바인딩에서 주로 사용된다.

참고: `args` 지시자는 단독으로 사용되기 보다는 파라미터 바인딩에서 주로 사용된다.



### @target, @within

- `@target`: 실행 객체의 클래스에 주어진 타입의 애너테이션이 있는 조인 포인트
  - `@target(hello.aop.member.annotation.ClassAop)`
  - **부모 클래스의 메서드까지** 어드바이스를 모두 적용. 인스턴스 기준
- `@within`: 주어진 애너테이션이 있는 타입 내 조인 포인트
  - `@within(hello.aop.member.annotation.ClassAop)`
  - **자기 자신의 클래스에 정의된 메서드에만** 어드바이스 적용. 해당 타입 기준



**주의**

`args`, `@args`, `@target` 포인트컷 지시자는 단독으로 사용하면 안된다.

위 포인트컷 지시자들은 실제 객체 인스턴스가 생성되고 실행될 때 어드바이스 적용 여부를 확인할 수 있는데, 스프링 컨테이너가 프록시를 생성하는 시점은 애플리케이션 로딩 시점이다. 이때 얘네들을 보고 스프링은 모든 스프링 빈에 AOP를 적용하려고 시도하는데, 스프링이 내부에서 사용하는 빈 중에 `final`로 선언된 애들도 있기 때문에 오류 발생할 수 있다.



### @annotation, @args

**`@annotation`**

- 메서드가 주어진 애너테이션을 갖고 있으면 조인 포인트 매칭

- `@annotation(hello.aop.member.annotation.MethodAop)`



메서드에 애너테이션이 있으면 매칭한다.

```java
package hello.aop.pointcut;

import hello.aop.member.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Slf4j
@Import(AtAnnotationTest.AtAnnotationAspect.class)
@SpringBootTest
public class AtAnnotationTest {

  @Autowired
  MemberService memberService;

  @Test
  void success() {
    log.info("memberService Proxy={}", memberService.getClass());
    memberService.hello("helloA");
  }

  @Slf4j
  @Aspect
  static class AtAnnotationAspect {

    @Around("@annotation(hello.aop.member.annotation.MethodAop)")
    public Object doAtAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
      log.info("[@annotation] {}", joinPoint.getSignature());
      return joinPoint.proceed();
    }
  }
}
```



**`@args`**

- 전달된 실제 인수의 런타입 타입이 주어진 타입의 애너테이션을 갖는 조인 포인트
- 전달된 인수의 런타임 타입에 `@Check` 애너테이션이 있는 경우 매칭



### bean

- 스프링 전용 포인트컷 지시자. 빈의 이름으로 지정!
- `@Around("bean(orderService) || bean(*Repository)")`
- `*` 같은 패턴 사용 가능



```java
package hello.aop.pointcut;

@Slf4j
@Import(BeanTest.BeanAspect.class)
@SpringBootTest
public class BeanTest {

  @Autowired
  OrderService orderService;

  @Test
  void success() {
    orderService.orderItem("itemA");
  }

  @Aspect
  static class BeanAspect {

    @Around("bean(orderService) || bean(*Repository)")
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
      log.info("[bean] {}", joinPoint.getSignature());
      return joinPoint.proceed();
    }
  }
}
```



### 매개변수 전달





### this, target