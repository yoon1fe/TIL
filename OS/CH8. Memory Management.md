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

- **OS 상주 영역** - interrupt vector와 함께 낮은 주소의 영역을 사용한다.
- **사용자 프로세스 영역** - 높은 주소의 영역을 사용한다.



### 사용자 프로세스 영역의 할당 방법

#### Contiguous allocation -> 옛날 방식

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

하나의 프로세스가 메모리의 여러 영역에 분산된어 올라갈 수 있도록 하는 방법이다.



### Paging

프로그램의 virtual memory를 **일정한 크기로 잘라서(page)** 이 페이지 단위로 physical memory에 올리거나 backing store에 내리는 기법이다. 

이 때, Physical memory도 페이지 단위만큼 잘라놓는다. 이를 페이지 프레임(frame)이라고 한다.

페이지 프레임이 있으면 hole이 생기지 않는다. **대신,**  주소 변환을 페이지 별로 해야 하기 때문에 Address binding이 복잡해진다.

주소 변환은 page table을 사용하여 logical address를 physical address으로 변환한다.

External fragmentation은 발생하지 않는다.

Internal fragmentation은 발생할 수 있다. (page frame의 크기 > 마지막 page의 크기)



### Segmentation

프로그램의 주소 공간(virtual memory)을 의미있는 단위로 자르는 기법이다.

크게 code, data, stack segment로 자른다.  세그먼트는 의미 단위이기 때문에 이보다 더 잘게 자를 수도 있다. 예를 들어 함수 별로 세그먼트로 자를 수도 있다.

크기가 균일하지 않기 때문에 연속 할당방식에서 발생했던 문제점(Dynamic Storage-Allocation Problem)이 동일하게 발생할 수 있다.







## Paging

Process의 virtual memory를 동일한 크기의 page단위로 나눈다.

Virtual memory의 내용은 page 단위로 불연속적으로 저장된다.

일부는 backing storage에, 일부는 physical memory에 저장될 수 있다.



Physical memory를 동일한 크기의 frame으로 나누고, logical memory를 (frame과 )동일한 크기의 page로 나눈다. 

주소 변환은 page table을 사용하여 logical address를 physical address으로 변환한다.

External fragmentation은 발생하지 않는다.

**Internal fragmentation**은 발생할 수 있다. **(page frame의 크기 > 마지막 page의 크기)**



### Paging 예

![image-20200822170453331](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2F5Myrx%2FbtqG9AS8YBf%2FP75YWe2HM1aSW99SnYgXk1%2Fimg.png)

주소 변환을 위해 page table을 활용한다.

logical address가 테이블의 인덱스(entry)를 가리키고,

테이블의 value 값으로 physical address의 frame number 를 갖는다.



### Address Translation Architecture

![image-20200822170633905](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsKgjc%2FbtqG62bK9g4%2Fc4aOzFDoxFAHacTB5tbj91%2Fimg.png)



CPU가 다루는 logical address는

- **page number(p)**와
- **page offset(d)**로 나뉜다.

page 번호를 통해 page table에서 frame number를 가져오고,

offset을 통해 해당하는 frame에서 얼만큼 떨어져있는지, 즉 내부에서 위치가 어딘지를 알 수 있다.

------

보통 p = 20, d = 12 이다.

밑에 다시 한 번 언급하겠지만, 정리하는 겸 써본당.

32bit 주소 체계 기준 보통 페이지의 크기를 **4KB**로 나눈다. 즉, 한 페이지 내부에서 위치를 찾으려면 2^12만큼의 정보를 갖고 있어야 하기 때문에 **12bit**가 필요한 것이다.

마찬가지로 32bit 주소 체계에서의 페이지 테이블의 엔트리 하나의 크기는 32bit, 즉 4B이다. 총 4GB만큼의 정보를 갖고 있어야 하기 때문이다. 그리고 페이지 테이블 하나의 엔트리의 개수는 4GB(메모리의 크기)/4KB(페이지의 크기) = 1M개가 된다. 1M = 2^20이므로, p는 20이 필요하다.



**그럼 page table은 어디에 위치하고 있을까?**

기초적인 MMU는 레지스터 두 개를 이용해서 주소를 변환했다.

프로그램 하나당 매우 많은 수의 페이지로 나뉠 수 있고, 그렇게 되면 table의 크기도 매우 커질 것이다. 또한, 프로그램마다 각각 별개의 page table을 갖고 있어야 한다. CPU 내부에 레지스터를 두어 page table 정보를 저장하는 것은 현실적으로 불가능하다.

따라서 page table은 **Main Memory**에 상주한다.

기존의 두 개의 레지스터(base, limit)는 다음과 같은 역할을 하게 된다.

- Page-Table Base Register(PTBR) - page table의 시작 위치를 가리킨다.
- Page-Table length Register(PTLR) - 테이블의 크기를 보관한다.

메모리에 접근하려면 page table에 접근해야 하고, 이 page table 또한 메모리에 있기 때문에 **결국 모든 메모리 접근 연산에는 2번의 memory access가 필요**하다.

속도 향상을 위해 별도의 하드웨어를 사용한다.

**Translation Look-aside Buffer(TLB)(= associative register)** 라는 일종의 캐시를 사용한다.

메인 메모리와 CPU 사이에 존재한다.

#### Paging Hardware with TLB

![image-20200822170650104](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbOidyj%2FbtqG3EQFbK7%2F8DYdvGmSNKHQjXhC77QZKk%2Fimg.png)

TLB는 주소 변환을 위한 캐시 메모리인 셈이다.

page table에서 빈번히 참조되는 일부 엔트리를 캐싱하고 있다.

그래서 page table에 접근하기 전에 TLB에 저장이 되어 있는지 먼저 확인하게 된다.

단, page table의 모든 내용을 담고 있지 않기 때문에 TLB에는 page number와 frame number를 가져야 한다.

또한 TLB에서 해당하는 page table의 엔트리를 찾으려면 O(n) 타임이 걸리게 된다. 따라서 parallel search가 가능한 Associative register를 활용해 탐색한다.

page table과 마찬가지로 TLB도 프로세스마다 다른 정보가 들어 있어야 하기 때문에, context switch 때 flush가 된다.



### Two-Level Page Table

> **참고**
>
> 32bit 주소 체계의 컴퓨터에서 인식할 수 있는 주소 공간은 2^32(4GB)만큼이다. 따라서 RAM이 4GB보다 큰 경우는 인식하지 못하는 것!
>
> **이 말을 반대로 말하면 2^n 각각을 구분하기 위해서는 n bit가 필요하다는 말도 된다!**

현대의 컴퓨터는 address space 체계가 매우 크다.

32bit 주소 체계에서 인식할 수 있는 주소 공간은 4GB이다. 페이지의 크기를 4KB로 나누었다면 약 100만개의 페이지가 생기게 되고, 이를 위한 1M 개의 page table entry가 필요할 것이다. 그리고, 각 page entry는 4byte인데**(32bit 주소 체계니깐)**, 그러면 프로세스당 4MB의 page table의 필요하게 된다.

하지만, 대부분의 프로그램은 4GB의 주소 공간 중 지극히 일부분만 사용하므로 page table 공간이 심하게 낭비된다.



따라서 page table 자체를 또 하나의 page로 보는 방식을 사용했다.

![image-20200822170717357](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FSTo02%2FbtqG6Cj3dQI%2F2iRfPDKf4LUDIPL2ZBFVU1%2Fimg.png)



Two-Level Paging 기법에서는 inner page table과 outer page table 두 가지의 테이블이 존재한다.



#### Two-Level Paging의 예

logical address (page size가 4K인 32bit 체계에서)의 구성은

- 20bit의 **page number** 와,

- 12bit의 **page offset** 으로 이루어져 있다.

  **Why?** 페이지의 크기는 4KB(4byte x 1K)이다. 즉, 2의 12제곱 바이트. 

  **메모리는 바이트 단위로 주소가 매겨지기 때문에**, 페이지 안에 어디에 위치해있나**(몇 번째 떨어져있는 바이트인지)**를 확인하기 위해서 총 12bit가 필요한 것!

Two-Level Paging 기법에서는 page table 자체가 page로 구성되기 때문에, page number는 다음과 같이 나뉜다.

(각 **page table entry가 4B**이므로, entry는 총 1024(1K)개가 존재한다. 이를 구분하기 위해 10bit가 필요)

- 10bit의 **page number**
- 10bit의 **page offset**



따라서, logical address는 다음과 같다.

![image-20200822171255902](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FIKq8A%2FbtqG6fh5Wnf%2FPXNAInTq6GZbKu0sKCUQd1%2Fimg.png)



#### 2단계 페이징에서의 주소 변환 scheme

![image-20200822171348132](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fb2rfjj%2FbtqG68CQWgg%2F24VpK2lXLHg3yelR9gzYfk%2Fimg.png)





그런데, 2단계 페이징 기법은 여전히 안쪽 페이지 테이블의 엔트리는 100만개가 필요하다.

오히려 바깥쪽 페이지 테이블이 하나 더 필요하기 때문에 1단계 페이징 기법보다 공간적으로나 시간적으로나 손해다.

-> 사용되지 않는 주소 공간에 대한 outer page table의 엔트리 값은 **NULL 값**을 갖는다.





### Multilevel Paging and Performance

주소 공간이 더 커게 되면 다단계 페이지 테이블이 필요해진다.

각 단계의 페이지 테이블들은 메모리에 존재하기 때문에, logical address의 physical address 변환에 더 많은 메모리 액세스가 필요하다.

-> 이를 TLB를 통해 메모리 접근 시간을 줄일 수 있다.



#### 4단계 페이지 테이블을 사용하는 경우

> Q. 메모리 접근 시간이 100ns, TLB 접근 시간이 20ns이고, 
>
> TLB hit ratio가 98%라면 effective memory access time은 얼마일까?

0.98 * (100 + 20) * 0.02 * (100 * 4 + 100 + 20) = 128ns. 결과적으로 주소 변환을 위해서는 28ns 만 소요된다.





### Page Protection

페이지 테이블에는 사실 frame number를 저장하는 비트말고 여러 비트가 있다. 

- frame number
- valid-invalid bit
- protection bit

![image-20200822183000441](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FchPkRU%2FbtqG2HfHeUA%2FkzfOCMaTaPbE9kuVYHef9k%2Fimg.png)

페이지 테이블은 인덱스를 통해 접근해야 하기 때문에 사용되지 않는 영역에 대해서도 엔트리가 생성되어야 한다.

위의 그림을 보면 6, 7번 엔트리는 사용하지 않기 때문에 frame number에 0이 들어가 있다. 하지만 이게 0번째 프레임을 가리키는건지 의미없는 값인지를 확인해주기 위해서 사용하지 않는 엔트리에는 invalid로 표시한다.

즉, valid로 표시되어 있단 것은 그 페이지가 **메모리에 올라와 있다**는 의미이다.

invalid로 표시되어 있으면 해당 주소의 **프레임에 유효한 내용이 없음**을 뜻한다.

* 프로세스가 그 주소 부분을 사용하지 않는 경우
* 해당 페이지가 메모리에 올라와 있지 않고 swap area에 있는 경우



**Protection bit** 는 페이지에 대한 접근(연산) 권한을 가진다. (read / write / read-only)

프로세스마다 각각의 페이지 테이블을 갖고 있기 때문에 프로세스간의 Protection을 의미하는 것이 아니다.

code 영역의 경우 사용자로부터 수정을 막기 위해 read-only 권한을 부여하고,

data 영역이나 stack 영역은 수정할 수 있기 때문에 read / write 권한을 부여하는 식이다.



**PTLR (Page Table Length Register)** - 페이지 테이블의 사이즈를 저장하는 레지스터이다. 페이지 테이블의 크기를 각각의 프로세스가 쓰는 만큼 줄여서 다른 프로세스의 페이지에 접근하는 것을 방지한다.



### Inverted Page Table

페이지 테이블은 공간에 대한 오버헤드가 매우 크다.

모든 프로세스 별로 logical address에 대응되는 모든 페이지에 대해 page table entry가 존재하고,

대응하는 페이지가 메모리에 있든 없든 페이지 테이블에는 엔트리로서 존재하기 때문이다.



기존의 페이지를 통한 주소 변환을 반대로 생각한 개념이 바로 Inverted page table 이다.

물리적 메모리의 frame 개수만큼을 담은 페이지 테이블을 갖는다. 따라서 **실제 메모리 크기만큼의 페이지 테이블만 있으면 된다.**

logical address에 어느 프로세스가 사용하는 것인지 명시해주기 위해 pid 정보를 갖고 있어야 한다.

![image-20200822184438580](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fbp7L7q%2FbtqG6gH6Qa6%2FhOWkpDQwA6wRR92skml3k0%2Fimg.png)

기존의 페이징 기법에서는 페이지 번호를 갖고 프레임 번호를 찾았는데, 

Inverted page table 기법은 프레임 번호를 갖고 몇 번째 페이지에 있는지를 찾는다.



이 방식은 공간적인 오버헤드를 줄일 수 있는 대신, 시간적 오버헤드가 있다. 페이지 테이블 전체를 탐색해야 하기 때문이다. TLB 방식처럼 associative register를 사용해서 탐색해볼 수도 있겠다. 하지만 너무 비싸다.





### Shared Page

여러 개의 프로그램들이 똑같은 코드를 쓸 때, 코드가 들어있는 페이지의 중복을 없애보자.

동일한 내용이 들어있는 페이지(Shared code, Re-entrant code)를 모두 메모리에 올리는 것이 아니라 하나만 올리는 방식이다.

이러한 Shared-code가 있는 페이지는 반드시 read-only로 설정한다.

Shared-code는 모든 프로세스의 logical address space 에서 동일한 위치에 있어야 한다.

![image-20200822185424510](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbhVoDg%2FbtqG4bUVPmk%2FQubHd6luHBiImH9k2kBnfK%2Fimg.png)







## Segmentation

프로그램에 있는 여러 의미 단위를 각각 세그먼트로 구성한다.

크게는 프로그램 전체를 하나의 세그먼트로 정의할 수도 있고, 작게는 프로그램을 구성하는 함수 하나 하나를 세그먼트로 정의할 수도 있다.

일반적으로는 code, data, stack 부분이 하나씩의 세그먼트로 정의된다.



### Segmentation Architecture

세그멘테이션에서의 주소 변환은 페이징 기법과 비슷한 면이 있다.

Logical address는 **<segment-number,  offset>** 으로 구성된다. 여기서 offset은 세그먼트 안에서의 위치를 나타낸다. 세그먼트 별로 서로 다른 physical memory 위치에 올라가 있기 때문에 세그먼트 별로 주소 변환을 해야 하므로 **Segment table**을 두고 있다.

주소 변환에 사용되는 기존의 **두 레지스터(base, limit)**는 다음과 같이 사용된다.

- Segment-table base register(STBR) - physical memory에서의 세그먼트 테이블의 위치

- Segment-table length register(STLR) - 프로그램이 사용하는 세그먼트의 수



### Segmentation Hardware

![image-20200823213315418](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fb42uLw%2FbtqG6e4362y%2FcOU6ISMFolsnmISQinl7fK%2Fimg.png)



trap: 원래 할당된 세그먼트의 범위를 벗어나서 액세스하는 경우이다. Segmentation fault ?



#### 단점 - Allocation

세그멘테이션 기법은 **External fragmentation**이 발생한다. 세그먼트의 길이가 모두 동일하지 않기 때문에 가변 분할 방식에서의 동일한 문제점들이 발생하는 것이다. 따라서 first bit / best fit 과 같은 방법을 사용한다.



#### 장점

세그먼트는 의미 단위로 쪼개기 때문에 **공유**와 **보안**에 있어서 페이징 기법보다 훨씬 효과적이다.

- Protection 
  - 각 세그먼트 별로 protection bit가 존재해서 의미 단위 별로 read/write와 같은 권한을 줄 수 있다.
- Sharing
  - 마찬가지로 의미 단위인 세그먼트를 공유하는 것이 훨씬 효과적이다.



### Example of Segmentation

![image-20200823214519973](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FFK2Xf%2FbtqG50zpqBQ%2FwKazJrYB9gPd2mnqeiIPI0%2Fimg.png)





### Paged Segmentation (Segmentation with Paging)

세그먼트 한 개가 여러 개의 페이지로 구성된다.

기존의 세그멘테이션 기법(pure segmentation)과 다르게, segment-table entry가 세그먼트의 base address를 갖고 있는 것이 아니라 세그먼트를 구성하는 page table의 base address를 갖고 있다.

즉, **선 세그멘테이션 후 페이징** 이다.