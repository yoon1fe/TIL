## 코드 커버리지는 어떻게 측정할까?

테스트 커버리지란? 테스트 코드가 내가 작성한 코드를 얼만큼 커버하느냐.. 얼만큼 테스트하느냐에 대한 지표. JaCoCo란 툴이 있다.

그럼 이런 툴이 어떻게 커버리지를 측정할 수 있을까??

-> 바이트 코드를 읽어와서 커버리지 챙겨야 하는 부분 카운트를 세고, 테스트 코드를 실행하면서 실행되는 부분을 카운트해서 비교!



## 모자에서 토끼를 꺼내는 마술

바이트 코드를 조작하는 것은 마술과도 같다!!

바이트코드 조작 라이브러리 

- ASM: https://asm.ow2.io/ 
- Javassist: https://www.javassist.org/ 
- ByteBuddy: https://bytebuddy.net/#/ < 백기선 추천



**ByteBuddy**

``` java
new ByteBuddy().redefine(Moja.class)	// 클래스 재정의
    .method(name("pullOut")).intercept(FixedValue.value("Rabbit!!"))
    .make().saveIn(new File("~/target/classes/"));		// 바이트 코드 수정 후 저장
```



## javaagent

Javaagent(JVM Agent)란? JVM에서 동작하는 자바 애플리케이션으로 JVM의 다양한 이벤트를 전달받거나 정보 질의, 바이트 코드 제어 등을 특정 API를 통해서 수행할 수 있다.



**javaagent JAR 파일 만들기**

`premain()` 메서드 구현해야 함. agent를 붙이는 방식은 크게 두 가지가 있다.

- premain 모드: 애플리케이션 실행할 때 옵션으로 줘서 agent 붙이는 방식
- agentmain 모드: 런타임 중에 동적으로 agent 붙이는 방식



이 방식은 `.class` 파일 자체를 변경하는 것이 아니고, 클래스 로더가 클래스를 읽어올 때, **javaagent를 거쳐서 변경된 바이트 코드를 읽어들여 사용**한다.

`VM options: -javaagent:/Users/keesun/workspace/MasulsaJavaAgent/target/MasulsaAgent-1.0-SNAPSHO T.jar`



```java
public class MasulsaAgent {
	public static void premain(String agentArgs, Instrumentation inst) {
        new AgentBuilder.Default()
            .type(ElementMatchers.any())
            .transform((builder, typeDescription, classLoader, javaModule) -> builder.method(named("pullOut")).intercept(FixedValue.value("Rabbit!" )).installOn(inst);
    }
}
```



## 바이트 코드 조작 활용 예

프로그램 분석 

- 코드에서 버그 찾는 툴
- 코드 복잡도 계산 

클래스 파일 생성 

- **프록시**
- 특정 API 호출 접근 제한
- 스칼라 같은 언어의 컴파일러

그밖에도 자바 소스 코드 건리지 않고 코드 변경이 필요한 여러 경우에 사용할 수 있다. 

- 프로파일러 (newrelic) 
- 최적화 
- 로깅 
- ...



스프링이 컴포넌트 스캔을 하는 방법 (asm 사용함) 

- 컴포넌트 스캔으로 빈으로 등록할 후보 클래스 정보를 찾는데 사용
- ClassPathScanningCandidateComponentProvider -> SimpleMetadataReader
- ClassReader와 Visitor 사용해서 클래스에 있는 메타 정보를 읽어온다.