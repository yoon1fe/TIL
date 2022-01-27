# CH5. CPU Scheduling

### CPU and I/O Bursts in Program Execution

모든 프로그램은 실행하면서 거치는 다음과 같은 일련의 path가 존재한다.



![img](https://blog.kakaocdn.net/dn/bF0prX/btqGe9a7Jwt/qi9LrPtbTRj2VFkHsLiqc0/img.png)



프로그램의`path`는 `CPU`를 사용하는 일과 `I/O` 요청에 대한 응답을 기다리는 일을 반복한다.

`CPU`를 연속적으로 사용하면서 인스트럭션을 수행하는 단계를 `CPU Burst`이라고 부르고,

`I/O`를 수행하는 단계를 `I/O Burst`라고 부른다.

 

#### CPU-burst Time의 분포



![img](https://blog.kakaocdn.net/dn/bp6TbO/btqGbjT1Z53/hI17cnFRqsgDIkk1pu2qYk/img.png)



I/O작업이 빈번한 작업(I/O bound job)이 있는 반면, CPU를 많이 사용하는 작업도 있다(CPU bound job).

사용자와 직접적으로 연관이 많은 `interactive job`에게 적절한 `response`를 제공해주어야 한다. 사용자가 너무 오래 기다리지 않도록 스케줄링을 해주어야 하기 때문이다.

이처럼 여러 종류의job(=process)이 섞여 있기 때문에CPU Scheduling이 필요하다.

따라서, 프로세스는 그 특성에 따라 다음 두가지로 나뉘게 된다.

- I/O-bound process
  - CPU를 잡고 계산하는 시간보다 I/O에 많은 시간이 필요한 job
  - 짧고 잦은 `CPU burst`
- CPU-bound process
  - 계산 위주의 job
  - 길고 빈번하지 않은 `CPU burst`

####  

#### CPU Scheduler / Dispatcher

`CPU Scheduler`는 `Ready` 상태의 프로세스 중에서 `CPU`를 줄 프로세스를 고른다.

`Dispatcher`는 CPU의 제어권을 `CPU Scheduler`에 의해 선택된 프로세스에게 넘긴다. 이 과정을 `Context Switch(문맥 교환)`이라고 한다.

다만 이들은 하드웨어나 소프트웨어를 지칭하는 것이 아니라, **운영체제 내부에서 이러한 역할을 하는 코드의 일부분**을 말한다.

####  

####  

#### CPU 스케줄링이 필요한 경우

CPU 스케줄링에는 크게 `preemptive(=강제로 빼앗음)`와 `nonpreemptive(=강제로 빼앗지 않고 자진 반납)`의 경우가 있다.

##### preemptive

`timer interrupt` - 할당 시간 만료로 CPU를 강제로 빼앗고 다른 프로세스에게 넘겨주어야 하는 경우

`device controller의 I/O 완료 interrupt` - I/O 작업을 마쳤으므로 해당 프로세스를 `Ready queue`에 대기시켜주어야 하는 경우

##### nonpreemptive

`I/O interrupt` - CPU가 어떤 프로세스에게 할당되어 있다가 I/O 요청이 들어오면 그 프로세스에게 CPU를 넘겨주어야 하는 경우

`Terminate` - 프로세스가 일을 모두 마치고 종료가 되면 CPU를 다른 프로세스에게 넘겨 주어야 하는 경우

 

 

### 스케줄링의 성능 척도(Scheduling Criteria) - Performance Index(=Performance Measure)

####  

#### 1. 시스템 입장에서의 성능 척도 - CPU가 얼마나 일을 많이 하는가

#### CPU utilization(이용률)

전체 시간 중에서 CPU가 놀지 않고 일한 시간의 비율

#### Throughput(처리량)

주어진 시간동안 처리한 작업의 양

 

#### 2. 프로그램(프로세스) 입장에서의 성능 척도 - 작업을 얼마나 빨리 처리하는가

#### Turnaround time(소요 시간, 반환 시간)

CPU를 받아서 모든 작업을 마치고 CPU를 다시 반환하기까지 걸린 시간. 프로세스가 시작되고 종료되는 시간이 아니라, **CPU를 할당받기 위해 `Ready queue` 에 들어온 시간과 프로세스가 종료된 시간의 차다.** `기다리는 시간 + CPU를 사용하는 시간`

#### Waiting time(대기 시간)

CPU를 할당받기 위해 `Ready queue`에서 대기한 시간의 총합

#### Response time(응답 시간)

`Ready queue`에 들어와서 처음 CPU를 얻기까지 걸린 시간

이때 `Waiting time`은 `Response time`을 포함하는 개념이다.

#####  

**시간에 대한 내용을 왜 세분화했을까?**

사용자의 입장에서 뭔가 하나라도 동작하는 것이 마음이 놓인다. 이러한 부분을 측정해 보여주는 시간이 바로 `Response time`인 것이다.

 



### Scheduling Algorithms

####  

#### FCFS (First-Come First-Served)

**요청한 순서대로 처리**하는 단순한 알고리즘이다. `Nonpreemptive scheduling`이며, 비효율적인 알고리즘이다.

`Burst time`이 긴 프로세스가 먼저 도착한다면, 짧은 프로세스들은 무작정 기다릴 수밖에 없다.



![img](https://blog.kakaocdn.net/dn/bODR3e/btqGgWpa9S2/ERx0DLftldbzmkfiXCDAn1/img.png)



`Time-sharing system`에서 `interacitve job`의 응답시간이 길어지기 때문에 좋은 스케줄링 기법이 아니다.

만약 `burst time`이 짧은 순서대로 들어온다면,



![img](https://blog.kakaocdn.net/dn/bnszBh/btqGc8FiwR6/DZbmhETVR7HzywPG5bAFKk/img.png)



FPFS는 프로세스의 순서에 따라 `waiting time`에 상당한 영향을 끼친다.

##### 문제점

- `Convoy effect` 이 발생한다.

  한 프로세스를 기다리기 위해 여러 프로세스들이 떼지어(convoy) 기다리게 되는 현상

####  

#### SJF (Shortest-Job-First)

**`CPU Burst time`이 가장 짧은 프로세스에게 CPU를 제일 먼저 할당**하는 알고리즘이다. 각 프로세스의 다음번 `CPU Burst time`을 스케줄링에 활용한다.

이 방식은 기본적으로 `Nonpreemptive`한 방식이다. 일단 CPU를 잡으면 이번 CPU burst가 완료될 때까지 CPU를 선점당하지 않는다.



![img](https://blog.kakaocdn.net/dn/bOV9hR/btqGgGz5wsz/oESXQljjVAiQrJVPXQpnf0/img.png)



`Preemptive`한 방식의 경우, 현재 수행중인 프로세스의 남은 `burst time`보다 더 짧은 `CPU burst time`을 가진 프로세스가 도착하면 CPU를 빼앗긴다. 이러한 방법을 `SRTF (Shortest-Remaining-Time-First)` 라고도 부른다.

- `SRTF` 방법은 `Optimal`하다. 주어진 프로세스들에 대해 `minimum average waiting time`을 보장한다.



![img](https://blog.kakaocdn.net/dn/bVt6zf/btqGisIClz5/MbgCMa2f4QCbIJvpmZbgYK/img.png)



##### 문제점

1. `Starvation` 문제가 발생한다.

   `CPU burst time`이 긴 프로세스는 영원히 CPU를 할당받을 수 없는 상황`(starvation)`이 발생하게 된다.

2. `CPU burst time`을 알 수 없다. 따라서 과거의 CPU 사용을 보고 예측한다.

##### `CPU burst time` 예측

과거의 `CPU burst time`을 이용해서 추정한다.



![img](https://blog.kakaocdn.net/dn/dPLZ4y/btqGfaIlkz4/9ysie9fZOyb7Zba2ZlRFY1/img.png)



 좀 더 똑똑하게 하려면 history 중요도가 멀어질수록(옛날 정보일수록) 가중치를 아주 급격히 낮추어서 줄 수도 있다.(exponential 하게)

![img](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FcHhbgn%2FbtqN8fQlG8S%2Fm33wUgjfWIIkEinTxmAVD1%2Fimg.png)



####  

####  

#### Priority Scheduling

**우선순위가 제일 높은 프로세스에게 CPU를 할당**하는 알고리즘이다. 정수로 우선순위를 표현하고, 작은 숫자가 우선순위가 높다는 기준을 가진다.

더 높은 우선순위를 가진 프로세스가 새로 들어오면 CPU를 넘겨주느냐/넘겨주지 않느냐에 따라 `preemptive / nonpreemptive`하다.

`SJF`도 일종의 `Priority scheduling`이라고 볼 수 있다.

이 방식도 마찬가지라 `Starvation` 문제를 가지고 있다. 낮은 우선순위의 프로세스는 영원히 실행되지 않을 수 있다. 이러한 문제점을 해결하는 방법으로 `Aging`이라는 기법을 사용한다.

`Aging`이란 우선순위가 낮은 프로세스라도 시간이 흘러감에 따라 우선순위를 높여주는 방식이다.



![img](https://blog.kakaocdn.net/dn/buILCs/btqGgw5emBA/k3Z94IbR5dWyIzAKDV9OaK/img.png)



####  

####  

#### RR (Round Robin)

가장 현대적인 `CPU Scheduling` 방식이다. 각 프로세스는 **동일한 크기의 할당 시간**`(time quantum), 일반적으로 10-100 ms`을 가진다. 할당 시간이 지나면 프로세스는 선점`(preemptive)`당하고 `Ready queue`의 제일 뒤에 가서 다시 줄을 선다.

라운드 로빈의 가장 큰 장점은 **응답 시간`(Response time)`이 짧아진다**는 것이다.

`n`개의 프로세스가 `Ready queue`에 있고 할당 시간이 `q time unit`인 경우, 각 프로세스는 최대 `q time unit`단위로 `CPU` 시간의 `1/n`을 얻는다. **-> 어떤 프로세스도 `(n-1) q time unit`이상 기다리지 않는다.**

`time quantum(q)`를 너무 크게 잡으면 FCFS와 같은 방식이 되고,

`time quantum`이 너무 작아지게 되면 가장 이상적인 라운드 로빈 방식이 되겠지만, `Context switch overhead`가 너무 커져 컴퓨터 성능에 악영향을 끼칠 수 있다.

**따라서 적당한 시간의 `time quantum`을 책정하는 것이 중요하다.**



![img](https://blog.kakaocdn.net/dn/m935D/btqGexDPoYT/0yRpskbn4WLaVqA6feJIEk/img.png)



일반적으로 `SJF`보다 `average turnaround time`이 길지만 `response time`이 더 짧다.

####  

####  

#### Multilevel Queue

`Ready queue`를 여러 개의 `class`로 분할한다. 각각의 `class`마다 다른 스케줄링 알고리즘 사용이 가능하다.

e.g) 두 개의 `queue`

```
foreground (interactive한 job을 추가)` - `Round Robin
background (batch - no human interaction)` - `FCFS
```

 

`interactive`한 프로세스들은 우선순위가 높고, 그렇지 않은 프로세스들은 우선순위를 낮게 지정한다.

각 큐에 대한 스케줄링 또한 필요하다.

- `Fixed priority scheduling`

  `foreground`에 있는 모든 프로세스를 처리하고 `background`에 있는 프로세스에게 CPU를 할당하는 방법이다. 이 경우에는 `starvation` 문제가 발생할 수 있다.

- `Time slice`

  각 큐에 `CPU time`을 적절한 비율로 할당해서 `starvation` 문제를 해결할 수 있다.

  e.g) `foreground`에 80%, `background`에 20% ..



![img](https://blog.kakaocdn.net/dn/dghzRz/btqGewZzx8z/nalywXSj0UpLWQcZdvXtJ1/img.png)



####  

#### Multilevel Feedback Queue

`Multilevel Queue`에서 프로세스가 `queue`들간 이동이 가능한 알고리즘이다. `Aging`을 이와 같은 방식으로 구현할 수 있다. e.g) 오래된 프로세스를 하위 레벨의 큐로 옮김

처음 들어오는 프로세스는 우선순위가 가장 높은 큐에 위치시켜 준다.

우선순위가 높은 큐일수록 `Round Robin` 기법에서 `time quantum`을 짧게 준다.



![img](https://blog.kakaocdn.net/dn/bDbPp7/btqGir30AzK/3kumASxqVhI7qr3jjV5rtk/img.png)



e.g)

1. 프로세스가 `Queue 0`에 들어옴
2. `Q0`에 있는 프로세스가 `Q1`의 프로세스를 `preempt` 함
3. `Q0`의 프로세스가 8ms만에 끝나지 않으면, `Q1`의 끝에 들어감
4. `Q1`의 프로세스가 16ms만에 끝나지 않으면, `Q2`의 끝에 들어감

###  

###  

### Multiple-Processor Scheduling

##### 이때까지 보았던 스케줄링은 CPU가 한 개일 때의 스케줄링 기법이다.

CPU가 여러 개인 경우, 스케줄링은 더욱 더 복잡해진다.

- ```
  Homogeneous processor
  ```

  인 경우

  - `Queue`에 한 줄로 세워서 각 프로세서가 알아서 꺼내가게 할 수 있다.
  - 반드시 특정 프로세서에서 수행되어야 하는 프로세스가 있는 경우에는 문제가 더 복잡해진다.

- ```
  Load sharing
  ```

  - 일부 프로세서에 `job`이 몰리지 않도록 부하를 적절히 공유하는 메커니즘이 필요하다.
  - 별개의 `queue`를 두는 방법 vs. 공동 `queue`를 사용하는 방법

- ```
  Symmentric Multiprocessing (SMP)
  ```

  - 각 프로세서가 각자 알아서 스케줄링을 결정한다.

- ```
  Asymmetric multiprocessing
  ```

  - 하나의 프로세서가 시스템 데이터의 접근과 공유를 책임지고 나머지 프로세서는 거기에 따른다.

####  

#### Real-Time Scheduling

`real time job`이란 데드라인이 있는 `job`이다. 즉, 정해진 시간 내에 반드시 처리해야 하는 `job`이다. 따라서 `CPU scheduling`을 할 때도 `real time job`은 데드라인을 보장해주어야 한다.

- `Hard real-time systems`

  `Hard real-time task`는 정해진 시간안에 반드시 끝내도록 스케줄링해야 한다.

- `Soft real-time computing`

  `Soft real-time task`는 일반 프로세스에 비해 높은 `priority`를 갖도록 해야 한다.

####  

#### Tread Scheduling

- `Local Scheduling`

  `User level thread`의 경우 사용자 수준의 `thread library`에 의해 어떤 `thread`를 스케줄할지 결정한다.

- `Global Scheduling`

  `Kernel level thread`의 경우 일반 프로세스와 마찬가지로 커널의 단기 스케줄러가 어떤 `thread`를 스케줄할지 결정한다.

###  

### Algorithm Evalution

어떤 스케줄링 알고리즘이 좋은지 평가하는 기준

1. `Queueing models`

   이론적인 방법이다. 확률 분포로 주어지는 `arrival rate`와 `service rate`등을 통해 각종 `performance index` 값을 계산한다.

2. `Implementation (구현) & Measurement (성능 측정)`

   실제 시스템에 알고리즘을 구현하여 실제 작업`(workload)`에 대해서 성능을 측정/비교한다.

3. `Simulation (모의 실험)`

   알고리즘을 모의 프로그램으로 작성 후 `trace`를 입력으로 하여 결과를 비교한다.

   `trace`: 시뮬레이션 프로그램에 들어갈 입력 데이터를 의미한다.