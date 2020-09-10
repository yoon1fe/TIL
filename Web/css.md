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





### 우선순위

`!important;` -> 우선순위를 가장 높인다.



1. 같은 우선 순위라면 아래에서 적용한 효과가 반영된다.
2. 우선 순위는 아이디 > 클래스 > 엘리먼트
3. 우선 순위를 높이려면 `!important;` 사용하면 된다.



폰트 크기

- px - 해상도에 따른 픽셀의 크기. 고정된 크기이다.
- em - 상대 크기. 배수.
- % - 상대 크기. 부모 엘리먼트 기준이다.

- rem - 상대 크기. root em이다. html에 정해져 있는 크기 기준에 대한 상대 크기이다.



e.g)

```css
html - font-size : 16px;

div (16px)
	p - 100%	(16px. div 기준)
	  - 1.5em	(24px. div 기준 1.5배)
	  - 1.5rem	(24px, html(root element) 기준 1.5배)
```



- inline: 별도의 크기 지정 불가능하다. 내용에 맞추어 크기가 자동으로 설정된다.
- block: 별도의 크기 지정 가능
- inline-block: 한 줄에 여러 개의 엘리먼트를 같이 사용할 수 있다. 크기를 가진다.







## 포지셔닝

`position: `

`static`: default값. 좌표 설정 불가능하다.

`absolute`: 부모 기준으로 좌표값만큼 위치시킨다. 하지만 부모가 default(static)이라면 좌표값을 사용하지 않기 때문에 그 위로 올라간다. 만약 마지막까지 올라갔는데 좌표값을 사용하는 부모가 없다면 왼쪽 최상단 기준으로 처리된다.

`relative`: 원래 들어가야 할 위치 기준으로 좌표값만큼 위치시킨다.

`fixed`: 무조건 body 기준!





float: block 옆에 두고 싶을 때 사용한다.



`display: none;`: 영역 자체를 없앤다

`visibility: hidden;` 영역을 없애지 않고 모습만 숨긴다.





*** padding은 width, height에 플러스된다!

이를 해결하기 위한 방법 -> box-sizing: border-box;