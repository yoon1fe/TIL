오늘은 RestTemplate의 Error Handling에 대해 간단히 알아보겠습니다.

RestTemplate은 스프링 3.0 부터 지원하는 객체로, 동기식으로 HTTP 요청을 수행하는 역할을 합니다. 참고로 스프링 5부터는 WebFlux 스택과 함께 WebClient 라는 새로운 HTTP 클라이언트가 도입되었고, 현재 RestTemplate은 Deprecated... 된줄 알았는데 되진 않고 WebClient 사용을 권장하고 있습니다.



RestTemplate은 ResponseErrorHandler 타입의 errorHandler라는 필드를 가집니다. ResponseErrorHandler 인터페이스는 다음 두 개의 메서드를 갖고 있구요.

``` java
package org.springframework.web.client;

import java.io.IOException;
import org.springframework.http.client.ClientHttpResponse;

public interface ResponseErrorHandler {
  boolean hasError(ClientHttpResponse var1) throws IOException;

  void handleError(ClientHttpResponse var1) throws IOException;
}
```



RestTemplate 의 외부 API 응답을 처리하는 `handleResponse()` 메서드에서 에러가 있는지 확인(`hasError()`)하고, 만약 있다면 그 에러를 처리(`handleError()`)합니다.

``` java
  protected void handleResponse(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
    ResponseErrorHandler errorHandler = this.getErrorHandler();
    boolean hasError = errorHandler.hasError(response);
    if (this.logger.isDebugEnabled()) {
      try {
        this.logger.debug(method.name() + " request for \"" + url + "\" resulted in " + response.getRawStatusCode() + " (" + response.getStatusText() + ")" + (hasError ? "; invoking error handler" : ""));
      } catch (IOException var7) {
      }
    }

    if (hasError) {
      errorHandler.handleError(response);
    }

  }
```



이러한 ResponseErrorHandler의 기본 구현체로는 DefaultResponseErrorHandler 가 있는데요, 이 클래스의 `handleError()` 메서드를 보면...


``` java
  public void handleError(ClientHttpResponse response) throws IOException {
    HttpStatus statusCode = this.getHttpStatusCode(response);
    switch (statusCode.series()) {
      case CLIENT_ERROR:    // 4xx
        throw new HttpClientErrorException(statusCode, response.getStatusText(), response.getHeaders(), this.getResponseBody(response), this.getCharset(response));
      case SERVER_ERROR:    // 5xx
        throw new HttpServerErrorException(statusCode, response.getStatusText(), response.getHeaders(), this.getResponseBody(response), this.getCharset(response));
      default:
        throw new UnknownHttpStatusCodeException(statusCode.value(), response.getStatusText(), response.getHeaders(), this.getResponseBody(response), this.getCharset(response));
    }
  }

```

HttpClientErrorException, HttpServerErrorException 등 RestClientResponseException 의 서브 클래스 예외들를 던지게 됩니다.

정리하자면, RestTemplate은 기본적으로 4xx, 5xx, 그리고 알 수 없는 HTTP 상태 코드가 응답으로 온다면  다음과 같은 예외를 던집니다.

- 4xx Client Error: HttpClientErrorException
- 5xx Server Error: HttpServerErrorException
- Unkown: UnknownHttpStatusCodeException



그런데 통신을 하다 보면 200 OK 의 응답뿐만 아니라 4xx, 5xx 응답도 따로 처리해야할 경우가 있습니다. 이런 경우에는 ResponseErrorHandler 를 재정의해서 RestTemplate 인스턴스에 세팅해주면 됩니다. 만약 `hasError()` 메서드를 재정의할 필요가 없다면 DefaultResponseErrorHandler의 서브 클래스를 만들어주면 되겠죠.



아래 코드는 외부 API에서 정의해준 4xx 번대 상태 코드를 처리하는 ResponseErrorHandler입니다.



``` java
public class CustomResponseErrorHandler extends DefaultResponseErrorHandler {

  @Override
  public void handleError(ClientHttpResponse response) throws IOException {
    HttpStatus statusCode = this.getHttpStatusCode(response);

    switch (statusCode) {
      case UNAUTHORIZED:
      case FORBIDDEN:
      case METHOD_NOT_ALLOWED:
      case NOT_ACCEPTABLE:
        // do something
      default:
        throw new UnknownHttpStatusCodeException(statusCode.value(), response.getStatusText(), response.getHeaders(), this.getResponseBody(response), this.getCharset(response));
    }
  }
}
```



그리고 이렇게 새로 정의한 ResponseErrorHandler를 restTemplate의 errorHandler에 세팅해주면 됩니다.

``` java
  restTemplate = new RestTemplate(requestFactory);
  restTemplate.setErrorHandler(new PayLetterRestTemplateResponseErrorHandler());
```









#####  Reference

https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html
https://velog.io/@dailylifecoding/Spring-RestTemplate-wont-Deprecate