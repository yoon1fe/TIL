# CH5. CPU Scheduling

### CPU and I/O Bursts in Program Execution

모든 프로그램은 실행하면서 거치는 다음과 같은 일련의 path가 존재한다.



![img](https://blog.kakaocdn.net/dn/bF0prX/btqGe9a7Jwt/qi9LrPtbTRj2VFkHsLiqc0/img.png)



프로그램의`path`는 `CPU`를 사용하는 일과 `I/O` 요청에 대한 응답을 기다리는 일을 반복한다.

`CPU`를 연속적으로 사용하면서 인스트럭션을 수행하는 단계를 `CPU Burst`이라고 부르고,

`I/O`를 수행하는 단계를 `I/O Burst`라고 부른다.

'

#### CPU-burst Time의 분포



![img](https://blog.kakaocdn.net/dn/bp6TbO/btqGbjT1Z53/hI17cnFRqsgDIkk1pu2qYk/img.png)



I/O작업이 빈번한 작업(I/O bound job)이 있는 반면, CPU를 많이 사용하는 작업도 있다(CPU bound job).

사용자와 직접적으로 연관이 많은 `interactive job`에게 적절한 `response`를 제공해주어야 한다. 사용자가 너무 오래 기다리지 않도록 스케줄링을 해주어야 하기 때문이다.

 

이처럼 여러 종류의 job(=process)이 섞여 있기 때문에 CPU Scheduling이 필요하다.

 

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

 

 

#### CPU 스케줄링이 필요한 경우

CPU 스케줄링에는 크게 `preemptive(=강제로 빼앗음)`와 `nonpreemptive(=강제로 빼앗지 않고 자진 반납)`의 경우가 있다.

##### preemptive

`timer interrupt` - 할당 시간 만료로 CPU를 강제로 빼앗고 다른 프로세스에게 넘겨주어야 하는 경우

`device controller의 I/O 완료 interrupt` - I/O 작업을 마쳤으므로 해당 프로세스를 `Ready queue`에 대기시켜주어야 하는 경우

##### nonpreemptive

`I/O interrupt` - CPU가 어떤 프로세스에게 할당되어 있다가 I/O 요청이 들어오면 그 프로세스에게 CPU를 넘겨주어야 하는 경우

`Terminate` - 프로세스가 일을 모두 마치고 종료가 되면 CPU를 다른 프로세스에게 넘겨 주어야 하는 경우