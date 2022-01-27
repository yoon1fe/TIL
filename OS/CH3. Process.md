# CH3. Process

> ### Process is a `program in execution`

프로세스란 현재 실행중인 프로그램을 말한다.

그리고 프로세스의 현재 상태를 나타내는 모든 것을 프로세스의 문맥(Context) 이라고 한다.

![img](https://blog.kakaocdn.net/dn/deuXYE/btqFZ6t5soX/wwjCWxAGDLkNOiLKyX5jH1/img.png)

###  

### 프로세스의 문맥(Context)이란?

프로그램이 무엇을 어떻게 실행했는지, 현재 상태가 어떤지를 정확하게 나타내기 위해 사용되는 개념이다. 특정 시점을 놓고 봤을 때, 이 프로세스가 어디까지 수행을 했는지를 **규명**하는데 필요하다.

 프로세스가 시작되면 그 프로세스만의 독자적인 주소공간을 가지는데, `(code` `data` `stack)` 프로세스가 CPU를 잡게 되면 CPU 내부의 `Program Counter`가 `code`의 어느 공간을 가리키고 있고, 인스트럭션을 읽어와서 연산을 수행한다.

 

 현재 시점의 프로세스의 문맥을 나타내기 위해서는 PC가 어디를 가리키고 있는가와 메모리에 어떤 내용을 담고 있는가를 알아야 한다.

1. CPU 수행 상태를 나타내는 하드웨어 문맥
   - Program Counter`이 가리키고 있는 코드`
   - 각종 register`에 저장되어 있는 값`
2. 프로세스의 주소 공간
   - code, data, stack`에 저장되어 있는 값`
3. 프로세스 관련 커널 자료 구조
   - PCB(Process Control Block) - 프로세스 하나마다 운영체제가 그 프로세스를 관리하기 위해 생성한다.
   - Kernel stack - 프로그램이 System call을 하면 PC가 커널 주소 공간을 가리켜 커널에서 함수가 호출된다. 이 때, 어떤 프로세스가 시스템 콜을 했는지 알기 위해 커널 주소 공간의 `stack` 부분에 시스템 콜을 한 프로세스의 커널 스택 값을 저장한다.

#### 프로세스의 문맥이 필요한 이유

 현대의 컴퓨터는 `time sharing, multitasking`, 즉 프로세스들이 번갈아 실행되기 때문에 하나의 프로세스가 CPU를 잡아서 작업을 처리하다가 다른 프로세스에게 CPU가 넘어간다. 이 때 프로세스의 현재 상태 `(문맥)`을 백업해놓지 않으면 다음에 CPU를 다시 잡았을 때 작업을 이어서 수행하지 못하게 되기 때문에 항상 프로세스의 문맥을 정확히 파악하고 있어야 한다.

### 프로세스의 상태(Process State)

#### Running

CPU를 잡고 인스트럭션을 수행중인 상태이다.

#### Ready

CPU를 기다리는 상태를 말한다. 핵심적인 부분은 메모리에 코드가 올라와 있어, CPU만 잡게 되면 인스트럭션을 바로 수행할 수 있는 상태이다. 보통 `Ready` 상태에 있는 프로세스들이 번갈아가면서 CPU를 점유하도록 해서 `Time sharing`을 구현하고 있다.

#### Blocked(wait, sleep)

CPU를 주어도 당장 인스트럭션을 수행할 수 없는 상태이다. 프로세스 자신이 요청한 event(예: I/O)가 즉시 만족되지 않아 이를 기다리는 상태이다. 예를 들어, 디스크에서 file을 읽어와야 인스트럭션을 수행할 수 있는 프로세스를 말한다.

##### New

프로세스가 생성중인 상태

##### Terminated

수행(execution)이 끝난 상태



![img](https://blog.kakaocdn.net/dn/b1EaPy/btqF2RBQBfO/zfXmn6p0FNOT7hTI2nypwk/img.png)

![img](https://blog.kakaocdn.net/dn/bLkiKH/btqFZ4XpbgF/iISwIIn7sUoQ4ajnZnlmR0/img.png)



각 큐들는 커널의 주소 공간에 존재한다.

### Process Control Block(PCB)



![img](https://blog.kakaocdn.net/dn/HJHuW/btqF1bhbDs4/FZhUphFzdEHU7WV99FssJk/img.png)



 운영체제가 프로세스를 관리하기 위해 프로세스당 유지하는 정보이다. 다음과 같은 구성 요소들을 구조체로 담고 있다. **PCB는 커널 스택의 처음에 위치**한다.

1. OS가 관리하기 위해 사용하는 정보
   - Process state - ready/blocked ...
   - Process ID
   - scheduling informatin, priority - 프로세스에게 CPU를 주기 위한 우선순위나 스케줄링 정보. 반드시 먼저 들어온 순대로 처리하는 것이 아니라, 프로세스에 우선순위를 두어 우선순위대로 처리한다.
2. CPU 수행 관련 하드웨어 값
   - Program Counter, registers
3. 메모리 관련
   - code, data, stack의 위치 정보
4. 파일 관련
   - Open file descriptors ...

### 문맥 교환(Context Switch)

CPU를 한 프로세스에서 다른 프로세스로 넘겨주는 과정을 말한다.

CPU가 다른 프로세스에게 넘어갈 때 운영체제는

1. CPU를 내어주는 프로세스의 상태를 그 프로세스의 PCB에 저장하고,
2. CPU를 새롭게 얻는 프로세스의 상태를 PCB에서 읽어온다.

![img](https://blog.kakaocdn.net/dn/6o2JJ/btqF0ZnLA1l/x0bMnqIkeRvvV3PS6av2wk/img.png)

###  

#### * 사용자 프로세스로부터 CPU가 운영체제로 넘어가는 것`(System call이나Interrupt 발생시)`은 `Context switch`라고 부르지 않는다.

`Context switch`는 한 사용자 프로세스에서 다른 사용자 프로세스로 넘어가는 것을 의미한다.

1. `System call`이나 `Interrupt` 후 `kernel mode`로 들어갔다가 다시 **해당 프로세스**로 넘어가는 것(다시 `user mode`로 복귀)갈 때는 `Context switch`가 일어나지 않고,
2. `I/O 요청 System call`이나 `Timer Interrupt` 발생 후에 CPU가 **다른 프로세스**로 넘어갈 때는 `Conext switch`가 일어난다.

(1)의 경우에도 CPU 수행 정보 등 context의 일부를 PCB에 저장해야 하지만, context switch를 하는 (2)의 경우 그 `overhead`가 훨씬 크다. 다른 프로세스로 넘어갈 때 cache memory를 모두 비워줘야`(flush)` 하기 때문이다.

 

### 프로세스를 스케줄링하기 위한 큐

#### Job queue

현재 시스템 내에 있는 모든 프로세스의 집합이다.

#### Ready queue

현재 메모리 내에 있으면서 CPU를 잡아서 실행되기를 기다리는 프로세스의 집합이다.

#### Device queues

I/O device의 처리를 기다리는 프로세스의 집합이다.

#### 프로세스들은 각 큐들을 오가며 수행된다.



![img](https://blog.kakaocdn.net/dn/bcxW2x/btqF0QEAFxl/ooUtEDIkr3OVYFu8FcvXz1/img.png)

![img](https://blog.kakaocdn.net/dn/lhbVl/btqF1SOM3nr/KXVaj0GgUYKXVAF2oeJck1/img.png)

### 스케줄러 (Scheduler)

#### Long-term scheduler(장기 스케줄러 or job scheduler)

- 시작 프로세스 중 어떤 것들을 `ready queue`로 보낼지 결정한다.
- 프로세스에 `memory(및 각종 자원)`을 주는 문제 `= admit` `new > ready`
- `degree of Multiprogramming(메모리에 여러 프로그램이 동시에 올라가는 것)`을 제어한다. = 메모리에 올라가는 프로그램의 수를 제어하는 역할이다.
- `time sharing system`에는 보통 장기 스케줄러가 없다. 그냥 곧바로 메모리에 올라가 `ready`상태가 된다.

#### Short-term scheduler(단기 스케줄러 or CPU 스케줄러)

- 어떤 프로세스를 다음 번에 `running`시킬지 결정한다.
- 프로세스에 `CPU`를 주는 문제
- 충분히 빨라야 한다. (millisecond 단위)

#### Medium-term scheduler(중기 스케줄러 or Swapper)

- 메모리에 여러 프로그램들이 동시에 너무 많이 올라가 있으면 여유 공간 마련을 위해 프로세스를 통째로 메모리에서 디스크로 쫓아낸다.
- 프로세스에게서 `memory`를 뺏는 문제
- `degree of Multiprogramming`을 제어한다.

현재의 시스템은 장기 스케줄러없이 메모리에 일단 다 올리고, 중기 스케줄러가 메모리에 올라가는 프로그램의 수`(degree of Multiprogramming)`를 제어한다.

 

 

### 프로세스의 상태 (+)

#### Suspended(stopped)

외부적인 이유로 프로세스의 수행이 정지된 상태이다. 프로세스는 통째로 디스크에 `swap out`된다. 중기 스케줄러에 의해 메모리에서 쫓겨난 상태이다.

- **Blocked - 자신이 요청한 event가 만족되면 Ready**
- **Suspended - 외부에서 resume해 주어야 Active**



![img](https://blog.kakaocdn.net/dn/buVpyM/btqF1o1Ln6A/TfaRkGVmL3y5y28fEfM9Uk/img.png)



### Thread

> #### `A thread(or lightweight process) is a basic unit of CPU utilization`

쓰레드는 프로세스 내부에 있는 `CPU 수행 단위`이다.

#### 각각의 쓰레드는

- Program Counter
- register set
- stack space

로 이루어져 있다.

#### 쓰레드가 동료 쓰레드와 공유하는 부분(=task)

- code section
- data section
- OS resources



![img](https://blog.kakaocdn.net/dn/bwz597/btqF4y4GPUn/LuAK2Hm9zzD6yvIaKeCbiK/img.png)



같은 일을 하는 여러 개의 프로세스를 띄어놓으면 프로세스마다 각자의 주소 공간이 만들어져 메모리 낭비가 된다. 이런 경우에 주소 공간을 하나만 띄어놓고 각자 다른 부분의 코드를 실행할 수 있게 하는것이 쓰레드의 개념이다.

 프로그램의 실행 위치를 가리키는 Program Counter를 여러 개를 두어, 프로그램 하나에 CPU 수행 단위만 여러 개를 두고 있는 것을 쓰레드라고 부른다.

 프로세스 하나 안에 여러 개의 쓰레드가 있기 때문에 메모리 주소 공간과 프로세스 상태, 프로세스가 사용하는 자원들을 공유하고, `CPU 수행에 관련된 정보(PC, registers 등)들`만 쓰레드 각자가 갖고 있다.

 

전통적인 개념의 `heavyweight process`는 하나의 쓰레드를 가지고 있는 `task`로 볼 수 있다.

 



![img](https://blog.kakaocdn.net/dn/benqdY/btqF6CZlqvP/DCSSLVOgM3uyb64IYEQoq1/img.png)



###  

### 쓰레드의 장점

#### Responsiveness - 응답성

따른 응답성을 가진다. 다중 쓰레드로 구성된 태스크 구조에서는 하나의 서버 쓰레드가 blocked (waiting) 상태인 동안에도 동일한 태스크 내의 다른 쓰레드가 실행 (running)되어 빠른 처리를 할 수 있다.

e.g) multi-threaded web - 한 쓰레드가 blocked되어도(e.g 네트워크), 다른 쓰레드가 동작한다.(e.g 디스플레이 출력)

#### Resource Sharing - 자원 공유

똑같은 일을 하는 프로그램을 별도의 프로세스들로 사용하지 않고, 하나의 프로세스만 만들고, 그 안에 CPU 수행 단위만 여러 개를 만들어 `code`, `data`, `프로세스의 resources`를 공유함으로써 자원을 효율적으로 사용할 수 있다. 따라서, IPC를 할 필요가 없다.

#### Economy - 경제성

응답성과 조금 다른 개념이다. 프로세스를 하나 만드는 것 자체가 오버헤드가 상당한 것에 비해, 쓰레드를 하나 생성하는 것은 오버헤드가 그리 크지 않다`(creating)`.

`context switching`또한 오버헤드가 크다. 이에 반해 프로세스 내부에서 쓰레드간에 `CPU switching`은 매우 간단하다.

e.g) Solaris OS의 경우, 위 두 가지 overhead가 각각 30배, 5배 차이난다.

#### Utilization of Multi Processor Architectures

여러 개의 CPU가 있는 환경에서 쓰레드를 두었을 때 얻을 수 있는 장점이다.

각각의 쓰레드가 서로 다른 CPU(Processor)에서 병렬적으로 일을 할 수 있어 효율적으로 실행이 가능하다.

e.g) 굉장히 큰 행렬을 곱하는 경우 등..

###  

### 쓰레드의 구현

어떤 쓰레드는 커널의 지원을 받고, 어떤 쓰레드는 library의 지원을 받는다. 각각의 쓰레드를 `Kernel Threads`, `User Threads`라고 부른다. 여러 운영체제, 시스템들이 나오면서 이 둘의 구분은 희미해졌다.

 

`Kernel Thread`는 쓰레드가 여러 개가 있다는 사실을 운영체제, 즉 커널이 알고 있다. 하나의 쓰레드에 대해서 다른 쓰레드로 CPU가 넘어가는 것도 커널이 CPU 스케줄링하듯이 넘겨준다.

 

`User Thread`는 운영체제는 쓰레드가 여러 개가 있다는 사실을 모른다. 유저 프로그램이 라이브러리의 지원을 받아서 스스로 여러 개의 쓰레드를 관리한다. 그렇기 때문에 구현 상의 제약점들이 있을 수 있다.