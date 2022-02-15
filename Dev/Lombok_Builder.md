JPA 강의를 듣던 중 작은 난관에 봉착했습니다.

하라는 대로 다 했는데 NPE가 뜬것입니다.



이리 보고 저리 봐도 잘못된 것이 없는데 왜 이럴까... IDE의 문제가 아닐까.. 컴파일러의 문제가 아닐까... 했지만 문제는 `@Builder` 요 녀석에게 있었습니다.



`@Builder` 어노테이션을 사용해서 객체를 만들었는데요, 아니글쎄 디폴트 값을 넣어준 필드 변수가 계속 `null` 이라는 겁니다.



찾아보니 `.builder().build();` 로 생성되는 객체의 필드 변수는 모두 기본값 (0 / `null`  / `false`) 이 세팅이 됩니다. 디폴트 값으로 뭘 줬든지 간에요. 



고맙게도 Lombok v1.16.16 버전부터 `@Builder.Default` 란 어노테이션이 생겼습니다. 디폴트 값 설정이 필요한 필드 변수에 위 어노테이션을 붙여주면 됩니다.



#### Account.java

```java
@Builder
public class Account {

  private Long id;
  private int num;

  @Builder.Default
  // @Default
  private Set<Integer> sets = new HashSet<>();
}


```



#### Test

```java
 @Test
 void test_builder() {
  Account account = Account.builder()
    .build();

  Assertions.assertNull(account.getId());
  Assertions.assertEquals(account.getNum(), 0);
  Assertions.assertNotNull(account.getSets());
 }

}
```









##### Reference

https://projectlombok.org/features/Builder