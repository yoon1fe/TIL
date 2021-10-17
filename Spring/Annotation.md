### Annotation 정리



`@ModelAttribute("requestDTO")`

@ModelAttribute 선언 후 자동으로 진행되는 작업들

1. 파라미터로 넘겨준 타입의 오브젝트를 자동으로 생성한다.
2. 생성된 오브젝트에 HTTP로 넘어온 값들을 자동으로 바인딩한다.
3. @ModelAttribute 가 붙은 객체가 자동으로 Model 객체에 추가되고, 따라서 requestDTO 객체가 view 단까지 전달된다.





`@RequestParam()`

Get Method - query string





`@ResponseBody`

ViewResolver 타지 않는다. -> HTTP 메시지 응답 바디에 직접 넣어주겠다.



``` java
  @GetMapping("hello-string")
  @ResponseBody
  public String helloString(@RequestParam("name") String name) {
    return "hello " + name;
  }
```

- `ViewResolver` 대신 `HttpMessageConverter` 가 동작한다.
- 기본 문자 처리 - `StringHttpMessageConverter`
- 기본 객체 처리 - `MappingJackson2HttpMessageConverter`





`@RequestBody`
