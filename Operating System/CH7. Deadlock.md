# Deadlock (교착상태)

데드락이란 일련의 프로세스들이 서로가 가진 자원을 기다리며 block된 상태이다.

이때 말하는 자원(Resource)는 하드웨어, 소프트웨어 등을 포함하는 개념이다. 물리적인 것 뿐만 아니라 개념적인 것도 포함한다.

```
e.g) I/O device, CPU cycle, memory space, semaphore, PID, 포트 번호, IP 주소, lock 등
```

그리고 프로세스가 자원을 사용하는 절차는 요청(Request), 할당(Allocate), 사용(Use), 반납(Release)과 같은 네 단계를 거친다.

## Deadlock 발생의 4가지 조건

데드락은 다음과 같은 네가지 경우에 반드시 발생한다.

### Mutual Exclusion (상호 배제)

매 순간 하나의 프로세스만이 자원을 사용할 수 있다.

### No Preemption (비선점)

프로세스는 자원을 스스로 내어놓을 뿐, 강제로 빼앗기지 않는다.

### Hold and Wait (보유 대기)

자원을 가진 프로세스가 다른 자원을 기다릴 때 보유 자원을 놓지 않고 계속 가지고 있어야 한다.

### Circular Wait (순환 대기)

자원을 기다리는 프로세스간에 사이클이 형성되어야 한다.

프로세스들이 꼬리를 물고 서로 가진 자원을 기다리는 상황이다.

##  

## Resource Allocation Graph (자원 할당 그래프)

프로세스와 자원을 정점(vertex)로 나타내고, 자원의 요청과 할당을 화살표로 나타낸 그래프이다.



![img](https://blog.kakaocdn.net/dn/bblGRK/btqGJwjqPxY/e799WXr1QAERH7WtzxTEe1/img.png)



그래프에 **cycle이 존재하지 않으면 Deadlock이 아니다.**

위의 그래프에서는 P3이 R3을 사용하고 반납하면 P2가 R3을 할당받아 사용하고 P2가 종료되면 P1도 모든 자원을 할당받아 사용할 수 있기 때문이다.

그래프에 cycle이 있으면, 데드락이 있을 수도 있고 없을 수도 있다.

- 모든 자원당 인스턴스가 한개밖에 없으면 데드락이다.

- 자원 내에 인스턴스가 여러 개가 있다면 데드락이 있을 수도 있고 없을 수도 있다.

  e.g)



![img](https://blog.kakaocdn.net/dn/F3gQN/btqGGDR2R3P/ib8WkIthSLyBT6TKgcNw31/img.png)



cycle이 존재하지만 P2나 P4가 자원을 release하면 모든 프로세스가 자원을 할당받을 수 있다.

 

##  

## Deadlock 처리 방법

차례대로 데드락을 처리하는 약한 방법이라고 할 수 있다.

### Deadlock Prevention

자원 할당 시 데드락의 4가지 필요 조건 중 어느 하나가 만족되지 않도록 하는 것이다.

### Deadlock Avoidance

자원 요청에 대한 부가적인 정보를 이용해서 데드락의 가능성이 없는 경우에만 자원을 할당한다.

시스템 state가 원래 state로 돌아올 수 있는 경우에만 자원을 할당한다.

### Deadlock Detection and Recovery

데드락 발생은 허용하되, 그에 대한 detection 루틴을 두어 데드락 발견시 recovery한다.

### Deadlock Ignorance

데드락을 시스템이 책임지지 않는 방식이다.

데드락은 현실적으로 잘 일어나지 않는다. 위의 방법들 자체가 계속 어떤 프로그램 등이 돌고 있어야 하고, 이는 CPU를 잡아 먹는 것이다. 따라서 위의 방법들에 대한 오버헤드가 너무 크기 때문에 현대의 대부분의 운영체제는 데드락을 무시하는 방법을 채택한다.

##  

##  

## Deadlock Prevention

위에서 말한 데드락의 필요 조건중 어느 하나를 만족하지 않도록 하는 방법이다.

### Mutual Exclusion

자원을 공유 가능하도록(sharable) 만드는 방법이다. 이렇게 만드는 것 자체가 어렵기 때문에 mutual exclusion 조건을 만족하지 않도록 만들 수 없는 경우가 많다.

### Hold and Wait

프로세스가 자원을 요청할 때 다른 어떤 자원도 가지고 있지 않도록 하는 방법이다.

1. 프로세스 시작 시 모든 필요한 자원을 할당받게 하는 방법 - hold를 없애는 방법

   -> resource utilization이 낮아진다.

2. 자원이 필요한 경우 보유 자원을 모두 놓고 다시 요청하는 방법 - hold가 없는 상태에서만 프로세스를 할당받을 수 있도록 하는 방법

   -> starvation이 있을 수 있다.

### No Preemption

프로세스가 자원 할당을 요청했을 때, 요청한 자원을 잡을 수 없는 경우 그 자원을 선점하는 방법이다.

현재까지의 작업이 모두 무효화될 수 있다는 문제점이 있다. 따라서 state를 쉽게 저장하고 복구할 수 있는 자원에서 사용해야 한다. e.g) CPU, memory

### Circular Wait

모든 자원 유형에 할당 순서를 정하여 정해진 순서대로만 자원을 할당하는 방법이다.

낮은 번호의 자원을 획득해야만 높은 번호의 자원을 획득할 수 있도록 하는 것이다.

Deadlock Prevention은 데드락을 원천적으로 방지할 수 있지만,

-> Utilization 저하, Throughput 감소, Starvation 문제 등이 발생할 수 있다.

##  

##  

## Deadlock Avoidance

Deadlock Prevention과 마찬가지로 데드락을 미연에 방지하는 방법이다.

자원을 요청할 때, 부가적인 정보를 이용해서 데드락의 가능성이 없는 경우(safe state)에만 자원을 할당해준다.

가장 단순하고 일반적인 모델은 프로세스들이 필요로 하는 각 자원별 최대 사용량을 미리 알고 있다고 가정하고 데드락을 피하는 방법이다.

> #### Safe state
>
> 시스템 state가 원래의 state로 돌아올 수 있는 sequence가 존재하는 경우이다.
>
> #### Unsafe state
>
> 데드락으로 들어갈 가능성이 있는 경우이다. 모든 unsafe state가 데드락으로 들어가진 않는다
>
> ![img](https://blog.kakaocdn.net/dn/bj42Or/btqGIpx6aWt/8JwKMiAkZk1q1dwPDTWXp0/img.png)

 

**Deadlock Avoidance는 시스템이 Unsafe state로 들어가지 않는 것을 보장하는 방법이다.**

이 방법에는 두가지의 알고리즘이 있다.

### Resource Allocation Graph Algorithm

**리소스당 하나의 인스턴스**를 가지고 있는 경우에 사용하는 알고리즘이다.

**Claim edge**를 사용한다.

Claim edge란 프로세스가 자원을 미래에 요청할 수도 있음을 의미하는 간선이다. 점선으로 표시한다.

프로세스가 해당 자원을 요청할 때 request edge(실선)으로 바뀐다.

리소스가 release되면 assignment edge는 다시 claim edge로 바뀐다.

이 때, request edge가 assignment edge로 변경될 때 (점선을 포함하여) cycle이 생기지 않는 경우에만 요청 자원을 할당한다.



![img](https://blog.kakaocdn.net/dn/nuVPq/btqGG9QwjY4/HQuSQmoIPPoHbob54pgDAK/img.png)



###  

###  

### Banker's Algorithm

**리소스당 여러 개의 인스턴스**를 가지고 있는 경우에 사용하는 알고리즘이다.

자료 구조

- **Available** - 각 타입 별 사용 가능한 자원의 수
- **Max** - 각 프로세스 별 요청할 수 있는 자원의 최대 개수
- **Allocation** - 현재 할당되어 있는 자원의 수
- **Need** - 각 프로세스 별 남아있는 필요한 자원의 수 (Max - Allocation)



![img](https://blog.kakaocdn.net/dn/b5ptI8/btqGHNlUXxM/CkRbnKEsmbXUzeOkjVRYd0/img.png)



자원을 요청하는 프로세스의 Max 값을 Available의 양이 커버할 수 있다면 그 프로세스의 요청에 응답하는 방식이다.



![img](https://blog.kakaocdn.net/dn/b1Dxjy/btqGJvx3w48/iHVgRhyGe5BzMwivvl1p41/img.png)



 

이처럼 sequence **<P1, P3, P4, P2, P0>** 가 존재하므로 시스템은 safe state라고 할 수 있다.

**Need의 자원의 양이 언제 필요한지 모르는 문제점**이 있다.

 

 

다음 두가지 방법은 데드락은 빈번히 발생하지 않기 때문에 데드락을 방지하기 위해서 비효율적인 방법을 쓰는 대신 데드락이 발생하도록 그냥 두는 방법이다.

### Deadlock Detection and Recovery

#### Detection

Resource Allocation Graph를 간소화한 Wait-for Graph로 cycle을 탐색할 수도 있다.

#### Recovery

1. Process Termination

   데드락에 연루된 모든 프로세스들을 죽이는 방법이다.

2. Resource Preemption

   데드락에 연루된 프로세스들을 victim process로 선정해 하나씩 죽여보는 방법이다.

   -> 동일한 프로세스가 계속해서 victim으로 선정되는 경우 Starvation 문제가 발생할 수 있다.

###  

### Deadlock Ignorance

데드락이 발생하지 않는다고 생각하고 아무런 조치를 취하지 않는 방법이다.

데드락은 매우 드물게 발생하므로 데드락에 대한 조치 자체가 더 큰 오버헤드일 수 있다.

따라서 만약 시스템에 데드락이 발생한 경우, 시스템이 비정상적으로 작동하는 것을 사람이 느끼고 직접 프로세스를 죽이는 등의 방법으로 대처할 수 있다.

UNIX, Windows 등 대부분의 범용 운영체제들이 채택하고 있는 방법이다.