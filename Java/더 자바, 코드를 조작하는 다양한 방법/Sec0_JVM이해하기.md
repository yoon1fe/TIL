## Java, JVM, JDK, JRE

![img](https://velog.velcdn.com/images/gudcks0305/post/d3c554ed-81fa-4155-8e46-016c995e2177/image.png)

**JVM (Java Virtual Machine)**

- 자바 가상 머신
- 자바 바이트 코드(.class 파일)를 각 OS에 특화된 네이티브 코드(머신 코드)로 변환(인터프리터 + JIT(짓,, Just In Time) 컴파일러)하여 실행
- 바이트 코드를 실행하는 표준(JVM 자체는 표준)이자 구현체(특정 벤더가 구현한 JVM, 오라클, 아마존, Azul 등..).
- 특정 플랫폼에 종속적



자바 바이트 코드

```
PS C:\Users\1Fe\Downloads> javac .\Hello.java
PS C:\Users\1Fe\Downloads> javap -c .\Hello.class
Compiled from "Hello.java"
public class Hello {
  public Hello();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: return

  public static void main(java.lang.String[]);
    Code:
       0: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
       3: ldc           #3                  // String Hello, Java!
       5: invokevirtual #4                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
       8: return
}
```



**JRE (Java Runtime Environment) - JVM + 라이브러리**

- 자바 애플리케이션을 실행할 수 있도록 구성된 배포판. 최소 단위. JVM 만 배포하지 않는다.
- JVM + 핵심 라이브러리 및 자바 런타임 환경에서 사용하는 프로퍼티 세팅이나 리소스 파일을 갖고 있다.
- 자바 컴파일러는 안들어있다!!
- 개발 관련 도구는 불포함 (이건 JDK에서 제공해줌)



**JDK(Java Development Kit) - JRE + 개발 툴**

- JRE + 개발에 필요한 툴
- 소스 코드를 작성할 때 사용하는 자바 언어는 플랫폼에 독립적이다.
- 오라클은 자바 11부터는 JDK만 제공하고 JRE는 따로 제공하지 않음. 자바 9부터 모듈 시스템이 도입되었는데, 모듈 시스템을 사용해서 JRE 구성 가능하다. (jlink..)
- Write Once Run Anywhere



**Java?**

- JDK에 들어있는 자바 컴파일러(javac)를 사용하여 바이트 코드(.class 파일)로 컴파일할 수 있음



**JVM 언어**

- JVM 기반으로 동작하는 프로그래밍 언어
- 클로저, 그루비, JRuby, Jython, Kotlin, Scala 등등...



## JVM 구조

![img](https://user-images.githubusercontent.com/68279162/172682942-ad2c8a9f-568c-4987-9a1a-c9ab2213ecd4.png)



**클래스 로더 시스템**

- .class 파일에서 바이트 코드를 읽고 메모리에 저장
- 로딩: 클래스를 읽어오는 과정
- 링크: 레퍼런스를 연결하는 과정
- 초기화: `static` 값들 초기화 및 변수에 할당



**메모리**

- **메서드 영역**: 클래스 수준의 정보 (클래스 이름, 부모 클래스 이름, 메서드, 변수) 저장함. 공유 자원이다.
- **힙 영역**: 객체를 저장. 역시 공유 자원
- **스택 영역**: **스레드마다** 런타임 스택을 만들고, 그 안에 메서드 호출을 스택 프레임이라 부르는 블럭으로 쌓는다. 스레드 종료하면 런타임 스택도 사라짐.
- **PC(Program Counter) 레지스터**: **스레드마다** 스레드 내 현재 실행할 스택 프레임을 가리키는 포인터가 생성된다. 운영체제에서의 그것과 일맥상통
- **네이티브 메서드 스택**: 역시 스레드마다 생성.



**실행 엔진**

- 인터프리터: 바이트 코드를 한 줄씩 실행
- JIT 컴파일러(바이트 코드 -> 네이티브 코드로 컴파일): 인터프리터 효율을 높이기 위해 **인터프리터가 반복되는 코드를 발견하면 JIT 컴파일러로 반복되는 코드를 모두 네이티브 코드로 바꿔둔다.** 그 다음부터 인터프리터는 네이티브 코드로 컴파일된 코드를 바로 사용하면 됨.
- GC(Garbage Collector): 더이상 참조되지 않는 객체를 모아서 정리



**JNI(Java Native Interface)**

- 자바 애플리케이션에서 C, C++, 어셈블리로 작성된 함수를 사용할 수 있는 방법 제공. Native 키워드를 사용한 메서드 호출



**네이티브 메서드 라이브러리**

- C, C++ 로 작성된 라이브러리



## 클래스 로더

![img](https://user-images.githubusercontent.com/68279162/172682956-1ca356a2-4dec-462e-979a-c7f8dbe10ff5.png)

로딩 - 링킹 - 초기화 순으로 진행



**로딩**

- 클래스 로더가 `.class` 파일을 읽고 그 내용에 따라 적절한 바이너리 데이터를 만들고 **메서드 영역**에 저장
- 메서드 영역에 저장하는 데이터
  - FQCN(Fully Qualified Class Name) - 패키지 경로까지 포함한 클래스 이름
  - 클래스, 인터페이스, enum
  - 메서드와 변수
- 로딩이 끝나면 해당 클래스 타입의 Class 객체를 생성하여 **힙 영역**에 저장



**링킹**

- verify, prepare, resolve(optional) 세 단계로 나뉨
- verify: `.class` 파일 형식이 유효한지 체크
- preparation: 클래스 변수(static 변수)와 기본값에 필요한 메모리
- resolve: 심볼릭 메모리 레퍼런스를 메서드 영역에 있는 실제 레퍼런스로 교체. 실제 힙 영역에 있는 레퍼런스를 참조하도록 한다. <- 선택적



**초기화**

- static 변수의 값 할당



클래스 로더는 계층 구조로 이루어져 있으며, 기본적으로 세 가지 클래스 로더가 제공된다.

- boot strap 클래스 로더: JAVA_HOME/lib에 있는 코어 자바 API 제공. 최상위 우선 순위를 가진 클래스 로더. 네이티브 코드로 작성되어 있다.
- 플랫폼(구 extension) 클래스 로더: JAVA_HOME/lib/ext 폴더 or java.ext.dirs 시스템 변수에 해당하는 위치에 있는 클래스를 읽는다
- 애플리케이션 클래스 로더: 애플리케이션 class path에서 클래스 읽는다
