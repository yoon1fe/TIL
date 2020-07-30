# CH4. Process Management

### 프로세스의 생성 (Process Creation)

부모 프로세스(Parent process)가 자식 프로세스(Child process)를 생성한다. 하나의 부모 프로세스는 여러 개의 자식 프로세스를 생성할 수 있다. 따라서 프로세스는 트리(계층 구조)를 형성한다. 프로세스는 자원(resources)를 필요로 하는데, 이는 운영체제로부터 받거나 부모 프로세스와 공유하는 형태를 갖는다.

#### 자원의 공유 형태

- 부모와 자식이 모든 자원을 공유하는 모델
- 일부를 공유하는 모델
- 전혀 공유하지 않는 모델 - 일반적인 모델

일반적으로 부모와 자식프로세스는 서로 자원을 경쟁하는 관계가 된다.

```
*Copy-On-Write(COW): Write가 발생했을 때 Copy를 하겠다. -> 내용이 바뀔 때(Write) Copy를 하고, 그 전까지는 부모 프로세스의 자원을 공유하는 것.
```

#### 프로세스의 수행(Execution)

- 부모와 자식은 공존하며 수행되는 모델
- 자식이 종료(terminate)될 때까지 부모가 기다리는(wait) 모델

#### 부모 프로세스가 자식 프로세스를 생성하는 방법

프로세스 생성은 사용자 프로세스인 부모 프로세스가 직접 하는 것이 아니라 시스템 콜`fork()`을 통해서 운영체제에게 자식 프로세스 생성을 요청한다.

부모 프로세스의 주소 공간과 운영체제에 있는 데이터(PCB, resources 등)를 복사하고, 자식 프로세스는 그 공간에 새로운 프로그램을 올린다.

e.g) UNIX에서

- fork() 시스템 콜이 새로운 프로세스를 생성한다.
  - 부모를 그대로 복사 (OS data except PID + binary)
  - 주소 공간 할당
- fork() 다음에 이어지는 exec() 시스템 콜을 통해 새로운 프로그램을 메모리에 올린다.



### 프로세스의 종료(Process Termination)

원칙적으로 부모 프로세스보다 자식 프로세스가 반드시 먼저 종료되도록 한다.

1. 프로세스가 마지막 명령을 수행한 후 운영체제에게 이를 알려준다.`(exit)` - 자발적
   - 자식이 부모에게 `output data`를 보낸다. `(wait 시스템 콜을 통해서)`
   - 프로세스의 각종 자원들이 운영체제에게 반납된다.

2. 부모 프로세스가 자식의 수행을 종료시킨다. `(abort)` - 강제 종료
   - 자식이 할당 자원의 한계치를 넘어섰을 때
   - 자식에게 할당된 태스크가 더 이상 필요하지 않을 때
   - 부모가 종료`(exit)`하는 경우
     - 운영체제는 부모 프로세스가 종료하는 경우 자식이 더 이상 수행되도록 두지 않는다.
     - 단계적인 종료가 이루어진다.



### fork() System call

프로세스는 `fork() system call`에 의해서 생성된다.

```c
int main(){
	int pid;
    pid = fork();
	if(pid == 0)		/* this is child */
		printf("\n Hello, I am child\n");
	else if(pid > 0)	/* this is parent */
		printf("\n Hello, I am parent\n");
}
```

fork()의 return 값

- `Parent process - pid > 0`

- `Child process - pid = 0`



자식 프로세스는 부모 프로세스의 `context`를 그대로 복제하기 때문에 fork(); 이후`(부모 프로세스의 PC가 가리키는 부분)`부터 실행한다.





### exec() System call

프로세스는 `exec() system call`에 의해 다른 프로그램을 실행할 수 있다.

```c
int main(){
	int pid;
    pid = fork();
	if(pid == 0){		/* this is child */
		execlp("/bin/date", "/bin/date", (char *)0);	
		printf("\n Hello, I am child\n");
	}
	else if(pid > 0)	/* this is parent */
		printf("\n Hello, I am parent\n");
}
```

exec()를 만나면 이전 정보를 모두 잊고 새로운 프로그램으로 덮어씌어진다. 

e.g) /bin/date의 main부터 실행한다. 

exec()를 호출하면 이전 상태로 되돌아올 수 없다. 따라서 위의 코드에서 자식 프로세스는 "Hello, I am child"를 출력하지 않는다.





### wait() System call

프로세스를 `blocked`상태로 만드는 시스템 콜이다. 보통 자식 프로세스를 만들고 호출한다. 자식 프로세스가 종료될 때까지 `blocked`상태를 유지한다.

프로세스 A가 wait() 시스템 콜을 호출하면

- 커널은 child가 종료될 때까지 프로세스 A를 sleep시킨다. (`blocked` 상태)
- child process가 종료되면 커널은 프로세스 A를 깨운다. (`ready` 상태)

```c
int main(){
	int  childPID;
    pid = fork();
	if(childPID == 0){
		/* code for child process */
	}
	else{
		
		wait();
	} 	
}
```





### exit() System call

프로세스를 종료시킬 때 호출하는 시스템 콜이다.

#### 프로세스의 종료의 종류

- 자발적 종료
  - 마지막 statement 수행 후 exit() 시스템 콜을 통해 종료
  - 프로그램에 명시적으로 적어주지 않아도 main 함수가 리턴되는 위치에 컴파일러가 알아서 넣는다.
- 비자발적 종료
  1. 부모 프로세스가 자식 프로세스를 강제 종료시키는 경우
     - 자식 프로세스가 한계치를 넘어서는 자원을 요청하는 경우
     - 자식에게 할당된 태스크가 더 이상 필요하지 않는 경우
  2. kill, break를 입력한 경우
  3. 부모 프로세스가 종료하는 경우
     - 부모 프로세스가 종료하기 전에 자식 프로세스들이 먼저 종료된다.



#### 프로세스와 관련된 시스템 콜

| fork()               | exec()            | wait()                    | exit()                                 |
| -------------------- | ----------------- | ------------------------- | -------------------------------------- |
| create a child(copy) | overlay new image | sleep until child is done | frees all the resources, notify parent |





### 프로세스 간 협력

- 원칙적으로 프로세스는 굉장히 독립적이다. (Independent process) 

  프로세스는 각자의 주소 공간을 가지고 수행되므로 원칙적으로 하나의 프로세스는 다른 프로세스의 수행에 영향을 미치지 못한다.

- 경우에 따라서 프로세스 간 협력을 해야 효율적일 수 있다. (Cooperating process)

  프로세스 협력 매커니즘을 통해 하나의 프로세스가 다른 프로세스의 수행에 영향을 미칠 수 있다.

- 프로세스 간 협력 매커니즘(IPC: InterProcess Communication)

  1. `message passing`: **커널**을 통해 메시지를 전달하는 방법
  2. `shared memory`: 서로 다른 프로세스 간에도 일부 주소 공간을 공유하게 하는 매커니즘이다. 물리적인 메모리에 매핑할 때부터 공유하도록 매핑해둔 것이다. 같은 메모리 공간을 공유하기 때문에 이 방법을 사용하는 두 프로세스는 신뢰성이 높아야 한다.

  * `thread`: 쓰레드는 사실상 하나의 프로세스이므로 프로세스 간 협력으로 보기는 어렵지만 동일한 프로세스를 구성하는 쓰레드들 간에는 주소 공간을 공유하기 때문에 협력이 가능하다.



#### Message Passing

`Message System` 

프로세스 사이에 공유 변수(shared variable)를 일체 사용하지 않고 통신하는 시스템

##### Message Passing의 방법 - 프로세스의 이름을 표시/표시하지 않음

1. Direct Communication

   통신하려는 프로세스의 이름을 명시적으로 표시

   ![image-20200730231040443](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fbk5ZAm%2FbtqGbqxoEA0%2FvJoTLLJzn0FK7JzZ3LibIK%2Fimg.png)

2. Indirect Communication

   `Mailbox(또는 port)`를 통해 메시지를 간접 전달

   ![image-20200730231229493](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbH4t6u%2FbtqF7KqywBk%2Fdrs9KAPqxy7un1H6N98zf0%2Fimg.png)



