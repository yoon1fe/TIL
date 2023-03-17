## 리플렉션 API: 클래스 정보 조회

리플렉션의 시작은 `Class<T>`



`Class<T>`에 접근하는 방법

- 모든 클래스를 로딩(힙에 클래스 정보 로드됨)한 다음 `Class<T>`의 인스턴스가 생긴다. `타입.class`로 접근 가능
- 모든 인스턴스는 `getClass()` 메서드를 갖고 있다. `인스턴스.getClass()`로 접근 가능
- 클래스를 문자열로 읽어오는 방법
  - `Class.forName("FQCN")`
  - 클래스패스에 해당 클래스가 없다면 `ClassNotFoundException` 발생



`Class<T>`를 통해 갖고 올 수 있는 정보

- 필드 목록
- 메서드
- 상위 클래스
- 인터페이스
- 애너테이션
- 생성자
- 등등...



## 애너테이션과 리플렉션

애너테이션은 근본적으로 주석과 동일하다. 그래서 리플렉션으로 조회해보면 볼 수 없다. 바이트 코드 로드했을때 메모리 상에는 올리지 않는다.

**주요 애너테이션**

- `@Retention`: 해당 애너테이션을 언제까지 유지할 것인가?? 
  - 소스
  - 클래스(default)
  - 런타임
- `@Inherit`: 해당 애너테이션을 하위 클래스까지 전달할 것인가?
- `@Target`: 어디에 사용할 수 있는가?



**리플렉션**

- `getAnnotations()`: 상속받은 (`@Inherit`) 애너테이션까지 조회
- `getDeclaredAnnotations()`: 자기 자신에만 붙어있는 애너테이션 조회



## 리플렉션 API: 클래스 정보 수정/실행

**Class 인스턴스 만들기**

- `Class.newInstance()` 는 deprecated 됨. 생성자를 통해서 생성해야 한다
- `Constructor.newInstance(params)`



**필드값 접근/설정**

- 특정 인스턴스가 갖고 있는 값을 가져오는 것이기 때문에 인스턴스가 필요함
- `Field.get(object)`
- `Field.set(object, value)`
- static 필드를 갖고 올 때는 object가 없어도 되니깐 null을 넘기면 된다



**메서드 실행**

- `Object Mehotd.invoke(object, params)`