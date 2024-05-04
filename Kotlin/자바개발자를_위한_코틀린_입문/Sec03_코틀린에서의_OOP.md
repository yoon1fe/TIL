## 클래스

### 클래스와 프로퍼티

**Java**

``` java
public class JavaPerson {
  private final String name;	// compile error
  private int age;
  
  // IDE의 도움을 받으면..
  
  public JavaPerson(String name, int age) {
    this.name = name;
    this.age = age;
  }
  
  public String getName() {
    return this.name;
  }
  
  public int getAge() {
    return this.age;
  }
  
  public void setAge(int age) {
    this.age = age;
  }

  // name 에는 setter 없음
}
```



**Kotlin**

``` kotlin
class Person constructor(name: String, age: Int){	// default 접근 제어자: public
  val name: String = name
  var age: Int = age
}
```

- property = field + getter + setter. 코틀린에서는 필드만 만들면 getter, setter 를 자동으로 만들어준다.

  - `.필드` 를 통해 getter와 setter 를 바로 호출한다.
  - 자바 클래스에 대해서도 `.필드` 로 getter / setter 사용!

- 생성자를 위에 선언. `constructor` 키워드는 생략 가능!

- 생성자에서 프로퍼티를 바로 만들 수도 있음. 즉, 클래스의 필드 선언과 생성자를 동시 선언 가능

  ``` kotlin
  class Person(
    val name: String, 
    var age: Int
  ) {}
  ```



### 생성자와 init

**클래스가 생성되는 시점에 나이를 검증해보자!**

**Java**

- 생성자에서 검증해줄 수 있겠다.

``` java
public JavaPerson(String name, int age) {
  if (age <= 0) {
    throw new IllegalArgumentException(...);
  }
  
  this.name = name;
  this.age = age;
}
```



**Kotlin**

- 코틀린에서는 어디서 하면 좋을까 ??
- `init` 블록. 클래스가 초기화되는 시점에 한 번 호출되는 블록.
  - 값을 적절히 만들어주거나, validation 에 쓰임

``` kotlin
class Person(
  val name: String, 
  var age: Int
) {
  init {
    if (age <= 0) {
      throw IllegalArgumentException(...);
    }
  }
}
```

- 추가 생성자는 어떻게 만들까?

- `constructor()` 키워드로 만들면 된다. `: this()` 위에 있는 기본 생성자(주 생성자, primary constructor) 호출한다는 의미.

  - 주 생성자는 반드시 존재해야 한다. 단, 파라미터가 하나도 없다면 생략 가능.

  `constructor(name: String) : this(name, 1)`

  - 부 생성자

  - 최종적으로 주 생성자를 호출해야 한다.

  - body 를 가질 수 있다. 바디는 역순으로 실행됨!!

  - 다만, **코틀린에서는 부 생성자보다 default parameter 를 권장한다.**

    ``` kotlin 
    class Person(
    	val name: String = "홍길동",
      var age: Ing = 1,
    ) {
      ...
    }
    ```



### 커스텀 getter, setter

**성인 여부 확인하는 기능**

``` kotlin
// 1. 
fun isAdult(): Boolean {
  return this.age >= 20
}

// 2. custom getter 사용
val isAdult: Boolean
  get() = this.age >= 20
```

- custom getter: 클래스에 프로퍼티가 있는 것처럼 보여주는 것.

- 강사 의견) 객체의 속성이라면 custom getter, 그렇지 않다면 함수로 표현



### backing field

**name 을 get 할 때 무조건 대문자로 바꾸기**

``` kotlin
class Person(
	name: String,
  var age: Int,
) {
  val name: String = name
  	get() = field.uppercase()
  	// get() = name.uppsercase() => name 은 다시 getter 를 부르니깐 무한 루프 발생!! field: 무한 루프를 막기 위한 예약어.
}
```

- 강사 경험) 개인적으로 custom getter 에서 backing field 를 쓰는 경우는 드물었다.



**set 할 때 대문자를 넣어주도록**

``` kotlin
class Person(
	name: String = "홍길동"
	var age: Int = 1
) {
  var name = name
  	set(value) {
      field = value.uppsercase()
    }
}
```

- 사실 setter 자체를 지양하기 때문에 custom setter 잘 안쓴다^^;
- update 함수를 쓰는 것이 좋다.



## 상속

### 추상 클래스

- 추상 클래스 Animal
- 자식 클래스 Cat, Penguin



#### `Animal`

**Java**

``` java
public abstract class JavaAnimal {
  protected final String species;
  protected final int legCount;
  
	// 생성자.. getter
  
  abstract public void move();
}
```



**Kotlin**

``` kotlin
abstract class Animal(
  protected val species: String,
  protected val legCount: Int,
) {
  
  abstract fun move()
  
}
```



#### `Cat`

**Java**

``` java
public class JavaCat extends JavaAnimal {
  public JavaCat(String species) {
    super(species, 4);
  }
  
  @Override
  public void move() {
    System.out.println("고양이 사뿐사뿐");
  }
}
```



**Kotlin**

``` kotlin
class Cat(
	species: String
) : Animal(species, 4) {
  
  override fun move() {
    println("고양이~")
  }
}
```

- 상속받을 때 `extends` 대신  `:` 사용. 타입을 쓸 때도 콜론 쓰는데, 컨벤션은
  - `변수명: 타입`
  - `클래스명 : 부모 클래스`



#### Penguin

**Java**

``` java
public final class JavaPenguin extends JavaAnimal {
  private final int wingCount;
  
  public JavaPenguin(String species) {
    super(species, 2);
    this.wingCount = 2;
  }
  
  @Override
  public void move() {
    System.out.println("펭귄 꽥꽥");
  }
  
  @Override
  public int getLegCount() {
    return super.legCount + this.wingCount;
  }
}
```



**Kotlin**

``` kotlin
class Penguin(
  var species: String
) : Animal(species, 2) {
  
  private val wingCount: Int = 2
  
  override fun move() {
    println("펭귄 꽥꽥")
  }
  
  override val legCount: Int
  	get() = super.legCount + this.wingCount	// 컴파일 오류 발생. legCount 에 open 키워드가 없기 때문!
}
```

- 프로퍼티를 override 할 때는 반드시 open 을 붙여주어야 한다.



### 인터페이스

**Flyable 과 Swimmable 인터페이스를 구현하는 Penguin 클래스**



``` kotlin
interface Flyable {
  fun act() {
    println("파닥 파닥")
  }
  
  // 추상 메서드
  // fun fly()
}

interface Swimmable {
  fun act() {
    println("어푸 어푸")
  }
}
```

- default 키워드 없이 디폴트 메서드 선언 가능



**Penguin**

``` kotlin
class Penguin(
  var species: String
) : Animal(species, 2), Swimmable, Flyable {
  
  private val wingCount: Int = 2
  
  override fun move() {
    println("펭귄 꽥꽥")
  }
  
  override val legCount: Int
  	get() = super.legCount + this.wingCount	// 컴파일 오류 발생. legCount 에 open 키워드가 없기 때문!
  
  override fun act() {
    super<Swimmable>act()
    super<Flyable>act()
  }
}
```

- 코틀린에서는 backing field 가 없는 프로퍼티를 Interface에 만들 수 있다.



### 클래스를 상속할 때 주의할 점

``` kotlin
open class Base(
	open val number: Int = 100
) {
  init {
    println("Base Class")
    println(number)
  }
}

class Derived(
	override val number: Int
) : Base(number) {
  init {
    println("Derived Class")
  }
}
```

- 다른 클래스가 상속할 수 있도록 `open` 으로 열어줘야 함.
- 상위 클래스 생성자가 실행되는 동안, 하위 클래스의 프로퍼티(number) 는 초기화되기 전이기 때문에 기본값 (0)이 나온다. 
- 상위 클래스를 설계할 때, 생성자나 초기화 블록에 사용되는 프로퍼티에는 open 을 피해야 한다!



### 상속 관련 지시어 정리

- `final`: override 를 할 수 없게 한다. default 로 보이지 않게 존재한다.
- `open`: override 를 열어준다.
- `abstract`: 반드시 override 해야 한다.
- `override`: 상위  타입을 오버라이드 하고 있다. 자바에서는 애너테이션으로 표현하지만, 코틀린에서는 키워드!!



## 접근 제어

visibility modifier



### 자바와 코틀린의 가시성 제거

public: 모든 곳에서 접근 가능

protected: **선언된 클래스** 또는 하위 클래스에서만 접근 가능

- 코틀린에서는 패키지를 namespace를 관리하기 위한 용도로만 사용한다. 가시성 제어에는 사용 X.

internal: (default 없어짐) 같은 모듈에서만 접근 가능

- 모듈: 한 번에 컴파일되는 Kotlin 코드. IDEA Module, Maven project, gradle source set ...

private: 선언된 클래스 내에서만 접근 가능



**기본 접근 지시어**

- 자바: default
- 코틀린: **public**



### 코틀린 파일의 접근 제어

코틀린은 .kt 파일에 변수, 함수, 클래스 여러 개를 바로 만들 수 있다.



### 다양한 구성 요소의 접근 제어

**클래스 안의 멤버**

- 동일함.



**생성자**

- 동일함. 단! 생성자에 접근 지시어를 붙이려면, `constructor`키워드를 적어주어야 한다.



**팁**

- 유틸성 코드 작성 - 직접 파일에 함수만 정의할 수 있음

  **StringUtils.kt**

  ``` kotlin
  package com.kotlin.yoon1fe
  
  fun isDirectoryPath(path: String): Boolean {
    return path.endsWith("/")
  }
  ```

  -> 자바 코드로 디컴파일해보면 `StringUtilsKt` 란 클래스에 `isDirectoryPath` 메서드가 static final 로 만들어진다.

  사용할 땐 `StringUtilsKt.isDirectoryPath()` 



**프로퍼티**

- 동일함. 단!
- 프로퍼티의 가시성을 설정해주는 방법 두 가지
  - 프로퍼티 정의 부분 앞에 접근 지시어 붙이기
  - setter 에만 추가로 가시성 부여 가능



### Java와 Kotlin을 함께 사용할 경우 주의할 점

- internal은 바이트 코드상 public 이 된다. 때문에 Java 코드에서는 Kotlin 모듈의 internal 코드를 가져올 수 있음.
- protected 가 각각 다르다. 자바의 같은 패키지의 코틀린 protected 멤버에 접근할 수 있음.



## object 키워드

### static 함수와 변수

**Java**

``` java
public class JavaPerson {
  private static final int MIN_AGE = 1;
  
  public static JavaPerson newBaby(String name) {
    return new JavaPerson(name, MIN_AGE);
  }
  
  private String name;
  private int age;
  
  private JavaPerson(String namae, int age) {
    this.name = name;
    this.age = age;
  }
}
```



**Kotlin**

``` kotlin
class Person private constructor(
	var name: String,
  var age: Int,
) {
  
  companion object {
    private const val MIN_AGE = 1
    fun newBaby(name: String): Person {
      return Person(name, MIN_AGE)
    }
  }
  
  
  
}
```

- 코틀린에는 static 키워드가 없다! companion object(동반 객체) 코드 블록으로 대체.
  - static: 클래스가 인스턴스화될 때 새로운 값이 복제되는 것이 아닌라, 정적으로 인스턴스끼리의 값을 공유
  - companion object: 클래스와 동행하는 유일한 오브젝트
    - 하나의 객체로 간주되기 때문에 이름을 붙일 수도 있고, interface를 구현할 수도 있음.

- const 키워드: 가 붙으면 컴파일 시에 변수가 할당된다.
  - 진짜 상수에 붙이기 위한 용도. 기본 타입과 String 에 붙일 수 있음.



**자바에서 companion object 접근**

- 이름이 없는 경우 
  - `Person.Companion.newBaby(...)`
    - 이름을 명시하지 않는다면 `Companion` 이란 이름이 생략된 것임.
  - `@JvmStatic`을 붙이면 `Person.newBaby(...)` 처럼 바로 접근 가능.
- 이름이 있다면
  - `Person.Factory.newBaby(...)`



### 싱글톤

`object Singleton`



``` kotlin
object SingletonObject {
  var a: Int = 0
}

...

println(SingletonObject.a)
```



### 익명 클래스

- 특정 인터페이스나 클래스를 상속받은 구현체를 일회성으로 사용할 때 쓰는 클래스.



**inteface**

``` java
public interface Movable {
  void move();
  void fly();
}
```



**코틀린에서 사용**

``` kotlin
moveSomethiing(object : Movable {
  override fun move() {
    println(...)
  }
  
  override fun fly() {
    println(...)
  }
})
```

- `Java: new 타입 이름()`
- `Kotlin: object : 타입 이름`



## 중첩 클래스

### 중첩 클래스

**static 을 사용하는 중첩 클래스**

클래스 안에 static 을 붙인 클래스. 밖의 클래스를 직접 참조 불가



**static을 사용하지 않는 중첩 클래스**

- 내부 클래스: 밖의 클래스 직접 참조 가능
  - 내부 클래스는 숨겨진 외부 클래스 정보를 갖고 있어, 참조를 해지하지 못하는 경우 메모리 누수가 생길 수 있고, 이를 디버깅하기 어렵다.
  - 내부 클래스의 직렬화 형태가 명확하게 정의되니 않아 직렬화에 있어 제한이 있다.
- 지역 클래스
- 익명 클래스

  

### 코틀린의 중첩 클래스와 내부 클래스

``` kotlin
class JavaHouse(
	private val address: String,
  private val livingRoom: LivingRoom,
) {
  // static
  class LivingRoom(
  	private val area: Double,
  )
  
  // non static
  inner class LivingRoom(
  	private val area: Double
  ) {
    val address: String
    get() = this@House.address	// 바깥 클래스 참조
  }
  
}
```

- 코틀린은 기본적으로 바깥 클래스를 참조하지 않는다.
- 바깥 클래스를 참조하고 싶으면 `inner` 키워드 추가



## 다양한 클래스를 다루는 방법

### Data Class

**Java**

``` java
public class PersonDto {
  private final String name;
  private final int age;
  
  public PersonDto(String name, int age) {
    this.name = name;
    this.age = age;
  }
}
```

- 계층간의 데이터를 전달하기 위한 Data Transfer Objecgt
- 데이터(필드), 생성자, getter, equals, hashCode, toString 등..



**Kotlin**

``` kotlin
data class PersonDto(
  val name: String,
  val age: Int
)
```

- `data` 키워드를 붙이면 자동으로 equals, hashCode, toString 만들어줌.

- Named argument까지 활용하면 builder pattern 과 같은 효과까지!



### Enum Class

**Java**

``` java
public enum Country {
  KOREA("KO"),
  AMERIA("US");
  
  private final String code;
  
  Country(String code) {
    this.code = code;
  }
  
  public String getCode() {
    return code;
  }
}
```

- 추가적인 클래스를 상속받을 수 없음.
- 인터페이스는 구현할 수 있으며, 각 코드가 싱글톤.



**Kotlin**

``` kotlin
enum class Country(
	private val code: String,
) {
  KOREA("KO"),
  AMERICA("US")
  ;
}
```



**when 은 enum class 혹은 sealed class 와 함께 사용할 경우, 진가를 발휘한당**

``` java
if (country == Country.KOREA) {
  ...
}
if (country == Country.AMERICA) {
  ...
}
```

- 코드가 많아지면 if else 로직 처리에 대한 애매함..



**when 사용**

``` kotlin
when (country) {
  Country.KOREA -> 
  Country.AMERICA -> 
}
```



### Sealed Class, Sealed Interface

sealed: 봉인을 한

- 상속이 가능하도록 추상 클래스를 만들까 하는데, 외부에서는 이 클래스를 상속받지 않길 원하면 ? -> 하위 클래스를 **봉인**하자!

- 컴파일 타임에 하위 클래스의 타입을 모두 기억한다. 즉, 런타임 시점에 클래스 타입이 추가될 수 없다.
- Enum 과 다른점
  - 클래스를 상속받을 수 있음
  - 하위 클래스는 멀티 인스턴스가 가능



``` kotlin
sealed class HyundaiCar(
	val name: String,
  val price: Long
)

class Avante : HyundaiCar("아반떼", 1_000L)
class Sonata : HyundaiCar("소나타", 2_000L)
class Grandeur : HyundaiCar("그랜저", 3_000L)
```

- 컴파일 타임에 하위 클래스의 타입을 기억하기 때문에, when 구문을 함께 쓰면 매우 강력!
- 강사 경험) 추상화가 필요한 Entity or DTO 에 sealed class 활용