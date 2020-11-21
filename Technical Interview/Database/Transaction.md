## 트랜잭션이란?

트랜잭션이란 **데이터베이스의 상태를 변화시키기 위해 수행하는 작업의 단위**이다. 트랜잭션은 작업의 **완전성**을 보장해주는 것이다. 즉, 논리적인 작업 셋을 모두 완벽하게 처리하거나 또는 처리하지 못할 경우에는 원 상태로 복구해서 작업의 일부만 적용되는 현상이 발생하지 않도록 만들어주는 기능이다. 사용자의 입장에서는 작업의 논리적 단위로 이해를 할 수 있고, 시스템의 입장에서는 데이터들을 접근 또는 변경하려는 프로그램의 단위가 된다.



#### [트랜잭션의 특징 - ACID]

트랜잭션은 다음의 4 가지 특성을 만족해야 한다. 

**원자성 (Atomicity)**

**All or Nothing**. 만약 트랜잭션 중간에 어떠한 문제가 발생한다면 트랜잭션에 해당하는 어떠한 작업 내용도 수행되어서는 안되며, 아무런 문제가 발생되지 않았을 경우에만 모든 작업이 수행되어야 한다. 이를 위한 SQL statement는 COMMIT/ROLLBACK.

**일관성 (Consistency)**

트랜잭션은 데이터베이스를 **하나의 consistent state에서 또 다른 consistent state로 옮기는 것**이어야 한다. 이를 위해 DBMS는 **integrity constraints(무결성 제약 조건)를 만족시켜야 한다**.

**고립성 (Isolation) **

여러 트랜잭션들이 동시에 수행되더라도 그들을 차례로 하나씩 수행한 것과 동일한 효과를 가져야 한다. 이를 위해 DBMS는 concurrency control을 제공한다. 대표적인 방법이 LOCK이다. 이를 통해 **트랜잭션이 독립적으로 실행**되도록 보장한다.

**지속성 (Durability)**

하나의 트랜잭션이 수행한 결과는 **다른 트랜잭션에 의해 변경되지 않는 한 영구적으로 보존되어야 한다**. 이를 위해 DBMS는 Backup & Recovery를 제공한다.



#### [Integrity Constraints]

데이터의 무결성을 유지하려는 DBMS의 주요 기능으로, 데이터에 적용되는 연산에 제한을 두어 **데이터의 무결성**을 유지한다.

- **Entity Integrity Constraints** - 모든 테이블이 Primary Key로 선택된 필드를 가져야 한다. Primary Key로 지정된 필드는 고유한 값을 가져야 하며, NULL을 허용하지 않는다.
- **Referential Integrity Constraints** - 참조 관계에 있는 두 테이블의 데이터가 항상 일관되게 유지되는 것을 말한다. A 테이블에서 B번째 튜플이 C 테이블의 D번째 튜플을 참고하고 있는데, 만약 C 테이블에서 D번째 튜플을 삭제하면, A 테이블의 B번째 튜플은 더 이상 존재하지 않는 데이터를 참조하게 된다. 이러한 허상 참조를 방지하기 위해 존재하는 제약 조건이다. 관련 기능: RESTRICTED, CASCADE, SET NULL 등
  - **RESTRICTED** - 레코드를 변경 또는 삭제하고자 할 때, 해당 레코드를 참조하고 있는 개체가 있다면 변경 또는 삭제 연산을 취소한다.
  - CASCADE - 레코드를 변경 또는 삭제하면 해당 레코드를 참조하고 있는 개체도 변경 또는 삭제된다.
  - **SET NULL** - 레코드를 변경 또는 삭제하면 해당 레코드를 참조하고 있는 개체의 해당 필드 값을 NULL로 설정한다.
  - **SET DEFAULT** - 레코드를 변경 또는 삭제하면 해당 레코드를 참조하고 있는 개체의 해당 필드 값을 DEFAULT로 설정한다.
  - **NO ACTION** - 일단 삭제 또는 수정한 후, 참조 검사를 한다.
- **Domain Integrity Constraints** - 테이블에 존재하는 필드에 대해 default 값, data type, null 허용, 범위 등을 지정하여 올바른 데이터가 저장될 수 있도록 제한을 두는 것이다.
- **Key Constraints** - Primary Key는 Candidate Key와 같이 유일성과 최소성에 대한 속성을 가져야 하며, NULL 값을 절대 가질 수 없다.





### 트랜잭션 격리 수준 (Transaction Isolation Level)

**Isolation level**이란 **트랜잭션에서 일관성없는 데이터를 허용하도록 하는 수준**을 말한다. 데이터베이스는 ACID 특징과 같이 트랜잭션이 독립적인 수행을 하도록 한다. 따라서 LOCKing을 통해, 트랜잭션이 DB를 다루는 동안 다른 트랜잭션이 관여하지 못하도록 막는 것이 필요하다. 하지만 무조건 locking으로 동시에 수행되는 수많은 트랜잭션들을 순서대로 처리하는 방식으로 구현하게 되면 데이터베이스의 성능은 떨어지게 될 것이다. 그렇다고 해서, 성능을 높이기 위해 locking의 범위를 줄인다면, 잘못된 값이 처리될 문제가 발생하게 된다.

-> 따라서 최대한 효율적인 locking 방법이 필요한 것이다.



#### [낮은 단계의 Isolation Level을 활용할 때 발생하는 현상들]

**Dirty Read**

다른 트랜잭션에 의해 수정되었지만 **아직 COMMIT 되지 않은 데이터를 읽는 것**을 말한다. 변경 후 아직 COMMIT 되지 않은 값을 읽었는데, 변경을 가한 트랜잭션이 최종적으로 ROLLBACK을 한다면, 그 값을 이미 읽어버린 트랜잭션은 Inconsistent한 상태에 놓이게 된다.

**Non-Repeatable Read**

하나의 트랜잭션 내에서 같은 쿼리를 두 번 수행하는데, 그 사이에서 **다른 트랜잭션이 값을 수정 또는 삭제하고 COMMIT 하여 앞의 쿼리와 뒤의 쿼리의 결과가 다르게 나타나는 현상**을 말한다.

**Phantom Read**

하나의 트랜잭션 내에서 같은 쿼리를 두 번 수행하는데, **첫 번째 쿼리의 결과에 없던 Phantom Record가 두 번째 쿼리의 결과에 나타나는 현상**을 말한다. 





#### [Isolation level 종류]

![img](https://lh5.googleusercontent.com/tZw21bL_9t54TegSAmMPDHePlYr5Yp43L3Jm4M_4pXjmMNAdAG2DrdunrxeFDG6mLQS_2atKCpp-dRe1SjFZb7S0H4g0_gGFxKpQ4ZZLppbDBfRTJOs63W6Xq0Ujhc3o0MfzUFSC)



1. **Read Uncommitted (레벨 0)**

   SELECT 문장이 수행되는 동안 해당 데이터에 shared lock이 걸리지 않는 계층.

   

   **트랜잭션이 처리중이거나, 아직 COMMIT 되지 않은 데이터를 다른 트랜잭션이 읽는 것을 허용**한다.

   e.g) 사용자1이 A라는 데이터를 B라는 데이터로 변경하는 동안 사용자2는 아직 완료되지 않은(Uncommitted) 트랜잭션이지만 데이터B를 읽을 수 있다.

   데이터베이스의 **일관성을 유지하는 것이 불가능**하다.

   

2. **Read Comitted (레벨 1)**

   SELECT 문장이 수행되는 동안 해당 데이터의에 shared lock이 걸리는 계층.

   

   **트랜잭션이 수행되는 동안 다른 트랜잭션이 접근할 수 없어 대기**하게 된다.

   **COMMIT이 이루어진 트랜잭션만 조회 가능**하도록 허용함으로써 Dirty Read를 방지해준다. 하지만 COMMIT된 데이터만 읽더라도 Non-Repeatable Read와 Phantom Read 현상을 막지는 못한다. 읽는 시점에 따라 결과가 다를 수 있기 때문이다. 하나의 트랜잭션 내에서 쿼리를 두 번 수행했는데 두 쿼리 사이에 다른 트랜잭션이 값을 변경/삭제하거나 새로운 레코드를 삽입하는 경우이다.

   Read Committed는 SQL 서버가 Default로 사용하는 Isolation level이다.

   e.g) 사용자1이 A라는 데이터를 B라는 데이터로 변경하는 동안 사용자2는 해당 데이터에 접근이 불가능하다.

   

3. **Repeatable Read (레벨 2)**

   트랜잭션이 완료될 때까지 SELECT 문장이 사용하는 모든 데이터에 shared lock이 걸리는 계층.

   

   트랜잭션이 범위 내에서 조회한 데이터의 내용이 항상 동일함을 보장한다. 다른 사용자는 트랜잭션 영역에 해당되는 데이터를 수정할 수 없다. 하지만 이는 Phantom Read 현상을 막지 못한다. 첫 번째 쿼리에서 없던 새로운 레코드가 나타날 수 있기 때문이다. 한 트랜잭션 내에서 쿼리를 두 번 수행했는데 두 쿼리 사이에 다른 트랜잭션이 새로운 레코드를 삽입하는 경우이다. (SELECT ~ FOR UPDATE 문: 특정 트랜잭션이 해당 데이터에 대해 UPDATE 할 때까지 LOCK을 걸어놔서 다른 트랜잭션이 접근하지 못하게 한다.)

   

4. **Serializable (레벨 3)**

   트랜잭션이 완료될 때까지 SELECT 문장이 사용하는 모든 데이터에 shared lock이 걸리는 계층.

   
   
   완벽한 읽기 일관성 모드를 제공한다. 다른 사용자는 트랜잭션 영역에 해당되는 데이터에 대한 수정 및 입력이 불가능하다.

















##### Reference

https://github.com/JaeYeopHan/Interview_Question_for_Beginner/tree/master/Database

https://gyoogle.dev/blog/computer-science/data-base/Transaction%20Isolation%20Level.html