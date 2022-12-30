## 메시지, 국제화

### 메시지

기획자가 어떤 문구를 고쳐달라고 하면.. 하나하나 일일이 변경하려면 골치가 아프다. 화면들을 다 찾아가면서 변경해야 한다.

`>>` HTML 파일에 메시지가 하드코딩되어 있기 때문!

이런 다양한 메시지를 한 곳에서 관리하도록 하는 기능을 **메시지 기능**이라고 한다.



ex) `messages.properties`

``` properties
...
item=상품
item.id=상품 ID
item.itemName=상품명
item.price=가격
item.quantity=수량
...
```



### 국제화

위의 `mesages.properties` 을 각 나라별로 따로 관리하면 서비스를 국제화할 수 있다.



ex) `messages_ko.properties`, `messages_en.properties` ...

HTTP `accept-language` 헤더 값을 사용하거나, 사용자가 직접 언어를 선택하도록 한 뒤 쿠키 등을 사용해서 어디서 접근한 것인지 처리하면 된다.



메시지/국제화 기능은 스프링이 기본적으로 제공한다!! thymeleaf도 스프링이 제공하는 메시지/국제화 기능을 편리하게 통합해서 제공한다.



## 스프링 메시지 소스 설정

스프링은 기본적인 메시지 관리 기능을 제공한다.

`MessageSource` 를 스프링 빈으로 등록하면 되는데, 얘는 인터페이스이므로 구현체인 `ResourceBundleMessageSource`를 빈으로 등록하면 된다. > 부트에서 자동으로 등록해줌. ㅎㅎ



``` java
@Bean
public MessageSource messageSource() {
 ResourceBundleMessageSource messageSource = new
ResourceBundleMessageSource();
 messageSource.setBasenames("messages", "errors");
 messageSource.setDefaultEncoding("utf-8");
 return messageSource;
}
```

- `basenames`: 설정 파일 이름 지정
  - `messages`로 지정하면 `messages.properties` 파일을 읽는다
  - 국제화 기능 적용하려면 파일명 마지막에 언어 정보를 추가하면 된다. 국제화 파일 찾을 수 없으면 기본으로 `messages.properties` 파일 사용
  - 파일 위치: `/resources/messages.properties`
  - 여러 파일을 한 번에 지정할 수 있다.
- `defaultEncoding`: 인코딩 정보 저장



`application.properties`

``` properties
spring.messages.basename=messages,config.i18n.messages
# 디폴트값
# spring.messages.basename=messages
```



`MessageSource`를 빈으로 등록하지 않고 별도의 설정을 하지 않으면`messages` 이름으로 기본 등록된다.



## 스프링 메시지 소스 사용

`MessageSource` 인터페이스는 메시지를 읽어오는 기능을 제공한다.



```java
package hello.itemservice.message;

import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class MessageSourceTest {

  @Autowired
  MessageSource ms;

  @Test
  void helloMessage() {
    assertThat(Locale.getDefault()).isEqualTo(Locale.US);

    String result = ms.getMessage("hello", null, Locale.KOREA);
    assertThat(result).isEqualTo("안녕");
  }

  @Test
  void notFoundMessageCode() {
    assertThatThrownBy(() -> ms.getMessage("no_code", null, null)).isInstanceOf(NoSuchMessageException.class);
  }

  @Test
  void notFoundMessageCodeDefaultMessage() {
    String result = ms.getMessage("no_code", null, "default message", null);
    assertThat(result).isEqualTo("default message");
  }

  @Test
  void argumentMessage() {
    String result = ms.getMessage("hello.name", new Object[]{"Spring"}, Locale.KOREA);
    assertThat(result).isEqualTo("안녕 Spring");
  }

  @Test
  void defaultLang() {
    assertThat(Locale.getDefault()).isEqualTo(Locale.KOREA);

    assertThat(ms.getMessage("hello", null, null)).isEqualTo("안녕");
    assertThat(ms.getMessage("hello", null, Locale.KOREA)).isEqualTo("안녕");
    assertThat(ms.getMessage("hello", null, Locale.ENGLISH)).isEqualTo("hello");
  }
}
```

- `Locale` 값이 없는 경우 `Locale.getDefault()`로 기본값을 사용.



## 웹 애플리케이션에 메시지 적용

타임리프 메시지 표현식 `#{...}`을 사용하면 스프링의 메시지를 편리하게 조회 가능



- 파라미터 사용: `<p th:text="#{hello.name(${item.itemName})}"></p>`



## 웹 애플리케이션에 국제화 적용

웹 브라우저의 언어 설정 값을 변경하면 요청 시에 `Accept-Language`(클라이언트가 서버에 기대하는 언어 정보)의 값이 변경된다.

스프링은 기본적으로 `Accept-Language` 헤더 값을 사용한다.

스프링은 `Locale` 선택 방식을 변경할 수 있도록 `LocaleResolver` 인터페이스를 제공하는데, 스프링 부트는 기본적으로 `Accept-Language`를 사용하는 `AcceptHeaderLocaleResolver`를 사용한다. 쿠키나 세션 기반으로 `Locale`을 선택하는 리졸버도 있다.