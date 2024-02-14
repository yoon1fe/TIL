## Mockito 로 Stubbing

요구사항: 일일 매출 통계를 메일로..



A 기능을 테스트 하는데 A 기능 안에 외부 네트워크 통신(메일 전송 등..) 이 있다면 이 기능을 mocking.



**Mock**

- 가짜 객체를 주입하고 이 가짜 객체가 어떤 동작을 할지 직접 지정 가능.

```java
Mockito.when(mailSendClient.sendEmail(any(String.class), any(String.class), any(String.class), any(String.class)))
    .thenReturn(true);
```

- mail.SendClient.sendEmail() 메서드를 호출할 때 아무 String 타입의 파라미터가 들어왔을 때 true 를 리턴!



**stubbing**

- mock 객체에 원하는 행위를 정의하는 것



## Test Double

**test double**

- 테스트 중인 시스템의 일부분이 완전히 준비되지 않았거나 테스트하기 어려운 상황에서 그 대안으로 사용될 수 있는 '가짜' 컴포넌트





**Dummy**

- 아무 동작도 하지 않는 깡통 객체



**Fake**

- 단순한 형태로 동일한 기능은 수행하나, 프로덕션에서 쓰기에는 부족한 객체 (ex. FakeRepository)



**Stub**

- 테스트에서 요청한 것에 대해 미리 준비한 결과를 제공하는 객체. 그 외에는 응답하지 않는다.



**Spy**

- Stub 이면서 호출된 내용(몇 번 호출됐는지,, 등)을 기록하여 보여줄 수 있는 객체.
- 일부는 실제 객체처럼 동작시키고 일부만 Stubbing 할 수 있다.



**Mock**

- 행위에 대한 기대를 명세하고, 그에 따라 동작하도록 만들어진 객체



**Stub 과 Mock 의 차이 ?**

https://martinfowler.com/articles/mocksArentStubs.html

- Stub
  - **상태 검증(State Verification)**
- Mock
  - **행위 검증(Behavior Verification)**



## 순수 Mockito로 검증해보기

**Mock 객체 생성**

- `Mockito.mock()`

  ``` java
  MailSendClient mailSendClient = mock(MailSendClient.class);
  ```

- 애너테이션 사용

  ``` java
  @ExtendWith(MockitoExtension.class)
  class MailServiceTest {
  
    @Mock
    private MailSendClient mailSendClient;
     
    ...
  }
  ```



**@InjectMocks**

- 인스턴스 생성할 때 Mock 객체 주입

  ``` java
    @Mock
    private MailSendClient mailSendClient;
  
    @Mock
    private MailSendHistoryRepository mailSendHistoryRepository;
  
    @InjectMocks
    private MailService mailService;
  ```



**Mockto.verify()**

```java
// then
Mockito.verify(mailSendHistoryRepository, times(1)).save(any(MailSendHistory.class));
```

- mailSendHistoryRepository.save() 가 한 번 호출됐는지 검증



**@Spy**

- `mockito.verify()` 랑 비슷...
- 로직에서 특정 메서드에 대해서만 stubbing 하고 싶을 때 사용

- ``` java
      doReturn(true)
          .when(mailSendClient)
          .sendEmail(anyString(), anyString(), anyString(), anyString());
  ```



## BDDMockito

given 절에 `Mockito.when()` ?

-> BDDMockito ! 이름만 바뀐거다.

```java
BDDMockito.given(mailSendClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
    .willReturn(true);
```



## Classicist vs. Mockist

mockist: 테스트하려는 레이어말고 나머지는 mocking 하자!



classicist: 객체들끼리의 상호작용 등을 함께 테스트 하는 것이 좋을텐데, 다 mocking 하면 어떡하냐 ? 



**실제 프로덕션 코드에서 런타임 시점에 일어날 일을 정확하게 Stubbing 했다고 단언할 수 있을까 ?**

- 이러한 리스크를 안고 가는 것보다.. 비용을 더 들여서 실제 구현체를 불러와서 더 넓은 범위의 테스트를 하는 것이 좋지 않을까..



고민이 많이 필요할 주제.