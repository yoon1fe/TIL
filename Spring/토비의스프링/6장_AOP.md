AOP(`Aspect Oriented Programming`) 는 IoC/DI, 서비스 추상화와 더불어 스프링의 3대 기반 기술 중 하나이다. AOP의 등장 배경과, AOP를 적용함으로써 얻을 수 있는 장점이 무엇인지 이해하고 AOP의 가치를 이해해보자.

스프링에 적용된 가장 인기있는 AOP의 적용 대상은 **선언적 트랜잭션 기능**이다. AOP를 이용하면 서비스 추상화를 통해 많은 근본적인 문제를 해결했던 트랜잭션 경계 설정 기능을 더욱 세련되고 깔끔하게 바꿀 수 있다.



### 6.1 트랜잭션 코드의 분리

서비스 추상화 기법을 적용해 `UserService` 코드를 트랜잭션 기술에 독립적이고 깔끔하게 만들었지만, 트랜잭션 경계 설정을 위한 코드가 맘에 안든다. 비즈니스 로직이 주인이어야 할 메서드 안에 트랜잭션 코드가 더 많은 자리를 차지하고 있다. `upgradeLevels()` 메서드를 다시 보면 다음과 같다.

```java
public void upgradeLevels() throws SQLException {
  PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
  TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

  try {
    List<User> users = userDao.getAll();

    for (User user : users) {
      if (userLevelUpgradePolicy.canUpgradeLevel(user)) {
        userLevelUpgradePolicy.upgradeLevel(user);
        userDao.update(user);
      }
    }

    transactionManager.commit(status);
  } catch (Exception e) {
    transactionManager.rollback(status);
    throw e;
  }
}
```



얼핏 보면 복잡해 보이지만 사실 위 코드는 두 가지 종류의 코드로 구분되어 있다. 비즈니스 로직 코드를 사이에 두고 트랜잭션 시작/종료 코드가 앞뒤로 위치하고 있다. 또한, 트랜잭션 경계 설정 코드와 비즈니스 로직 코드 사이에 서로 주고받는 정보가 없다. 즉, 이 두 코드는 완전히 독립적인 코드이다. 다만 비즈니스 로직 부분이 트랜잭션의 시작과 종료 작업 사이에서 수행되어야 한다는 것만 지켜지면 된다.

먼저 비즈니스 로직을 담당하는 코드를 메서드로 추출해서 독립시켜 본다.

```java
public void upgradeLevels() throws SQLException {
  PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
  TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

  try {
    upgradeLevelsInternal();

    transactionManager.commit(status);
  } catch (Exception e) {
    transactionManager.rollback(status);
    throw e;
  }
}

private void upgradeLevelsInternal() {
  List<User> users = userDao.getAll();

  for (User user : users) {
    if (userLevelUpgradePolicy.canUpgradeLevel(user)) {
      userLevelUpgradePolicy.upgradeLevel(user);
      userDao.update(user);
    }
  }
}
```



이전보다 깔끔해졌지만, 여전히 트랜잭션을 담당하는 코드가 `UserService` 내부에 있다. 다만, 만약 트랜잭션 코드를 밖으로 뺀다면 `UserService` 를 사용하는 클라이언트 코드에서는 트랜잭션 기능이 빠진 채 사용하게 될 것이다. 이는 구체적인 구현 클래스를 직접  참조하는 경우의 전형적인 단점이다. 그렇다면 이는 클라이언트와 구현 클래스 사이에 인터페이스를 두면 된다. DI의 기본 아이디어는 실제로 사용할 오브젝트의 클래스 정체는 감춘 채 인터페이스를 통해 간접적으로 접근하는 것이다.

`UserService`를 인터페이스로 만들고, 기존 코드는 `UserService` 인터페이스의 구현 클래스에 넣으면 클라이언트와의 결합은 약해지고, 직접 구현 클래스에 의존하지 않기 때문에 유연한 확장이 가능해진다.



트랜잭션의 경계 설정 책임만을 맡는 `UserService` 인터페이스를 구현하는 새로운 구현 클래스를 둘 수 있다. 스스로 비즈니스 로직은 담고 있지 않기 때문에 또 다른 비즈니스 로직을 담고 있는 `UserService` 의 구현 클래스에 실질적인 로직 처리 작업을 위임하는 것이다. 따라서 `UserServiceImpl` 에는 처음 코드 그대로 로직만을 구현했던 모습으로 돌아왔다.

```java
package springbook.user.service;

@Setter
@Getter
public class UserServiceImpl implements UserService {

  public static final int MIN_LOGCOUNT_FOR_SILVER = 50;
  public static final int MIN_RECOMMEND_FOR_GOLD = 30;
  UserDao userDao;
  UserLevelUpgradePolicy userLevelUpgradePolicy;
  
  public void upgradeLevels() {
    List<User> users = userDao.getAll();

    for (User user : users) {
      if (userLevelUpgradePolicy.canUpgradeLevel(user)) {
        userLevelUpgradePolicy.upgradeLevel(user);
        userDao.update(user);
      }
    }
  }
}
```



비즈니스 트랜잭션 처리를 담은 `UserServiceTx`는 `UserService` 를 구현하고, 같은 인터페이스를 구현한 다른 오브젝트에게 고스란히 작업을 위임하도록 만들면 된다.



```java
package springbook.user.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import springbook.user.domain.User;

@Getter
@Setter
public class UserServiceTx implements UserService {
  UserService userService;
  PlatformTransactionManager transactionManager;

  @Override
  public void upgradeLevels() {
    userService.upgradeLevels();
  }

  @Override
  public void add(User user) {
    TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
    try {
      userService.upgradeLevels();
      this.transactionManager.commit(status);
    } catch (RuntimeException e) {
      this.transactionManager.rollback(status);
      throw e;
    }
  }
}
```



트랜잭션 경계 설정 코드의 분리의 장점을 크게 두 가지가 있다.

첫째, 비즈니스 로직을 담당하고 있는 클래스를 작성할 때 트랜잭션과 같은 기술적인 내용을 신경쓰지 않아도 된다. 트랜잭션은 DI를 이용해 트랜잭션 기능을 가진 오브젝트가 먼저 실행되도록만 하면 된다.

둘째, 비즈니스 로직에 대한 테스트를 손쉽게 만들 수 있다는 점이다.
