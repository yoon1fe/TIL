## Garbage Collection Algorithms

여러 Garbage Collector들이 어떠한 알고리즘들을 채택해왔고 발전해왔는지를 공부해보자!!



그 전에 어떤 객체를 Garbage로 볼 것이냐에 대해 먼저 알아보자. Garbage Collector는 객체를 **Reachable**과 **Unreachable**의 상태로 구분한다. 구분하는 방법은 Root set 과의 관계로 판단한다. Root set으로부터 어떤 식으로든 Reference 관계가 있다면 Reachable Object라고 판단하고, 그렇지 않다면 Unreachable Object라고 판단한다. Root set을 간단하게 설명하면 객체들 간의 참조 사슬의 시작점이라고 생각하면 된다.

Root set은 아래의 세 가지 형태로 나뉜다. 

1. JVM Stack 내의 Local Variable Section과 Operand Stack에서의 참조

   (Java 메소드 내에서 실행하는 지역 변수 또는 파라미터에 의한 참조)

2. JVM Method Area의 Constant pool에서의 참조 (정적 변수에 의한 참조)

3. 아직 메모리에 남아 있는 Native Method로 넘겨진 Object에서의 참조(JNI에 의해 생성된 객체에 대한 참조)



위의 세 가지 형태의 Root set으로부터 이어진 참조 사슬에 포함되어 있으면 Reachable Object이고 그렇지 않은 것은 Unreachable Object이다. Reachability에 대한 더 자세한 내용은 [Java Reference와 GC(Nave D2)](https://d2.naver.com/helloworld/329631) 에서 공부해보자..



#### [Garbage Collection의 기본 알고리즘]

아래는 여러 GC 알고리즘에서 사용되는 기본적인 개념이라고 생각하면 되겠다.

- Reference Counting Algorithm
- Mark-and-Sweep Algorithm
- Mark-and-Compact Algorithm
- Copying Algorithm
- Generational Algorithm



**Reference Counting Algorithm**

Garbage의 Detection에 초점이 맞추어진 초기 알고리즘이다. 각 객체마다 Reference Count를 관리하여 Reference Count가 0이 되면 GC를 수행한다. 

이 방식은 각 객체마다 Reference Count를 변경해 주어야 하기 때문에 그에 대한 관리 비용이 크고, 참조를 많이 하고 있는 객체의 Reference Count가 0이 될 경우 연쇄적으로 GC가 발생할 수 있는 문제가 있다. 또한, circular 참조 구조에서 메모리 누수가 발생할 가능성도 크다.

![Image for post](https://miro.medium.com/max/786/1*FGBQ4XLK57woaihzcrJ1og.png)

위와 같은 구조에서 a로부터 참조가 끊어졌다 해도 다른 노드로부터의 참조가 남아 있기 때문에 Reference Count가 0이 되지 않고, 그로 인해 Garbage 대상이 되지 않아 메모리 누수로 이어지게 된다.



**Mark-and-Sweep Algorithm**

위에서 말한 Reference Counting Algorithm의 단점을 극복하기 위해 나온 알고리즘이다. 이 방식에서의 Garbage Detection이 바로 Root set에서 시작하는 Reference의 관계를 추적한다. 그래서 Tracing Algorithm이라고도 하며, Mark Phase 와 Sweep Phase 로 나뉜다.

Mark Phase에서는 Garbage 대상이 아닌 **살아남아야 할 객체**에 Marking하는 방식으로 수행되며, Markiing을 위해 각 객체의 object hear에 flag나 별도의 bitmap table 등을 사용한다. Mark Phase가 끝나면 곧바로 Sweep Phase에 진입하는데, 이 단계에서는 Marking 정보를 활용하여 Marking 되지 않은 객체를 지우는 작업을 한다. 그리고 Sweep 이 완료되면 살아남은 모든 객체의 Marking 정보를 초기화 한다.

![Image for post](https://miro.medium.com/max/725/1*AHV486VNqwHwBxl6wEb-QA.png)



위 그림에서 Before는 GC 발생 전의 상황이고 이후 Sweep Phase까지 진행된 후에는 Marking 되지 않은 객체들이 지워진다. 이 알고리즘은 Reference 관계를 정확히 파악하고 Reference 관계의 변경 시에 별도의 오버헤드가 없어 성능이 비교적 좋기는 하지만, GC가 수행되는 도중 Mark 작업의 정확성과 Memory Corruption을 방지하기 위해 Heap의 사용이 제한되기 때문에 Suspend 현상이 발생한다.

그리고 그림에서처럼 객체들이 지워진 공간이 fragmentation으로 남게 되어 메모리 할당이 불가능한 상태가 된다.



**Mark-and-Compact Algorithm**

기존 Mark-and-Sweep Algorithm의 Fragmentation이라는 약점을 극복하기 위해 나온 알고리즘이다. Sweep 대신 Compact라는 용어를 사용했지만 Sweep이 사라진 것은 아니고 **Compact Phase** 안에 포함되어 있다.

![Image for post](https://miro.medium.com/max/725/1*L1PoMDWf_Zud71I1ACstIw.png)



기존의 Sweep 과정까지는 동일하고, 이후에 Compact 과정을 더 거치게 된다. 이 Compaction 과정은 운영체제에서의 그것과 같은가 보다. 따라서 마찬가지로 Compaction 과정을 거치면 메모리 공간의 효율을 높일 수 있다. 하지만 Compaction 작업 이후 살아남은 모든 객체들의 Reference를 업데이트하는 작업이 필요하기 때문에 부가적인 오버헤드가 발생한다.



**Copying Algorithm**

Copying Algorithm 또한 Fragmentation 문제를 해결하기 위해 제시된 또 다른 알고리즘이다. 현대의 GC가 채택하고 있는 Generational Algorithm이 이 Copying Algorithm을 발전시킨 형태이다. Copying Algorithm의 기본 아이디어는 Heap을 **Active 영역**과 **InActive 영역**으로 나누어 Active 영역에만 객체를 할당할 수 있게 하고, Active 영역이 꽉 차게 되면 GC를 수행하는 것이다. GC를 수행하면 suspend 상태가 되고 살아남은 객체를 InActive 영역으로 **복사**하는 작업을 수행한다.

Copy 작업을 완료하면 Active 영역에는 Garbage Object만 남게 되고, InActive 영역에는 살아남은객체들만 남게 된다. 이후 Active 영역을 싹 밀어주면 Free memory 상태가 되고, Active 영역과 InActive 영역이 서로 바뀌게 된다. 이를 **Scavenge**라고 하고,  Active 영역과 InActive 영역의 구분은 물리적인 구분이 아니라 논리적인 구분일 뿐이다.

![Image for post](https://miro.medium.com/max/725/1*be2sOK5Bj8VbCCl5cfHuUg.png)



방금 말했듯이 각 영역의 구분은 개념적인 구분이기 때문에 그림처럼 Heap 영역을 반으로 나누어 한 쪽에만 객체를 할당한다고 이해하면 된다. 

복사하는 과정에서, Fragmentation 문제를 해결하기 위해 각 객체들의 Reference를 업데이트하면서 연속된 메모리 공간에 차곡차곡 옮겨준다.

이 Copy Algorithm은 Fragmentation 방지에는 효과적이지만, 전체 Heap 영역의 절반 정도밖에 사용하지 않는다는 공간 활용의 비효율성, suspend 현상, copy에 대한 오버헤드가 존재한다는 단점이 있다.



**Generational Algorithm**

드디어 마지막이다. 마지막으로 소개되는 만큼 가장 좋은 놈이 아닐까? 사실 위에서 말했던 Young Generation, Old Generation이라는 영역으로 나누는 알고리즘이 바로 이 Generational Algorithm이다.



- 대부분의 할당된 객체는 오랫동안 참조되지 않으며, 즉 금방 Garbage(Unreachable) 대상이 된다.
- 오래된 객체에서 젊은 객체로의 참조는 거의 없다.

Generational Algorithm은 전에 말했던 **Weak Generational Hypothesis**라는 가설을 바탕으로 Young Generation과 Old Generation 이라는 영역으로 Heap 영역을 구분 지었다.

![Image for post](https://miro.medium.com/max/500/1*sHR2tQUERqZBz56R8YvBLQ.png)



객체는 가장 먼저 Young Generation에 할당되고 GC가 수행될 때마다 살아남은 객체에 Age를 기록한다. HotSpot JVM에서는 이 Age의 임곗값의 기본값은 31이라고 한다. Object Header에 age를 기록하는 부분이 6비트로 되어 있어서 그렇댄다.. 그냥 몇 번 살아남았는지에 대한 정보이다. 이 age가 특정 임곗값을 넘어가게 되면 Old Generation으로 Copy하는 작업을 수행한다. 이를 Promotion이라고 하며, 대부분의 객체는 Young Generation에서 살다가 Garbage가 되기 때문에 Copy 작업의 횟수를 최소화시킬 수 있다. 또한, Old Generation으로 copy 하며 Compaction 작업이 이루어진다.

우리가 주로 사용하고 있는 HotSpot JVM이 바로 이 Generational Algorithm을 바탕으로 다음과 같이 Generational Heap을 구성하고 있다.

![Image for post](https://miro.medium.com/max/1479/1*JUToosyFqTBACLA3sUD6vw.png)









##### Reference

https://medium.com/@joongwon/jvm-garbage-collection-algorithms-3869b7b0aa6f