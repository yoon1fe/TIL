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

### 확장함수

도입 배경: 자바와 100프로 호환되는 언어를 만들자! 기본 자바 코드 위에 자연스럽게 코틀린 코드를 추가할 수는 없을까 ? 

-> 어떤 클래스 안에 있는 메서드 처럼 호출할 수 있지만, 함수는 밖에 만들 수 있도록 하자!



``` kotlin
// String 클래스 안에 있는 것 같은 함수
fun String.lastChar(): Char {
  return this[this.length - 1]
}
```

- `String(수신객체 타입)` 클래슬 확장
- 함수 안에서는 `this(수신객체)` 로 인스턴스에 접근



**사용**

``` kotlin
fun main() {
  val str= "ABC"
  println(str.lastChar())
}
```



**그럼, 확장함수가 public 이고, 확장함수에서 수신객체 클래스의 private 함수를 가져오면 캡슐화가 깨지는 게 아닌가??**

- 확장함수는 클래스에 있는 private, protected 멤버를 가져올 수 없다!



**멤버함수와 확장함수의 시그니처가 같다면 ?**

- 멤버함수가 우선적으로 호출된다.



**확장함수가 override 된다면?**

- ``` kotlin
  val train: Train = Train()
  train.isExpensive()	// Train 의 확장함수
  
  val srt1: Train = Srt()
  srt1.isExpensive()	// Train 의 확장함수
  
  val srt2: Srt = Srt()
  srt2.isExpensive()	// Srt 의 확장함수
  ```

- 인스턴스는 중요하지 않다. 해당 변수의 현재 타입, 즉 정적인 타입에 의해 어떤 확장함수가 호출될지 결정됨.



**추가**

- 자바에서는 정적 메서드를 부르는 것처럼 사용 가능

- 확장함수라는 개념은 확장 프로퍼티와도 연결된다.

  확장 프로퍼티의 원리는 확장함수 + custom getter와 동일



### infix 함수

중위함수, 함수를 호출하는 새로운 방법!

`변수.함수이름(argument)` 대신, `변수 함수이름 argument`

``` kotlin
fun Int.add(other: Int): Int {
  return this + other
}

infix fun Int.add2(other: Int): Int {
  return this + other
}

2.add(4)

3.add2(4)
3 add2 4
```

- infix는 멤버함수에도 붙일 수 있다.



### infline 함수

- 함수가 호출되는 대신, 함수를 호출한 지점에 함수 본문을 그대로 복붙하고 싶은 경우 사용
- 함수를 파라미터로 전달할 때 오버헤드를 줄일 수 있다
- 하지만 성능 측정과 함께 신중하게 사용해야 함



### 지역 함수

- 함수 안에 함수 선언



**as-is**

``` kotlin
fun createPerson(firstName: String, lastName: String): Person {
  if (firstName.isEmpty()) {
    ...
  }
  if (lastName.isEmpty()) {
    ...
  }
  
  return Person(firstName, lastName, 1)
}
```



**to-be**

``` kotlin
fun createPerson(firstName: String, lastName: String): Person {
  fun validateName(name: String, fieldName: String) {
    if (name.isEmpty()) {
	    throw IllegalArgumentException()      
    }
  }
  
  validateName(firstName, "firstName")
  validateName(lastName, "lastName")
  
  
  return Person(firstName, lastName, 1)
}
```

- 함수로 추출하면 좋을 것 같은데, 이 함수를 지금 함수 내에서만 사용하고 싶을 때 사용
- 하지만, depth가 깊어지기도 하고, 코드가 그렇게 깔끔하지 않다..

- 강사) 써본 적 없댄다.



## 람다 - 함수형 프로그래밍의 시작~

### Java에서 람다를 다루기 위한 노력

- 요구사항이 많아지는데 메서드에 파라미터를 추가하는 것만으로 대응이 안된다

- 인터페이스와 익명 클래스를 사용하자!

- 하지만, 익명 클래스는 복잡하다! 또한 다양한 조건의 filter가 필요할 수 있음

- JDK 8부터 람다(이름이 없는 함수) 등장! Predicate, Consumer 등 많이 만들어 두었음

  +) 간결한 스트림도 등장. ++) 메서드 레퍼런스

- **메서드 자체를 직접 넘겨주는 것처럼** 쓸 수 있다.

- 바꿔 말하면, 자바에서 함수는 변수에 할당되거나 파라미터로 전달할 수 없다! 함수를 2급 시민으로 간주.



### 코틀린에서의 람다

- 코틀린에서는 자바와 근본적으로 다른 부분이 있음. 바로 **함수가 그 자체로 값이 될 수 있다는 점**. 변수에 할당될 수도, 파라미터로 넘길 수도 있다.



``` kotlin
// 람다를 만드는 방법 1
val isApple = fun(fruit: Fruit): Boolean {
  return fruit.name == "사과"
}

// 함수의 타입 표기
val isApple: (Fruit) -> Boolean = fun(fruit: Fruit): Boolean {
  ...
}

// 람다를 만드는 방법 2
val isApple2 = { fruit: Fruit -> fruit.name == "사과" }

// 람다를 직접 호출하는 방법 1
isApple(Fruit("사과", 1000))

// 람다를 직접 호출하는 방법 2
isApple.invoke(Fruit("사과", 1000))

// 사용 방법
private fun filterFruits(
  fruits: List<Fruit>, filter: (Fruit) -> Boolean
): List<Fruit> {
  val results = mutableListOf<Fruit>()
  for (fruit in fruits) {
    if (filter(fruit)) {
      results.add(fruit)
    }
  }
  return results
}


filterFruits(fruits, isApple)
```

- 참고) 중괄호와 화살표를 활용한 형태를 함수에 넣어줄 때, 그 함수에서 받는 함수 파라미터가 마지막에 있으면 소괄호 밖에 중괄호를 넣을 수 있다!!

  `filterFruits(fruits) { fruit -> fruit.name == "사과" }`

- 파라미터가 하나인 경우 변수 명을 명시안하고 `it`만 써도 됨

  `filterFruits(fruits) { it.name == "사과"}`

- 람다를 여러 줄 작성할 수 있고, 마지막 줄의 결과가 람다의 반환값



**코틀린에서는 함수가 1급 시민!**



### Closure

**Java**

``` java
String targetFruitName = "바나나";
targetFruitName = "수박";
filterFruits(fruits, (fruit) -> targetFruitName.equals(fruit.getName()));
```

- Variable used in lambda expression should be final or effectively final 오류 발생. 자바에서는 람다를 쓸 때 사용할 수 있는 변수에 제약이 있다!



**Kotlin**

``` kotlin
var targetFruitName = "바나나"
targetFruitName = "수박"
filterFruits(fruits) { it.name == targetFruitName }
```

- 코틀린에서는 가능!!
- 람다가 시작하는 지점에 참조하고 있는 변수들을 모두 포획하여 그 정보를 갖고 있다.
- 이렇게 해야만 람다를 진정한 일급 시민으로 간주할 수 있다. 이 데이터 구조를 **Closure**라고 부름.



### 다시 try-with-resources

``` kotlin
fun readFile(path: String) {
  BufferedReader(FileReader(path)).use { reader ->
    println(reader.readLine())
  }
}
```



## 컬렉션을 함수형으로

### filter, map

``` kotlin
data class Fruit(
  val id: Long,
  val name: String,
  val factoryPrice: Long,
  val currentPrice: Long,
)
```



**요구사항**

- 사과만 주세요

  `val apples = fruits.filter { fruit -> fruit.name == "사과" }`

  - 필터에서 인덱스가 필요하다면: `filterIndexed`

- 사과의 가격들을 알려주세요 ..

  ``` kotlin
  fruits.filter { fruit -> fruit.name == "사과"}
  	.map { fruit -> fruit.currentPice }
  ```

  - 인덱스 : `mapIndexed`
  - null 이 아닌 것만: `mapNotNull`



### 다양한 컬렉션 처리 기능

- 모든 과일이 사과인가요 ?

  `val isAllApple = fruits.all { fruit -> fruit.name == "사과" }`

  - `none` 도 있음

- 출고가 10,000원 이상의 과일이 하나라도 있나요?

  `fruits.any { fruit -> fruit.factoryPrice >= 10_000 }`

- `count`, `sortedBy`, `sortedByDescending`, `distinctBy`, `first()`, `firstOrNull()`, `last()`, `lastOrNull()`



### List 를 Map 으로

- 과일 이름 -> List<과일> map 이 필요해용

  `Map<String, List<Fruit>> = fruits.groupBy {fruit -> fruit.name}`

- id -> 과일 Map

  `Map<Long, Fruit> = fruits.associateBy { fruit -> fruit.id }`

- Key, Value 동시 처리

  `Map<String, List<Long>> = fruits.groupBy({ fruit -> fruit.namae }, { fruit -> fruit.factoryPrice })`

- Map 에 대해서도 앞선 기능들을 대부분 사용 가능!



### 중첩된 컬렉션 처리

List 안에 List ..

- 출고가와 현재가가 동일한 과일 골라줘

  `fruitsInList.flatMap { list -> list.filter {fruit -> fruit.factoryPrice == fruit.currentPrice }}`

  ``` kotlin
  data class Fruit() {
    val isSamePrice: Boolean
    	get() = factoryPrice == currentPrice
  }
  
  ..
  
  val List<Fruit>.samePriceFilter: List<Fruit>
  	get() = this.filter(Fruit::isSamePrice)
  
  ..
  
  val samePriceFruits = fruitsInList.flatMap {list -> list.samePriceFilter }
  ```

- `List<List<Fruit>>` ->`List<Fruit>`

  `fruitsInList.flatten()`
