# [JUnit] @Before vs @BeforeClass vs @BeforeEach vs @BeforeAll

신입 기술 교육 때 JUnit을 사용해서 테스트 코드를 짜다가 @Before 라는 어노테이션을 알게 되었습니다. 이 어노테이션이 붙으면 @Test 어노테이션이 붙은 메소드가 실행되기 전에 먼저 실행됩니다. 그래서 보통 

```java
@Before
public void setUp() {
	//setup befor testing
}
```

이런 식으로 많이 쓰더군여. 

근데 또 @BeforeClass 란 어노테이션도 있댑니다. 근데 또 @BeforeEach 도 있고 @BeforeAll 도 있다네요. 그래서 얘들을 한 번 간단히 비교해볼까 합니다.



## 1. @Before

[공식 문서](https://junit.org/junit4/javadoc/latest/index.html)

@Before 어노테이션은 JUnit 4에 있는 어노테이션입니다. 역할은 위에서 말했듯이 간단합니다. @Test 메소드보다 먼저 실행됩니다. 가령 테스트 코드에서 사용할 리스트가 있으면 테스트 메소드가 실행되기 전에 @Before 메소드에서 리스트를 만들어 놓는 것이죠.

```java
public class Example {
	private List list;
	
	@Before
	public void setUp() {
		list = new ArrayList<>();
	}
	
	@Test
	public void test1() {
		...
	}
	
	@Test
	public void test2() {
		...
	}
}
```

그럼 이 @Before 어노테이션이 붙으면 어느 한 테스트 메소드 실행 이전에만 실행될까요? 왠지 @BeforeEach 어노테이션이 또 있으니 그럴것 같지 않나요 ?! 그렇지 않습니다. **각각의** 테스트 메소드 실행 전에 실행됩니다. 이렇게 되면 각각의 테스트 메소드가 서로의 실행 결과에 영향을 끼칠 수도 있을 것 같은 좋지 않은 예감이 듭니다. 그래서 JUnit 은 **@After** 라는 어노테이션도 제공해줍니다. 

```java
	@After
	public void tearDown() {
		list.clear();
	}
```

얘도 마찬가지로 각각의 테스트 메소드 실행 후에 실행됩니다. 그럼 `Example` 클래스는 이런 식으로 실행됩니다.

```shell
>>> setUp()
>>> test1()
>>> tearDown()
>>> setUp()
>>> test2()
>>> tearDown()
```



## 2. @BeforeClass

[공식 문서](https://junit.org/junit4/javadoc/latest/org/junit/BeforeClass.html)

다음은 @BeforeClass 어노테이션을 알아봅시다. 양심없이 공식 문서에 적힌 말을 그대로 옮겨보자면, 데이터베이스에 접근하는 것과 같이 비용이 많이 드는 설정이 테스트 전에 필요한 경우가 있습니다. 공통된 작업을 굳이 테스트 메소드 하나하나마다 해줄 필요는 없겠지요. @BeforeClass 어노테이션이 붙은 메소드는 테스트 메소드들이 실행되기 전 **한 번** 실행됩니다. 이 어노테이션이 붙은 메소드는 반드시 static으로 선언되어야 합니다. 이름이 Before Class 이니깐 `static` 으로 선언된 메소드에 달아야 하는 것을 명심합시다.

그리고 @Before 어노테이션에게 @After 어노테이션이 있는 것처럼 @BeforeClass 어노테이션에게도  @AfterClass 어노테이션이 있답니다. 그럼 위에 있는 코드에서 @Before와 @After 를 각각 @BeforeClass, @AfterClass 로 바꾸면 `Example` 클래스는 이렇게 실행되겠죠.

```shell
>>> setUp()
>>> test1()
>>> test2()
>>> tearDown()
```



## 3. @BeforeEach, @BeforeAll

[@BeforeEach](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/BeforeEach.html)

[@BeforeAll](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/BeforeAll.html)

이제 대망의 @BeforeEach, @BeforeAll 어노테이션입니다. 이 친구들은 JUnit 5 에 나온 어노테이션들입니다. 그리고 JUnit 5 에는 @Before, @BeforeClass 어노테이션이 없답니다.. 고로 얘들은 그냥 이름만 바뀐거나 진배없습니다... 다만 공식 문서에서 언급된 추가적인 부분을 쪼금 적어보겠습니다.

먼저 @BeforeEach 가 붙은 메소드는 **각각의 ** @Test, @RepeatedTest, @ParameterizedTest, @TestFactory, @TestTemplate 가 붙은 메소드 실행 전에 실행됩니다. 이 어노테이션들에 대해서는 따로 공부하고 정리를 해야겠습니다. 그리고 이 어노테이션이 붙은 메소드의 시그니처를 알아봅시다. 세가지 조건이 있습니다. 

1. 리턴 타입으로는 반드시 void
2. 접근 제한자로 private 사용 금지
3. static으로 선언 금지

다음은 @BeforeAll 입니다. 이 어노테이션이 붙은 메소드는 **모든** 테스트 메소드가 실행되기 전에 **한 번** 실행됩니다. 그리고 @BeforeAll 가 붙은 메소드의 시그니처는 다음과 같은 조건이 붙습니다.

1. 리턴 타입으로는 반드시 void
2. 접근 제한자로 private 사용 그미
3. 반드시 static으로 선언









##### Reference

https://www.baeldung.com/junit-before-beforeclass-beforeeach-beforeall

