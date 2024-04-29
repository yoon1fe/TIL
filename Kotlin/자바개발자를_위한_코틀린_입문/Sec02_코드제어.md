## 제어문

### if

**Java**

``` java
private void validateScoreIsNotNegative(int score) {
  if (score < 0) {
    throw new IllegalArgumentException(String.format("%s 는 0보다 작을 수 없음.", score));
  }
}
```



**Kotlin**

단순 변환

``` kotlin
fun validateScoreIsNotNegative(score: Int) {
  if (score < 0) {
    throw IllegalArgumentException("${score} 는 0보다 작을 수 없음.");
  }
}
```

- void 생략
- fun: 함수
- 예외 던질 때 new 키워드 사용 X



**else 있는 경우**

**Java**

``` java
private String getPassOrFail(int score) {
  if (score >= 50) {
    return "P";
  } else {
    return "F";
  }
}
```



**Kotlin**

``` kotlin
fun getPassOrFail(score: Int): String {
  if (score >= 50) {
    return "P" 
  } else {
    return "F"
  }
}
```



자바와 코틀린의 문법 상 차이는 크게 없지만, 한 가지 다른 점이 있다.

자바에서는 if-else 는 **Statement** 이지만, 코틀린에서는 **Expression**!



### Expression, Statement

- Statement: 프로그램의 문장. 하나의 값으로 도출되지 않는다.
- Expression: 하나의 값으로 도출되는 문장.



``` java
int score = 30 + 40;	// Statement && Expression 
String grade = if (score >= 50) {
    return "P";
  } else {
    return "F";
  }		// Statement, !Expression
```

- 즉, 코틀린에서는 위처럼 사용 가능!



``` kotlin
fun getPassOrFail(score: Int): String {
  return if (score >= 50) {
    "P" 
  } else {
    "F"
  }
}
```



코틀린에서는 if-else 가 expression 이기 때문에 3항 연산자가 없다!!

if - else if - else 문도 동일함.



**간단 팁**

- 어떠한 값이 특정 범위에 포함되어 있는지 여부

  - Java: `if (0 <= score && score <= 100) {}`

  - Kotlin: `if (score in 0..100) {}`



### switch, when

**Java**

``` java
private String getGradeWithSwitch(int score) {
  switch (score / 10) {
    case 9:
      return "A";
    case 8:
      return "B";
    case 7:
      return "C";
    default:
      return "D";
  }
}
```



**Kotlin**

- 코틀린에서는 switch 문이 사라졌다. 대신 사용할 수 있는 when 구문이 있음.
- when 도 expression.

``` kotlin
fun getGradeWithSwitch(score: Int): String {
  return when (score / 10) {
    9 -> "A"
    8 -> "B"
    7 -> "C"
    else -> "D"
  }
}
```



==

``` kotlin
fun getGradeWithSwitch(score: Int): String {
  return when (score) {
    in 90..99 -> "A"
    in 80..89 -> "B"
    in 70..79 -> "C"
    else -> "D"
  }
}
```



**사용사례**

- 조건부에 Expression 은 뭐든 들어올 수 있음. ex) `is`

**Java**

``` java
private boolean startsWithA(Object obj) {
  if (obj instanceof String) {
    return ((String) obj).startsWith("A");
  } else {
    return false;
  }
}
```



**Kotlin**

``` kotlin
fun startsWithA(obj: Any): Boolean {
  return when (obj) {
    is String -> obj.startsWith("A")
    else -> false
  }
}
```



- 여러 개의 조건을 동시에 검사 가능 (`,`로 구분)

``` java
private void judgeNumber(int number) {
  if (number == 1 || number == 0 || number == 1) {
    ...
  }
}
```



``` kotlin
fun judgeNumber(number: Int) {
  when (number) {
    1, 0, -1 -> println("어디서 많이 본 숫자")
    else -> println("~~")
  }
}
```



- when(값) 의 값이 없을 수도 있다. early return 처럼 동작. `if` 문으로 early return 하는 것처럼..

``` kotlin
fun judgeNumber2(number: Int) {
  when {
    number == 0 -> println(...)
    number % 2 == 0 -> println(...)
    else -> println(...)
  }
}
```



- when 은 Enum class, 혹은 Sealed Class 와 함께 사용할 경우 더욱더 진가를 발휘한다.



## 반복문

코틀린의 반복문은 자바의 반복문과 매우 유사.



### for-each

**Java**

``` java
List<Long> numbers = Arrays.asList(1L, 2L, 3L);
for (long number : numbers) {
  System.out.println(number);
}
```



**Kotlin**

``` kotlin
val numbers = listOf(1L, 2L, 3L)
for (number in numbers) {
  println(number)
}
```

- 콜론 대신 in. Iterable 이 구현된 타입이라면 모두 들어갈 수 있음



### 전통적인 for 문

**Java**

``` java
for (int i = 1; i <= 3; i++) {
  System.out.println(i);
}
```



**Kotlin**

``` kotlin
for (i in 1..3) {
  println(i)
}
```

- `i..k`: i부터 k까지. 모두 포함.

- 숫자가 내려가는 경우는?
  - **Java**: `for (int i = 3; i >= 1; i--)`
  - **Kotlin**: `for (i in 3 downTo 1)`

- 2칸씩 올라가는 경우는?
  - **Java**: `for (int i = 1; i <= 5; i+=2)`
  - **Kotlin**: `for (i in 1..5 step 2)`



### Progression, Range

**동작 원리**

- `..` 연산자: 범위를 만들어내는 연산자
- `1..3`: 1부터 3의 범위

- 실제 `Range` 라는 클래스가 있고, `Range` 클래스는 `Progression`(등차수열) 클래스를 상속받는다.
- `1..3`: 의 의미는 1에서 시작하고 3으로 끝나는 공차 1의 등차수열을 만들어줘 라는 뜻!
- `3 downTo 1`: 시작값 3, 끝값 1, 공차 -1인 등차수열
- `1..5 step 2`: 시작값 1, 끝값 5, 공차 2인 등차수열
- `step`, `downTo` 도 함수이다. (중위 호출 함수) 
  - `변수.함수이름(argument)` 대신 `변수 함수이름 argument`



### while

- 자바와 완전 동일!



## 예외

### try-catch-finally 구문



**주어진 문자열을 정수로 변경**

**Java**

``` java
private int parseIntOrThrow(@NotNull String str) {
  try {
    return Integer.parseInt(str);
  } catch (NumberFormatException e) {
    throw new IllegalArgumentException(String.format("주어진 %s는 숫자가 아닙니다.", str));
  }
}
```



**Kotlin**

``` kotlin
fun parseIntOrThrow(str: String): Int {
  try {
    return str.toInt()
  } catch (e: NumberFormatException) {
    throw IllegalArgumentException("주어진 ${str}는 숫자가 아닙니다.")
  }
}
```

- 문법 자체는 동일



**실패하면 null 을 리턴**

**Java**

``` java
private int parseIntOrThrow(@NotNull String str) {
  try {
    return Integer.parseInt(str);
  } catch (NumberFormatException e) {
    return null;
  }
}
```



**Kotlin**

``` kotlin
fun parseIntOrThrow(str: String): Int {
  return try {
    str.toInt()
  } catch(e: NumberFormatException) {
    null
  }
}
```

- try-catch 구문도 expression 으로 간주된다!

- try-catch-finally 도 동일!



### checked exception, unchecked exception

**Java**

``` java
public void readFile() throws IOException {
  File currentFile = new File(".");
  File file = new File(currentFile.getAbsolutePath() + "/a.txt");
  BufferedReader reader = new BufferedReader(new FileReader(file));
  System.out.println(reader.readLine());
  reader.close();
}
```

- `IOException` 은 checked 예외



**Kotlin**

``` kotlin
fun readFile() {
  val currentFile = File(".")
  val file = File(currentFile.absolutePath + "/a.txt")
  val reader = BufferedReader(FileReader(file))
  println(reader.readLine())
  reader.close()
}
```

- throws 를 명시하지 않아도 컴파일 오류가 발생하지 않는다
- 코틀린에서는 checked 예외와 unchecked 예외를 구분하지 않고, 모두 **Unchecked Exception** 임!



### try with resources

**Java**

``` java
public void readFile(String path) throws IOException {
  try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
    System.out.println(reader.readLine());
  }
}
```



**Kotlin**

- 코틀린에는 try with resources 구문이 없음
- 대신 use 라는 inline 확장 함수를 써야 함

``` kotlin
fun readFile(path: String) {
  BufferedReader(FileReader(path)).use {
    reader ->
    println(reader.readLine())
  }
}
```



## 함수

### 함수 선언 문법

**Java**

``` java
public int max(int a, int b) {
  if (a > b) {
    return a;
  }
  return b;
}
```



**Kotlin**

``` kotlin
fun max(a: Int, b: Int): Int {
  // 1
  if (a > b) {
    return a
  }
  return b
  
  // 2
  return if (a > b) {
    a
  } else {
    b
  }
}

// 3
fun max(a: Int, b: Int): Int = 
	if (a > b) {
    a
  } else {
    b
  }
```

- public 은 생략 가능.
- if else 구문은 expression 이므로 바로 리턴 가능.
- 함수가 하나의 결과값이라면 {} block 대신 `=` 사용 가능

- 다음 코드도 동일한 코드

  `fun max(a: Int, b: Int) = if (a > b) a else b`

  - `=` 을 사용하는 경우 반환 타입 생략 가능



### default parameter

**Java**

``` java
public void repeat(String str, int num, boolean useNewLine) {
  for (int i = 1; i <= num; i++) {
    if (useNewLine) {
      System.out.println(str);
    } else {
      System.out.print(str);
    }
  }
}

// 많은 코드에서 useNewLine 으로 true 를 넘겨준다면?
public void repeat(String str, int num) {
  repeat(str, num, true);	// 오버로딩
}

// 많은 코드에서 num 으로 3을 넘겨준다면?
public void repeat(String str) {
  repeat(str, 3, true);
}
```

- 메서드를 이렇게 많이 만들어야 하나 ?



**Kotlin**

- 디폴트 파라미터라는 걸 제공한다.

``` Kotlin
fun repeat(
  str: String, 
  num: Int = 3, 
  useNewLine: Boolean = true
) {
  for (i in 1..num) {
    if (useNewLine) {
      println(str)
    } else {
      print(str)
    }
  }
}
```



### named argument (parameter)

위의 repeat 함수를 호출할 때, num은 3을 그대로 쓰고 useNewLine은 false 를 쓰고 싶다면?



``` kotlin
repeat("Hello World", useNewLine = false)
```

- Builder 를 만들지 않고 Builder 의 장점을 가질 수 있음.

- 코틀린에서 자바 함수를 가져다 사용할 때는 named argument 를 사용할 수 **없음**



### 가변 인자

**Java**

``` java
public static void printAll(String... strings) {
  for (String str : strings) {
    System.out.println(str);
  }
}
```



**Kotlin**

``` kotlin
fun main() {
  printAll("A", "B", "C")
  
  // 배열을 넣을 땐 스프레드 연산자(*) 를 붙여주어야 한다
  val array = arrayOf("A", "B", "C")
  printlnAll(*array)
}

fun printAll(vararg strings: String) {
  for (str in strings) {
    println(str) 
  }
}
```