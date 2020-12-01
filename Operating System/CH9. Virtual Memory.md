# Virtual Memory

멀티 프로그래밍을 위해서는 여러 개의 프로세스들을 동시에 메모리에 올려두어야 한다. 가상 메모리 기법은 **프로세스 전체가 메모리 내에 올라오지 않더라도 실행이 가능하도록 하는 기법**으로, 말 그대로 없는 것을 있는 것처럼 보이게 하는 기술이다. 

하나의 프로그램은 실질적으로 사용하는 메모리가 그리 많지 않다. 따라서 프로그램을 실행할 때, 프로그램 전체를 메모리에 올리지 않고 일부분만 올림으로써 더 많은 프로그램을 메모리에 올릴 수 있는 것이다. -> 결국 이 역시도 리소스를 최대한 쥐어짜내기 위함이다.



물리적인 메모리의 주소 변환은 운영체제가 관여하지 않는다.

하지만 virtual memory 부분은 운영체제가 전적으로 관여한다.



이 챕터에서 다루는 내용은 Paging 기법을 사용한다고 가정한다.

실제로도 대부분의 시스템은 Paging 기법을 사용하고 있다.



## Demand Paging

실제로 필요할 때 페이지를 메모리에 올리는 기법이다.

Paging 기법은 프로그램이 실행될 때 그 프로세스를 구성하는 주소 공간의 페이지를 한꺼번에 메모리에 올리는 것이 아니라, Demand Paging 기법을 사용해서 요청된 페이지만 메모리에 적재하는 것이다.

Damand Paging 기법을 사용함으로써,

- **I/O 양의 감소** - 프로그램에서 빈번히 사용되는 공간은 지극히 제한적이다. 좋은 SW는 특별한 예외 상황에 대한 대비를 해 놓는데, 그런 잘 사용되지 않는 부분을 올리지 않고 필요한 부분만 올림으로써 I/O 양을 줄일 수 있다.
- **Memory 사용량 감소**
- **빠른 응답 시간**
- **더 많은 사용자 수용**

와 같은 이점이 있다.



#### Valid/invalid bit의 사용

Invalid - 사용되지 않는 주소 영역인 경우나 페이지가 물리적 메모리에 없는 경우를 의미한다.

처음에는 모든 page entry가 invalid로 초기화되어 있다.

주소 변환 시에 invalid bit이 set 되어 있으면 **page fault** 가 발생한다.





## Page Fault

invalid page를 접근하면 MMU가 trap을 발생시키게 되고 (page fault trap), CPU가 운영체제에게로 넘어간다. **(Kernel mode)**

Kernel mode에서 page fault handler(프로그램)가 호출된다. 

다음과 같은 순서로 page fault를 처리한다.

1. 잘못된 요청(e.g bad address, protection violation)인 경우에는 abort 한다.
2. 빈 페이지 프레임을 얻는다. (없으면 뺏어온다: replace)
3. 해당 페이지를 디스크에서 메모리로 읽어온다.
   1. disk I/O가 끝나기까지 이 프로세스는 CPU를 preempt 당한다.(block 상태로)
   2. Disk read가 끝나면 page tables entry에 기록하고, valid bit를 갱신한다.
   3. ready queue에 프로세스를 넣는다.
4. 이 프로세스가 CPU를 잡고 다시 running 상태가 된다.
5. 중단되었던 인스트럭션을 재개한다.



![image-20200923174454828](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FcXipvr%2FbtqJsjV7x6F%2FarcIQcGBHcGSczkBkkJAp1%2Fimg.png)





## Free frame이 없는 경우

빈 프레임이 없는 경우, 기존에 있는 페이지를 쫓아내야(victim) 한다. -> Page replacement

이 때는 곧바로 사용되지 않을 페이지를 쫓아내는 것이 좋다.

동일한 페이지가 여러 번 메모리에서 쫓겨났다가 다시 로드된다면 오버헤드가 클 것이다.



### Replacement Algorithm

쫓아낼 페이지 프레임을 결정하는 알고리즘이다. 

**page-fault rate를 최소화**하는 것이 목표이다.



주어진 **page reference string**에 대해 page fault를 얼마나 내는지를 계산해서 알고리즘의 성능을 평가한다.

reference string의 예

`1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5`



![image-20200923175645535](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbtWEpE%2FbtqJju5VJAQ%2FEibhbo9ilICPSS0fIE3C50%2Fimg.png)







**그럼, 어떤 알고리즘이 가장 좋은 알고리즘일까?**

## Page Replacement Algorithm



### Optimal Algorithm

이론적으로 가장 **최적의 알고리즘**이다.

가장 먼 미래에 참조되는 page를 victim으로 선정한다.

미래에 참조될 페이지를 모두 알고 있다는 가정을 하기 때문에 어떤 프레임을 쫓아내야 할지 알 수 있는 것이다.

따라서 실제 시스템에서는 사용될 수 **없는** 알고리즘이다.

다만, 다른 알고리즘의 성능에 대한 **upper bound**를 제공해준다.

![image-20200923182405667](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbdfFwT%2FbtqJn1vP2wC%2F4B4kGqy8KYsnn0YdySZ5hK%2Fimg.png)

> 9 page faults. -> 이보다 적게 page fault가 날 수 없다.





실제로 사용하는 알고리즘은 미래를 알 수 없기 때문에, **과거의 정보를 바탕**으로 victim을 선정한다.



### FIFO(First In First Out) Algorithm

가장 단순한 알고리즘으로, **먼저 들어온 것을 먼저 내쫓는** 알고리즘이다.

![image-20200923202145347](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FcgzwxL%2FbtqJrxHgY1G%2F3dtKHmqTf4dB8O3VZwZzsk%2Fimg.png)

> 15 page faults

* Belady's Anomaly

  페이지 프레임 수를 늘렸는데 page fault 수가 증가하는 경우가 있다.

  e.g) `1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5`





### LRU(Least Recently Used) Algorithm

가장 덜 최근에 사용된 프레임을 victim으로 선정하는 알고리즘이다.

즉, **사용한지(참조된지) 가장 오래된 프레임**을 쫓아낸다.

![image-20200923202238875](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbqVw55%2FbtqJofmRLSr%2FRJjLf7I4h3mGzFEo6a6Q11%2Fimg.png)

> 12 page faults



### LFU(Least Frequently Used) Algorithm

**참조 횟수(reference count)가 가장 적은페이지 프레임을** victim으로 선정하는 알고리즘이다. 참조 횟수가 적은 페이지가 뒤에도 참조될 가능성이 낮을 것이라는 가정을 둔 알고리즘이다.

최저 잠조 횟수인 페이지가 여러 개인 경우에는

- LFU 알고리즘 자체에서는 여러 페이지들 중 임의의 페이지를 선정한다.
- 성능 향상을 위해서 가장 오래 전에 참조된 페이지를 선정할 수도 있다.



### MFU(Most Frequently Used) Algorithm

LFU 알고리즘과 반대로 **참조 횟수가 가장 많은 페이지 프레임**을 victim으로 선정하는 알고리즘이다. 참조가 많이 된 페이지가 앞으로는 참조될 가능성이 낮을 것이라는 가정을 둔 알고리즘이다.



**LFU, MFU와 같이 Counting에 기반한 두 알고리즘은 Optimal Algorithm의 성능에 제대로 근사하지 못하기 때문에, 잘 쓰이지 않는다.**



### LRU & LFU 구현

#### LRU

Linked List로 구현할 수 있다.

참조되는 페이지를 가장 밑으로 보내주면 된다.

#### LFU

참조 횟수를 비교해주어야 하므로 리스트로 구현하면  최악의 경우 O(n) 이 걸린다.

따라서 Heap을 이용해서 구현한다. Heap을 이용하면 O(log n) 으로 줄일 수 있다.

![image-20200923205402026](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fo7j00%2FbtqJjvRtBEe%2FlVoJvXm8fFV1PZCPdW6ePk%2Fimg.png)





### * 다양한 캐싱 환경

**캐싱 기법**이란 한정된 빠른 공간(=Cache)에 요청된 데이터를 저장해 두었다가 후속 요청 시 캐시로부터 직접 서비스하는 방식이다.

paging system 외에도 cache memory, buffer caching, web caching 등 다양한 분야에서 사용된다.



#### 캐시 운영의 시간 제약

교체 알고리즘에서 삭제할 항목을 결정하는 일에 지나치게 많은 시간이 걸리는 경우 실제 시스템에서 사용할 수 없다.

대부분의 캐싱 환경에서는 O(1) ~ O(log n) 정도까지 허용한다.



**그렇다면, Paging System에서 LRU, LFU와 같은 알고리즘을 사용할 수 있을까?**

운영체제는 page table entry의 값이 valid인 경우에 주소 변환을 통해서 메모리에 접근한다.

하지만 invalid인 경우에는 trap(page fault)이 발생하게 되고, 이 때 운영체제가 CPU를 잡게 된다(Kernel mode).

그렇기 때문에 운영체제가 CPU를 잡은 시점에서 가장 오래 전에 참조된 페이지, 참조 횟수가 가장 적은 페이지에 대한 정보가 **없다**.

**그래서 Clock Algorithm 이란 알고리즘을 사용한다.**





## Clock Algorithm

LRU의 근사(approximation) 알고리즘 이다.

Second chance Algorithm, NUR(Not Used Recently), NRU(Not Recently Used) 라고도 불린다.

자료구조로 **Circular list**를 사용하기 때문에 시계와 비슷하다고 해서 clock algorithm이란 이름이 붙었다.

**Reference bit**를 사용해서 교체 대상 페이지를 선정한다. 

reference bit는 OS에 의해 초기에 0으로 초기화되고, 해당 페이지가 참조되었을 때 하드웨어에 의해 1로 변경된다.

![image-20200923221049955](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fc7dpXv%2FbtqJsvWMXBJ%2FKphfBpEEYHIWgW7FGRgk11%2Fimg.png)

해당 페이지가 참조되면 그 페이지의 reference bit을 1로 set해줌으로써 참조가 되었다는 것을 표시해준다. 이는 운영체제가 아닌 하드웨어가 담당하는 일이다.

운영체제가 victim을 찾을 때, reference bit를 참고한다.

reference bit가 1이면 0으로 바꾸고 다음을 조사한다. -> 한 번 더 기회를 주는 것

reference bit가 0이라는 것은 운영체제가 모든 페이지를 탐색하는 동안 한 번도 그 페이지가 참조되지 않았다는 의미이다. 그렇기 때문에 reference bit가 0인 페이지를 victim으로 선정한다.



### Clock Algorithm의 개선

**modified bit(= dirty bit)**을 함께 사용하는 방법이다.

modified bit은 **해당 페이지가 수정이 되었는지를 표시하는 비트**이다.

- 0: 메모리에 올라온 후 수정되지 않은 페이지를 의미한다. 이미 backing store의 값과 동일하기 때문에 backing store에 새로 쓸 필요 없이 물리적 메모리에서 쫓아내는 작업만 수행한다.

- 1: 메모리에 올라온 이후 한 번 이상 내용을 수정한 페이지이다. backing store에 수정사항을 반영하고 메모리에서 쫓아낸다.

따라서 reference bit의 값이 0이고 modified bit의 값이 0인 것과 1인 것 두가지가 존재할 경우에는 0인 페이지를 victim으로 선정하는 것이 좋다.



결과적으로, 

- reference bit = 1 : 최근에 참조된 페이지
- modified bit = 1 : 최근에 변경된 페이지 (I/O를 동반하는 페이지)

라고 할 수 있다.



따라서 (R, M) 이 있을 때, **(1, 1) -> (1, 0) -> (0, 1) -> (0, 0)** 순으로 victim으로 정하기 좋다.





## Page Frame의 Allocation

이때까지의 페이지 교체 알고리즘은 어떤 프로세스에 해당하는 페이지인지 고려하지 않고 victim을 선정했다.

실제로는 프로그램이 원할히 실행되려면 일련의 페이지들이 같이 메모리에 적재되어야 더 효율적이다.



Allocation problem: 각 프로세스에 얼마만큼의 페이지 프레임을 할당할 것인가?



#### Allocation의 필요성

메모리 참조 명령어 수행 시 명령어, 데이터 등 여러 페이지를 동시에 참조하는 경우가 있다. 즉, 프로그램마다 할당되어야 하는 페이지의 수가 있는 것이다.

또한, loop를 구성하는 페이지들은 한꺼번에 allocate 되는 것이 유리하다. 최소한의 allocation이 없으면 매 loop마다 page fault가 발생하기 때문이다.

이러한 이유로 **적절한 Allocation이 필요**하다.



#### Allocation Scheme

- Equal allocation: 모든 프로세스에 똑같은 개수 할당
- Proportional allocation: 프로세스 크기에 비례하여 할당
- Priority allocation: 프로세스의 우선 순위에 따라 다르게 할당



### Global vs. Local Replacement

#### Global replacement

replace 시 다른 프로세스에 할당된 프레임을 빼앗아 올 수 있다.

프로세스별 할당량을 조절하는 또 다른 방법이다.

FIFO, LRU, LFU 등의 알고리즘을 global replacement로 사용시에 해당한다.





#### Local replacement

자신(프로세스)에게 할당된 프레임 내에서만 replacement가 일어난다.

FIFO, LRU, LFU 등의 알고리즘을 프로세스 별로 운영시에 해당한다.





### Thrashing

#### Thrashing이란

Thrashing이란 **프로세스의 원활한 수행에 필요한 최소한의 페이지 프레임 수를 할당받지 못한 경우**를 말한다.

![image-20200923225042835](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fbyhqct%2FbtqJpgMzzWl%2F3F4PKKYJdyF6Zes7FR7RxK%2Fimg.png)



실행중인 프로그램의 개수가 많아질수록,

전체적으로 Page fault rate가 높아지고 CPU utilization이 낮아진다.

CPU utilization이 낮아지면 운영체제는 MPD(MultiProgramming Degree)를 높여야 한다고 판단하게 된다.

그럼 또 다른 프로세스가 시스템에 추가되고, 프로세스 당 할당된 프레임의 수는 더욱 감소한다.

프로세스는 페이지의 swap in/swap out으로 매우 바빠지고, CPU utilization은 점점 나빠진다.

결과적으로 Low Throughput을 야기시킨다.



Thrashing을 방지하기 위해 **Working-St Model, PFF Algorithm**이 존재한다.





### Working-Set Model

프로세스는 특정 시간 동안 일정 장소만을 집중적으로 참조한다. 이를 **레퍼런스의 locality**라고 한다.

집중적으로 참조되는 해당 페이지들의 집합을 **locality set**이라고 한다.

e.g) loop를 구성하는 페이지



Locality에 기반하여 프로세스가 일정 시간동안 원활하게 수행되기 위해 한꺼번에 메모리에 올라와 있어야 하는 페이지들의 집합을 **Working Set** 이라고 정의한다.

Working-Set Model에서는 프로세스의 working-set 전체가 메모리에 올라와 있어야 수행되고, 그렇지 않을 경우 모든 프레임을 반납한 후 swap out(suspend state)시킨다.

이는 Thrashing을 방지할 수 있고, Multiprogramming degree를 결정한다.



**그럼 Working-Set은 어떻게 결정할까?**

미래에 어떤 페이지들이 메모리에 올라가있는 것이 좋은지는 알 수 없다. 따라서 마찬가지로 과거의 정보를 통해 결정한다.

Working set window(dt)를 통해 알아낼 수 있다.



e.g) window의 크기가 10인 경우

![image-20200923230212720](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fbtu4wV%2FbtqJpijjCHn%2FAs7PlDD7JHvzKZElsleKH1%2Fimg.png)

첫번째 window의 경우, {1,2,5,6,7} 총 다섯 개의 페이지가 이 프로그램의 Working-set이다. 이 프로그램에게 다섯 개의 페이지를 할당할 수 있으면 이를 올리고, 그렇지 않으면 모두 swap out시키고 이 프로그램을 suspend 상태로 변경한다.





### Page-Fault Frequency(PFF) Scheme

Working-Set Model과 마찬가지로 Multiprogramming degree를 조절하면서 Thrashing을 방지하는 방법이다.

![image-20200923230544550](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FctC6Fg%2FbtqJsbK0FdY%2FTRuKQ7dyI0Iswhr0AAMWkk%2Fimg.png)

page-fault rate의 상한값과 하한값을 둔다.

- page-fault rate가 상한값을 넘으면 프레임을 더 할당한다.
- page-fault rate가 하한값 이하면 할당 프레임 수를 줄인다.

빈 프레임이 없으면 일부 프로세스를 swap out 시킨다.