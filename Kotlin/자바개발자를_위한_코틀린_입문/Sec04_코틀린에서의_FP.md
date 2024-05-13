## 배열, 컬렉션

### 배열

- 배열은 잘 사용하지 않는다!

``` kotlin
val array = arrayOf(100, 200)

// 반복문
// 1. indices = IntRange(0, lastIndex)
for (i in array.indices) {
  println("${i} ${array[i]}")
}

// 2. 인덱스와 밸류를 한 번에 받는 방법
for ((idx, value) in array.withIndex()) {
  println("${idx} ${value}")
}
```

- `plus`함수로 값을 편하게 추가 가능

  `array.plus(300)`



### Collection - List, Set, Map(+ Mutable..)

코틀린에서는 불변/가변을 지정해주어야 한다!

- 가변(Mutable) 컬렉션: 컬렉션이 element 추가/삭제 가능
- 불변 컬렉션: 컬렉션에 element 추가/삭제 불가능



**List**

``` kotlin
val numbers = listOf(100, 200)
val emptyList = emptyList<Int>()	// 리스트에 들어올 타입을 명시적으로 적어주어야 함
printNumbers(emptyList())	// Int 로 추론 가능하기 때문에 생략 가능

private fun printNumbers(numbers: List<Int>) {
  
}

numbers.get(0)
numbers[0]

for (number in numbers) {
  println(number)
}

for ((idx, value) in numbers.withIndex()) {
  
}

// 가변 리스트
val numbers = mutableListOf(100, 200)
numbers.add(300)
```

- TIP) 우선 불변 리스트를 만들고, 꼭 필요한 경우 가변 리스트로 바꾸자!



**Set**

- 집합은 List 와 다르게 순서가 없고, 같은 element는 하나만 존재할 수 있음.
- 자료구조적 의미만 제외하면 모든 기능이 List 와 비슷하다!

``` kotlin
val numbers = setOf(100, 200)

for (number in numbers) {
  println(number)
}

for ((idx, number) in numbers.withIndex()) {
  
}

// 가변 집합
val numbers = mutableSetOf(100, 200)
```



**Map**

``` kotlin
// JDK 8까지 방식처럼
val oldMap = mutableMapOf<Int, String>()
oldMap.put(1, "ONE")
oldMap[1] ="ONE"
// JDK 9부터 처럼..
val new = mapOf(1 to "ONE", 2 to "TWO")


for (key in oldMap.keys) {
  ...
}

for ((key, value) in oldMap.entries) {
  
}
```



### 컬렉션의 null 가능성, Java와 함께 사용하기

**null 가능성**

- `List<Int?>`: element null 가능, 리스트는 절대 null 이 아님
- `List<Int>?`: element null 불가, 리스트 null 가능
- `List<Int?>?`: element null 가능, 리스트 null 가능



**자바는 읽기 전용 컬렉션과 변경 가능 컬렉션을 구분하지 않는다.**

- 코틀린 코드에서 만든 불변 컬렉션을 자바가 사용할 때는 가변/불변 여부를 모르므로 element 를 추가할 수 있다. 자바에서 element 를 추가하고 코틀린이 이 컬렉션을 다시 참조하면 오동작을 일으킬 수 있음



**자바는 nullable 타입과 non-nullable 타입을 구분하지 않는다.**

- 코틀린에서 non-nullable 리스트를 만들었는데, 자바에서 null 을 리스트에 추가하면 오류 발생할 수 있음



**코틀린쪽의 컬렉션이 자바에서 호출되면 컬렉션 내용이 변경될 수 있음을 감안해야 한다.!!**

- 코틀린쪽에서 `Collections.unmodifiableXXX()` 를 활용하면 변경 자체를 막을 수는 있다.



**코틀린에서 자바 컬렉션을 가져다 사용할 때 플랫폼 타입을 신경써야 한다.**

- 자바 코드를 보며 맥락을 확인하고, 자바 코드를 가져오는 지점을 wrapping 한다.



## 함수











## 람다











## 컬렉션을 함수형으로







