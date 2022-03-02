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



### 6.2 고립된 단위 테스트

가장 좋은 테스트 방법은 가능한 한 작은 단위로 쪼개서 테스트하는 것이다. 테스트가 실패했을 때 원인을 찾기 쉽기 때문이다. 반대로 테스트가 진행되는 동안 실행된 코드의 양이 많다면 원인을 찾기 어려울 것이다. 

하지만 작은 단위로 테스트하고 싶어도 그럴 수 없는 경우가 많다. 테스트 대상이 다른 오브젝트나 환경에 의존적이라면 작은 단위의 테스트가 주는 장점을 얻기 힘들다.



`UserServiceTest`의 테스트 대상은 `UserService` 클래스이다. `UserService` 는 `UserDao`, `TransactionManager` 에 대한 의존관계를 갖고 있기 때문에 테스트가 진행되는 동안 같이 실행된다. 즉, `UserService` 만 테스트하는 것이 아닌 그 뒤에 있는 훨씬 더 많은 오브젝트와 환경, 서비스, 네트워크 등을 함께 테스트하는 셈이 된다. `UserService` 코드에 문제가 없더라도 다른 이유때문에 테스트가 실패할 수 있다.



그래서 테스트의 대상이 외부 환경 등에 영향받지 않도록 고립시켜야 한다. 테스트를 의존 대상으로부터 분리해서 고립시키는 방법은 테스트를 위한 대역`Test Double` 을 사용하는 것이다. 트랜잭션 코드를 독립시켰기 때문에 `UserServiceImpl` 은  `PlatformTransactionManager`에 더 이상 의존적이지 않으므로 이에 대한 대역은 필요없다. `UserDao` 는 테스트의 대상이 정상적으로 수행되도록 도와주기만 하는 스텁이 아니라, 부가적인 검증 기능까지 추가한 목 오브젝트로 만든다. `UserServiceImpl`의 `upgradeLevels()` 메서드는 리턴값이 없기 때문에 메서드를 실행하고 그 결과를 받아서 검증하는 것이 불가능하기 때문이다.



목 오브젝트는 스텁과 같은 방식으로 테스트 대상을 통해 사용될 때 필요한 기능을 지원해주어야 한다.



`MockUserDao` 는 어차피 `UserServiceTest` 에서만 쓸 것이니 다음과 같이 `UserServiceTest` 내부에 스태틱 내부 클래스로 만들면 편하다.

```java
@Getter
static class MockUserDao implements UserDao {

  private List<User> users;
  private List<User> updated = new ArrayList<>();
  
  private MockUserDao(List<User> users) {
    this.users = users;
  }

  @Override
  public List<User> getAll() {
    return this.users;	// 스텁 기능 제공
  }

  @Override
  public void update(User user) {
    updated.add(user);	// 목 오브젝트 기능 제공
  }	
    
  @Override
  public void add(User user) {
    throw new UnsupportedOperationException();
  }

  @Override
  public User get(String id) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteAll() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getCount() {
    throw new UnsupportedOperationException();
  }
}
```



인터페이스를 구현하면 사용하지 않는 메서드도 모두 만들어주어야 한다는 부담이 있다. 이럴 땐 `UnsupportedOperationException` 을 던지도록 만드는 것이 좋다. 빈 채로 두거나 `null` 리턴해도 문제될 것은 없지만 실수로 사용했을 때 `UnsupportedOperationException` 을 던짐으로써 지원하지 않는 기능이라는 예외를 명시해주면 좋다.



```java
  @Test
  public void upgradeLevels() throws Exception {
    UserServiceImpl userServiceImpl = new UserServiceImpl();

    MockUserDao mockUserDao = new MockUserDao(this.users);
    UserLevelUpgradePolicy userLevelUpgradePolicy = new UserLevelUpgradePolicyImpl();
    userServiceImpl.setUserDao(mockUserDao);
    userServiceImpl.setUserLevelUpgradePolicy(userLevelUpgradePolicy);

    userServiceImpl.upgradeLevels();

    List<User> updated = mockUserDao.getUpdated();
    assertThat(updated.size(), is(2));
    checkUserAndLevel(updated.get(0), "bbb", Level.SILVER);
    checkUserAndLevel(updated.get(1), "ddd", Level.GOLD);
  }

  private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel) {
    assertThat(updated.getId(), is(expectedId));
    assertThat(updated.getLevel(), is(expectedLevel));
  }
```



고립된 테스트에서는 테스트 대상 오브젝트(`UserServiceImpl`)를 직접 생성하면 된다. 독립적으로 동작하는 테스트 대상이기 때문에 스프링 컨테이너에서 빈을 가져올 필요가 없다. 



단위 테스트를 만들기 위해서는 스텁이나 목 오브젝트 사용이 필수적이다. 많은 코드가 대부분 의존 오브젝트를 필요로 하기 때문이다. 하지만 단위 테스트는 목 오브젝트를 만드는 등 작성이 번거롭다는 단점이 있다. 특히 테스트 메서드별로 다른 검증이 필요하다면, 같은 의존 인터페이스를 구현한 여러 개의 목 클래스를 선언해주어야 한다. 다행해도, 이런 번거로움을 해결해주는 다양한 목 오브젝트 지원 프레임워크가 있다.



위에서 직접 목 오브젝트를 선언해서 진행한 테스트를 Mockito를 이용해서 바꿔보자. Mockito와 같은 목 프레임워크는 목 클래스를 따로 선언할 필요없이, 간단한 메서드 호출만으로 테스트용 목 오브젝트를 만들 수 있다. 

``` java
UserDao mockUserDao = mock(UserDao.class);
```



이렇게 만들어진 목 오브젝트는 아무런 기능이 없다. 여기에 `getAll()` 메서드가 사용자 목록을 리턴하도록 스텁 기능을 추가해준다. 이는 다음 코드 한 줄로 가능하다.



``` java
when(mockUserDao.getAll()).thenReturn(this.users);
```



말 그대로, `mockUserDao`의 `getAll()` 메서드가 호출됐을 때(`when`), `users` 리스트를 리턴하라(`thenReturn`)는 말이다. 다음으로 `mockUserDao`의 `update()` 메서드가 두 번 호출됐는지 확인하고 싶다면, 다음과 같은 코드를 넣어주면 된다.



```java
verify(mockUserDao, times(2)).update(any((User.class)));
```



`User` 타입의 오브젝트를 파라미터로 받으며 `update()` 메서드가 두 번 호출됐는지 확인하라는 것이다.



Mockito 목 오브젝트는 다음 네 단계를 거쳐서 사용하면 된다. 두 번째와 네 번째는 필요한 경우에만 사용할 수 있다.

- 인터페이스를 이용해 목 오브젝트를 만든다.
- 목 오브젝트가 리턴할 값이 있으면 이를 지정해준다. 메서드가 호출되면 예외를 강제로 던지게 만들 수도 있다.
- 테스트 대상 오브젝트에 DI 해서 목 오브젝트가 테스트 중에 사용되도록 만든다.
- 테스트 대상 오브젝트를 사용한 후에 목 오브젝트의 특정 메서드가 호출됐는지, 어떤 값을 가지고 몇 번 호출됐는지를 검증한다.



Mockito를 사용한 `upgradeLevels()` 메서드는 다음과 같다.

```java
@Test
public void upgradeLevels() throws Exception {
  UserServiceImpl userServiceImpl = new UserServiceImpl();

  UserLevelUpgradePolicy userLevelUpgradePolicy = new UserLevelUpgradePolicyImpl();
  UserDao mockUserDao = mock(UserDao.class);
  when(mockUserDao.getAll()).thenReturn(this.users);

  userServiceImpl.setUserDao(mockUserDao);
  userServiceImpl.setUserLevelUpgradePolicy(userLevelUpgradePolicy);

  userServiceImpl.upgradeLevels();

  verify(mockUserDao, times(2)).update(any((User.class)));
  verify(mockUserDao).update(users.get(1));
  assertThat(users.get(1).getLevel(), is(Level.SILVER));
  verify(mockUserDao).update(users.get(3));
  assertThat(users.get(3).getLevel(), is(Level.GOLD));
}
```



### 6.3 다이나믹 프록시와 팩토리 빈



### 6.4 스프링의 프록시 팩토리 빈



### 6.5 스프링 AOP



### 6.6 트랜잭션 속성



### 6.7 어노테이션 트랜잭션 속성과 포인트컷



### 6.8 트랜잭션 지원 테스트

