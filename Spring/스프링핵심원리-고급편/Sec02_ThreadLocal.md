## 필드 동기화

### 개발

Sec01 때는 트랜잭션ID와 `level`을 동기화하기 위해 `TraceId`를 파라미터로 넘기도록 구현했었다. 어쨌든 동기화는 성공이지만, 로그를 출력하는 모든 메서드에 파라미터를 추가해야 하는 문제가 발생..



**`LogTrace` 인터페이스**

``` java
public interface LogTrace {

  TraceStatus begin(String message);
  void end(TraceStatus status);
  void exception(TraceStatus status, Exception e);

}
```



**`FieldLogTrace`**

``` java
@Slf4j
public class FieldLogTrace implements LogTrace {

  private static final String START_PREFIX = "-->";
  private static final String COMPLETE_PREFIX = "<--";
  private static final String EX_PREFIX = "<X-";
  private TraceId traceIdHolder; //traceId 동기화, 동시성 이슈 발생

  @Override
  public TraceStatus begin(String message) {
    syncTraceId();
    TraceId traceId = traceIdHolder;
    Long startTimeMs = System.currentTimeMillis();
    log.info("[{}] {}{}", traceId.getId(), addSpace(START_PREFIX,
        traceId.getLevel()), message);
    return new TraceStatus(traceId, startTimeMs, message);
  }

  @Override
  public void end(TraceStatus status) {
    complete(status, null);
  }

  @Override
  public void exception(TraceStatus status, Exception e) {
    complete(status, e);
  }

  private void complete(TraceStatus status, Exception e) {
    Long stopTimeMs = System.currentTimeMillis();
    long resultTimeMs = stopTimeMs - status.getStartTimeMs();
    TraceId traceId = status.getTraceId();
    if (e == null) {
      log.info("[{}] {}{} time={}ms", traceId.getId(),
          addSpace(COMPLETE_PREFIX, traceId.getLevel()), status.getMessage(),
          resultTimeMs);
    } else {
      log.info("[{}] {}{} time={}ms ex={}", traceId.getId(),
          addSpace(EX_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs,
          e.toString());
    }
    releaseTraceId();
  }

  private void syncTraceId() {
    if (traceIdHolder == null) {
      traceIdHolder = new TraceId();
    } else {
      traceIdHolder = traceIdHolder.createNextId();
    }
  }

  private void releaseTraceId() {
    if (traceIdHolder.isFirstLevel()) {
      traceIdHolder = null; //destroy
    } else {
      traceIdHolder = traceIdHolder.createPreviousId();
    }
  }

  private static String addSpace(String prefix, int level) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < level; i++) {
      sb.append((i == level - 1) ? "|" + prefix : "| ");
    }
    return sb.toString();
  }
}
```

- `HelloTraceV2`랑 거의 동일
- `TraceId`를 동기화하는 부분만 파라미터 -> `TraceId traceIdHolder` 필드를 사용하도록 변경



### 적용

`FieldLogTrace`를 스프링 빈으로 등록. -> 싱글톤으로 생성된다.



### 동시성 문제

`FieldLogTrace`는 심각한 동시성 문제를 갖고 있다.



**동시에 여러 번 요청하는 경우 로그**

```
[nio-8080-exec-7] [13500b67] OrderController.request()
[nio-8080-exec-7] [13500b67] |-->OrderServiceV3.orderItem()
[nio-8080-exec-7] [13500b67] | |-->OrderRepositoryV3.save()
[nio-8080-exec-8] [13500b67] | | |-->OrderController.request()
[nio-8080-exec-8] [13500b67] | | | |-->OrderServiceV3.orderItem()
[nio-8080-exec-8] [13500b67] | | | | |-->OrderRepositoryV3.save()
[nio-8080-exec-7] [13500b67] | |<--OrderRepositoryV3.save() time=1003ms
[nio-8080-exec-7] [13500b67] |<--OrderServiceV3.orderItem() time=1003ms
[nio-8080-exec-7] [13500b67] OrderController.request() time=1004ms
[nio-8080-exec-8] [13500b67] | | | | |<--OrderRepositoryV3.save() time=1004ms
[nio-8080-exec-8] [13500b67] | | | |<--OrderServiceV3.orderItem() time=1004ms
[nio-8080-exec-8] [13500b67] | | |<--OrderController.request() time=1004ms
```

- 각각 다른 7, 8번 스레드가 요청을 수행하는데 `traceIdHolder` 를 공유하기 때문에 위와 같이 모두 같은 `traceId`, 이상한 `level`이 꼬인다!!!
- `FieldLogTrace`는 싱글톤으로 등록된  스프링 빈이다. 이 하나만 있는 인스턴스의 필드를 여러 스레드가 동시에 접근하기 때문에 문제가 발생한다.



### 예제



**`FieldService`**

``` java
@Slf4j
public class FieldService {

  private String nameStore;

  public String logic(String name) {
    log.info("저장 name={} -> nameStore={}", name, nameStore);
    nameStore = name;
    sleep(1000);
    log.info("조회 nameStore={}", nameStore);
    return nameStore;
  }

  private void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
```



**`FieldServiceTest`**

``` java
package hello.advanced.trace.threadlocal;

import hello.advanced.trace.threadlocal.code.FieldService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class FieldServiceTest {

  private FieldService fieldService = new FieldService();

  @Test
  void field() {
    log.info("main start");
    Runnable userA = () -> {
      fieldService.logic("userA");
    };
    Runnable userB = () -> {
      fieldService.logic("userB");
    };
    Thread threadA = new Thread(userA);
    threadA.setName("thread-A");
    Thread threadB = new Thread(userB);
    threadB.setName("thread-B");

    threadA.start(); //A실행
//    sleep(2000); // 동시성 문제 발생X
 sleep(100); //동시성 문제 발생O

    threadB.start(); //B실행
    sleep(3000); // 메인 쓰레드 종료 대기
    log.info("main exit");
  }

  private void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}

```

- threadA <-> threadB의 실행 사이 텀을 많이 주면 정상적인 결과 나옴
- 그렇지 않다면 threadA가 조회하기 전에 threadB가 nameStore에 저장하므로 정상적으로 데이터 저장 및 조회가 되지 않는다. 
- 이렇게 여러 스레드가 동일한 인스턴스의 필드를 변경하면서 발생하는 문제를 동시성 문제라고 한다! 참고로 지역 변수에서는 발생하지 않는다. 스레드마다 각각 다른 메모리 영역에 할당되니깐..



## ThreadLocal

`ThreadLocal`: 한 스레드만 접근할 수 있는 특별한 저장소. 각 스레드마다 별도의 내부 저장소를 제공한다.



**ThreadLocalService**

``` java
@Slf4j
public class ThreadLocalService {

  private ThreadLocal<String> nameStore = new ThreadLocal<>();

  public String logic(String name) {
    log.info("저장 name={} -> nameStore={}", name, nameStore.get());
    nameStore.set(name);
    
    sleep(1000);
    
    log.info("조회 nameStore={}", nameStore.get());
    return nameStore.get();
  }

  private void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}

```

- 값 저장: `ThreadLocal.set(xxx)`
- 값 조회: `ThreadLocal.get()`
- 값 제거: `ThreadLocal.remove()`

- 주의: 해당 스레드가 ThreadLocal을 모두 사용하고 나면 `ThreadLocal.remove()`를 호출해서 ThreadLocal에 저장된 값을 제거해주어야 한다.



**테스트 결과**

``` 
[Test worker] main start
[thread-A] 저장 name=userA -> nameStore=null
[thread-B] 저장 name=userB -> nameStore=null
[thread-A] 조회 nameStore=userA
[thread-B] 조회 nameStore=userB
[Test worker] main exit
```

잘된다.



## ThreadLocal 동기화

**`ThreadLocalLogTrace`**

``` java
@Slf4j
public class ThreadLocalLogTrace implements LogTrace {

  private static final String START_PREFIX = "-->";
  private static final String COMPLETE_PREFIX = "<--";
  private static final String EX_PREFIX = "<X-";
  private final ThreadLocal<TraceId> traceIdHolder = new ThreadLocal<>();

  @Override
  public TraceStatus begin(String message) {
    syncTraceId();
    TraceId traceId = traceIdHolder.get();
    Long startTimeMs = System.currentTimeMillis();
    log.info("[{}] {}{}", traceId.getId(), addSpace(START_PREFIX, traceId.getLevel()), message);
    return new TraceStatus(traceId, startTimeMs, message);
  }

  @Override
  public void end(TraceStatus status) {
    complete(status, null);
  }

  @Override
  public void exception(TraceStatus status, Exception e) {
    complete(status, e);
  }

  private void complete(TraceStatus status, Exception e) {
    Long stopTimeMs = System.currentTimeMillis();
    long resultTimeMs = stopTimeMs - status.getStartTimeMs();
    TraceId traceId = status.getTraceId();
    if (e == null) {
      log.info("[{}] {}{} time={}ms", traceId.getId(),
          addSpace(COMPLETE_PREFIX, traceId.getLevel()), status.getMessage(),
          resultTimeMs);
    } else {
      log.info("[{}] {}{} time={}ms ex={}", traceId.getId(),
          addSpace(EX_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs,
          e.toString());
    }
    releaseTraceId();
  }

  private void syncTraceId() {
    TraceId traceId = traceIdHolder.get();
    if (traceId == null) {
      traceIdHolder.set(new TraceId());
    } else {
      traceIdHolder.set(traceId.createNextId());
    }
  }

  private void releaseTraceId() {
    TraceId traceId = traceIdHolder.get();
    if (traceId.isFirstLevel()) {
      traceIdHolder.remove(); //destroy
    } else {
      traceIdHolder.set(traceId.createPreviousId());
    }
  }

  private static String addSpace(String prefix, int level) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < level; i++) {
      sb.append((i == level - 1) ? "|" + prefix : "| ");
    }
    return sb.toString();
  }
}

```

- `traceIdHolder`: String -> `ThreadLocal`로 변경. `set()`, `get()` 사용
- ThreadLocal을 모두 사용하고 나면 꼭 `remove()`를 호출해서 저장된 값을 제거해주어야 한다.



### 주의사항

ThreadLocal 의 값을 사용 후 제거하지 않고 그냥 두면 WAS처럼 스레드 풀을 사용하는 경우 심각한 문제가 발생할 수 있다!!

스레드 풀을 사용하는 경우, 요청이 와서 사용된 스레드는 제거되지 않고 스레드 풀에 계속 살아있게 된다. 따라서 스레드 로컬의 해당 스레드 전용 보관소에 데이터도 함께 살아있다!! 따라서 해당 요청이 끝나면 **반드시** `ThreadLocal.remove()`로 데이터를 제거해주자.