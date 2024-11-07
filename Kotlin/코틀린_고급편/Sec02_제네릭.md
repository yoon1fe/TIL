## 제네릭과 타입 파라미터

**예제**

- Cage 클래스 - 동물을 넣거나 꺼낼 수 있음
  - getFirst(): Animal - 첫 번째 동물을 가져온다.
  - put(animal: Animal) - 동물을 넣는다
  - moveFrom(cage: Cage) - 다른 Cage에 있는 동물을 모두 가져온다.



**Cage.kt**

``` kotlin
package advanced.generic

class Cage {
    private val animals: MutableList<Animal> = mutableListOf()

    fun getFirst(): Animal {
        return animals.first()
    }

    fun put(animal: Animal) {
        this.animals.add(animal)
    }

    fun moveFrom(cage: Cage) {
        this.animals.addAll(cage.animals)
    }
}
```



**Animal.kt**

```kotlin
package advanced.generic

abstract class Animal(
    val name: String,
)

abstract class Fish(name: String) : Animal(name)

// 금붕어
class GoldFish(name: String) : Fish(name)

// 잉어
class Carp(name: String) : Fish(name)
```



Cage에 잉어를 넣었다가 빼려고 하면

```kotlin
fun main() {
    val cage = Cage()
    cage.put(Carp("잉어"))

    val carp: Carp = cage.getFirst()
}
```

- Type Mismatch!

- 간단한 해결법으로는  타입 캐스팅이 있다. `as` 키워드를 이용해 타입 캐스팅 가능.

  `val carp: Carp = cage.getFirst() as Carp`

- 근데 위 코드는 컴파일 타임에 에러를 잡을 수 없기 때문에 위엄한 코드이다.

- 타입 안전하게 잉어를 가져오는 법

  - safe type casting + 엘비스 연산자

    `val carp: Carp = cage.getFirst() as? Carp ?: throw IllegalArgumentException()`

  - 여전히 실수할 여지가 있다..

- **그렇다면, 동일한 Cage 클래스이지만 잉어만 넣을 수 있는 Cage, 금붕어만 넣을 수 있는 Cage를 구분하는 방법은 어떨까? 이 방법을 사용하면 타입 안전하게 잉어를 Cage에 넣었다가 잉어 타입으로 가져올 수 있다.**



**제네릭**

```kotlin
class Cage2<T> {
    private val animals: MutableList<T> = mutableListOf()

    fun getFirst(): T {
        return animals.first()
    }

    fun put(animal: T) {
        this.animals.add(animal)
    }

    fun moveFrom(cage: Cage2<T>) {
        this.animals.addAll(cage.animals)
    }

}
```

- Cage2: 제네릭 클래스
- <T\>의 T: 타입 파라미터
- Cage2 클래스를 인스턴스화할 때 타입 정보를 넣어주어야 하고, 그 때 넣어준 타입 정보가 모두 Cage2 클래스의 T를 대체!



```kotlin
val cage2 = Cage2<Carp>()
cage2.put(Carp("잉어"))

// 이제 as 없이도 된다
val carp2: Carp = cage2.getFirst()
```



## 배열과 리스트, 제네릭과 무공변

**다음 요구사항**

- 금붕어 Cage에 금붕어 한 마리를 넣고, 물고기 Cage에 `moveFrom` 메서드를 사용해 금붕어를 옮겨보자.

```kotlin
val goldFishCage = Cage2<GoldFish>()
goldFishCage.put(GoldFish("금붕어"))

val fishCage = Cage2<Fish>()
fishCage.moveFrom(goldFishCage)	// Type Mismatch!!
```

- 금붕어를 바로 물고기 Cage에 넣는 것은 문제가 없다..

- Required: Cage2<Fish\>
- Found: Cage2<GoldFish\>
  - 이 두 리스트는 아무 관계가 없다. == 무공변(in-variant, 불공변)



**왜 Fish 와 GoldFish 간의 상속 관계가 제네릭 클래스에는 유지되지 않을까??**

- 자바의 배열은 제네릭과 다름. A객체가 B객체의 하위 타입이면 A[] 배열은 B[] 배열의 하위 타입이다. == 공변(co-variant) 이는 매우 위험하다. 런타임에서야 예외를 낼 수 있음.

- `List`는 제네릭을 사용하고 있고, 불공변하기 때문에 컴파일 시점에 오류를 확인할 수 있다!!

  ``` java
  List<String> strs = List.of("A", "B", "C"); List<Object> objs = strs; // Type Mismatch
  ```



## 공변과 반공변

**그럼 어떻게 하면 위 문제를 해결할 수 있을까??**

- `moveFrom` 함수를 호출할 때 `Fish` 와 `GoldFish`의 상속 관계를 `Cage2<Fish>` 와 `Cage2<GoldFish>`에도 이어주도록 하자! == 공변(co-variant)하게 만든다.

- 코틀린에서 함수의 공변을 표현하기 위해서는 함수에 있는 타입 파라미터 앞에 `out`을 붙이면 된다!! 아주 간단.



```kotlin
fun moveFrom(otherCage: Cage2<out T>) {
    this.animals.addAll(otherCage.animals)
}
```

- `out`: 변성 애너테이션(variance annotation)
- `out`을 붙이게 되면 `otherCage`로부터 **데이터를 꺼내는 것만 가능하다!**
  - otherCage는 생산자(데이터를 꺼내는) 역할만 할 수 있다.



**왜 out을 붙이면 생산자 역할만 할 수 있을까?**

- 만약 otherCage가 소비자 역할도 할 수 있다고 했을 때..

  - 다음 코드가 가능해짐

  ``` kotlin
  fun moveFrom(otherCage: Cage2<out T>) {
    otherCage.put(this.getFirst())	// 에러없다고 가정
    this.animals.addAll(otherCage.animals)
  }
  
  ...
  
  val goldFishCage = Cage2<GoldFish>()
  goldFishCage.put(GoldFish("금붕어"))
  
  val cage2 = Cage2<Fish>()
  cage2.put(Carp("잉어"))
  cage2.moveFrom(goldFishCage)
  ```

  - 잉어 -> Cage2<GoldFish\> 에 넣게 된다..!!
  - 즉, 소비자 역할도 주게 되면 타입 안정성이 깨지게 된다.



**반대의 경우는?**

- Cage2<Fish\>가 Cage2<GoldFish\>의 하위 타입이고 싶을 땐?
- `in` 붙여주면 된다. == 반공변(contra-variant)

``` kotlin
fun moveTo(otherCage: Cage2<in T>) {
  otherCage.animals.addAll(this.animals)
}
```

- in이 붙은 otherCage는 데이터를 받을 수만 있다! == 소비자



**정리**

- `out`: (함수 파라미터 입장에서) 생산자, 공변
- `in`: (함수 파라미터 입장에서) 소비자, 반공변



## 선언 지점 변성 / 사용 지점 변성

- kotlin: `out`
- java: `? extends`



- kotlin: `in`
- java: `? super`



이런 복잡한 타입을 계속해서 퍼지게 하는 대신, 제네릭 클래스 자체를 공변시키거나 반공변시킬 수는 없을까?? `out` 사용하지 않고 `Cage2<T>` **클래스 자체를 공변**하게..

- 코틀린에서는 가능하다!!



```kotlin
class Cage3<T> {
    private val animals: MutableList<T> = mutableListOf()

    fun getFirst(): T {
        return this.animals.first()
    }

    fun getAll(): List<T> {
        return this.animals
    }
}
```

- Cage3는 생산만 하는 클래스임. == 데이터를 내보내기만 함.



다음 코드가 동작하게 하려면?

```kotlin
fun main() {
    val fishCage = Cage3<Fish>()
    val animalCage: Cage3<Animal> = fishCage
}
```

- `class Cage3<out T>`
- Declaration-site variance. 선언 지점 변성
  - 참고) 메서드 파라미터 등에서 사용하는 변성은 use-site variance(사용 지점 변성)



**코틀린 표준 라이브러리 예시**

```kotlin
public interface List<out E> : Collection<E> {
    // Query Operations

    override val size: Int
    override fun isEmpty(): Boolean
    override fun contains(element: @UnsafeVariance E): Boolean
    override fun iterator(): Iterator<E>

    // Bulk Operations
    override fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean
    
    ...
    
}
```

- 코틀린의 List는 불변 컬렉션이므로 데이터를 생산(반환)만 함. `out` 키워드 확인
- 단, `contains()`와 같이 데이터를 소비해야 하는 함수의 경우 `@UnsafeVariance` 애너테이션을 붙여주어야 한다.



## 제네릭 제약과 제네릭 함수

**제네릭 제약(Generic Constraints)**

- Cage 클래스에는 동물만 넣고 싶지만, 사실 숫자나 문자열도 넣을 수 있음.
- 타입 파라미터 T에 Animal과 그 하위 타입만 들어오게 하고 싶다면? -> 제네릭 제약
- `class Cage5<T : Animal>` 로 정의하면 됨!



```kotlin
package advanced.generic

fun main() {
    val fishCage = Cage5<Fish>()
    val stringCage = Cage5<String>()	// Type argument is not within its bounds.
}

class Cage5<T : Animal> {
    private val animals: MutableList<T> = mutableListOf()

    fun getFirst(): T {
        return this.animals.first()
    }

    fun getAll(): List<T> {
        return this.animals
    }
}
```

- 타입 파라미터의 상한(upper bound)를 지정.

- 여러 개의 제약을 두고 싶다면 문법이 조금 달라짐.
- `class Cage5<T>where T : Animal, T : Comparable<T>`



**Null 타입 방지**

- `Cage2<GoldFish?>()` 가능.
- null 타입이 들어오는 것을 방지하기 위해 `Cage2<T : Any>`로 선언.
  - 타입 파라미터를 `Any` 타입의 하위 타입으로 제한하여 nullable 타입을 들어오지 못하도록 막는 것.



**함수에서는 제네릭**

- `sorted`: `public fun <T : Comparable<T>> Iterable<T>.sorted(): List<T>`
- 제네릭 함수를 사용하면 유틸성 기능 개발에 유용하다.



## 타입 소거와 Star Projection

- 코틀린은 자바와 달리 언어 초기부터 제네릭이 고려되었기 때문에 raw type 객체를 만들 수 없다.
- 하지만 코틀린도 JVM 위에서 동작하기 때문에 런타임 시점에 타입 정보가 사라진다. == 타입 소거(Type erasure)

- `if (data is List<String>) ` : Error: Cannot check for instance of erased type: List<String\>
  - 런타임에 타입이 소거되기 때문에 `List<String>` 타입인지 알 수 없음.
  - start projection을 활용해 최소한 List 인지는 확인할 수 있다.



**star projection**

- `if (data is List<*>)`
- 해당 타입 파라미터에 어떤 타입이 들어있는지는 모른다.



**제네릭 함수에서의 타입 소거**

```kotlin
fun <T> T.toSuperString(): String {
    println(T::class.java.name) // T가 무엇인지 런타임 때도 알 수 없기 때문에 오류가 난다
    return "Super $this"
}
```



주어진 리스트에 T 타입을 가진 원소가 하나라도 있는지 확인하는 코드

- 제네릭을 사용해 일반화할 수 없다면, 각 타입마다 유틸 함수를 별도로 만들어야 할 것.

- ```kotlin
  // 우리가 원하는 형태
  fun <T> List<*>.hasAnyInstanceOf(): Boolean {
      return this.any { it -> it is T }
  }
  // 각 타입별로 만드는 방법
  fun List<*>.hasAnyInstanceOfString(): Boolean {
      return this.any { it is String }
  }
  fun List<*>.hasAnyInstanceOfInt(): Boolean {
      return this.any { it is Int }
  }
  ```

- `reified` 키워드와 inline 함수를 사용하면 해결 가능하다.

  - inline 함수는 함수의 본문을 함수 호출 지점에 옮기는데, 이러한 점을 활용해 주어진 제네릭 타입 정보를 알 수 있음.

  ```kotlin
  inline fun <reified T> List<*>.hasAnyInstanceOf(): Boolean {
      return this.any { it is T }
  }
  ```

  - `reified` 키워드의 한계
    - reified 키워드가 붙은 타입 T를 이용해
    - T의 인스턴스를 만들거나
    - T의 companion object를 가져올 수는 없다.



## 제네릭 용어 정리 및 간단한 팁

**제네릭 클래스**

- `class Cage<T>`
- 타입 파리미터를 사용한 클래스



**Raw 타입**

- `List list = new ArrayList();`
- 제네릭 클래스에서 타입 매개변수를 사용하지 않고 인스턴스화 하는 것
- 코틀린에서는 Raw 타입 사용 불가



**변성**

- 제네릭 클래스 타입 파라미터에 따라 제네릭 클래스 간의 상속 관계가 어떻게 되는지를 나타내는 용어



**무공변(불공변, in-variant)**

- 타입 파라미터끼리는 상속 관계더라도, 제네릭 클래스 간에는 상속 관계가 없다는 의미
- 변성을 부여하지 않았다면 제네릭 클래스는 기본적으로 무공변하다.



**공변(co-variant)**

- 타입 파라미터간의 상속 관계가 제네릭 클래스에서도 동일하게 유지된다는 의미
- `out` 변성 애너테이션 사용



**반공변(contra-variant)**

- 타입 파라미터간의 상속 관계가 제네릭 클래스에서는 반대로 유지된다는 의미
- `in`



**선언 지점 변성**

- 클래스 자체를 공변하거나 반공변하게 만드는 방법



**사용 지점 변성**

- 특정 함수나 변수에 대해 공변/반공변 만드는 방법



**제네릭 제약**

- 제네릭 클래스의 타입 파라미터에 제한을 거는 방법



**타입 소거**

- JDK 호환성을 위해 런타임때 제네릭 클래스의 타입 파라미터가 지워지는 것
- inline 함수 + reified 키워드를 이용해 타입 소거를 일부 막을 수 있다.



**star projection**

- 어떤 타입이든 들어갈 수 있다는 의미



### 팁

**타입 파라미터 섀도잉**

``` kotlin
class Cage<T : Animal> {
    fun <T : Animal> addAnimal(animal: T) {
    }
}
```

- 위 코드의 잘못된 점??
- 함수의 T는 클래스의 타입 파라미터(T)와 같은 게 아니고, 덮어 씌운 것.

- 다음과 같이 잘못 들어가도 오류가 발생하지 않게 됨.

  ``` kotlin
  val cage = Cage<GoldFish>()
  cage.addAnimal(GoldFish("금붕어"))
  cage.addAnimal(Carp("잉어"))
  ```

- 타입 파라미터 섀도잉은 피해야 하고, 함수 타입 파라미터를 쓰려면 이름이 겹치지 않도록 주의하자..



**제네릭 클래스의 상속**

```kotlin
open class CageV1<T : Animal> {
    fun addAnimal(animal: T) {
    }
}

class CageV2<T : Animal> : CageV1<T>() {
}

class GoldFishCageV2 : CageV1<GoldFish>() {
}
```



**제네릭과 Type Alias**

```kotlin
// as-is
fun handleCacheStore(store: Map<PersonDtoKey, MutableList<PersonDto>>) {
}

// to-be
typealias PersonDtoStore = Map<PersonDtoKey, MutableList<PersonDto>>

fun handleCacheStore(store: PersonDtoStore) {
}
```
