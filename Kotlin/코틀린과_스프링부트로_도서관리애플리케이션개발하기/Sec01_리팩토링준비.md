## 테스트 코드란?

- 프로그래밍 코드를 이용해(사람의 손을 거치지 않고) 코드를 검증!



## 코틀린 코드 작성 준비

**코틀린 코드를 작성하기 위해서는**

- build.gradle 에 플러그인 및 dependency 필요

  ```groovy
  plugins {
      ...
      id 'org.jetbrains.kotlin.jvm' version '1.6.21'
  }
  
  ...
  
  dependencies {
    	...
      implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk8'
  }
  
  compileKotlin {
      kotlinOptions {
          jvmTarget = '11'
      }
  }
  
  compileTestKotlin {
      kotlinOptions {
          jvmTarget = '11'
      }
  }
  ```



## 계산기 테스트 코드 작성

**계산기 요구사항**

- 정수만을 취급
- 계산기가 생성될 때 숫자 한 개 받음
- 최초 숫자가 기록된 이후에는 연산자 함수를 통해 숫자를 받아 지속적으로 계산



```kotlin
package com.group.libraryapp.calculator

data class Calculator(
    var number: Int
) {

    fun add(operand: Int) {
        this.number += operand
    }

    fun minus(operand: Int) {
        this.number -= operand
    }

    fun multiply(operand: Int) {
        this.number *= operand
    }

    fun divide(operand: Int) {
        if (operand == 0) {
            throw IllegalArgumentException("Division by zero")
        }
        this.number /= operand
    }
}
```



**Test**

```kotlin
package com.group.libraryapp.calculator

fun main() {
    val calculatorTest = CalculatorTest()
    calculatorTest.addTest()
    calculatorTest.minusTest()
    calculatorTest.multiplyTest()
    calculatorTest.divideTest()
    calculatorTest.divideExceptionTest()
}

class CalculatorTest {

    fun addTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.add(3)

        // then
        if (calculator.number != 8) {
            throw IllegalArgumentException()
        }
    }

    fun minusTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.minus(3)

        // then
        if (calculator.number != 2) {
            throw IllegalArgumentException()
        }
    }

    fun multiplyTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.multiply(3)

        // then
        if (calculator.number != 15) {
            throw IllegalArgumentException()
        }
    }

    fun divideTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.divide(2)

        // then
        if (calculator.number != 2) {
            throw IllegalArgumentException()
        }
    }

    fun divideExceptionTest() {
        // given
        val calculator = Calculator(5)

        // when
        try {
            calculator.divide(0)
        } catch (e: IllegalArgumentException) {
            // then
            return
        } catch (e: Exception) {
            throw IllegalArgumentException()
        }

        throw IllegalArgumentException("기대하는 예외가 발생하지 않음")
    }
}
```



**수동으로 만든 테스트 코드의 단점**

- 테스트 클래스와 메서드가 생길 때마다 메인 메서드에 수동으로 코드를 작성해주어야 하고, 메인 메서드가 아주 커진다. 테스트 메서드를 개별적으로 실행하기도 어려움.
- 테스트가 실패한 경우 무엇을 기대했고, 어떤 잘못된 값이 들어와 실패했는지 확인 어려움. Try-catch 문 등 직접 구현해야 하는 부분이 많음.
- 테스트 메서드별로 공통적으로 처리해야 하는 부분이 있다면 메서드마다 중복 발생
- 이러한 단점을 극복하기 위해 **JUnit5** 쓰자!



## JUnit5 & 테스트 코드 리팩토링

**애너테이션**

- `@Test`: 테스트 메서드 지정.
- `@BeforeEach`: 각 테스트 메서드가 수행되기 전에 실행되는 메서드 지정
- `@AfterEach`: 각 테스트 메서드가 수행된 후에 실행되는 메서드 지정
- `@BeforeAll`: 모든 테스트 수행하기 전에 최초 1회 수행되는 메서드 지정
- `@AfterAll`: 모든 테스트 수행된 후에 최초 1호 ㅣ수행되는 메서드 지정



```kotlin
package com.group.libraryapp.calculator

import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class JunitCalculatorTest {

    @Test
    fun addTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.add(3)

        // then
        assertThat(calculator.number).isEqualTo(8)
    }

    @Test
    fun minusTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.minus(3)

        // then
        assertThat(calculator.number).isEqualTo(2)
    }

    @Test
    fun multiplyTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.multiply(3)

        // then
        assertThat(calculator.number).isEqualTo(15)
    }

    @Test
    fun divideTest() {
        // given
        val calculator = Calculator(5)

        // when
        calculator.divide(2)

        // then
        assertThat(calculator.number).isEqualTo(2)
    }

    @Test
    fun divideExceptionTest() {
        // given
        val calculator = Calculator(5)

        // when
        val message = assertThrows<IllegalArgumentException> {
            calculator.divide(0)
        }.message

        // then
        assertThat(message).contains("Division by zero")

        // when & then
        assertThrows<IllegalArgumentException> {
            calculator.divide(0)
        }.apply {
            assertThat(message).isEqualTo("Division by zero")
        }
    }
}
```



## JUnit5 으로 Spring Boot 테스트

**Spring Boot 의 Layered Architecture**

스프링 빈

- Controller
- Service
- Repository

POJO

- Domain



따라서 각 계층은 테스트 하는 방법이 다르다.

- Domain: 클래스 테스트
- Service, Repository: 스프링 빈을 사용하여 테스트 (@SpringBootTest). 데이터 위주의 검증
- Controller: 스프링 빈을 사용하여 테스트(@SpringBootTest). 응답받은 JSON을 비롯한 HTTP 위주의 검증



```kotlin
package com.group.libraryapp.service.user

import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.dto.user.request.UserCreateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val userService: UserService,
) {

    @Test
    fun saveUserTest() {
        // given
        val request = UserCreateRequest("홍길동", null)

        // when
        userService.saveUser(request)

        // then
        val results = userRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("홍길동")
        assertThat(results[0].age).isEqualTo(null)	// User.name 에 @Nullable 애너테이션 추가 필요
    }
}
```



## 유저 관련 기능 테스트

```kotlin
package com.group.libraryapp.service.user

@SpringBootTest
class UserServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val userService: UserService,
) {

    @Test
    fun saveUserTest() {
        // given
        val request = UserCreateRequest("홍길동", null)

        // when
        userService.saveUser(request)

        // then
        val results = userRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("홍길동")
        assertThat(results[0].age).isEqualTo(null)
    }

    @Test
    fun getUsersTest() {
        // given
        userRepository.saveAll(listOf(
            User("A", 20),
            User("B", null),
        ))

        // when
        val result = userService.getUsers()

        // then
        assertThat(result).hasSize(2)
        assertThat(result).extracting("name").containsExactlyInAnyOrder("A", "B")
        assertThat(result).extracting("age").containsExactlyInAnyOrder(20, null)
    }
  
    @Test
    fun updateUserNameTest() {
        // given
        val saved = userRepository.save(User("A", null))
        val request = UserUpdateRequest(saved.id, "B")

        // when
        userService.updateUserName(request)

        // then
        val result = userRepository.findAll()[0]
        assertThat(result.name).isEqualTo("B")
    }
}
```

- 생성 테스트와 조회 테스트를 같이 돌리면 실패한다.

  - 두 테스트는 Spring Context 를 공유하기 때문!

- 테스트가 끝나면 공유 자원인 DB를 깨끗하게 지워주자.

  - `@AfterEach` 애너테이션 사용

    ``` kotlin
        @AfterEach
        fun clean() {
            userRepository.deleteAll()
        }
    ```

- `@DisplayName` 으로 테스트명 명시 가능



## 책 관련 기능 테스트

``` kotlin
package com.group.libraryapp.service.book

@SpringBootTest
class BookServiceTest @Autowired constructor(
    private val bookService: BookService,
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
) {

    @AfterEach
    fun clean() {
        bookRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("책 등록이 정상 동작한다.")
    fun saveBookTest() {
        // given
        val request = BookRequest("이상한 앨리스")

        // when
        bookService.saveBook(request)

        // then
        val books = bookRepository.findAll()
        assertThat(books).hasSize(1)
        assertThat(books[0].name).isEqualTo("이상한 앨리스")
    }

    @Test
    @DisplayName("책 대출이 정상 동작한다")
    fun loanBookTest() {
        // given
        bookRepository.save(Book("이상한 나라의 엘리스"))
        val savedUser = userRepository.save(User("최태현", null))
        val request = BookLoanRequest("최태현", "이상한 나라의 엘리스")

        // when
        bookService.loanBook(request)

        // then
        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].bookName).isEqualTo("이상한 나라의 엘리스")
        assertThat(results[0].user.id).isEqualTo(savedUser.id)
        assertThat(results[0].isReturn).isFalse
    }

    @Test
    @DisplayName("책이 진작 대출되어 있다면, 신규 대출이 실패한다")
    fun loanBookFailTest() {
        // given
        bookRepository.save(Book("이상한 나라의 엘리스"))
        val savedUser = userRepository.save(User("최태현", null))
        userLoanHistoryRepository.save(
            UserLoanHistory(
                savedUser, "이상한 나라의 엘리스", false
            )
        )
        val request = BookLoanRequest("최태현", "이상한 나라의 엘리스")

        // when & then
        assertThrows<IllegalArgumentException> {
            bookService.loanBook(request)
        }.apply {
            assertThat(message).isEqualTo("진작 대출되어 있는 책입니다")
        }
    }

    @Test
    @DisplayName("책 반납이 정상 동작한다")
    fun returnBookTest() {
        // given
        bookRepository.save(Book("이상한 나라의 엘리스"))
        val savedUser = userRepository.save(User("최태현", null))
        userLoanHistoryRepository.save(UserLoanHistory(savedUser, "이상한 나라의 엘리스", false))
        val request = BookReturnRequest("최태현", "이상한 나라의 엘리스")

        // when
        bookService.returnBook(request)

        // then
        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].isReturn).isTrue
    }
}
```

- `@SpringBootTest`: 스프링 애플리케이션 테스트위한 Context
- `@Autowired constructor()`: 생성자 앞에 `@Autowired` 사용해 생성자 Bean 주입받도록
- `@AfterEach`: 각 테스트(`@Test`)가 끝나면 실행되는 함수 지정
- `@DisplayName`: 테스트가 실행될 때 표시될 이름 지정

- 대출 정상 처리(happy case)와 대출 실패 케이스 모두 검증 필요!



## 정리

1. Kotlin을 사용하기 위해 필요한 설정
2. 테스트란 무엇이고, 왜 중요한가
3. JUnit5의 기초 사용법
4. JUnit5와 Spring Boot를 함께 사용해 테스트를 작성하는 방법
5. 여러 API에 대한 Service 계층 테스트 실습