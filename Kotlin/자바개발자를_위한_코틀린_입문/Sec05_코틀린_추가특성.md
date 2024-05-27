## 코틀린의 이모저모

### Type Alias와 as import

**Type Alias**

- 긴 이름의 클래스 혹은 함수 타입이 있을 때, 이를 축약하거나 더 좋은 이름을 쓰고 싶다!



``` kotlin
fun filterFruits(fruits: List<Fruit>, filter: (Fruit) -> Boolean) {
  ...
}
// (Fruit) -> Boolean 이란 타입이 너무 길다!!
-->
typealias FruitFilter = (Fruit) -> Boolean
fun filterFruits(fruits: List<Fruit>, filter: FruitFilter) {
  ...
}
```



**as import**

- 다른 패키지의 같은 이름을 가진 함수를 동시에 가져오고 싶다면?
- as import: 어떤 클래스나 함수를 임포트할 때 이름을 바꾸는 기능

``` kotlin
import com.aaa.bbb.printHelloWorld as printHelloWorldA
import com.aaa.ccc.printHelloWorld as printHelloWorldB
```



### 구조 분해와 componentN 함수

**구조 분해**

- 복합적인 값을 분해하여 여러 변수를 한 번에 초기화하는 것

``` kotlin
data class Person(
	val name: String,
  val age: Int
)

fun main() {
  val person = Person("홍길동", 21)
  val (name, age) = person
  // 위와 같은 코드
  // val name = person.component1()
  // val age = person.component2()
  println("이름: ${name}, 나이: ${age}")
}
```

- data class 는 componentN 함수를 자동으로 만들어준다. 데이터 클래스의 N번째 프로퍼티를 가져오는 기능



**Data class 가 아닌데 구조 분해를 사용하고 싶다면, componentN 함수를 직접 구현해줄 수도 있다.**

``` kotlin
class Person(
	val name: String,
  val age: Int
) {
  operator fun component1(): String {
    return this.name
  }
  
  operator fun component2(): Int {
    return this.age
  }
}
```



### Jump와 Label

**return / break / continue **

- 자바랑 완전 동일.

- 단! foreach 에서는 continue, break 등 사용 못함.

- 꼭 foreach 와 같이 쓰고 싶다면,

  ``` kotlin
  val numbers = listOf(1, 2, 3, 4)
  run {
    numbers.forEach { number ->
      if (number == 3) {
        return@run			// break
        return@foreach	// continue
      }
      println(number)
    }
  }
  ```

- 그냥 for 문 쓰자.



**Label**

- 특정 expression에 라벨 이름@을 붙여 하나의 라벨로 간주하고, break, continue, return 등을 사용하는 기능

- 자바의 `outer:` 라벨 요거랑 비슷한듯.
- 라벨을 사용한 Jump 는 사용하지 않는 것을 강력 추천!



### takeIf, takeUnless

``` kotlin
fun getNumberOrNull(): Int? {
  return if (number <= 0) {
    null
  } else {
    number
  }
}
```

- 코틀린에서는 메서드 체이닝을 위한 특이한 함수를 제공한다.

  ``` kotlin
  fun getNumberOrNullV2(): Int? {
    return number.takeIf { it > 0}
  }
  ```

  - 주어진 조건을 만족하면 그 값을, 그렇지 않으면 null 을 반환한다.

- `takeUnless`: 반대



## scope function

### scope function 이란?

- 일시적인 영역을 형성하는 함수



``` kotlin
fun printPerson(person: Person?) {
  if (person != null) {
    println(person.name)
    println(person.age)
  }
  
  // 리팩터링
  person?.let {
    println(it.name)
    println(it.age)
  }
}
```

- let: scope function 의 한 종류. 파라미터로 함수를 받아서 그 함수를 실행시킴.



**scope function**

- 람다를 이용해 일시적인 영역을 만들고, 코드를 더 간결하게 만들거나 method chaining 에 활용하는 함수



### scope function의 분류

**람다의 결과를 반환**

- let: it
- run: this



**객체 그 자체를 반환**

- also: it
- apply: this



- with - 확장함수 아님.

  ``` kotlin
  with(person) {
    println(name)
    println(this.age)
  }
  ```

  - with(파라미터, 람다): this를 사용해 접근하고, this 생략 가능



**it, this**

- this: 생략 가능, 다른 이름 붙일 수 없음
- it: 생략 불가능, 다른 이름 붙일 수 있음



### 언제 어떤 scope function을 사용해야 할까?



**let**

- 하나 이상의 함수를 call chain 결과로 호출할 때

  ``` kotlin
  val strings = listOf("APPLE", "CAR")
  strings.map { it.length }
  	.filter { it > 3 }
  	.let(::println)
  //.let { lengths -> println(lengths) }
  ```

- non-null 값에 대해서만 code block 을 실행시킬 때

  ``` kotlin
  val length = str?let {
    println(it.uppercase())
    it.length
  }
  ```

- 일회성으로 제한된 영역에 지역 변수를 만들 때

  ``` kotlin
  val numbers = listOf("one", "two")
  val modifiedFirstItem = numbers.first()
  	.let { firstItem ->
      if (firstItem.length >= 5) firstItem else "!$firstItem!"
    }.uppsercase()
  println(modifiedFirstItem)
  ```



**run**

- 객체 초기화와 반환 값의 계산을 동시에 해야 할 때

  `val person = Person("홍길동", 29).run(personRepository::save)`

  - 강사) 개인적으로 잘 쓰지 않는다. 반복되는 생성 후처리는 생성자, 프로퍼티, init block 에 넣는 것이 좋다.



**apply** - 객체 그 자체가 반환

- 객체 설정할 때 객체를 수정하는 로직이 call chain 중간에 필요할 때
  - ex) test fixture 만들 때



**also**

- 객체를 수정하는 로직이 call chain 중간에 필요할 때



**with**

- 특정 객체를 다른 객체로 변환해야 하는데, 모듈 간의 의존성에 의해 정적 팩토리 혹은 toClass 함수를 만들기 어려울 때

  ``` kotlin
  return with(person) {
    PersonDto(
    	name = name,
      age = age
    )
  }
  ```



### scope function과 가독성

``` kotlin
// 1
if (person != null && person.isAdult) {
  view.showPerson(person)
} else {
  view.showError()
}

// 2
person?.takeIf { it.isAdult }
	?.let(view::showPerson)
	?: view.showError()
```

- 1번 코드: 전통적인 if - else 활용
- 2번 코드: 
  - scope function 활용한 코틀린스러운 코드. 숙련된 코틀린 개발자만 더 알아보기 쉽다.. 
  - 구현 1의 디버깅이 쉽다.
  - 구현 1이 수정도 더 쉽다.
  - 그럼 둘 중에 어떤 코드가 더 좋을 코드일까?



사용 빈도가 적은 관용구는 코드를 더 복잡하게 만들고, 이런 관용구들을 한 문장 내에서 조합해 사용하면 복잡성이 훨씬 증가한다.

적절한 convention을 적용해서 scope function 을 사용하면 유용하게 활용할 수 있다~
