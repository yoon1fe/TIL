## Java Garbage Collection (GC)



C/C++ 프로그래밍을 할 때는 메모리 누수(Memoeyr leak)를 막기 위해 객체를 생성한 후 사용하지 않는 객체의 메모리를 프로그래머가 직접 해제해주어갸 했다. 하지만, 자바에서는 JVM의 구성 요소 중 하나인 **Garbage Collection(GC)이 자동으로 사용되지 않는 객체를 해제**한다.

JVM의 메모리는 총 5가지 영역(method, heap, stack, PC, Nativ method stack)으로 나뉘는데, GC는 Heap 영역만 다룬다. 일반적으로 다음과 같은 경우가 GC의 대상이 된다.

1. 객체가 NULL인 경우 (e.g String str = null;)
2. 블럭 실행 종료 후, 블럭 안에서 생성된 객체
3. 부모 객체가 NULL인 경우 포함하는 자식 객체



GC에 대해서 알아보기 전에 **'stop-the-world'**란 단어를 알아야 한다. 이는 **GC을 실행하기 위해 JVM이 애플리케이션 실행을 멈추는 것**이다. stop-the-world가 발생하면 GC를 실행하는 쓰레드를 제외한 나머지 쓰레드는 모두 작업을 멈추고, GC 작업이 완료된 이후에 중단됐던 작업이 다시 시작된다. 어떤 GC 알고리즘을 사용하더라도 stop-the-world가 발생하고, Minor GC 수행 때도 발생한다. 다만 **이 때는 대부분의 애플리케이션에서 일시 정지되는 시간이 무시할 수 있을 정도로 낮다**. 대개 GC 튜닝이란 이 'stop-the-world' 시간을 줄이는 것이다.



자바에서는 개발자가 프로그램 코드에서 명시적으로 메모리를 해제하지 않기 때문에 가비지 컬렉터(Garbage Collector)가 더 이상 필요없는 (쓰레기) 객체를 찾아 지우는 작업을 한다. 이 가비지 컬렉터는 두 가지 가정 하에 만들어졌다.

1. 대부분의 객체는 금방 접근 불가능 상태(unreachable)가 된다.
2. 오래된 객체에서 젊은 객체로의 참조는 아주 적게 존재한다.

이러한 가설을 **'weak generational hypothesis'**라고 한다. 이 가정의 장점을 최대화하기 위해 HotSpot VM에서는 크게 두 개의 물리적인 공간을 나누었다. 이를 각각 Young 영역, Old 영역이라고 한다.



#### [Heap 영역에서의 데이터의 흐름]



![JavaGarbage1](https://d2.naver.com/content/images/2015/06/helloworld-1329-1.png)



**Young 영역 (Young Generation)**

새롭게 생성된 객체의 대부분이 위치하는 곳이다. 대부분의 객체가 금방 접근 불가능한 상태가 되기 때문에 많은 객체가 Young 영역에 생성되었다가 사라진다. 이 영역에서 객체가 사라질 때 **Minor GC**가 발생한다고 말한다.



**Old 영역 (Old Generation)**

접근 불가능 상태가 되지 않아 Young 영역에서 살아남은 객체가 여기로 복사된다. 대부분 Young 영역보다 큰 크기가 할당되며, 크기가 큰 만큼 Young 영역보다 GC는 적게 발생한다. 이 영역에서 객체가 사라질 때 **Major GC(혹은 Full GC)**가 발생한다고 말한다.

 

**(Permanent 영역)**

JVM이 클래스들과 메소드들을 설명하기 위해 필요한 메타 데이터들과 억류(intern())된 문자열 정보가 저장되는 곳이다. 얘는 Method Area에 포함이 된다. 이 영역에서도 GC가 발생할 수 있는데, 여기서 GC가 발생해도 Major GC의 횟수에 포함된다. JDK 8부터 Metaspace로 교체되었고,  JDK 7부터는 String.intern() 메소드의 호출 결과가 Heap 영역으로 들어간다고 한다.. 여기가 String Constant Pool 인가??



그렇다면 **Old 영역에 있는 객체가 Young 영역의 객체를 참조하는 경우에는 어떻게 처리될까?** 이러한 경우를 처리하기 위해서 Old 영역에는 512 바이트의 덩어리로 되어 있는 카드 테이블(card table)이 존재한다.

카드 테이블에는 Old 영역에 있는 객체가 Young 영역의 객체를 참조할 때마다 정보가 표시된다. Young 영역의 GC를 실행할 때에는 Old 영역에 있는 모든 객체의 참조를 확인하지 않고, 이 카드 테이블만 뒤져서 GC의 대상인지 식별한다.

![JavaGarbage2](https://d2.naver.com/content/images/2015/06/helloworld-1329-2.png)



#### [Young 영역의 구성]

Minor GC는 JVM이 새 객체에 대한 공간을 할당할 수 없을 때 발생한다. Young 영역은 세 개의 영역으로 나뉜다.

- Eden 영역
- Survivor 영역 두 개



각 영역의 처리 절차를 순서에 따라 기술하면 다음과 같다.

1. 새로 생성된 대부분의 객체는 Eden 영역에 위치한다.
2. Eden 영역에서 GC가 한 번 발생한 후 살아남은 객체는 Survivor 영역 중 하나로 이동된다.
3. Eden 영역에서 GC가 발생하면 이미 살아남은 객체가 존재하는  Survivor 영역으로 객체가 계속 쌓인다.
4. 하나의 Survivor 영역이 가득 차면 그 중에서 살아남은 객체를 다른 Survivor 영역으로 이동한다. 그러면 가득 찼던 Survivor 영역에는 아무 데이터도 없는 생태가 된다.
5. 이 과정을 반복하다가 계속 살아 남아있는 객체는 Old 영역으로 이동하게 된다.

![JavaGarbage3](https://d2.naver.com/content/images/2015/06/helloworld-1329-3.png)



#### [Old 영역에 대한 GC]

Old 영역은 기본적으로 데이터가 가득 차면 GC를 실행한다. Java 7 기준 다섯 가지 방법이 있...지만 자바 15 기준으로 몇개만 한 번 살펴보자.

- Serial GC
- Parallel GC
- ~~Parallel Old GC (Parallel Compacting GC)~~
- ~~Concurrent Mark & Sweep GC (CMS)~~ -> 이 방식은 오라클 공식 문서를 찾아보니 Java 15에는 빠져있다.
- G1(Garbage First) GC
- (The Z GC)



**Serial GC** (-XX:+UseSerialGC)

Young 영역에서의 GC는 앞에서 설명한 방식을 사용한다. Old 영역의 GC는 mark-sweep-compact라는 알고리즘을 사용한다. 이 알고리즘의 첫 단계는 Old 영역에 살아있는 객체를 식별**(Mark)**하는 것이다. 그 다음에는 힙 영역의 앞부분부터 확인하여 살아있는 것만 남긴다**(Sweep)**. 마지막 단계에서는 각 객체들이 연속되게 쌓이도록 힙의 가장 앞 부분부터 채워서 객체가 존재하는 부분과 객체가 없는 부분으로 나눈다**(Compaction)**.

Serial GC는 적은 메모리와 CPU 코어 개수가 적을 때 적합한 방식이다.



**Parallel GC** (-XX:+UseParallelGC)

Parallel GC는 Serial GC와 기본적인 알고리즘은 같다. 그러나 Serial GC는 GC를 처리하는 쓰레드가 하나인 것에 비해, Parallel GC는 GC를 처리하는 쓰레드가 여러 개이다. 그렇기 때문에 Serial GC보다 빠르게 객체를 처리할 수 있다. Parallel GC는 메모리가 충분하고 코어의 개수가 많을 때 유리하다. 이 GC는 Throughput GC라고도 부른다.



![JavaGarbage4](https://d2.naver.com/content/images/2015/06/helloworld-1329-4.png)



**G1 GC** (-XX:+UseG1GC)

eclipse.ini 를 살펴 보니 현재 디폴트로 되어 있도라...

G1 GC는 지금까지 열심히 이해했던 Young Generation 과 Old Generation을 깔끔히 잊어버리자.

G1 GC는 바둑판 모양의 각 영역에 객체를 할당하고 GC를 실행한다. 그러다가 해당 영역이 꽉 차면 다른 영역에서 객체를 할당하고 GC를 실행한다. 즉, 지금까지 Young 영역들에서 Old 영역으로 뭐시기 하는 단계가 사라진 GC 방식이다.



![G1Heap](https://mirinae312.github.io/img/jvm_gc/G1Heap.png)



하드웨어가 발전하면서 Java 애플리케이션에서 사용할 수 있는 메모리의 크기도 점차 커져갔다. 하지만 기존의 GC 알고리즘들로는 큰 메모리에서 좋은 성능(짧은 stop-the-world)을 내기 힘들었기 때문에 이에 초점을 둔 G1 GC가 등장했다. 따라서 G1 GC의 가장 큰 장점은 좋은 성능이다. G1 GC에서의 힙 영역은 Region이라는 특정한 크기로 나눠서 각 Region의 상태에 따라 역할(Eden, Survivor, Old)이 동적으로 부여되는 상태이다. JVM의 힙 영역은 2048개의 Region으로 나뉠 수 있으며, 각 Region의 크기는 1MB ~ 32MB 사이로 지정될 수 있다.

G1 GC에서 생판 처음 보는 영역들을 알아보자.

- **Humonous** - Region 크기의 50%를 초과하는 큰 객체를 저장하기 위한 공간으로, 이 Region에서는 GC 동작이 최적으로 동작하지 않는다.
- **Available / Unused** - 아직 사용되지 않은 Region을 의미한다.



G1 GC에서 **Young GC**를 수행할 때는 STW 시간을 최대한 줄이기 위해서 멀티 스레드로 GC를 수행한다. Young GC는 각 Region 중 GC 대상 객체가 가장 많은 Region(Eden 또는 Survivor 역할) 에서 수행되며, 이 Region에서 살아 남은 객체를 다른 Region(Survivor 역할) 으로 옮긴 후, 비워진 Region을 사용 가능한 Region으로 돌리는 형태로 동작한다.

G1 GC에서 **Major GC**를 수행할 때는 **Initial Mark -> Root Region Scan -> Concurrent Mark -> Remark -> Cleanup -> Copy** 단계를 거치게 된다.

- **Initial Mark**

  Old Region에 존재하는 객체들이 참조하는 Survivor Region을 찾는다. 이 과정에서도 STW가 발생한다.

- **Root Region Scan**

  Initial Mark에서 찾은 Survivor Region에 대한 GC 대상 객체 스캔 작업을 진행한다.

- **Concurrent Mark**

  전체 힙의 Region에 대해 스캔 작업을 진행하며, GC 대상 객체가 발견되지 않은 Region은 이후 단계를 처리하는데 제외시킨다.

- **Remark**

  애플리케이션을 멈추고(STW) 최종적으로 GC 대상에서 제외될 객체(살아남을 객체)를 식별한다.

- **Cleanup**

  애플리케이션을 멈추고(STW) 살아있는 객체가 가장 적은 Region에 대한 미사용 객체 제거를 수행한다. 이후 STW를 끝내고, 앞선 GC 과정에서 와전히 비워진 Region을 Freelist에 추가하여 재사용될 수 있게 한다.

- **Copy**

  GC 대상 Region이었지만 Cleanup 과정에서 완전히 비워지지 않은 Region의 살아 남은 객체들을 새로운 (Available / Unused) Region에 복사하여 Compaction 작업을 수행한다.


![Alt G1FullGC](https://mirinae312.github.io/img/jvm_gc/G1FullGC.png)



**The Z GC** (-XX:+UseZGC)

Java 11 부터 추가된 GC이다. 확장 가능한 낮은 레이턴시의 GC라고 한다..

GC가 동작할 때 STW 로 인해 성능에 큰 영향을 미쳐왔는데, Z GC는 애플리케이션과 Concurrently하게 동작하는데, Heap Reference를 위해 Load barrier를 사용한다. 사실 무슨 말인지 아직 잘 모르겠다... Z GC가 내세우는 가장 큰 장점 중 하나가 STW의 시간이 절대 10ms 를 넘지 않는 것이라는데, 버전이 업데이트되다 보면 언젠가 이 방식이 디폴트가 될지도 모를 일이다. 허허













##### Reference

https://docs.oracle.com/en/java/javase/15/

https://d2.naver.com/helloworld/1329

https://plumbr.io/blog/garbage-collection/minor-gc-vs-major-gc-vs-full-gc

https://gyoogle.dev/blog/computer-language/Java/Garbage%20Collection.html

https://medium.com/@joongwon/jvm-garbage-collection-algorithms-3869b7b0aa6f

https://mirinae312.github.io/develop/2018/06/04/jvm_gc.html

https://readystory.tistory.com/48