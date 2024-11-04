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





## 제네릭 제약과 제네릭 함수





## 타입 소거와 Star Projection





## 제네릭 용어 정리 및 간단한 팁





