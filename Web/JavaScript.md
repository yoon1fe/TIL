## JavaScript

프로토타입 기반의 스크립트 프로그래밍 언어로, 객제 지향 개념을 지원한다.

웹 브라우저가 JavaScript를 HTML과 함께 다운로드 하여 실행한다.

웹 브라우저가 HTML 문서를 읽어 들이는 시점에 JavaScript Engine이 실행된다.

대부분의 JavaScript Engine은 ECMAScript 표준을 지원한다. (ES)



###  JavaScript 사용

HTML 문서 내부에서 사용하려면 `<script>` 태그를 사용한다.

속성

- src - 외부 JavaScript 파일 포함할 때 사용, 생략 가능하다.

- type - 미디어 타입을 지정할 때 사용한다. `text/javascript`로 지정한다.

  **HTML5부터는 type 속성 생략 가능하다.**



`<script>` 태그는 `<head>`나 `<body>` 어느 곳에든 선언 할 수 있다.

`<body>` 안의 끝 부분에 둘 것을 권장한다.

`<head>` 안에 위치한 JavaScript는 브라우저의 각종 입/출력 발생 이전에 초기화되기 때문에 브라우저가 먼저 점검한다.

`<body>`안에 위치하면 브라우저가 HTML로부터 해석하여 화면에그리기 때문에 사용자가 빠르다고 느낄 수 있다.



### 기본 문법

- **주석** - `/* comment */`

- **변수** - 타입을 명시하지 않는다. var keyword를 사용하여 선언한다.

  ECMAScript 표준에 따라 Camel case를 사용한다.

  키워드, 공백 문자, 숫자로 시작 -> 불가능하다.

  특수 문자로는 _와 $ 사용할 수 있다.

- **자료형** - 프로그램은 정적인 데이터 값을 동적으로 변환해 가면서 정보를 얻는다.

  primitive type과 object type으로 분류된다.

  원시 타입에는 숫자(number), 문자열(string), boolean(boolean), null(object), undefined(undefined) 총 5개가 있다.

  - **숫자** - 모든 숫자를 8byte의 실수 형태로 처리한다. 그렇기 때문에 특정 소수점을 정확하게 표현하지 못한다.

    **언더 플로우, 오버 플로우, 0으로 나누는 연산**에 대해 예외 발생을 시키지 않는다. 

    parseInt()의 경우 바꿀 수 있는 만큼 숫자로 바꿔준다. 

    e.g) 

    parseInt('1A'); 	//1

    parseInt('A');		// NaN(Not a Number)

    parseInt(100 / 0); // Infinity

  - **문자열** - 16bit의 Unicode 문자를 사용한다. 'a'는 한 글자짜리 문자열이다.

    #### backtick(`) - template 문자열

    e.g) `${name}님의 나이는 ${age}살 입니다.`로 사용 가능.

  - **boolean, null, undefined** - **null**은 값이 없거나 비어있음을 의미하고, **undefined**는 값이 초기화되지 않았음(정의되지 않음)을 의미한다.

    값을 할당하지 않은 변수는 **undefined**가 할당되고,(시스템 레벨)

    코드에서 명시적으로 값이 없음을 나타낼 때(프로그램 레벨)는 **null**을 사용한다.

  - 자동 형변환 - 서로 다른 자료형의 연산이 가능하다. 모든 자료형을 var로 선언하기 때문에 혼란을 야기할 수 있다.

- **상수(constant)** - ES6부터 const keyword가 추가되어 상수를 지원한다.

- ECMAScript 6 - **let,** **const** keyword가 추가되었다.

  | 키워드 | 구분 |  선언 위치  | 재선언 |
  | :----: | :--: | :---------: | :----: |
  |  var   | 변수 | 전역 스코프 |  가능  |
  |  let   | 변수 | 해당 스코프 | 불가능 |
  | const  | 상수 | 해당 스코프 | 불가능 |

- **함수** - JavaScript에서 함수는 일급(first-class) 객체이다.

  프로그램 실행 중에 동적으로 생성할 수 있다.

  함수 선언문, 함수 표현식, Function 생성자 함수 방식을 제공한다.

  ```javascript
  // 함수 선언문
  function func(a, b){
  // 함수 표현식
  var func = function(a, b){}
  // Function 생성자 함수
  var func = new Function(a, b);
  ```

  파라미터에 대한 타입은 명시하지 않는다.

  함수를 호출할 때 정의된 파라미터와 전달 인자의 개수가 일치하지 않아도 호출할 수 있다.

- **객체(Object)** - 이름-값 으로 구성된 프로퍼티의 집합이다. 숫자, 문자열, boolean, null, undefined를 제외한 모든 값은 객체이다.

  객체는 dot(.)이나 대괄호([])를 사용해서 속성 값에 접근한다.

  객체에 없는 속성에 접근하면 undefined를 반환한다.

  객체 속성 값을 조회할 때 || 연산자를 사용할 수도 있다.

  만약 속성명에 연산자가 포함된 경우, [] 표기법으로만 접근 가능하다. 

  e.g) member.user-name	// NaN

  객체는 복사되지 않고 참조된다.



### 호이스팅(Hoisting)

변수를 위쪽으로 끌어 올리는 것이다.

변수를 끌어 올릴 때는 선언부만 갖고 온다.

**let는 호이스팅이 안된다!**







### Web Browser와 Window 객체

Window 객체는 웹 브라우저에서 작동하는 JavaScript의 최상위 전역 객체이다.

Window 객체에는 브라우저와 관련된 여러 객체와 속성, 함수가 있다.

JavaScript에서 기본으로 제공하는 프로퍼티와 함수도 포함되어 있다.

BOM(Browser Object Model)으로 불리기도 한다.







### DOM

DOM(Document Object Model)은 HTML과 XML 문서의 구조를 정의하고 API를 제공한다.

DOM은 문서 요소 집합을 트리 형태의 계층 구조로 HTML을 표현한다.

HTML 계층 구조의 제일 위에는 document 노드가 있다.

그 아래로 HTML 태그나 엘리먼트들을 표현하는 노드와 문자열을 표현하는 노드가 있다.



JavaScript를 사용하여 DOM을 검색하거나 제어할 수 있다.

```javascript
document.getElementById
document.getElementByTagName
document.getElementByName
document.getElementByClassName
```

DOM을 조회할 때 Id로 찾으면 가장 빨리 찾을 수 있다.

HTML 표준 스펙상 같은 HTML 문서 안에 중복되는 Id가 있으면 안된다.

검색한 문서에서 innerText나 innerHTML 속성으로 요소를 변경하거나 추가할 수 있다.

JavaScript로 HTML 요소의 속성을 변경할 수 있다.

 getAttribute('속성명'); setAttribute('속성명', '값');

문서 요소에서 style.color로 문서 요소의 색을 변경할 수 있다.



### 이벤트

웹 페이지에서 여러 종류의 상호작용이 있을 때마다 이벤트가 발생한다.

사용자가 마우스를 클릭했을 때, 키보드를 눌렀을 때 등과 같이 다양한 종류의 이벤트가 존재한다.



- click 이벤트 - 사용자가 마우스를 클릭했을 때 발생한다.

  onclick="~~";

- 키보드 이벤트 - 키보드 조작할 때 발생한다.

  onkeypress - 키를 눌렀다 뗀 시점에 발생

  onkeydown - 키를 누르는 순간 발생

- 폼(Form) 이벤트 - form 태그가 전송될 때 submit 이벤트가 발생한다.





