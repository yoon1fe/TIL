# Memory Management

메모리란 주소를 통해서 접근하는 매체이다. 주소는 두 가지로 나뉜다.

### Logical address (=virtual address)

프로그램이 실행되고 프로세스마다 독립적으로 갖게 되는 주소 공간이다.

각 프로세스마다 0번지부터 시작한다.

CPU가 다루는 주소는 **logical address**이다.

### Physical address

물리적인 메모리 주소로, 메모리에 실제로 올라가는 위치이다.

### Symbolic address

변수나 함수 이름을 주고 그 변수에 어떠한 값을 넣거나 함수 이름을 통해서 함수 호출을 하는 것과 같이, 프로그래밍을 할 때 숫자로 된 주소가 아닌 symbol로 된 주소를 갖고 프로그래밍을 하게 된다. 이때 사용하는 주소가 symbolic address 이다.



### 주소 바인딩

프로그램에 실제 주소를 할당하는 것이다.

프로그램이 디스크에 있다가 실행되면서 메모리에 올라가는 것을 address에 바인딩 되었다고 한다.

Symbolic addrses -> Logical address -> physical address의 과정을 거치게 된다.

**그렇다면, 이러한 주소 바인딩은 언제 이루어질까?**



## 주소 바인딩 (Address Binding)

![image-20200816211453237](C:\Users\1Fe\AppData\Roaming\Typora\typora-user-images\image-20200816211453237.png)

### Compile time binding

**컴파일 시점에 물리적인 주소가 결정**되는 방법이다. 그렇기 때문에 프로그램을 올릴 때 항상 실행파일에서 결정되어 있는 논리적 주소에 올려야 한다. 즉, 다른 주소가 많이 비어있음에도 항상 0~30 에만 올려야 하기 때문에 매우 비효율적이다.

시작 위치가 변경된다면 재컴파일된다. 이처럼 변경할 수 없으므로 컴파일러에 의해 생성되는 코드를 절대 코드(absolute code)라고 부른다.

오늘날의 컴퓨터 시스템들은 멀티태스킹 등 때문에 Compile time binding을 사용하지 않는다.

### Load time binding

프로그램이 시작돼서 **메모리에 처음 올라갈 때 물리적 주소가 결정**된다.

Loader의 책임하에 physical address의 주소가 부여된다.

컴파일러가 재배치 가능한 코드라고 해서 relocatable code를 생성한다.

### Execution time binding (= Run time binding)

Load time binding처럼 실행시에 물리적 주소가 결정된다. 

수행이 시작된 이후에도 **프로세스의 메모리 상의 위치가 바뀔 수 있다.**

오늘날 대부분의 컴퓨터 시스템들은 Run time binding을 지원한다.

CPU가 주소를 참조할 때마다 binding을 점검한다. (address mapping table)

**하드웨어적인 지원이 필요**하다. e.g) base and limit registers, MMU ..



## Memory-Management Unit (MMU)

Logical address를 physical address로 매핑해주는 하드웨어 디바이스이다.

![image-20200816214351580](C:\Users\1Fe\AppData\Roaming\Typora\typora-user-images\image-20200816214351580.png)

CPU가 메모리의 346번지에 있는 내용을 요청하면 주소 변환이 필요한데, 기본적인 MMU에서는  relocation register와 limit register를 통해 주소를 변환한다.

- **relocation register** - 프로세스가 physical memory에 올라가 있는 시작 위치

- **limit register** - 프로그램의 크기 (프로세스의 virtual memory의 길이)

  CPU가 이 범위를 벗어나는 주소를 요청할 경우 trap(software interrupt)를 통해 오류를 알리고 프로세스를 메모리에서 내린다.



user program은 logical address만을 다룬다. 실제 physical address를 볼 수도 없고 알 필요도 없다.



## 용어 정리

### Dynamic Loading

프로세스 전체를 메모리에 미리 다 올리는 것이 아니라 해당 루틴이 불려질 때 메모리에 load 하는 것이다.

memory utilization이 향상된다.

가끔씩 사용되는 많은 양의 코드의 경우 유용하다. e.g) 오류 처리 루틴

운영체제의 특별한 지원없이 프로그램 자체에서 구현을 해야한다. (OS는 라이브러리를 통해 지원 가능)

**보통 최근 컴퓨터에서 필요한 부분만 메모리에 올리는 건 페이징 기법이지, 다이나믹 로딩 기술이 아니다.**



### Overalys

메모리에 프로세스의 부분 중 실제 필요한 정보만을 올리는 것이다.

운영체제의 지원없이 사용자에 의해 구현되어야 한다. - Manual Overlay라고도 부른다.

프로그래밍이 매우 복잡하다.



### Swapping

프로세스를 일시적으로 메모리에서 **통째로** backing store로 쫓아 내는 것이다.

#### Backing store (= swap area)

메모리에서부터 쫓겨나는 프로그램을 저장하는 디스크를 말한다. 

#### Swap In/ Swap Out

통째로 쫓겨나서 backing store로 내려가는 것을 swap out, 반대로 backing store로 쫓겨났던 프로세스가 다시 메모리로 올라오는 것을 swap in이라고 부른다.

일반적으로 중기 스케줄러(swapper)에 의해 swap out 시킬 프로세스를 선정한다.

우선순위가 낮은 프로세스를 swap out시키고 우선순위가 높은 프로세스를 메모리에 올리는 식의 priority-based CPU scheduling algorithm을 사용한다.

Compile time, Load time binding에서는 **반드시 이전의 메모리 주소로 돌아와야 한다**. 따라서, Run time binding이 지원되어야 swapping을 효율적으로 사용할 수 있다.

swap time은 대부분 **transfer time(swap되는 양에 비례하는 시간)**이다.



**프로그램 전체가 swap in/out 되는 것은 디스크 I/O이기 때문에 오버헤드가 굉장히 크다.**



### Dynamic Linking

**Linking을 실행 시간(execution time)까지 미루는 기법**이다.

프로그램을 작성하고 컴파일하고 링크에서 실행파일을 만든다. 여러 군데 존재하던 컴파일된 파일들을 묶어서 하나의 실행파일로 만드는 것을 Linking이라고 한다. 

#### Static  linking

라이브러리가 프로그램의 **실행 파일 코드에 포함**이 된다.

실행 파일의 크기가 커진다.

동일한 라이브러리를 각각의 프로세스가 메모리에 올리므로 **메모리 낭비**가 있다. 

e.g) printf() 함수의 라이브러리 코드

#### Dynamic linking

라이브러리가 컴파일될 때까지 실행 파일 코드에 포함되지 않고, **실행될 때 연결(link)된다.**

라이브러리 호출 부분에서 라이브러리 루틴의 위치를 찾기 위한 stub이라는 작은 코드를 둔다.

라이브러리가 이미 메모리에 있으면 그 루틴의 주소로 가고, 없으면 디스크에서 읽어 온다.





## Allocation of Physical Memory

**물리적인 메모리를 어떻게 관리할 것인가?**



메모리는 일반적으로 두 영역으로 나뉘어 사용된다.

- OS 상주 영역 - interrupt vector와 함께 낮은 주소의 영역을 사용한다.
- 사용자 프로세스 영역 - 높은 주소의 영역을 사용한다.



### 사용자 프로세스 영역의 할당 방법

#### Continuous allocation -> 옛날 방식

각각의 프로세스가 메모리의 연속적인 공간에 적재(load)되도록 하는 방법이다.

- Fixed partition allocation (고정 분할 방식)
- Variable partition allocation (가변 분할 방식)

#### Noncontiguous allocation

하나의 프로세스가 메모리의 여러 영역에 분산되어 올라갈 수 있도록 하는 방법이다.

- Paging
- Segmentation
- Paged Segmentation





## Contiguous Allocation

### 고정 분할(Fixed partition) 방식

물리적 메모리를 미리 몇 개의 영구적인 파티션으로 나눈다.

파티션의 크기가 모두 동일한 방식과 서로 다른 방식이 존재한다.

파티션당 하나의 **프로그램**이 적재된다.

이 방법은 융통성이 없다.

- 동시에 메모리에 적재되는 프로그램의 수가 고정된다.
- 최대 수행 가능 프로그램의 크기가 제한된다.

**Internal fragmentation**과 **External fragmentation**이 발생한다.



#### Internal fragmentation (내부 조각)

**프로그램 크기보다 파티션의 크기가 큰 경우** 발생한다. 

하나의 **파티션 내부**에서 발생하는 사용되지 않는 메모리 조각이다.

파티션이 특정 프로그램에 배정되었지만 사용되지 않는 공간이다.

#### External fragmentation (외부 조각)

**프로그램 크기보다 파티션의 크기가 작은 경우** 발생한다.

아무 프로그램에도 배정되지 않는 **빈 파티션임에도** 프로그램이 올라갈 수 없는 **작은 파티션**을 말한다.



### 가변 분할(Variable partition) 방식

프로그램의 크기를 고려해서 할당한다.

분할의 크기, 개수가 동적으로 변한다.

기술적 관리 기법이 필요하다.

**External fragmentation**이 발생한다.



### Hole

hole이란 프로세스 사이사이에 가용한 메모리 공간을 뜻한다.

다양한 크기의 hole들이 메모리 여러 곳에 흩어져 있다.

프로세스가 도착하면 수용 가능한 hole을 할당받게 된다.

OS는 다음 정보를 유지하고 있어야 한다.

- 할당 공간
- 가용 공간(hole)



### Dynamic Storage-Allocation Problem

**가변 분할 방식**에서 size가 n인 요청을 만족하는 가장 적절한 hole을 찾는 문제이다.

#### First-fit

Size가 n 이상인 것들 중에서 가장 먼저 찾아지는 hole에 할당하는 방식이다.

가장 빠른 방식이다.

#### Best-fit

Size가 n 이상인 가장 작은 hole을 찾아서 할당하는 방식이다.

Hole들의 리스트가 크기순으로 정렬되지 않은 경우 모든 hole을 탐색해야 한다.

비슷한 크기의 hole에 많이 할당되면 다른 프로세스들에 할당될 수 없는 많은 수의 아주 작은 hole들이 발생하게 되는 단점이 있다.

#### Worst-fit

가장 큰 hole에 할당하는 방식이다.

Best-fit과 마찬가지로 모든 리스트를 탐색해야 한다.

상대적으로 아주 큰 hole들이 생성된다.



**First-fit과 Best-fit이 Worst-fit보다 속도와 공간 이용률 측면에서 효과적인 것으로 알려져 있다. (실험적인 결과)**



### Compaction

**External fragmentation** 문제를 해결하는 방법 중 하나이다.

사용중인 메모리 영역을 한군데로 몰고 hole들을 다른 한 곳으로 몰아 큰 block(hole)을 만드는 방법이다.

매우 많은 비용이(**Overhead**) 든다.

프로세스의 주소가 Run time에 동적으로 재배치가 가능한 경우에만 수행될 수 있다.





## Noncontiguous Allocation

