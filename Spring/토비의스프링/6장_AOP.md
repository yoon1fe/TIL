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

트랜잭션만을 위한 `UserServiceTx` 클래스를 만들고 여기서만 트랜잭션 관리를 처리했기 때문에, `UserServiceImpl` 에는 트랜잭션 관련 코드가 하나도 남지 않게 되었다. 이렇게 분리된 부가 기능을 담은 클래스는 중요한 특징이 있다. 바로 부가 기능을 제외한 나머지 기능은 원래 핵심 기능을 가진 클래스로 위임해주어야 한다.

이렇게 구성했을 때의 문제는 클라이언트가 핵심 기능을 가진 클래스를 직접 사용해버리면 부가 기능이 적용될 수 없다는 점이다. 그래서 부가 기능을 가진 클래스가 클라이언트로부터 하여금 자신을 거쳐서 핵심 기능을 사용하도록 만들어야 한다. 그러기 위해서는 클라이언트는 인터페이스를 통해서만 핵심 기능을 사용하게 하고, 부가 기능 자신도 같은 인터페이스를 구현한 뒤에 자신이 그 사이에 끼어 들어야 한다. 

이렇게 자신이 클라이언트가 사용하려 하는 실제 대상인 것처럼 위장해서 클라이언트의 요청을 받아주는 것을 **프록시`proxy`** 라고 부른다. 그리고 프록시를 통해 최종적으로 요청을 위임받아 처리하는 실제 오브젝트를 타겟 또는 실체라고 부른다.

프록시의 사용 목적은 크게 두 가지로 구분된다. 첫째는 클라이언트가 타겟에 접근하는 방법을 제어하기 위함이다. 두 번째는 타겟에 부가적인 기능을 부여해주기 위해서다. 이 두 경우는 목적에 따라서 디자인 패턴에서는 다른 패턴으로 구분한다.



**데코레이터 패턴**은 **타겟에 부가적인 기능을 런타임 시에 다이나믹하게 부여**해주기 위해 프록시를 사용하는 패턴이다. 데코레이터 패턴에서는 두 개 이상의 프록시를 사용할 수 있다. 순서를 정해서 단계적으로 위임하는 구조로 만들 수 있다. 프록시로서 동작하는 각 데코레이터는 위임하는 대상에도 인터페이스로 접근하기 때문에 다음 위임 대상을 인터페이스로 선언하고 런타임 시에 주입받을 수 있도록 만들어야 한다. 



디자인 패턴에서 말하는 **프록시 패턴은** 타겟에 대한 접근 방법을 제어한다는 의미가 강하다. 프록시 패턴에서의 프록시는 타겟의 기능을 확장하거나 추가하지 않고, **클라이언트가 타겟에 접근하는 방식을 변경**해준다. 타겟 오브젝트는 생성하기 복잡하거나 당장 필요하지 않은 경우에는 필요한 시점까지 오브젝트를 생성하지 않는 것이 좋다. 그런데 타겟 오브젝트에 대한 레퍼런스가 미리 필요할 경우에 프록시 패턴을 적용하면 된다. 프록시의 메서드를 통해 타겟 오브젝트를 사용하려고 시도하면, 그때 프록시가 타겟 오브젝트를 생성하고 요청을 위임해주는 식이다.

특정 상황에서 타겟에 대한 접근 권한을 제어할 때도 프록시 패턴을 사용할 수 있다. 수정 가능한 오브젝트가 있는데 언젠가 읽기 전용으로만 동작해야 한다면 프록시를 만들어서 이 프록시의 특정 메서드를 사용하려 하면 접근이 불가능하다고 예외를 발생시키면 된다. `Collections` 의 `unmodifiableCollection()` 을 통해 만들어지는 오브젝트가 전형적인 접근 권한 제어용 프록시이다.



프록시는 기존 코드에 영향을 주지 않으면서 새로운 기능을 추가할 수 있는 유용한 방법이지만, 만드는 일이 상당히 번거롭다. 자바에는 `java.lang.reflect` 패키지 안에 프록시를 쉽게 만들 수 있도록 도와주는 클래스들이 있다. 목 프레임워크와 비슷하게, 프록시처럼 동작하는 오브젝트를 동적으로 생성한다.

다이나믹 프록시는 리플렉션 기능을 이용해서 프록시를 만들어준다. 리플렉션은 자바의 코드 자체를 추상화해서 접근하도록 만든 것이다.



```java
package springbook.learningtest.junit;
...
public class ReflectionTest {

    @Test
    public void invokeMethod() throws Exception {
        String name = "Spring";

        // length()
        assertThat(name.length(), is(6));

        Method lengthMethod = String.class.getMethod("length");
        assertThat((Integer) lengthMethod.invoke(name), is(6));

        // charAt()
        assertThat(name.charAt(0), is('S'));

        Method charAtMethod = String.class.getMethod("charAt", int.class);
        assertThat((Character) charAtMethod.invoke(name, 0), is('S'));
    }
}
```



다이나믹 프록시를 이용한 프록시를 만들어보자. 프록시를 적용할 타겟 클래스와 인터페이스를 만든다.



```java
package springbook.user.hello;

public interface Hello {

    String sayHello(String name);
    String sayHi(String name);
    String sayThankYou(String name);
}
```



```java
package springbook.user.hello;

public class HelloTarget implements Hello {

    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }

    @Override
    public String sayHi(String name) {
        return "Hi " + name;
    }

    @Override
    public String sayThankYou(String name) {
        return "Thank You " + name;
    }
}
```



이제 `Hello` 인터페이스를 구현한 프록시를 만들어보자. 프록시에는 데코레이터 패턴을 적용해서 타겟인 `HelloTarget` 에 리턴하는 문자를 대문자로 바꾸는 부가 기능을 추가한다. 위임과 기능 부가라는 두 가지 프록시의 기능을 모두 처리하는 클래스이다.



```java
package springbook.user.hello;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HelloUppercase implements Hello {
    Hello hello;
    
    @Override
    public String sayHello(String name) {
        return hello.sayHello(name).toUpperCase();      // 위임 + 부가 기능
    }

    @Override
    public String sayHi(String name) {
        return hello.sayHi(name).toUpperCase();
    }

    @Override
    public String sayThankYou(String name) {
        return hello.sayThankYou(name).toUpperCase();
    }
}
```



```java
@Test
public void simpleProxy() {
    Hello hello = new HelloTarget();    // 타겟은 인터페이스를 통해 접근

    assertThat(hello.sayHello("Toby"), is("Hello Toby"));
    assertThat(hello.sayHi("Toby"), is("Hi Toby"));
    assertThat(hello.sayThankYou("Toby"), is("Thank You Toby"));

    Hello proxiedHello = new HelloUppercase(new HelloTarget());
    assertThat(proxiedHello.sayHello("Toby"), is("HELLO TOBY"));
    assertThat(proxiedHello.sayHi("Toby"), is("HI TOBY"));
    assertThat(proxiedHello.sayThankYou("Toby"), is("THANK YOU TOBY"));
}
```



이 프록시는 프록시 적용의 일반적인 문제점 두 가지 모두 갖고 있다. 1. 인터페이스의 모든 메서드를 구현해 위임하도로록 해야 하고, 2. 부가 기능인 리턴값을 대문자로 바꾸는 기능이 모든 메서드에서 중복된다.



클래스로 만든 프록시인 `HelloUppercase` 를 다이나믹 프록시를 이용해 만들어보자. 다이나믹 프록시는 프록시 팩토리에 의해 런타임 시에 동적으로 만들어지는 오브젝트이다. 다이나믹 프록시 오브젝트는 타겟의 인터페이스와 같은 타입으로 만들어진다. 부가 기능은 프록시 오브젝트와는 독립적으로 `InvocationHandler` 를 구현한 오브젝트에 담는다.



```java
@AllArgsConstructor
public class UppercaseHandler implements InvocationHandler {

    Hello target;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String ret = (String) method.invoke(target, args);
        return ret.toUpperCase();
        
    }
}
```



이 `InvocationHandler` 를 사용하고 `Hello` 인터페이스를 구현하는 프록시는 다음과 같이 만들 수 있다. 다이나믹 프록시 생성은 `Proxy` 클래스의 `newProxyInstance()` 스태틱 팩토리 메서드를 이용하면 된다.



```java
Hello proxiedHello = (Hello) Proxy.newProxyInstance(
        getClass().getClassLoader(),
        new Class[]{Hello.class},
        new UppercaseHandler(new HelloTarget()));
```



다이나믹 프록시 방식은 직접 정의해서 만든 프록시보다 훨씬 유연하고 많은 장점이 있다. 먼저, 만약 `Hello` 인터페이스의 메서드가 많아진다면 그 많은 메서드를 모두 직접 구현해야 하는 수고를 덜 수 있다. 두 번째 장점은 타겟의 종류에 상관없이도 적용 가능하다는 점이다.



어떤 종류의 인터페이스를 구현한 타겟이든 상관없이 재사용할 수 있고, 메서드의 리턴 타입이 스트링인 경우에만 대문자로 바꿔주도록 다음과 같이 코드를 수정할 수 있다.

```java
public class UppercaseHandler implements InvocationHandler {
    Object target;
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Object ret = method.invoke(target, args);
        if (ret instanceof String) {
            return ((String) ret).toUpperCase();
        } else {
            return ret;
        }
    }
}
```



이제 `UserServiceTx` 를 다이나믹 프록시 방식으로 변경해보자. 



```java
package springbook.user.service;

@AllArgsConstructor
@Setter
public class TransactionHandler implements InvocationHandler {

    private Object target;
    private PlatformTransactionManager transactionManager;
    private String pattern;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().startsWith(pattern)) {
            return invokeInTransaction(method, args);
        } else {
            return method.invoke(target, args);
        }
    }
    
    public Object invokeInTransaction(Method method, Object[] args) throws Throwable {
        TransactionStatus status=
                this.transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            Object ret = method.invoke(target, args);
            this.transactionManager.commit(status);
            return ret;
        } catch (InvocationTargetException e) {
            this.transactionManager.rollback(status);
            throw e.getTargetException();
        }
    }
}
```



다이나믹 프록시 오브젝트는 일반적인 스프링의 빈으로 등록할 방법이 없다. 스프링의 빈은 기본적으로 클래스 이름과 프로퍼티로 정의된다. 스프링은 지정된 클래스 이름을 갖고 리플렉션을 이용해서 해당 클래스의 오브젝트를 만든다. 다이나믹 프록시 오브젝트는 클래스가 어떤 것인지도 알 수 없기 때문에 스프링의 빈으로 등록할 수가 없다. 그래서 팩토리 빈을 이용한 빈 생성 방법으로 빈을 생성해야 한다. 팩토리 빈이란 스프링을 대신해서 오브젝트의 생성 로직을 담당하도록 만들어진 특별한 빈을 말한다.

팩토리 빈을 만드는 가장 간단한 방법은 스프링의 `FactoryBean` 이라는 인터페이스를 구현하는 것이다. 팩토리 빈의 `getObject()` 메서드에 다이나믹 프록시 오브젝트를 만들어주는 코드를 넣으면 된다.



```java
package springbook.user.service;

@Setter
public class TxProxyFactoryBean implements FactoryBean<Object> {

    Object target;
    PlatformTransactionManager transactionManager;
    String pattern;
    Class<?> serviceInterface;      // 다이나믹 프록시 생성할 때 필요

    @Override
    public Object getObject() throws Exception {
        TransactionHandler txHandler = TransactionHandler.builder()
                .target(target)
                .transactionManager(transactionManager)
                .pattern(pattern)
                .build();

        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{serviceInterface}, txHandler);
    }

    @Override
    public Class<?> getObjectType() {
        return serviceInterface;
    }
    
    public boolean isSingleton() {
        return false;
    }
}
```



이렇게 팩토리 빈이 만드는 다이나믹 프록시는 구현 인터페이스나 타겟의 종류에 제한이 없기 때문에 `UserService` 외에도 트랜잭션 부가 기능이 필요한 오브젝트를 위한 프록시를 만들 때 재사용이 가능하다.



프록시 팩토리 빈 방식의 장점으로는 재사용성이 있다. 또한, 데코레이터 패턴의 두 가지 문제점을 해결해준다. 

단점으로는 한 번에 여러 개의 클래스에 공통적인 부가기능을 제공하는 일은 불가능하단 점이 있다. 또 다른 문제점은 `TransactionHandler` 오브젝트가 프록시 팩토리 빈 개수만큼 만들어진다는 점이다. 즉, 설정의 중복이 많이 일어난다. 이러한 문제점을 **스프링의 프록시 팩토리 빈**이 해결해 줄 수 있다.



### 6.4 스프링의 프록시 팩토리 빈

스프링은 일관된 방법으로 프록시를 만들 수 있게 도와주는 추상 레이어를 제공한다. 생성된 프록시는 스프링의 빈으로 등록되어야 한다. 스프링은 **프록시 오브젝트를 생성해주는 기술을 추상화한 팩토리 빈**을 제공해준다.



스프링의 `ProxyFactoryBean` 은 프록시를 생성해서 빈 오브젝트로 등록하게 해주는 팩토리 빈이다. 프록시를 생성하는 작업만 담당하고, 프록시를 통해 제공해줄 부가 기능은 별도의 빈에 둘 수 있다. 프록시에서 사용할 부가 기능은 `MethodInterceptor` 인터페이스를 구현해서 만든다. `MethodInterceptor`의 `invoke()` 메서드는 `ProxyFactoryBean`으로부터 타겟 오브젝트에 대한 정보도 함께 제공받기 때문에 타겟 오브젝트에 상관없이 독립적으로 만들어질 수 있다. 따라서 `MethodInterceptor` 오브젝트는 타겟이 다른 여러 프록시에서 함께 사용할 수 있고, 싱글톤 빈으로 등록할 수 있다.



```java
@Test
public void proxyFactoryBean() {
    ProxyFactoryBean pfBean = new ProxyFactoryBean();
    pfBean.setTarget(new HelloTarget());
    pfBean.addAdvice(new UppercaseAdvice());

    Hello proxiedHello = (Hello) pfBean.getObject();

    assertThat(proxiedHello.sayHello("Toby"), is("HELLO TOBY"));
    assertThat(proxiedHello.sayHi("Toby"), is("HI TOBY"));
    assertThat(proxiedHello.sayThankYou("Toby"), is("THANK YOU TOBY"));
}

static class UppercaseAdvice implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        String ret = (String) methodInvocation.proceed();
        return ret.toUpperCase();
    }
}
```



`InvocationHandler`를 구현했을 때와 달리, `MethodInterceptor`를 구현한 `UpperAdvice`에는 타겟 오브젝트가 없다. `MethodInterceptor`로는 메서드 정보와 함께 타겟 오브젝트가 담긴 `MethodInvocation` 오브젝트가 전달된다. `MethodInterceptor`는 타겟 오브젝트의 메서드를 실행할 수 있는 기능이 있기 때문에 `MethodInterceptor`는 부가 기능을 제공하는 데만 집중할 수 있다. `MethodInvocation` 은 일종의 콜백 오브젝트로, `proceed()` 메서드를 호출하면 타겟 오브젝트의 메서드를 내부적으로 실행해준다. 즉, `MethodInvocation` 구현 클래스는 일종의 템플릿인 셈이다. 이 점이 바로 `ProxyFactoryBean` 의 장점이다. `ProxyFactoryBean` 은 작은 단위의 템플릿/콜백 구조를 응용해서 적용했기 때문에 템플릿 역할을 하는 `MethodInvocation`을 싱글톤으로 두고 공유할 수 있다. 또한, `ProxyFactoryBean` 에는 여러 개의 `MethodInterceptor`를 추가할 수 있다.



어드바이스`Advice`는 `MethodInterceptor` 처럼 타겟 오브젝트에 적용하는 부가 기능을 담은 오브젝트를 말한다. 어드바이스는 타겟 오브젝트에 종속되지 않는 순수한 부가 기능만을 담은 오브젝트이다.



`ProxyFactoryBean` 은 기본적으로 JDK가 제공하는 다이나믹 프록시를 만들어준다. 경우에 따라서는 CGLib라는 오픈소스 바이트코드 생성 프레임워크를 이용해 프록시를 만들기도 한다.



이전에 JDK 다이나믹 프록시를 이용한 방식에서는 `pattern` 을 통해 부가 기능을 적용시킬 메서드를 선정했었다. `ProxyFactoryBean` 를 사용하면 포인트컷을 사용해 메서드를 선정할 수 있다. **포인트컷**이란 메서드 선정 알고리즘을 담은 오브젝트를 말한다. 프록시는 클라이언트로부터 요청을 받으면 먼저 포인트컷에게 부가 기능을 부여할 메서드인지 확인해달라고 요청한다. 포인트컷은 `Pointcut` 인터페이스를 구현해서 만든다. 



```java
@Test
public void pointcutAdvisor() {
    ProxyFactoryBean pfBean = new ProxyFactoryBean();
    pfBean.setTarget(new HelloTarget());

    NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
    pointcut.setMappedName("sayH*");
    
    pfBean.addAdvisor(new DefaultBeanFactoryPointcutAdvisor(pointcut, new UppercaseAdvice()));
    
    Hello proxiedHello = (Hello) pfBean.getObject();

    assertThat(proxiedHello.sayHello("Toby"), is("HELLO TOBY"));
    assertThat(proxiedHello.sayHi("Toby"), is("HI TOBY"));
    assertThat(proxiedHello.sayThankYou("Toby"), is("Thank You Toby"));	// 포인트컷의 선정조건에 맞지 않음
}
```



포인트컷을 어드바이스와 함께 등록하기 위해서는 `Advisor`로 묶어서 한 번에 추가해야 한다. 따로 등록한다면 어떤 어드바이스에 대해 어떤 포인트컷을 적용할지 애매해지기 때문이다.



**어드바이저 = 포인트컷(메서드 선정 알고리즘) + 어드바이스(부가 기능)**



### 6.5 스프링 AOP

### 6.5 스프링 AOP

아직 부가 기능의 적용이 필요한 타겟 오브젝트마다 거의 비슷한 내용의 `ProxyFactoryBean` 빈 설정 정보를 추가해주어야 하는 문제가 있다. 프록시를 다이나믹하게 생성했던 것처럼, 반복적인 `ProxyFactoryBean` 설정 문제도 설정 자동 등록기법으로 해결하면 좋을 것 같다.



빈 후처리기는 스프링 빈 오브젝트가 만들어진 이후에 다시 가공할 수 있게 해준다. 빈 후처리기는 `BeanPostProcessor` 인터페이스를 구현해서 만든다. `DefaultAdvisorAutoProxyCreator` 는 어드바이저를 이용한 자동 프록시 생성기이다. 빈 후처리기를 빈으로 등록해두면, 스프링은 빈 오브젝트가 생성될 때마다 빈 후처리기에 보내서 후처리 작업을 요청한다. 



이를 잘 이용하면 스프링이 생성하는 빈 오브젝트의 일부를 프록시로 포장하고, 프록시를 빈으로 대신 등록할 수도 있다. 이것이 바로 **자동 프록시 생성 빈 후처리기**이다. 빈 후처리기는 프록시가 생성되면 원래 컨테이너가 전달해준 빈 오브젝트 대신 프록시 오브젝트를 컨테이너에게 돌려준다. 컨테이너는 최종적으로 빈 후처리기가 돌려준 오브젝트를 빈으로 등록하고 사용한다.



![img](https://blog.kakaocdn.net/dn/bkGXBZ/btqPAuX9Atp/2ZxRpcdGnUGPjWMlrLNQkK/img.png)



적용할 빈이 선정하는 로직이 추가된 포인트컷이 담긴 어드바이저를 등록하고 빈 후처리기를 사용하면 일일이 `ProxyFactoryBean` 빈을 등록하지 않아도 타겟 오브젝트에 자동으로 프록시가 적용되게 할 수 있게 된다. 포인트컷에는 메서드만 선정하는 것 뿐만 아니라, 어떤 빈에 프록시를 적용할지를 선택하는 기능도 있다.



```java
package org.springframework.aop;

public interface Pointcut {
    Pointcut TRUE = TruePointcut.INSTANCE;

    ClassFilter getClassFilter();				// 프록시를 적용할 클래스인지 확인

    MethodMatcher getMethodMatcher();		// 어드바이스를 적용할 메서드인지 확인
}
```



이제 클래스 필터를 적용한 포인트컷을 작성해보자. 메서드 이름만 비교하던 `NameMatchMethodPointcut` 을 상속해서 프로퍼티로 주어진 이름 패턴을 갖고 클래스 이름을 비교하는 `ClassFilter` 를 추가하면 된다. 



```java
public class NameMatchClassMethodPointcut extends NameMatchMethodPointcut {

    public void setMappedClassName(String mappedClassName) {
        this.setClassFilter(new SimpleClassFilter(mappedClassName));
    }

    static class SimpleClassFilter implements ClassFilter {
        String mappedName;
        public SimpleClassFilter(String mappedName) {
            this.mappedName = mappedName;
        }

        @Override
        public boolean matches(Class<?> aClass) {
            return PatternMatchUtils.simpleMatch(mappedName,
                    aClass.getSimpleName());
        }
    }
}
```



좀 더 편리한 포인트컷 작성 방법을 알아보자.







### 6.6 트랜잭션 속성



### 6.7 어노테이션 트랜잭션 속성과 포인트컷



### 6.8 트랜잭션 지원 테스트

