## Lombok 동작 방식

**Lombok**

`@Getter`, `@Setter`, `@Builder` 등의 애너테이션과 애너테이션 프로세서를 제공하여 표준적으로 작성해야 할 코드를 개발자 대신 생성해주는 라이브러리



**롬복 동작 원리**

컴파일 시점에 **애너테이션 프로세서**(자바가 제공하는 기술, 컴파일할 때 끼어드는거)를 사용하여 소스코드의 AST(Abstract Syntax Tree)를 조작한다.



**논란거리?**

- 공개된 API가 아닌 컴파일러 내부 클래스를 사용하여 기존 소스코드를 조작 -> 일종의 해킹이다!!!
- 원래는 애너테이션 프로세서를 통해 애너테이션이 붙은 클래스의 정보를 참조만 할 수 있다.
- 특히 이클립스의 경우, java agent를 사용하여 컴파일러 클래스까지 조작하여사용한다. 해당 클래스들 역시 공개된 API가 아니다 보니 버전 호환성 문제가 생길 수 있다.



## 애너테이션 프로세서



**`Processor` 인터페이스**

- 여러 라운드에 걸쳐서 소스 및 컴파일된 코드를 처리할 수 있다



유틸리티

- `AutoService`: 서비스 프로바이더 레지스트리 생성기

  ``` java
  @AutoService(Processor.class)
  public class MagicMojaProcessor extends AbstractProcessor {
    ...
  }
  ```

  컴파일 시점에 애너테이션 프로세서를 사용하여 META-INF/services/javax.annotation.processor.Processor 파일 자동으로 생성해 줌.