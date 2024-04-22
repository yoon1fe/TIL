## 코틀린에서 변수를 다루는 방법

### 변수 선언 키워드 - var, val

**Java**

``` java
long number1 = 10L;
final long number2 = 10L;

Long number3 = 1_000L;
Person person = new Person("홍길동");
```



**Kotlin**

``` kotlin
var number1 = 10L	// 가변 변수
var number1: Long = 10L	// 타입 지정
val number2 = 10L	// 불변 변수
```

- 컴파일러가 타입을 알아서 추론해주기 때문에 타입을 명시할 필요 없다. 물론 명시적으로 작성해줄 수 도 있음.

- 초기값을 지정해주지 않은 경우 컴파일러가 타입 추론을 못하기 때문에 타입을 명시해야함.

  - `var number1: Long`

  - `val number2: Long`
    - val 은 불변이지만, 아직 초기화되지 않은 변수에 한해서 값을 넣어줄 수 있음

- val 컬렉션에는 element 를 추가할 수 있다.
- tip) 코드를 클린하게 가꾸기 위해서는 모든 변수는 우선 val 로 만들고, 꼭 필요한 경우 var 로 변경하자.



### Primitive Type

**Java**

``` java
long number1 = 10L;
Long number3 = 1_000L;
```



**Kotlin**

``` kotlin
var number1 = 10L
var number3 = 1_000L
```

- 코틀린에서는 reference type 으로 명시되어있더라도 연산 등을 처리할 때 내부적으로 알아서 적절히 처리해준다.
- 프로그래머가 boxing / unboxing 고려하지 않도록 알아서 코틀린이 처리해줌.



### nullable 변수

- 코틀린에서는 기본적으로 모든 변수에 null 이 들어갈 수 없도록 설계되었음.
- null 이 들어갈 수 있다는 걸 표현하는건 `?`
- nulluable 변수는 아예 다른 타입으로 간주된다.

``` kotlin
var number3: Long? = 1_000L
number3 = null		// OK
```



**객체 인스턴스화**

- 코틀린에서는 객체 인스턴스를 생성할 때 `new` 키워드 사용 안함.

``` kotlin
val person = Person("홍길동")
```



## 코틀린에서 Null 을 다루는 방법

### 코틀린에서의 null 체크

다음 자바 코드는 안전한 코드일까?

``` java
public boolean startsWithA(String str) {
  return str.startsWith("A");
}
```

- str 에 null 이 들어오면 NPE 발생!



``` java
public boolean startsWithA(String str) {
  if (str == null) {
    // throw new ~~..
    // return null;
    // return false;
  }
  return str.startsWith("A");
}
```



**Kotlin**

``` kotlin
fun startsWithA1(str: String?): Boolean {
  if (str == null) {
    throw IllegalArgumentException("null이 들어옴")
  }
  return str.startsWith("A")
}

fun startsWithA2(str: String?): Boolean? {
  if (str == null) return null
  return str.startsWith("A")
}

fun startsWithA3(str: String?): Boolean {
//   str.startsWith("A")	// 컴파일 오류!!
  if (str == null) {
    return false
  }
  return str.startsWith("A")
}
```

- 코틀린은 위에서 null 여부 체크하면 밑에서는 not null 이라는걸 추론.



### Safe Call 과 Elvis 연산자

코틀린에서는 null 이 가능한 타입을 완전히 다르게 취급한다!

null 이 가능한 타입만을 위한 기능은 없을까?

-> safe call, Elvis 연산자



**Safe Call**

- `?.`  : null 이 아닌 경우에만 뒤 메서드를 호출해줘~

  - ex) 

    ``` kotlin
    val str: String? = "ABC"
    str.length	// 불가능
    str?.length	// 가능
    ```

- null이 아니면 실행하고, null 이면 실행하지 않는다.



**Elvis 연산자**

- `?:` `str?.length ?: 0`
- 앞의 연산 결과가 null 이면 뒤의 값을 사용
- early return 에도 사용된다!



**위의 코틀린 코드를 좀 더 코틀린 스럽게**

``` kotlin
fun startsWithA1(str: String?): Boolean {
  return str?.startsWith("A") ?: throw IllegalArgumentException("null이 들어옴")
}

fun startsWithA2(str: String?): Boolean? {
  return str?.startsWith("A")
}

fun startsWithA3(str: String?): Boolean {
  return str?.startsWith("A") ?: false
}
```



### null 아님 단언

- `!!`: 이 변수는 절대 Null 이 아니야!!







``` kotlin
fun startsWithA3(str: String?): Boolean {
  return str!!.startsWith("A") ?: false
}
```

- 혹시나 Null 이 들어오면 NPE가 발생하기 때문에 정말 null 이 아닌게 확실한 경우에만 널 아님 단언 `!!` 을 사용해야 함



### 플랫폼 타입

코틀린에서 자바 코드를 가져다 사용할 때 어떻게 처리될까 ?

코틀린은 `@Nullable`, `@NotNull` 과 같은 애너테이션을 인식하고 이해한다.



**플랫폼 타입**

위의 애너테이션이 없다면 코틀린에서는 이 값이 nullable 한지, non-nullable한지 알 수 없다. 이러한 타입을 플랫폼 타입이라고 함. 플랫폼 타입은 런타임에 예외가 발생할 수 있음.



## 코틀린에서 Type을 다루는 방법

### 기본 타입

Byte, Short, Int, Long, Float, Double 등.. 

- 코틀린은 선언된 기본값을 보고 타입을 추론한다.

  ``` kotlin
  val number1 = 3		// Int
  val number2 = 3L	// Long
  
  val number3 = 3.0f// Float
  val number4 = 3.0	// Double
  ```

- 코틀린에서 기본 타입간의 변환은 **명시적으로** 이루어져야 한다.(자바: 암시적. int -> long)

  - 반드시 **`to변환타입()`** 을 써서 변환해야 함.

- 변수가 nullable 이라면 적절한 처리가 필요함



### 타입 캐스팅

기본 타입이 아닌 일반 타입은 캐스팅을 어떻게 하지?



**자바**

``` java
public static void printAgeIfPerson(Object obj) {
  if (obj instanceof Person) {
    Person person = (Person) obj;
    System.out.println(person.getAge());
  }
}
```



**코틀린**

``` kotlin
fun printAgeIfPerson(obj: Any) {
  if (obj is Person) {
    val person = obj as Person	// as Person 생략 가능. 스마트 캐스트!
    println(person.age)
  }
}
```

- `is` == `instanceof`
- `!is` 도 있음.



obj로 null 이 올 수 있다면??

`val person = obj as? Person`

- `as?`: safe call 처럼 obj 가 null 이라면 person = null



### 코틀린의 3가지 특이한 타입

**Any**

- 자바의 Object 역할. 모든 객체의 최상위 타입
- 모든 primitive type 의 최상위 타입도 Any
- Any 자체로는 null 을 포함할 수 없어서 null을 포함하고 싶다면 Any? 로 표현.
- Any 에 equals / hashCode / toString 존재



**Unit**

- 자바의 void 와 동일한 역할
- void 와 다르게 Unit 은 그 자체로 타입 인자로 사용 가능
- 함수형 프로그래밍에서 Unit 은 단 하나의 인스턴스만 갖는 타입을 의미. 즉, 코틀린의 Unit 은 실제 존재하는 타입이라는 것을 표현.



**Nothing**

- 함수가 정상적으로 끝나지 않았다는 사실을 표현
- 무조건 예외를 반환하는 함수, 무한 루프 함수 등

``` kotlin
fun fail(message: String): Nothing {
  throw IllegalArgumentException()
}
```



### String Interpolation, String indexing

- `${변수}`
- `val log = "사람의 이름은 ${person.name} 이고, 나이는 ${person.age} 세 입니다."`
- `$변수` 를 사용할 수도 있다.

- 코틀린 공식 코딩 컨벤션에서는

  > Don't use curly braces when inserting a simple variable into a string template. Use curly braces only for longer expressions

  라고 한다.

- 여러 줄에 걸친 문자열을 작성해야 할 때, 큰 따옴표 세개 쓰면 편하다.

  ``` kotlin
  """
  	으아아
  	으아아아
  	으아아아!
  """.trimIndent()
  ```

- 코틀린에서는 자바의 배열처럼 대괄호를 통해 문자열의 특정 문자를 가져올 수 있음.

  `str[0]`



## 코틀린에서 연산자를 다루는 방법

### 단항 연산자 / 산술 연산자

- 단항 연산자 `++`, `--`
- 산술 연산자 `+`, `-`, `/`, `%` 등..
- 산술대입 연산자 `+=`, `*=` 등등...
- 완전 동일!



### 비교 연산자와 동등성, 동일성

**비교 연산자**

- `>`, `<`, `>=`, `<=` 동일!
- 단, 자바와는 다르게 객체를 비교할 때 비교 연산자를 호출하면 자동으로 `compareTo` 를 호출해준다!



**동등성, 동일성**

- 동등성: 두 객체의 값이 같은가? `equals()`
- 동일성: 가리키는 주소가 같은가? `==`

- 코틀린에서는 동일성에 `===`, 동등성에 `==` 사용! `==`를 사용하면 코틀린이 알아서 `equals()` 를 호출해준다.



### 논리 연산자 / 코틀린에 있는 특이한 연산자

**논리 연산자**

- 자바와 동일!
- 자바처럼 lazy 연산



**특이한 연산자**

- `in` / `!in`: 컬렉션이나 범위에 포함되어 있/지 않다.
- `a..b`: a 부터 b 까지의 범위 객체를 생성
- `a[i]`
- `a[i] = b`



### 연산자 오버로딩

- 코틀린에서는 객체마다 연산자를 직접 정의할 수 있다.