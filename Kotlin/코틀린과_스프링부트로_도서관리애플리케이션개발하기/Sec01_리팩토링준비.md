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





## 책 관련 기능 테스트