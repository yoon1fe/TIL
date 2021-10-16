### Annotation 정리



`@ModelAttribute("requestDTO")`

@ModelAttribute 선언 후 자동으로 진행되는 작업들

1. 파라미터로 넘겨준 타입의 오브젝트를 자동으로 생성한다.
2. 생성된 오브젝트에 HTTP로 넘어온 값들을 자동으로 바인딩한다.
3. @ModelAttribute 가 붙은 객체가 자동으로 Model 객체에 추가되고, 따라서 requestDTO 객체가 view 단까지 전달된다.

