## 예제 프로젝트 - V0

상품 주문하는 프로세스. Controller -> Service -> Repository



## 로그 추적기

### 요구사항 분석

로그 추적기를 만들어보자.



**요구사항** 

- 모든 PUBLIC 메서드의 호출과 응답 정보를 로그로 출력 
- 애플리케이션의 흐름을 변경하면 안됨 
  - 로그를 남긴다고 해서 비즈니스 로직의 동작에 영향을 주면 안됨
- 메서드 호출에 걸린 시간 
- 정상 흐름과 예외 흐름 구분 
  - 예외 발생시 예외 정보가 남아야 함 
- 메서드 호출의 깊이 표현 
- HTTP 요청을 구분 
  - HTTP 요청 단위로 특정 ID를 남겨서 어떤 HTTP 요청에서 시작된 것인지 명확하게 구분이 가능해야 함 
  - 트랜잭션 ID (DB 트랜잭션X), 여기서는 하나의 HTTP 요청이 시작해서 끝날 때 까지를 하나의 트랜잭션이라 함



### V1 - 프로토타입 개발

애플리케이션의 모든 로직에 직접 로그를 남겨도 되지만, 그것보다는 더 효율적인 개발 방법이 필요하다. 특히 트랜잭션ID와 깊이를 표현하는 방법은 기존 정보를 이어 받아야 하기 때문에 단순히 로그만 남긴다고 해결할 수 있는 것은 아니다.



로그 추적기 기반 데이터를 갖고 있는 `TraceId`, `TraceStatus`

```java
package hello.advanced.trace;

public class TraceId {

  private String id;
  private int level;

  public TraceId() {
    this.id = createId();
    this.level = 0;
  }

  private TraceId(String id, int level) {
    this.id = id;
    this.level = level;
  }

  private String createId() {
    return UUID.randomUUID().toString().substring(0, 8);
  }

  public TraceId createNextId() {
    return new TraceId(id, level + 1);
  }

  public TraceId createPreviousId() {
    return new TraceId(id, level - 1);
  }

  public boolean isFirstLevel() {
    return level == 0;
  }

  public String getId() {
    return id;
  }

  public int getLevel() {
    return level;
  }
}
```

- 트랜잭션 id
- 깊이를 표현하는 level



**TraceStatus**

```java
package hello.advanced.trace;

public class TraceStatus {

  private TraceId traceId;
  private Long startTimeMs;
  private String message;

  public TraceStatus(TraceId traceId, Long startTimeMs, String message) {
    this.traceId = traceId;
    this.startTimeMs = startTimeMs;
    this.message = message;
  }

  public Long getStartTimeMs() {
    return startTimeMs;
  }

  public String getMessage() {
    return message;
  }

  public TraceId getTraceId() {
    return traceId;
  }
}
```

- 로그의 상태 정보
- `startTimeMs`: 로그 시작 시간. 로그 종료 시 이 시작 시간 기준으로 전체 수행 시간 계산 가능



**HelloTraceV1.java**

```java
package hello.advanced.trace.hellotrace;

@Slf4j
@Component
public class HelloTraceV1 {

  private static final String START_PREFIX = "-->";
  private static final String COMPLETE_PREFIX = "<--";
  private static final String EX_PREFIX = "<X-";

  public TraceStatus begin(String message) {
    TraceId traceId = new TraceId();
    Long startTimeMs = System.currentTimeMillis();

    log.info("[{}] {}{}", traceId.getId(), addSpace(START_PREFIX, traceId.getLevel()), message);
    return new TraceStatus(traceId, startTimeMs, message);

  }

  public void end(TraceStatus status) {
    complete(status, null);
  }

  public void exception(TraceStatus status, Exception e) {
    complete(status, e);
  }

  private void complete(TraceStatus status, Exception e) {
    Long stopTimeMs = System.currentTimeMillis();
    long resultTimeMs = stopTimeMs - status.getStartTimeMs();
    TraceId traceId = status.getTraceId();
    if (e == null) {
      log.info("[{}] {}{} time={}ms", traceId.getId(), addSpace(COMPLETE_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs);
    } else {
      log.info("[{}] {}{} time={}ms ex={}", traceId.getId(), addSpace(EX_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs, e.toString());
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

- 로그 추적기 클래스. 실제 로그를 시작하고 종료할 수 있다. 
- `TraceStatus begin(String message)`: 로그 시작. 로그 메시지를 파라미터로 받아서 시작 로그 출력
- `void end(TraceStatus status)`: 로그 정상 종료. 파라미터로 시작 로그의 상태를 전달받아서 실행 시간 계산하고 시작할 때와 동일한 로그 메시지 출력
- `void exception(TraceStatus status, Exception e)`: 로그를 예외 상황으로 종료. 실행 시간, 예외 정보를 포함한 결과 로그 출력



**테스트 코드**

```java
package hello.advanced.trace.hellotrace;

class HelloTraceV1Test {

  @Test
  void begin_end() {
    HelloTraceV1 trace = new HelloTraceV1();
    TraceStatus status = trace.begin("hello");
    trace.end(status);
  }

  @Test
  void begin_exception() {
    HelloTraceV1 trace = new HelloTraceV1();
    TraceStatus status = trace.begin("hello");
    trace.exception(status, new IllegalArgumentException());
  }

}
```



결과

```
22:19:40.023 [Test worker] INFO hello.advanced.trace.hellotrace.HelloTraceV1 - [125944d9] hello
22:19:40.028 [Test worker] INFO hello.advanced.trace.hellotrace.HelloTraceV1 - [125944d9] hello time=7ms ex=java.lang.IllegalArgumentException
```



### V1 - 적용

로그 추적기를 애플리케이션에 적용해보자.



**`OrderControllerV1.java`**

```java
package hello.advanced.app.v1;

@RestController
@RequiredArgsConstructor
public class OrderControllerV1 {

  private final OrderServiceV1 orderService;
  private final HelloTraceV1 trace;

  @GetMapping("/v1/request")
  public String request(String itemId) {

    TraceStatus status = null;
    try {
      status = trace.begin("OrderController.request()");
      orderService.orderItem(itemId);
      trace.end(status);
      return "ok";
    } catch (Exception e) {
      trace.exception(status, e);
      throw e;
    }
  }
}
```

- `HelloTraceV1`: 로그 주입기 객체를 주입받는다.
- 단순하게 `trace.begin()`, `trace.end()`만 있으면 될 것 같은데 예외처리 부분도 생각해야 한다. 지저분한 `try - catch` 코드가 추가됨



**`OrderServiceV1.java`**

```java
package hello.advanced.app.v1;

@Service
@RequiredArgsConstructor
public class OrderServiceV1 {

  private final OrderRepositoryV1 orderRepository;
  private final HelloTraceV1 trace;

  public void orderItem(String itemId) {
    TraceStatus status = null;
    try {
      status = trace.begin("OrderServiceV1.orderItem()");
      orderRepository.save(itemId);
      trace.end(status);
    } catch (Exception e) {
      trace.exception(status, e);
      throw e;
    }
  }
}
```



**`OrderRepository.java`**

```java
package hello.advanced.app.v1;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryV1 {

  private final HelloTraceV1 trace;

  public void save(String itemId) {
    TraceStatus status = null;
    try {
      status = trace.begin("OrderRepositoryV1.save()");
      // 저장 로직
      if (itemId.equals("ex")) {
        throw new IllegalArgumentException("예외 발생!!");
      }

      sleep(1000);

      trace.end(status);
    } catch (Exception e) {
      trace.exception(status, e);
      throw e;
    }

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



결과

```
2023-07-17 22:31:13.679  INFO 8264 --- [nio-8080-exec-1] h.a.trace.hellotrace.HelloTraceV1        : [713d058e] OrderController.request()
2023-07-17 22:31:13.680  INFO 8264 --- [nio-8080-exec-1] h.a.trace.hellotrace.HelloTraceV1        : [c662df83] OrderServiceV1.orderItem()
2023-07-17 22:31:13.680  INFO 8264 --- [nio-8080-exec-1] h.a.trace.hellotrace.HelloTraceV1        : [a07cca1e] OrderRepositoryV1.save()
2023-07-17 22:31:14.688  INFO 8264 --- [nio-8080-exec-1] h.a.trace.hellotrace.HelloTraceV1        : [a07cca1e] OrderRepositoryV1.save() time=1008ms
2023-07-17 22:31:14.688  INFO 8264 --- [nio-8080-exec-1] h.a.trace.hellotrace.HelloTraceV1        : [c662df83] OrderServiceV1.orderItem() time=1008ms
2023-07-17 22:31:14.688  INFO 8264 --- [nio-8080-exec-1] h.a.trace.hellotrace.HelloTraceV1        : [713d058e] OrderController.request() time=1009ms
```

- 얼추 모양새가 있다. 레벨 기능은 없다. 트랜잭션ID도 다름.



예외 결과

``` 
2023-07-17 22:31:51.605  INFO 8264 --- [nio-8080-exec-2] h.a.trace.hellotrace.HelloTraceV1        : [e33742d6] OrderController.request()
2023-07-17 22:31:51.605  INFO 8264 --- [nio-8080-exec-2] h.a.trace.hellotrace.HelloTraceV1        : [314a3e97] OrderServiceV1.orderItem()
2023-07-17 22:31:51.605  INFO 8264 --- [nio-8080-exec-2] h.a.trace.hellotrace.HelloTraceV1        : [5f1d82e1] OrderRepositoryV1.save()
2023-07-17 22:31:51.605  INFO 8264 --- [nio-8080-exec-2] h.a.trace.hellotrace.HelloTraceV1        : [5f1d82e1] OrderRepositoryV1.save() time=0ms ex=java.lang.IllegalArgumentException: 예외 발생!!
2023-07-17 22:31:51.605  INFO 8264 --- [nio-8080-exec-2] h.a.trace.hellotrace.HelloTraceV1        : [314a3e97] OrderServiceV1.orderItem() time=0ms ex=java.lang.IllegalArgumentException: 예외 발생!!
2023-07-17 22:31:51.605  INFO 8264 --- [nio-8080-exec-2] h.a.trace.hellotrace.HelloTraceV1        : [e33742d6] OrderController.request() time=1ms ex=java.lang.IllegalArgumentException: 예외 발생!!
```



추가로 필요한 요구사항

- 메서드 호출 깊이
- 같은 HTTP 요청이면 같은 트랜잭션ID 남기기



위 기능은 직전 로그의 깊이와 트랜잭션 ID를 알아야 한다. == 문맥 정보가 필요함



### V2 - 파라미터로 동기화 개발

문맥을 넘기는 가장 간단한 방법은 첫 로그에서 사용한 트랜잭션ID와 레벨을 다음 로그에 넘기는 것.



```java
public TraceStatus begin(String message) {
  TraceId traceId = new TraceId();
  Long startTimeMs = System.currentTimeMillis();

  log.info("[{}] {}{}", traceId.getId(), addSpace(START_PREFIX, traceId.getLevel()), message);
  return new TraceStatus(traceId, startTimeMs, message);
}

public TraceStatus beginSync(TraceId beforeTraceId, String message) {
  TraceId nextId = beforeTraceId.createNextId();
  Long startTimeMs = System.currentTimeMillis();

  log.info("[{}] {}{}", nextId.getId(), addSpace(START_PREFIX, nextId.getLevel()), message);
  return new TraceStatus(nextId, startTimeMs, message);
}
```

- `beginSync()`: 기존 `TraceId`에서 `createNextId()`를 통해 다음 ID 구한다.



### V2 - 적용

기존 코드에 `TraceId`를 넘기도록 메서드 파라미터를 모두 고쳐준다..

컨트롤러(맨 처음)을 제외한 `Service`, `Repository` 에서는 `beginSync` 메서드 사용.



**`Controller`**

```java
package hello.advanced.app.v2.v1;

@RestController
@RequiredArgsConstructor
public class OrderControllerV2 {

  private final OrderServiceV2 orderService;
  private final HelloTraceV2 trace;

  @GetMapping("/v2/request")
  public String request(String itemId) {

    TraceStatus status = null;
    try {
      status = trace.begin("OrderController.request()");
      orderService.orderItem(status.getTraceId(), itemId);
      trace.end(status);
      return "ok";
    } catch (Exception e) {
      trace.exception(status, e);
      throw e;
    }
  }
}
```



**`Service`**

```java
package hello.advanced.app.v2.v1;

@Service
@RequiredArgsConstructor
public class OrderServiceV2 {

  private final OrderRepositoryV2 orderRepository;
  private final HelloTraceV2 trace;

  public void orderItem(TraceId traceId, String itemId) {
    TraceStatus status = null;
    try {
      status = trace.beginSync(traceId, "OrderServiceV2.orderItem()");
      orderRepository.save(status.getTraceId(), itemId);
      trace.end(status);
    } catch (Exception e) {
      trace.exception(status, e);
      throw e;
    }
  }

}
```



**`Repository`**

```java
package hello.advanced.app.v2.v1;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryV2 {

  private final HelloTraceV2 trace;

  public void save(TraceId traceId, String itemId) {
    TraceStatus status = null;
    try {
      status = trace.beginSync(traceId, "OrderRepositoryV2.save()");
      // 저장 로직
      if (itemId.equals("ex")) {
        throw new IllegalArgumentException("예외 발생!!");
      }

      sleep(1000);

      trace.end(status);
    } catch (Exception e) {
      trace.exception(status, e);
      throw e;
    }

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



**남은 문제**

- HTTP 요청을 구분하고 깊이를 표현하기 위해 `TraceId` 동기화가 필요하다!
- 방금은 관련 메서드의 모든 파라미터를 수정하는 방식으로 구현함. 인터페이스가 있다면 인터페이스까지 모두 고쳐야 한다..
- 만약 컨트롤러를 통해서 서비스를 호출하는 것이 아니라 다른 곳에서 서비스부터 호출하는 상황이라면 파라미터로 넘길 `TraceId`가 없다;