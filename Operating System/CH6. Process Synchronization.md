# CH6. Process Synchronization

여러 주체가 하나의 데이터를 동시에 접근하려는 경우를  `Race Condition(경쟁 상태)`이라고 한다. 데이터의

 `Race condition`이 있으면 데이터의 최종 연산 결과는 마지막에 그 데이터를 다룬 프로세스에 따라 달라진다. 즉, 결과가 접근의 순서에 의존적이다.

-> 결과가 안정적이지 않다. 

따라서 `Race condition`을 막기 위해서는 `Concurrent process`는 동기화`(Synchronize)`되어야 한다.



멀티 프로세서 시스템에서 

- 공유 메모리를 사용하는 프로세스들 간

- 커널 내부 데이터를 접근하는 루틴들 간에 

  e.g) 커널 모드 수행 중 인터럽트로 커널 모드가 다른 루틴을 수행 시

`Race condition`이 발생할 수 있다.

구체적으로 다음과 같은 세 가지 경우를 볼 수 있다.

**Kernel 수행 중 인터럽트 발생 시**

![image-20200809133452820](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fbo3aop%2FbtqGnHmyW8J%2FY96FSFaiMxLzrZxpFzScD1%2Fimg.png)

e.g) 커널 모드가 수행되는 도중에 인터럽트가 발생해 인터럽트를 처리하는 경우

커널에서 `Count`변수를 ++하려고 `load`한 뒤 인터럽트가 발생해서 `Interrupt handler`가 `Count--`를 하면 어떻게 될까? 인터럽트가 수행된 후 커널에서는 이미 `load`한 값을 처리하기 때문에 `Count--`는 반영되지 않는다.

* 양쪽 다 커널 코드이므로 `kernel address space`를 공유한다.

`Race condition` 해결 방법

> 인터럽트가 들어와도 인스트럭션이 처리되기 전까지는 인터럽트를 처리하지 않도록 한다.



**프로세스가 `System call`을 하여 `kernel mode`로 수행중일 때 `Context switch` 가 일어나는 경우**

![image-20200809134132891](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fd6mtac%2FbtqGkWdtSj0%2FC6nnnbLc44EUGW9gCqGxdK%2Fimg.png)

`Race condition` 해결 방법

> 이 경우에는 프로세스가 커널 모드로 수행 중일 때는 `CPU`를 뺏기지않도록`(preempt)`하지 않는다. 커널 모드에서 사용자 모드로 복귀할 때 `preempt` 한다.



**`Multiprocessor` 에서 `Shared memory` 내의 `kernel data`에 접근할 경우**

![image-20200809134808101](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FxDteq%2FbtqGtewplfD%2FiivPtLkAC2LkwJKdxZx9gk%2Fimg.png)

`Race condition` 해결 방법

> 1) 한 번에 하나의 `CPU`만이 커널에 들어갈 수 있게 한다.
>
> 2) 커널 내부에 있는 각 공유 데이터에 접근할 때마다 그 데이터에 대한 `lock`/`unlock`을 한다.



#### `Process Synchronization` 문제

공유 데이터`(shared data)`의 동시 접근`(concurrent access)`은 데이터의 불일치 문제`(inconsistency)`를 발생시킬 수 있다.

일관성 유지를 위해서는 협력 프로세스간의 실행 순서를 정해주는 메커니즘이 필요하다.



#### `Critical-Section Problem`

`Critical-Section` 이란 **공유 데이터에 접근하는 코드**를 말한다.

각 프로세스의 `code segment`에는 공유 데이터를 접근하는 코드인 `Critical-Section`이 존재한다.



#### `General structure of a typical process`

```pseudocode
do{
	entry section
	critical section
	exit section
	reamainder section
} while(true);
```





### `Critical section Problem` 해결을 위한 충족 조건

#### Mutual Exclusion(상호 배제)

프로세스가 `ciritical section` 부분을 수행중이면 다른 모든 프로세스들은 그들의 `critical section`에 들어가면 안된다.

#### Progress(진행)

아무도 `critical section`에 있지 않은 상태에서 `critical section`에 들어가고자 하는 프로세스가 있으면 `critical section`에 들어가게 해주어야 한다.

>  아무도 `critical section`에 들어가지 못하는 경우가 없어야 한다.

#### Bounded Waiting(유한 대기)

프로세스가 `critical section`에 들어가려고 요청한 후부터 그 요청이 허용될 때까지 다른 프로세스들이 `critical section`에 들어가는 횟수에 한계가 있어야 한다.

> `starvaton`에 대한 `guarantee`가 있어야 한다.

#### 가정

- 모든 프로세스의 수행 속도는 0보다 크다.
- 프로세스들 간의 상대적인 수행 속도는 가정하지 않는다.



### Algorithm 1 - Strict Alternation

**사용하는 변수**

> `int turn;`

![image-20200809140857392](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FdOgWpA%2FbtqGnIlsGOC%2Fe4uUud1TDlhk1XoaJaiF0k%2Fimg.png)

`turn`이 i이면 `프로세스 i`가 `critical section`에 들어간다. `프로세스 i`가 `critical section`에서 빠져 나오면 `turn`을 j로 바꿔줌과 동시에 `프로세스 j`가 `critical section`에 들어가게 된다.

#### 이 방식은 `Mutual Exclusion`은 충족한다. 하지만 `Progress`를 충족하지 못한다. 

이 알고리즘은 반드시 프로세스가 교대로 `critical section`에 들어간다. 

e.g) `프로세스 i`가 빈번히 `critical section`에 들어가려 하고 `프로세스 j`는 `critical section`에 들어가지 않으려 하면 `프로세스 i`는 영원히 `critical section`에 들어가지 못하는 상황이 발생한다.



### Algorithm 2

**사용하는 변수**

> `int turn;`
>
> `boolean flag[2];`  - 초기에 모두 `false`

![image-20200809142459861](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FSjB2V%2FbtqGsMGPgSM%2F5Gsy4BcAHeLoTG5gyhrFR0%2Fimg.png)

`프로세스 i`가 `critical section`에 들어가고자 할 때 `flag[i]`를 `true`로 바꾼다.

`프로세스 j`가 `critical section`에 들어가 있는지 체크한다.

`critical section`에서 빠져 나오면 `flag[i]`를 `false`로 바꿔준다.

#### 이 경우에도 `Mutual Exclusion`은 충족하지만 `Progress` 를 만족하지 못한다.

e.g) `flag`가 모두 `true`인 경우 아무 프로세스도 `critical section`에 들어가지 못할 수도 있다.



### Algorithm 3 - Peterson's Algorithm

**사용하는 변수**

> `int turn;`
>
> ``boolean flag[2];`  - 초기에 모두 `false`

![image-20200809142506507](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbdqfWz%2FbtqGspkD5ub%2FOxk1pBfzH0DXdKhndWVUL1%2Fimg.png)

`critical section`에 들어가기 전에 **`flag`와 `turn`을 모두 확인**한다.

이 경우에는 세가지 조건을 모두 만족시킨다.

#### `Mutual Exclusion`

`프로세스 i`는 **`flag[j] == false || turn == i`** 인 경우에만 `critical section`에 들어갈 수 있다.

만약 두 프로세스가 동시에 `critical section`에 들어간다면,

**`flag[i] ==flag[j]==true(모순) && turn==i and turn==j(모순)`**

#### `Progress`

`flag[j] = false`라면 `프로세스 j`는 `critical section`에 들어갈 마음이 없는 것이다. 따라서 `프로세스 i`에서 `while`문에 걸리지 않는다.

#### `Bounded Waiting`

프로세스가 한 번만 `critical section`에 들어가면 다음부턴 무조건 `critical section`에 들어갈 수 있다.



#### 문제점 - `Busy Waiting(=sping lock)`

`while`문을 돌면서 기다리기 때문에 계속 `CPU`와 메모리를 쓰면서 기다리게 된다.





### `Synchronization Hardware`

하드웨어적으로 하나의 인스트럭션만 주어지면 이러한 `critical section`문제는 쉽게 해결된다. `Test & Modify`를 `atomic`하게 수행할 수 있도록 지원하면 된다.

`Race condition`은 사실상 데이터를 읽고 쓰는 것을 하나의 인스트럭션으로 처리할 수 없기 때문에 생겼던 것이다.



`Test_and_set(a)`

`a`라는 데이터의 현재값을 읽고, `a`의 값을 1로 바꾸어주는 것을 하나의 인스트럭션으로 처리한다.

단, 이러한 하나의 큰 인스트럭션을 만들어 수행하게 되면 `clock cycle`이 증가한다.





### `Mutex Locks(=Lock)`

프로세스는 `critical section`에 들어갈 때 반드시 `lock`을 획득하고, 나올 때 `lock`을 반납해야 한다.

```pseudocode
do{
	acquire lock
	critical section
	release lock
	remainder section
} while(true);
```

- 장점: 반응속도가 빠르다.
- 단점: CPU가 낭비된다. `(busy waiting)`





### `Semaphore`

앞의 방식들을 추상화시킨 일종의 추상 자료형이다.

크게 두 종류의 세마포어가 있다.

- `Counting semaphore`
  - 도메인이 0 이상인 임의의 정수값
  - 주로 `resource counting`에 사용된다.
- `Binary semaphore(= mutex)`
  - 0 또는 1 값만 가질 수 있는 세마포어
  - 주로 `mutual exclusion (lock/unlock)`에 사용된다.

#### `Semaphore S`

- 정수 자료형 - 여러 개의 프로세스를 처리할 수 있다.

- 아래의 두 가지 `atomic` 연산에 의해서만 접근 가능하다.

  ```pseudocode
  wait(S):	while (S <= 0) do no-op;	//busy wait
  //P(S)		S--;
  		
  signal(S):	
  //V(S)		S++;
  ```

`wait()` 연산은 세마포어를 획득하는 연산이고, 

`signal()` 연산은 세마포어를 반납하는 연산이다.

이 방식도 `busy waiting(=spin lock)`이 있어 비효율적이다.

이에 대한 해결책으로 `Block & Wakeup` 방식의 구현이 있다. `(=sleep lock)`



#### `Block & Wakeup`

공유 데이터를 얻길 기다리는 프로세스 자체를 `Blocked(CPU를 얻을 수 있는 자격 자체가 사라진다.)`하는 방법이다. 

![image-20200809150319859](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2F2R5ex%2FbtqGqX98S63%2FWsDkc5sL4aXUVs0CGbwCwk%2Fimg.png)

`block`과 `wakeup`을 다음과 같이 가정한다.

- `block` - 커널은 `block`을 호출한 프로세스를 `suspend`시킨다. 이 프로세스의 `PCB`를 세마포어에 대한 `wait queue`에 넣는다.
- `wakeup(P)` - `block`된 `프로세스 P`를 `wakeup` 시킨다. 이 프로세스의 `PCB`를 `ready queue`로 옮긴다.

![image-20200809151030906](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FcH1lqP%2FbtqGrz111s3%2F8aEXRp2ZiVcEbHX41yKEk1%2Fimg.png)





![image-20200809150552722](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbPKerx%2FbtqGqoUrHQf%2FtAW3ak6JPDXvpDRu3HK2Zk%2Fimg.png)

- `wait()` - 자원의 여분이 없다면 `block` 상태가 된다.

- `signal()` - 자원을 반납하고, 자원을 기다리는 프로세스가 있다면 그 프로세스를 깨워`(wakeup(P))`준다.



#### `Busy-wait` vs. `Block/wakeup`

`Block/wake up overhead` vs. `Critical section의 길이`

- `critical section`의 길이가 긴 경우 `Block/wakeup`이 적당하다.
- `critical section`의 길이가 매우 짧은 경우 `Block/wakeup`의 오버헤드가 `busy-wait` 오버헤드보다 더 커질 수 있다.
- 일반적으로는 `Block/wakeup` 방식이 더 좋다.