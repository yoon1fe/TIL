## @Aspect 프록시

프록시를 적용하려면 (포인트컷 + 어드바이스)로 구성된 어드바이저`Advisor`를 만들어서 스프링 빈으로 등록만 하면 된다. 그러면 자동프록시 생성기가 알아서 처리해줌.



스프링은 `@Aspect` 애너테이션으로 매우 편리하게 포인트컷 + 어드바이스로 구성된 어드바이저 생성 기능을 지원함.



```java
package hello.proxy.config.v6_aop.aspect;

import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Slf4j
@Aspect
@RequiredArgsConstructor
public class LogTraceAspect  {

  private final LogTrace logTrace;

  @Around("execution(* hello.proxy.app..*(..))")
  public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
    TraceStatus status = null;

    try {
      String message = joinPoint.getSignature().toShortString();
      status = logTrace.begin(message);

      //target 호출
      Object result = joinPoint.proceed();

      logTrace.end(status);
      return result;
    } catch (Exception e) {
      logTrace.exception(status, e);
      throw e;
    }
  }

}
```

- `@Aspect`: 애너테이션 기반 프록시 적용
- `@Around("execution(* hello.proxy.app..*(..))")`
  - `@Around` 값에 포인트컷 표현식.
  - `@Around`가 붙은 메서드 로직이 어드바이스(`Advice`)
  - `ProceedingJoinPoint joinPoint`: `MethodInvocation`과 유사. 내부에 실제 호출 대상, 전달 인자, 어떤 객체와 어떤 메서드가 호출되었는지에 대한 정보 포함
  - `joinPoint.proceed()`: 실제 호출 대상(`target`) 호출



**`@Aspect` 동작 원리**

자동 프록시 생성기(`AnnotationAwareAspectJAutoProxyCreator`)는 `Advisor`를 자동으로 찾아서 필요한 곳에 프록시를 생성 및 적용한다. 추가 역할로 `@Aspect` 애너테이션이 붙은 클래스를 찾아서 얘를 `Advisor`로 만들어준다. 그래서 이름에 `AnnotationAware`가 붙어 있음.



**`@Aspect`를 어드바이저로 변환하는 과정**

1. 실행: 스프링 애플리케이션 로딩 시점에 자동 프록시 생성기를 호출
2. 모든 `@Aspect` 빈 조회: 자동 프록시 생성기는 스프링 컨테이너에서 `@Aspect` 애너테이션이 붙은 스프링 빈을 모두 조회
3. 어드바이저 생성: `@Aspect` 어드바이저 빌더를 통해 어드바이저 생성
4. `@Aspect` 기반 어드바이저 저장: 생성한 어드바이저를 `@Aspect` 어드바이저 빌더 내부에 저장



**`@Aspect` 어드바이저 빌더**

`BeanFactoryAspectJAdvisorsBuilder`

- `@Aspect` 정보 기반으로 포인트컷, 어드바이스, 어드바이저를 생성 및 보관한다.

- 생성한 어드바이저를 빌더 내부 저장소에 캐싱하고, 캐시에 어드바이저가 이미 있다면 캐시에 저장된 어드바이저를 반환한다.



**어드바이저 기반으로 프록시 생성하는 과정**

1. 생성: 스프링 애플리케이션 로딩 시점에 스프링 빈 대상이 되는 객체들이 생성됨
2. 전달: 생성된 객체를 빈 저장소에 등록하기 직전에 빈 후처리기에 전달
3. Advisor 빈 조회: 스프링 컨테이너에서 `Advisor` 빈 조회
   - `@Aspect` Advisor 조회: `@Aspect` 어드바이저 빌더 내부에 저장된 `Advisor` 모두 조회
4. 프록시 적용 대상 체크
5. 프록시 생성
6. 빈 등록