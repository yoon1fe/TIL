## 1) Click 이벤트

### Vanilla JS

```html
<body>
	<button>Click</button>
	
	<script type="text/javascript">
		let btn = document.querySelector('button');
		
        btn.onclick=function(){
            console.log('성공');
        };
        
		btn.onclick = () => console.log('성공');
        
		btn.addEventListener('click', ()=>{
			console.log('성공');
		});
	</script>
</body>
```



### jQuery

```html
<body>
	<button>Click</button>

	<script type="text/javascript">
// 		let btn = $('button');
// 		btn.on('click', ()=> console.log('성공!'));
		$('button').on('click', ()=>console.log('성공'));
        
        //단축 이벤트 설정
		$('button').click(function() {
			console.log('성공');
		});
	</script>
</body>
```



## 2) Click - 다른 값 가져오기

### Vanilla JS

```html
<body>
	<button>Click</button>
	<div class='result'>0</div>

	<script type="text/javascript">
		const btns = document.getElementsByTagName('button');
//      const btn = document.querySelector('button');
		const result = document.getElementsByClassName('result');
//		const result = document.querySelector('.result');
        
		btns[0].addEventListener('click', function(){
			result[0].innerText++;
		});
	</script>
</body>
```



### jQuery

```html
<body>
	<button>Click</button>
	<div class='result'>0</div>

	<script type="text/javascript">
		$('button').on('click', ()=>{
		$('.result').text(parseInt($('.result').text()) + 1);
//		result.innerText = parseInt(result.innerText + 1);	 
		});
	</script>
</body>
```



## 3) Click - div에 텍스트 추가

### Vanilla JS

```html
<body>
	<input type='text' id='msg'>
	<div class='result'></div> 
	
	<script type="text/javascript">
		const msg = document.getElementById('msg');
		const result = document.getElementsByClassName('result')[0];
		msg.addEventListener('keyup', ()=>{
			result.innerText = msg.value;
		});
	</script>
</body>
```



### jQuery

```html
<body>
	<input type='text' id='msg'>
	<div class='result'></div> 
	
	<script type="text/javascript">
		$('#msg').on('keyup', ()=> {
			$('.result').text($('#msg').val());
		});
// 		const msg = $('#msg');
// 		msg.on('keyup', ()=> {
// 			$('.result').text(msg.val());
// 		});
        $('#msg').keyup(function() {
// 			$('.result').text(this.value);
			// this  는 자바스크립트 객체이다!
			$('.result').text($(this).val());
		});
        
        // 이벤트 객체 이용 : e.target
		// 이벤트 콜백 함수를 화살표 함수로 만들 경우 this는 이벤트 대상 엘리먼트가 아니다.
		// 따라서 이벤트 객체의 target을 이용해야 한다.
		$('#msg').keyup((e)=>{
            console.dir(this);		// -> Window 가 찍힘
// 			$('.result').text(e.target.value);
			$('.result').text($(e.target).val());
		});
	</script>
</body>
```





## 4) 속성값 다루기

### Vanilla JS

```html
<body>
	<div>
		<button id='btn1'>속성확인</button>
		<button id='btn2'>속성추가</button>
	</div>

	<img id='img1' src="../images/ice_americano.jpg">
	<div id='result'></div>

	<script type="text/javascript">
		const btn1 = document.querySelector('#btn1');
		const btn2 = document.querySelector('#btn2');
		const img1 = document.querySelector('#img1');
		const result = document.querySelector('#result');
		
        // 속성에 접근하는 세 가지 방법!
		btn1.addEventListener('click', (e)=> {
			result.innerHTML = img1.getAttribute('alt');
			result.innerHTML = img1.alt;
			result.innerHTML = img1['alt'];
		});
		
		btn2.addEventListener('click', ()=>{
			img1.setAttribute('alt', '아이스 아메리카노');
			img1.alt = '아이스 아메리카노';
			img1['alt'] = '아이스 아메리카노';
		});
	</script>
</body>
```



### jQuery

```html
<body>
	<div>
		<button id='btn1'>속성확인</button>
		<button id='btn2'>속성추가</button>
	</div>

	<img id='img1' src="../images/ice_americano.jpg">
	<div id='result'></div>

	<script type="text/javascript">
		$('#btn1').on('click', (e)=> {
			$('#result').text($('#img1').attr('alt'));
		});

		$('#btn2').click(()=>{
			$('#img1').attr('alt', '아이스 아메리카노');
		});
	</script>
</body>
```



## 5) 사용자 정의 속성

### Vanilla JS

```html
<body>
	<div>
		<button id='btn1'>속성확인</button>
		<button id='btn2'>속성추가</button>
	</div>

	<div id='div1' data-cnt='10'>테스트용</div>
	<div id='div2'></div>

	<script type="text/javascript">
		const btn1 = document.getElementById('btn1');
		const btn2 = document.getElementById('btn2');
		const div1 = document.getElementById('div1');
		const div2 = document.getElementById('div2');
		
		btn1.addEventListener('click', ()=>{
			div2.innerHTML = `data-cnt : ${div1.getAttribute('data-cnt')}, data-name : ${div1.getAttribute('data-name')}`;
		});
		
		btn2.onclick = () => {
			div1.setAttribute('data-name', '추가됨');
		};
		
	</script>
</body>
```



### jQuery

```html
<body>
	<div>
		<button id='btn1'>속성확인</button>
		<button id='btn2'>속성추가</button>
	</div>

	<div id='div1' data-cnt='10'>테스트용</div>
	<div id='div2'></div>

	<script type="text/javascript">
	
		// data() 메소드 활용
		$('#btn1').on('click', ()=>{
			$('#div2').text("data-cnt : " + $('#div1').data('cnt') + " data-name : " + $('#div1').data('name'));
		});
		
		$('#btn2').click(e=>{
			$('#div1').data('name', '추가됨');
		});
	</script>
</body>
```





## 6) 엘리먼트 생성 및 추가

### Vanilla JS

```html
<body>
	<div>
		<button id='btn1'>확인</button>
		<button id='btn2'>추가</button>
	</div>
	<div id='result'>추가 버튼 클릭 시 여기에 내용이 적용됩니다.</div>

	<script type="text/javascript">
		const btn1 = document.getElementById('btn1');
		const btn2 = document.getElementById('btn2');
		const result = document.getElementById('result');
		
		btn1.onclick = e=>{
			console.log(result.innerText);
		};
        
		btn2.addEventListener('click', e=>{
			result.innerHTML += "<h2>추가</h2>";
		});
		
		// DOM API 사용
		btn2.addEventListener('click', e=>{
			let newH2 = document.createElement('h2');
			newH2.innerText = '추가';
			result.appendChild(newH2);
		});
	</script>
</body>
```



### jQuery

```html
<body>
	<div>
		<button id='btn1'>확인</button>
		<button id='btn2'>추가</button>
	</div>
	<div id='result'>추가 버튼 클릭 시 여기에 내용이 적용됩니다.</div>

	<script type="text/javascript">
		$('#btn1').on('click', e=>{
			console.log($('#result').text());
		});
		
		$('#btn2').click(()=>{
// 			$('<h2>추가</h2>').appendTo($('#result'));
			$('#result').append('<h2>추가</h2>');
		});
	</script>
</body>
```





## 7) CSS 설정

### Vanilla JS

```html
<body>
	<div>
		<button id="btn1">배경색 변경하기(#345)</button>
		<button id="btn2">글자색 변경하기(white)</button>
		<button id="btn3">테두리 추가하기(10px solid tomato)</button>
		<button id="btn4">테두리 둥글게(50%)</button>
	</div>
	<div id="result">TEST CSS</div>
	<script>
 // 각 버튼 클릭 시 버튼의 타이틀에 맞는 css를 적용한다.
    // Vanila Javascript 를 이용(element.style.속성 활용)

    /* 객체.style.속성명(camel 표기사용)
    const result = document.getElementById('result');
    document.getElementById('btn1').onclick = function () {
        result.style.backgroundColor = '#345';
    };
    document.getElementById('btn2').onclick = function () {
        result.style.color = 'white';
    };
    document.getElementById('btn3').onclick = function () {
        result.style.border = '10px solid tomato';
    };
    document.getElementById('btn4').onclick = function () {
        result.style.borderRadius = '50%';
    };
    */

    /*  객체.style.속성명([camel, 하이픈] 표기사용)
    const result = document.getElementById('result');
    document.getElementById('btn1').onclick = function () {
        result.style['backgroundColor'] = '#345';
    };
    document.getElementById('btn2').onclick = function () {
        result.style['color'] = 'white';
    };
    document.getElementById('btn3').onclick = function () {
        result.style['border'] = '10px solid tomato';
    };
    document.getElementById('btn4').onclick = function () {
        result.style['border-radius'] = '50%';
    };
    */

    /*  CSSStyleDeclaration 객체 이용(하이픈 표기 사용)
    const styleObj = document.getElementById('result').style;
    document.getElementById('btn1').onclick = function () {
        styleObj.setProperty('background-color', '#345');
    };
    document.getElementById('btn2').onclick = function () {
        styleObj.setProperty('color', 'white');
    };
    document.getElementById('btn3').onclick = function () {
        styleObj.setProperty('border', '10px solid tomato');
    };
    document.getElementById('btn4').onclick = function () {
        // styleObj.setProperty('border-radius', '50%');
        styleObj.setProperty('border-radius', '50%');
    };
    */

    // 인라인 스타일 전체 한꺼번에 처리하기
    /*
    const result = document.getElementById('result');
    result.setAttribute('style',
        'background-color: #345; color: white; border: 10px solid tomato; border-radius: 50%');
    */
    const result = document.getElementById('result');
    result.setAttribute('style',
        'background-color: #345; color: white; border: 10px solid tomato; border-radius: 50%');
    
</script>
</body>
```



### jQuery

```html
<body>
	<div>
		<button id="btn1">배경색 변경하기(#345)</button>
		<button id="btn2">글자색 변경하기(white)</button>
		<button id="btn3">테두리 추가하기(10px solid tomato)</button>
		<button id="btn4">테두리 둥글게(50%)</button>
	</div>
	<div id="result">TEST CSS</div>
	<script>
	
	$('#btn1').click(()=>{
		$('#result').css('background-color', "#345");
	});
	$('#btn2').on('click', function(){
		$('#result').css('color', 'white');
	});
	$('#btn3').on('click', ()=>{
		$('#result').css('border', '10px solid tomato');
	});
	$('#btn4').on('click', function(){
		$('#result').css('border-radius', '50%');
	});
</script>
</body>
```





## 7) class 속성

### Vanilla JS

```html
<body>
	<div>
		<div id='btn'>class 적용</div>
	</div>
	<div id='result'>TEST CSS</div>
	<script type="text/javascript">
	
	document.getElementById('btn').onclick = ()=>{
// 		document.getElementById('result').setAttribute('class', 'test');
		document.getElementById('result').className = 'test';
	};
	
	</script>
</body>
```



### jQuery

```html
<body>
	<div>
		<div id='btn'>class 적용</div>
	</div>
	<div id='result'>TEST CSS</div>
	<script type="text/javascript">

	$('#btn').click(()=>{
		$('#result').addClass('test');				// class 추가
// 		$('#result').removeClass('test');			// class 삭제
// 		$('#result').toggleClass('test');			// class 추가/삭제
	});
	</script>
</body>
```





## 8) checkbox 값 가져오기

### Vanilla JS

```html
<body>
	<form>
		<button type="button" id="btn">확인</button>
		<div>
			오늘 점심 메뉴?
			<div>
				<input type="checkbox" id="menu1" name="menu" value="불고기"> <label
					for="menu1">불고기</label>
			</div>
			<div>
				<input type="checkbox" id="menu2" name="menu" value="불백"> <label
					for="menu2">불백</label>
			</div>
			<div>
				<input type="checkbox" id="menu3" name="menu" value="쭈꾸미"> <label
					for="menu3">쭈꾸미</label>
			</div>
		</div>
		<div id="result"></div>
	</form>
	<script>
	document.getElementById('btn').onclick = (function(){
		const check_obj = document.getElementsByName('menu');
		let checkedList = [];
		check_obj.forEach(function (item){
			if(item.checked == true){
				checkedList.push(item.value);
			}
		});
		
		document.getElementById('result').innerText = checkedList.length + "개 : " + checkedList;
	});         
    </script>
</body>
```



### jQuery

```html
<body>
	<form>
		<button type="button" id="btn">확인</button>
		<div>
			오늘 점심 메뉴?
			<div>
				<input type="checkbox" id="menu1" name="menu" value="불고기"> <label
					for="menu1">불고기</label>
			</div>
			<div>
				<input type="checkbox" id="menu2" name="menu" value="불백"> <label
					for="menu2">불백</label>
			</div>
			<div>
				<input type="checkbox" id="menu3" name="menu" value="쭈꾸미"> <label
					for="menu3">쭈꾸미</label>
			</div>
		</div>
		<div id="result"></div>
	</form>
	<script>
    $('#btn').click(function () {
        const ckArr = $('input[name="menu"]:checked');
        
        let text = "";
        ckArr.each(function (index, item) {
            text += item.value + " ";
        });
        $('#result').text(`${ckArr.length}개 : ${text}`);
    });
    </script>
</body>
```



## 9) checkbox 한꺼번에 처리

### Vanilla JS

```html
<body>
	<form>
		<div><input type='checkbox' id='all'>전체</div>
		<div>
			오늘 점심 메뉴?
			<div>
				<input type="checkbox" id="menu1" name="menu" value="불고기"> <label
					for="menu1">불고기</label>
			</div>
			<div>
				<input type="checkbox" id="menu2" name="menu" value="불백"> <label
					for="menu2">불백</label>
			</div>
			<div>
				<input type="checkbox" id="menu3" name="menu" value="쭈꾸미"> <label
					for="menu3">쭈꾸미</label>
			</div>
		</div>
		<div id="result"></div>
	</form>
	<script>
		const all = document.getElementById('all');
		all.addEventListener('click', ()=>{
			const menu = document.getElementsByName('menu');
			if(all.checked){
				menu.forEach((item)=>{
					item.checked = true;
				});
			}else{
				menu.forEach((item)=>{
					item.checked = false;
				});
			}
		});
		
		document.getElementById('all').addEventListener('click', function () {
            const allCkStatus = this.checked;
            const ckList = document.getElementsByName('menu');
            ckList.forEach(function (item) {
                item.checked = allCkStatus;
            });
        });
    </script>
</body>
```



### jQuery

```html
<body>
	<form>
		<div><input type='checkbox' id='all'>전체</div>
		<div>
			오늘 점심 메뉴?
			<div>
				<input type="checkbox" id="menu1" name="menu" value="불고기"> <label
					for="menu1">불고기</label>
			</div>
			<div>
				<input type="checkbox" id="menu2" name="menu" value="불백"> <label
					for="menu2">불백</label>
			</div>
			<div>
				<input type="checkbox" id="menu3" name="menu" value="쭈꾸미"> <label
					for="menu3">쭈꾸미</label>
			</div>
		</div>
		<div id="result"></div>
	</form>
	<script>
		$('#all').click(()=>{
			$('input[name=menu]').prop('checked', this.checked);
		});	
	
// 		==
// 		document.getElementById('all').addEventListener('click', function () {
//             const allCkStatus = this.checked;
//             const ckList = document.getElementsByName('menu');
//             ckList.forEach(function (item) {
//                 item.checked = allCkStatus;
//             });
//         });
    </script>
</body>
```

