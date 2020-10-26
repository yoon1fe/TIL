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

Old 영역은 기본적으로 데이터가 가득 차면 GC를 실행한다. GC 방식에 따라서 처리 절차가 달라지기 때문에, Java 7 기준 다섯 가지 방법이 있다.

- Serial GC
- Parallel GC
- Parallel Old GC (Parallel Compacting GC)
- Concurrent Mark & Sweep GC (CMS)
- G1(Garbage First) GC















##### Reference

https://d2.naver.com/helloworld/1329

https://plumbr.io/blog/garbage-collection/minor-gc-vs-major-gc-vs-full-gc

https://gyoogle.dev/blog/computer-language/Java/Garbage%20Collection.html

https://medium.com/@joongwon/jvm-garbage-collection-algorithms-3869b7b0aa6f

https://mirinae312.github.io/develop/2018/06/04/jvm_gc.html