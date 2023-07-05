## 커밋, 롤백

``` java
package hello.springtx.propagation;

@Slf4j
@SpringBootTest
public class BasicTxTest {

  @Autowired
  PlatformTransactionManager txManager;

  @TestConfiguration
  static class Config {

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
      return new DataSourceTransactionManager(dataSource);
    }
  }

  @Test
  void commit() {
    log.info("트랜잭션 시작");

    TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

    log.info("트랜잭션 커밋 시작");
    txManager.commit(status);
    log.info("트랜잭션 커밋 완료");
  }

  @Test
  void rollback() {
    log.info("트랜잭션 시작");

    TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

    log.info("트랜잭션 롤백 시작");
    txManager.rollback(status);
    log.info("트랜잭션 롤백 완료");
  }

}

```



**`commit()`**

- `txManager.getTransaction(new DefaultTransactionAttribute())`: 트랜잭션 매니저를 통해 트랜잭션을 시작(획득)

- `txManager.commit(status)`: 트랜잭션 커밋

- 실행 로그

  ```
  2023-07-04 20:09:00.853  INFO 56253 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 트랜잭션 시작
  2023-07-04 20:09:00.863 DEBUG 56253 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Creating new transaction with name [null]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
  2023-07-04 20:09:00.864 DEBUG 56253 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Acquired Connection [HikariProxyConnection@2043169223 wrapping conn0: url=jdbc:h2:mem:60fa9141-e869-40ca-a0a9-c884160d5f10 user=SA] for JDBC transaction
  2023-07-04 20:09:00.865 DEBUG 56253 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Switching JDBC Connection [HikariProxyConnection@2043169223 wrapping conn0: url=jdbc:h2:mem:60fa9141-e869-40ca-a0a9-c884160d5f10 user=SA] to manual commit
  2023-07-04 20:09:00.866  INFO 56253 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 트랜잭션 커밋 시작
  2023-07-04 20:09:00.866 DEBUG 56253 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Initiating transaction commit
  2023-07-04 20:09:00.866 DEBUG 56253 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Committing JDBC transaction on Connection [HikariProxyConnection@2043169223 wrapping conn0: url=jdbc:h2:mem:60fa9141-e869-40ca-a0a9-c884160d5f10 user=SA]
  2023-07-04 20:09:00.867 DEBUG 56253 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Releasing JDBC Connection [HikariProxyConnection@2043169223 wrapping conn0: url=jdbc:h2:mem:60fa9141-e869-40ca-a0a9-c884160d5f10 user=SA] after transaction
  2023-07-04 20:09:00.868  INFO 56253 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 트랜잭션 커밋 완료
  
  ```



**`rollback()`**

- 실행 로그

  ```
  2023-07-04 20:09:28.347  INFO 56341 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 트랜잭션 시작
  2023-07-04 20:09:28.348 DEBUG 56341 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Creating new transaction with name [null]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
  2023-07-04 20:09:28.349 DEBUG 56341 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Acquired Connection [HikariProxyConnection@2043169223 wrapping conn0: url=jdbc:h2:mem:1a303b48-2eae-4809-8313-c3ca03e9f418 user=SA] for JDBC transaction
  2023-07-04 20:09:28.350 DEBUG 56341 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Switching JDBC Connection [HikariProxyConnection@2043169223 wrapping conn0: url=jdbc:h2:mem:1a303b48-2eae-4809-8313-c3ca03e9f418 user=SA] to manual commit
  2023-07-04 20:09:28.350  INFO 56341 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 트랜잭션 롤백 시작
  2023-07-04 20:09:28.350 DEBUG 56341 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Initiating transaction rollback
  2023-07-04 20:09:28.350 DEBUG 56341 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Rolling back JDBC transaction on Connection [HikariProxyConnection@2043169223 wrapping conn0: url=jdbc:h2:mem:1a303b48-2eae-4809-8313-c3ca03e9f418 user=SA]
  2023-07-04 20:09:28.351 DEBUG 56341 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Releasing JDBC Connection [HikariProxyConnection@2043169223 wrapping conn0: url=jdbc:h2:mem:1a303b48-2eae-4809-8313-c3ca03e9f418 user=SA] after transaction
  2023-07-04 20:09:28.351  INFO 56341 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 트랜잭션 롤백 완료
  
  ```



## 트랜잭션 두 번 사용

위 테스트 코드에 트랜잭션을 하나 더 추가해보자.



**트랜잭션이 각각 따로 사용되는 경우 (트랜잭션 1 종료 이후 트랜잭션 2 수행)**

``` java
  @Test
  void double_commit() {
    log.info("트랜잭션1 시작");
    TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
    log.info("트랜잭션1 커밋 시작");
    txManager.commit(tx1);

    log.info("트랜잭션1 시작");
    TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
    log.info("트랜잭션1 커밋 시작");
    txManager.commit(tx2);
  }
```

- ```
  2023-07-04 20:12:01.629  INFO 56799 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 트랜잭션1 시작
  2023-07-04 20:12:01.632 DEBUG 56799 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Creating new transaction with name [null]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
  2023-07-04 20:12:01.632 DEBUG 56799 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Acquired Connection [HikariProxyConnection@726690425 wrapping conn0: url=jdbc:h2:mem:3ed878ef-4877-4951-a527-22daea1b77af user=SA] for JDBC transaction
  2023-07-04 20:12:01.634 DEBUG 56799 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Switching JDBC Connection [HikariProxyConnection@726690425 wrapping conn0: url=jdbc:h2:mem:3ed878ef-4877-4951-a527-22daea1b77af user=SA] to manual commit
  2023-07-04 20:12:01.635  INFO 56799 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 트랜잭션1 커밋 시작
  2023-07-04 20:12:01.636 DEBUG 56799 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Initiating transaction commit
  2023-07-04 20:12:01.636 DEBUG 56799 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Committing JDBC transaction on Connection [HikariProxyConnection@726690425 wrapping conn0: url=jdbc:h2:mem:3ed878ef-4877-4951-a527-22daea1b77af user=SA]
  2023-07-04 20:12:01.637 DEBUG 56799 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Releasing JDBC Connection [HikariProxyConnection@726690425 wrapping conn0: url=jdbc:h2:mem:3ed878ef-4877-4951-a527-22daea1b77af user=SA] after transaction
  2023-07-04 20:12:01.637  INFO 56799 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 트랜잭션1 시작
  2023-07-04 20:12:01.637 DEBUG 56799 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Creating new transaction with name [null]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
  2023-07-04 20:12:01.637 DEBUG 56799 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Acquired Connection [HikariProxyConnection@130574494 wrapping conn0: url=jdbc:h2:mem:3ed878ef-4877-4951-a527-22daea1b77af user=SA] for JDBC transaction
  2023-07-04 20:12:01.638 DEBUG 56799 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Switching JDBC Connection [HikariProxyConnection@130574494 wrapping conn0: url=jdbc:h2:mem:3ed878ef-4877-4951-a527-22daea1b77af user=SA] to manual commit
  2023-07-04 20:12:01.638  INFO 56799 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 트랜잭션1 커밋 시작
  2023-07-04 20:12:01.638 DEBUG 56799 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Initiating transaction commit
  2023-07-04 20:12:01.638 DEBUG 56799 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Committing JDBC transaction on Connection [HikariProxyConnection@130574494 wrapping conn0: url=jdbc:h2:mem:3ed878ef-4877-4951-a527-22daea1b77af user=SA]
  2023-07-04 20:12:01.638 DEBUG 56799 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Releasing JDBC Connection [HikariProxyConnection@130574494 wrapping conn0: url=jdbc:h2:mem:3ed878ef-4877-4951-a527-22daea1b77af user=SA] after transaction
  
  ```

- tx1과 tx2가 커넥션 풀에서 `conn0` 같은 커넥션을 획득해서 사용했다. 하지만 tx1이 커넥션 사용 후 반납하고, 이후 tx2가 커넥션 `conn0`을 획득했기 때문에 둘은 완전히 다른 **커넥션**으로 인지하는 것이 맞다.
  - 둘을 구분하는 방법?
  - 히카리 커넥션 풀에서 커넥션을 획득하면 실제 커넥션을 그대로 반환하는 것이 아니라 내부 관리를 위해 히카리 프록시 커넥션이라는 객체를 생성해서 반환한다. 물론 내부에는 실제 커넥션이 포함되어 있다. 이 객체의 주소를 확인하면 커넥션 풀에서 획득한 커넥션을 구분할 수 있다.
- tx1: `Acquired Connection [HikariProxyConnection@726690425 wrapping conn0`
- tx2: `Acquired Connection [HikariProxyConnection@130574494 wrapping conn0`

히카리 커넥션 풀이 반환해주는 커넥션을 다루는 프록시 객체의 주소가 각각 다른 것 확인 가능.

결론) DB와 연결을 맺는 물리적인 커넥션은 `conn0`을 통해 커넥션이 재사용됨. 각각 커넥션 풀에서 커넥션을 조회할 때는 ㄷ른 프록시 객체가 사용됨.



위의 경우는 트랜잭션을 각자 관리하기 때문에 트랜잭션을 묶을 수 없다.



## 전파 기본

트랜잭션이 이미 진행중인데, 추가로 트랜잭션을 수행하면 어떻게 될까?? > 이럴 때 어떻게 동작할지 결정하는 것을 **트랜잭션 전파(propagation)**이라고 함. 디폴트: `REQUIRED`



**외부 트랜잭션이 수행중인데 내부 트랜잭션이 추가로 수행됨**

- 외부 트랜잭션: 먼저 수행중인 트랜잭션
- 내부 트랜잭션: 외부에 트랜잭션이 수행되고 있는 와중에 호출되는 트랜잭션

- 스프링은 이 경우 외부 트랜잭션과 내부 트랜잭션을 묶어서 하나의 트랜잭션을 만들어준다. (내부가 외부에 참여)



**물리 트랜잭션, 논리 트랜잭션**

- 논리 트랜잭션: 트랜잭션 매니저를 통해 트랜잭션을 시작하는 단위. 하나의 물리 트랜잭션으로 묶인다.
- 물리 트랜잭션: 실제 DB에 적용되는 트랜잭션. 실제 커넥션을 통해 트랜잭션을 시작하고, 커밋/롤백하는 단위.
- 트랜잭션이 사용중일때 또 다른 트랜잭션이 내부에 사용되면 복잡해진다. 이때 논리 트랜잭션 개념을 도입하면 다음과 같은 단순한 원칙을 만들 수 있음
  - 모든 논리 트랜잭션이 커밋되어야 물리 트랜잭션이 커밋된다.
  - 하나의 논리 트랜잭션이라도 롤백되면 물리 트랜잭션은 롤백된다.



## 전파 예제

``` java
  @Test
  void inner_commit() {
    log.info("외부 트랜잭션 시작");
    TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
    log.info("outer.isNewTransaction()={}", outer.isNewTransaction());

    log.info("내부 트랜잭션 시작");
    TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
    log.info("inner.isNewTransaction()={}", inner.isNewTransaction());
    log.info("내부 트랜잭션 커밋");
    txManager.commit(inner);

    log.info("외부 트랜잭션 커밋");
    txManager.commit(outer);
  }
```

- 외부 트랜잭션은 처음 수행된 트랜잭션. `isNewTransaction=true`
- 내부 트랜잭션은 외부 트랜잭션에 참여.
  - 트랜잭션 참여 == 내부 트랜잭션이 외부 트랜잭션을 그대로 이어 받아서 따른다.
  - 반대로 생각하면 외부 트랜잭션의 범위가 내부 트랜잭션까지 넓어짐
  - 외부에서 시작된 물리적인 트랜잭션의 범위가 내부 트랜잭션까지 넓어짐
  - 정리하면 **외부 트랜잭션과 내부 트랜잭션이 하나의 물리 트랜잭션으로 묶이는 것**



코드를 보면 내/외부 트랜잭션 커밋을 각각 한 번씩 호출했다. 하나의 커넥션에 커밋/롤백은 한 번만 호출 가능하다.



**실행 결과**

```
외부 트랜잭션 시작
Creating new transaction with name [null]:
PROPAGATION_REQUIRED,ISOLATION_DEFAULT
Acquired Connection [HikariProxyConnection@1943867171 wrapping conn0] for JDBC
transaction
Switching JDBC Connection [HikariProxyConnection@1943867171 wrapping conn0] to
manual commit
outer.isNewTransaction()=true

내부 트랜잭션 시작
Participating in existing transaction	(내부 트랜잭션이 기존재하는 외부 트랜잭션에 참여)
inner.isNewTransaction()=false
내부 트랜잭션 커밋

외부 트랜잭션 커밋
Initiating transaction commit
Committing JDBC transaction on Connection [HikariProxyConnection@1943867171 
wrapping conn0]
Releasing JDBC Connection [HikariProxyConnection@1943867171 wrapping conn0]
after transaction
```

- `txManager.commit(inner);` 코드는 아무 것도 안한다.
- 외부 트랜잭션만 물리 트랜잭션을 시작하고, 커밋한다.

- 스프링은 여러 트랜잭션이 함께 사용되는 경우, **처음 트랜잭션을 시작한 외부 트랜잭션이 실제 물리 트랜잭션을 관리**하도록 한다. 이를 통해 트랜잭션 중복 커밋 문제를 해결!



**핵심 정리**

- 트랜잭션 매니저에 (논리) 커밋을 호출한다고 해서 항상 실제 커넥션에 물리 커밋이 발생하지는 않는다.
- 신규 트랜잭션인 경우에만 실제 커넥션을 사용해서 물리 커밋/롤백을 수행한다. 신규 트랜잭션이 아니면 실제 물리 커넥션을 사용하지 않는다.
- 이렇게 트랜잭션이 내부에서 추가로 사용되면 트랜잭션 매니저에 커밋하는 것이 항상 물리 커밋으로 이어지지 않는다.
- 트랜잭션이 내부에서 추가로 사용되면, 트랜잭션 매니저를 통해 논리 트랜잭션을 관리하고, 모든 논리 트랜잭션이 커밋되면 물리 트랜잭션이 커밋된다.



## 외부 롤백

바깥이 롤백되는 케이스는 어렵지 않음.

논리 트랜잭션이 하나라도 롤백되면 전체 물리 트랜잭션은 롤백된다. 따라서 내부 트랜잭션이 커밋되어도 외부 트랜잭션이 롤백되면 전체가 롤백된다.



``` java
  @Test
  void outer_rollback() {
    log.info("외부 트랜잭션 시작");
    TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
    
    log.info("내부 트랜잭션 시작");
    TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
    log.info("내부 트랜잭션 커밋");
    txManager.commit(inner);
    
    log.info("외부 트랜잭션 롤백");
    txManager.rollback(outer);
  }
```



**실행 로그**

```
2023-07-06 07:22:54.046  INFO 37426 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 외부 트랜잭션 시작
2023-07-06 07:22:54.048 DEBUG 37426 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Creating new transaction with name [null]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2023-07-06 07:22:54.049 DEBUG 37426 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Acquired Connection [HikariProxyConnection@1165725635 wrapping conn0: url=jdbc:h2:mem:242fd9c3-6fab-4e18-b020-40fc457cda1f user=SA] for JDBC transaction
2023-07-06 07:22:54.051 DEBUG 37426 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Switching JDBC Connection [HikariProxyConnection@1165725635 wrapping conn0: url=jdbc:h2:mem:242fd9c3-6fab-4e18-b020-40fc457cda1f user=SA] to manual commit
2023-07-06 07:22:54.051  INFO 37426 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 내부 트랜잭션 시작
2023-07-06 07:22:54.051 DEBUG 37426 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Participating in existing transaction
2023-07-06 07:22:54.051  INFO 37426 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 내부 트랜잭션 커밋
2023-07-06 07:22:54.051  INFO 37426 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 외부 트랜잭션 롤백
2023-07-06 07:22:54.051 DEBUG 37426 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Initiating transaction rollback
2023-07-06 07:22:54.052 DEBUG 37426 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Rolling back JDBC transaction on Connection [HikariProxyConnection@1165725635 wrapping conn0: url=jdbc:h2:mem:242fd9c3-6fab-4e18-b020-40fc457cda1f user=SA]
2023-07-06 07:22:54.052 DEBUG 37426 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Releasing JDBC Connection [HikariProxyConnection@1165725635 wrapping conn0: url=jdbc:h2:mem:242fd9c3-6fab-4e18-b020-40fc457cda1f user=SA] after transaction

```

- 외부 트랜잭션이 물리 트랜잭션을 시작하고 롤백
- 내부 트랜잭션은 물리 트랜잭션에 직접적인 관여하지 않음



## 내부 롤백

내부 트랜잭션이 롤백하더라도 내부 트랜잭션은 물리 트랜잭션에 영향을 주지 않는다. 근데 외부 트랜잭션은 커밋해버린다. 전체를 롤백해야 하는데.. 스프링은 어떻게 이 문제를 처리할까????



``` java
  @Test
  void inner_rollback() {
    log.info("외부 트랜잭션 시작");
    TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
    
    log.info("내부 트랜잭션 시작");
    TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
    log.info("내부 트랜잭션 롤백");
    txManager.rollback(inner);
    
    log.info("외부 트랜잭션 커밋");
    assertThatThrownBy(() -> txManager.commit(outer)).isInstanceOf(UnexpectedRollbackException.class);
  }
```



``` 
2023-07-06 07:31:54.283  INFO 39111 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 외부 트랜잭션 시작
2023-07-06 07:31:54.286 DEBUG 39111 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Creating new transaction with name [null]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2023-07-06 07:31:54.286 DEBUG 39111 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Acquired Connection [HikariProxyConnection@933346995 wrapping conn0: url=jdbc:h2:mem:95595a44-06a7-4886-a7e5-5296325db4ca user=SA] for JDBC transaction
2023-07-06 07:31:54.287 DEBUG 39111 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Switching JDBC Connection [HikariProxyConnection@933346995 wrapping conn0: url=jdbc:h2:mem:95595a44-06a7-4886-a7e5-5296325db4ca user=SA] to manual commit
2023-07-06 07:31:54.287  INFO 39111 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 내부 트랜잭션 시작
2023-07-06 07:31:54.288 DEBUG 39111 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Participating in existing transaction
2023-07-06 07:31:54.288  INFO 39111 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 내부 트랜잭션 롤백
2023-07-06 07:31:54.288 DEBUG 39111 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Participating transaction failed - marking existing transaction as rollback-only
2023-07-06 07:31:54.288 DEBUG 39111 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Setting JDBC transaction [HikariProxyConnection@933346995 wrapping conn0: url=jdbc:h2:mem:95595a44-06a7-4886-a7e5-5296325db4ca user=SA] rollback-only
2023-07-06 07:31:54.288  INFO 39111 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 외부 트랜잭션 커밋
2023-07-06 07:31:54.330 DEBUG 39111 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Global transaction is marked as rollback-only but transactional code requested commit
2023-07-06 07:31:54.330 DEBUG 39111 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Initiating transaction rollback
2023-07-06 07:31:54.331 DEBUG 39111 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Rolling back JDBC transaction on Connection [HikariProxyConnection@933346995 wrapping conn0: url=jdbc:h2:mem:95595a44-06a7-4886-a7e5-5296325db4ca user=SA]
2023-07-06 07:31:54.331 DEBUG 39111 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Releasing JDBC Connection [HikariProxyConnection@933346995 wrapping conn0: url=jdbc:h2:mem:95595a44-06a7-4886-a7e5-5296325db4ca user=SA] after transaction

```

- **내부 트랜잭션 롤백**: `Participating transaction failed - marking existing transaction as rollback-only`: 내부 트랜잭션 롤백하는 시점에 요 트랜잭션을 `rollback-only`로 마킹!
- **외부 트랜잭션 커밋**: `Global transaction is marked as rollback-only but transactional code requested commit`: 전체 트랜잭션은 롤백해야 하는데 커밋을 해버렸다!!

- 외부 트랜잭션은 신규 트랜잭션이므로 DB 커넥션에 실제 커밋을 호출한다. 이때 트랜잭션 동기화 매니저에 롤백 전용(`rollbackIOnly=true`) 여부를 먼저 확인한다. 만약 표시가 있으면 물리 트랜잭션을 커밋하지 않고 **롤백**한다.
- 실제 DB에 롤백이 반영되고 물리 트랜잭션도 종료.
- 개발자는 커밋을 기대했는데 롤백이 되었으므로 트랜잭션 매니저가 `UnexpectedRollbackException` 예외를 던진다!!



## REQUIRES_NEW

내부 트랜잭션과 외부 트랜잭션을 완전히 분리해서 사용하는 방법.

각각 별도의 물리 트랜잭션을 사용해서 내부 트랜잭션이 롤백되어도 외부 트랜잭션에 영향이 없다. 반대도 마찬가지.



**REQUIRES_NEW**

- 물리 트랜잭션을 분리하려면 **내부 트랜잭션을 시작할 때 `REQUIRES_NEW` 옵션을 사용하면 된다.**
- 외부 트랜잭션과 내부 트랜잭션이 각각 별도의 물리 트랜잭션을 갖게 된다. 즉, DB 커넥션을 따로 사용한다.

- 각 트랜잭션의 커밋/롤백 여부가 다른 트랜잭션에 영향을 주지 않는다.



``` java
  @Test
  void inner_rollback_requires_new() {
    log.info("외부 트랜잭션 시작");
    TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
    log.info("outer.isNewTransaction()={}", outer.isNewTransaction());

    log.info("내부 트랜잭션 시작");
    DefaultTransactionAttribute definition = new DefaultTransactionAttribute();

    definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    TransactionStatus inner = txManager.getTransaction(definition);
    log.info("inner.isNewTransaction()={}", inner.isNewTransaction());

    log.info("내부 트랜잭션 롤백");
    txManager.rollback(inner); // 롤백

    log.info("외부 트랜잭션 커밋");
    txManager.commit(outer); // 커밋
  }
```

- 내부 트랜잭션을 시작할 때 전파 옵션인 `propagationBehavior 에 PROPAGATION_REQUIRES_NEW` 옵션을 주면 내부 트랜잭션을 시작할 때 기존 트랜잭션에 참여하는 것이 아니라 새로운 물리 트랜잭션을 만들어서 시작하게 된다.



```
2023-07-06 07:49:56.728  INFO 42561 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 외부 트랜잭션 시작
2023-07-06 07:49:56.730 DEBUG 42561 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Creating new transaction with name [null]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2023-07-06 07:49:56.730 DEBUG 42561 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Acquired Connection [HikariProxyConnection@933346995 wrapping conn0: url=jdbc:h2:mem:2111df70-b349-4337-8513-c23dabc5d25e user=SA] for JDBC transaction
2023-07-06 07:49:56.731 DEBUG 42561 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Switching JDBC Connection [HikariProxyConnection@933346995 wrapping conn0: url=jdbc:h2:mem:2111df70-b349-4337-8513-c23dabc5d25e user=SA] to manual commit
2023-07-06 07:49:56.732  INFO 42561 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : outer.isNewTransaction()=true
2023-07-06 07:49:56.732  INFO 42561 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 내부 트랜잭션 시작
2023-07-06 07:49:56.732 DEBUG 42561 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Suspending current transaction, creating new transaction with name [null]
2023-07-06 07:49:56.732 DEBUG 42561 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Acquired Connection [HikariProxyConnection@1559760379 wrapping conn1: url=jdbc:h2:mem:2111df70-b349-4337-8513-c23dabc5d25e user=SA] for JDBC transaction
2023-07-06 07:49:56.732 DEBUG 42561 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Switching JDBC Connection [HikariProxyConnection@1559760379 wrapping conn1: url=jdbc:h2:mem:2111df70-b349-4337-8513-c23dabc5d25e user=SA] to manual commit
2023-07-06 07:49:56.733  INFO 42561 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : inner.isNewTransaction()=true
2023-07-06 07:49:56.733  INFO 42561 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 내부 트랜잭션 롤백
2023-07-06 07:49:56.733 DEBUG 42561 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Initiating transaction rollback
2023-07-06 07:49:56.733 DEBUG 42561 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Rolling back JDBC transaction on Connection [HikariProxyConnection@1559760379 wrapping conn1: url=jdbc:h2:mem:2111df70-b349-4337-8513-c23dabc5d25e user=SA]
2023-07-06 07:49:56.734 DEBUG 42561 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Releasing JDBC Connection [HikariProxyConnection@1559760379 wrapping conn1: url=jdbc:h2:mem:2111df70-b349-4337-8513-c23dabc5d25e user=SA] after transaction
2023-07-06 07:49:56.734 DEBUG 42561 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Resuming suspended transaction after completion of inner transaction
2023-07-06 07:49:56.734  INFO 42561 --- [    Test worker] hello.springtx.propagation.BasicTxTest   : 외부 트랜잭션 커밋
2023-07-06 07:49:56.734 DEBUG 42561 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Initiating transaction commit
2023-07-06 07:49:56.734 DEBUG 42561 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Committing JDBC transaction on Connection [HikariProxyConnection@933346995 wrapping conn0: url=jdbc:h2:mem:2111df70-b349-4337-8513-c23dabc5d25e user=SA]
2023-07-06 07:49:56.735 DEBUG 42561 --- [    Test worker] o.s.j.d.DataSourceTransactionManager     : Releasing JDBC Connection [HikariProxyConnection@933346995 wrapping conn0: url=jdbc:h2:mem:2111df70-b349-4337-8513-c23dabc5d25e user=SA] after transaction

```

- 내/외부 트랜잭션 모두 신규 트랜잭션. 물리 트랜잭션을 시작한다. 내부 트랜잭션에서 `PROPAGATION_REQUIRES_NEW` 옵션을 사용했기 때문.
- 내부 트랜잭션은 신규 트랜잭션이므로 롤백하면 실제 물리 트랜잭션을 롤백. `conn1` 커넥션 사용
- 외부 트랜잭션도 신규 트랜잭션이므로 커밋하면 실제 물리 트랜잭션을 커밋. `connn0` 커넥션 사용
- 참고: `REQUIRES_NEW`를 사용하면 DB 커넥션이 동시에 두 개가 사용된다는 점 주의!



## 다양한 전파 옵션

전파 옵션 디폴트: `REQUIRED`. 실무에서는 대부분 REQUIRED 옵션 사용. 가끔씩 `REQUIRES_NEW` 사용..



**REQUIRED**

- 가장 많이 사용하는 기본 설정. **기존 트랜잭션이 없으면 생성하고, 있으면 참여한다.**
- 트랜잭션이 필수라는 의미



**REQUIRES_NEW**

- **항상 트랜잭션 생성한다.**



**SUPPORT**

- **트랜잭션을 지원한다.** 기존 트랜잭션이 없으면 없는 대로 진행하고, 있으면 참여한다.



**NOT_SUPPORT**

- **트랜잭션을 지원하지 않는다.** 기존 트랜잭션이 있든 없든 트랜잭션 없이 진행한다. 기존 트랜잭션은 보류한다.



**MANDATORY**

- **트랜잭션이 반드시 있어야 한다.** 기존 트랜잭션이 없으면 **예외가 발생!** (`IllegalTransactionStateException`)



**NEVER**

- **트랜잭션을 사용하지 않는다.** 기존 트랜잭션이 존재하면 예외 발생!



**NESTED**

- **기존 트랜잭션이 없으면 새로운 트랜잭션 생성**
- **기존 트랜잭션이 있으면 중첩 트랜잭션 생성**
  - 중첩 트랜잭션은 외부 트랜잭션의 영향을 받지만, 외부에 영향을 주지 않는다.
  - 중첩 트랜잭션은 롤백되어도 외부 트랜잭션은 커밋 가능
  - 외부 트랜잭션이 롤백되면 중첩 트랜잭션도 롤백
- JPA에서 사용 불가