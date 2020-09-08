## CSS

Cascading Style Sheets의 약자이다.



CSS 규칙은 선택자(selector)와 선언(declaration) 두 부분으로 구성된다.

선택자는 규칙이 적용되는 element이다.

선언 부분에서는 선택자에 적용될 스타일을 작성하면 된다.

선언은 중괄호로 감싸며, 속성(proterty)와 값(value)로 이루어져 있다.



e.g) `.css { margin : 30px; color : #000; }`



`.`: class

`#`: id

`h1`: element

`h1 h2` -> 자손

`h1>h2` -> 자식

`#a+p` -> a를 찾고 바로 뒤에 있는 게 p 일 때만 p를 선택

`#a~p` -> 모든 형제를 다 선택

`[]` -> 속성을 갖고 있는 element 선택

`a[href]` -> a 태그 중 href 속성을 갖고 있는 element 선택

`a[href="abc"]` -> a 태그 중 href 속성을 갖고 있고 "abc" 값을 갖고 있는 element 선택

`p, h1` -> p와 h1태그 선택. 그룹 선택자라고 한다.



e.g) `h1.a` -> id가 a인 h1을 찾아라



가상 클래스 선택자 요소

:link -> 방문하지 않은 링크 선택





margin 방향 순서



​		1

4			2

​		3



따로 적지 않으면 마주보는 쪽의 값을 가진다.

margin: 10 5

-> 상하 10 좌우 5

margin: 10 5 7

-> 상 10 오 5 아래 6 왼 "5"