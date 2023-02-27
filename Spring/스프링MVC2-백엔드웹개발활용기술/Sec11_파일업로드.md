**HTML 폼 전송 방식**

- `application/x-www-form-urlencoded`
- `multipart/form-data`

이렇게 두 개가 있다. 위의 방식은 HTTP 메시지 바디에 **문자**로 전송할 항목을 입력하는데, 파일을 업로드하려면 **바이너리 데이터**를 전송해야 한다.



**`multipart/form-data`**

form 태그에 별도로 `enctype="multipart/form-data"`를 지정하면 된다. 이 방식은 다른 종류의 여러 파일과 폼의 내용을 함께 전송할 수 있다. 한 번에 여러 종류의 데이터를 보낼 수 있어서 이름이 멀티파트 이다. text, image/png 등등..



**Part**

`multipart/form-data`는 `application/x-www-form-urlencoded`보다 복잡하고 각각의 파트로 나누어져 있다. 그럼 이렇게 복잡한 HTTP 메시지를 서버에서 어떻게 사용할 수 있을까???



## 서블릿과 파일 업로드

``` java
public class ServletUploadControllerV1 {

        @GetMapping("/upload")
        public String newFile() {
          return "upload-form";
        }

        @PostMapping("/upload")
        public String saveFileV1(HttpServletRequest request) throws
            ServletException, IOException {
          log.info("request={}", request);
          String itemName = request.getParameter("itemName");
          log.info("itemName={}", itemName);
          Collection<Part> parts = request.getParts();
          log.info("parts={}", parts);
          return "upload-form";
        }
      }
```

- `request.getParts()`: `multipart/form-data` 전송 방식에서 각각 나누어진 부분을 받아서 확인 가능



참고. **멀티파트 사용 옵션**

- 업로드 사이즈 제한

  ``` properties
  spring.servlet.multipart.max-file-size=1MB
  spring.servlet.multipart.max-request-size=10MB
  ```

  사이즈 넘으면 `SizeLimitExceededException` 발생

- `spring.servlet.multipart.enabled`

  이 옵션을 끄면 서블릿 컨테이너는 멀티파트와 관련된 처리를 하지 않는다. 디폴트는 `true`



**서버에 파일 업로드**

- 파일 저장 경로

  `application.properties`

  ``` properties
  file.dir=경로 ex) /Users/yoon1fe/study/files/
  ```

  해당 경로에 폴더 미리 만들어두어야 함 + 마지막 `/` 반드시 포함되어야 함



참고: 큰 용량의 파일을 업로드할 때 로그가 너무 많이 나오므로 다음 옵션은 끄는게 좋다.

- `logging.level.org.apache.coyote.http11=debug`



서블릿이 제공하는 `Part`는 이런 저런 편의 메서드를 제공해주긴 하지만, `HttpServletRequest`를 사용해야 하고, 추가로 파일 부분만 구분하려면 여러 코드가 필요하다. 스프링은 아주 편리하게 이 부분을 제공해준다~



## 스프링과 파일 업로드

스프링은 `MultipartFile` 인터페이스를 제공한다.



```java
public class SpringUploadController {

 @Value("${file.dir}")
 private String fileDir;

 @GetMapping("/upload")
 public String newFile() {
  return "upload-form";
 }

 @PostMapping("/upload")
 public String saveFile(@RequestParam String itemName,
   @RequestParam MultipartFile file, HttpServletRequest
   request) throws IOException {
  log.info("request={}", request);
  log.info("itemName={}", itemName);
  log.info("multipartFile={}", file);
  if (!file.isEmpty()) {
   String fullPath = fileDir + file.getOriginalFilename();
   log.info("파일 저장 fullPath={}", fullPath);
   file.transferTo(new File(fullPath));
  }
  return "upload-form";
 }
}
```

- `@RequestParam MultipartFile file`: 업로드하는 HTML Form의 name에 맞춰서 `@RequestParam` 적용하면 된다. `@ModelAttribute`에서도 동일하게 적용 가능
- `file.getOriginalFilename()`: 업로드 파일 명
- `file.transferTo(new File(fullPath))`: 파일을 해당 경로(`fullPath`)에다 저장
