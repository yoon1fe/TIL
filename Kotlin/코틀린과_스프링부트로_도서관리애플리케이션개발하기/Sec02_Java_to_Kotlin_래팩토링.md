## 리팩토링 계획 세우기

1. Java 로 작성된 도서관리 애플리케이션을 Kotlin 으로 완전히 리팩토링
2. Kotlin + JPA 코드 작성
3. Kotlin + Spring



**리팩토링 순서**

- Domain -> Repository -> Service -> Controller/DTO



## 도메인 계층 변경

### Book.java

``` kotlin
@Entity
class Book(
    val name: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    
    init {
        if (name.isBlank()) {
            throw IllegalArgumentException("이름은 비어 있을 수 없습니다.")
        }
    }
}
```

- default parameter가 있는 필드는 아래에 두는 것이 관례.

- JPA 사용하기 위해서는 아무 argument 를 받지 않는 기본 생성자가 필요한데, 코틀린에서는 '주 생성자'를 만들 때 프로퍼티를 함께 만들어주는 방식을 사용함으로써, 아무런 argument를 받지 않는 기본 생성자가 존재하지 않는다.

  - 해당 에러를 해결하려면 아래 플러그인 추가하면 된다.

  `plugins { id "org.jetbrains.kotlin.plugin.jpa" version "1.6.21" }`

- `Book newBook = new Book(request.getName(), null);` 

  - 자바 코드에선 코틀린의 디폴트 파라미터를 인식하지 못한다. 따라서 뒤에 null 붙여주어야 함.

- 코틀린 클래스에 대한 리플렉션을 할 수 없어서 기존 테스트 다 깨짐. 아래 의존성 추가

  ```groovy
  implementation 'org.jetbrains.kotlin:kotlin-reflect:1.6.21'
  ```



### UserLoanHistory.java

```kotlin
@Entity
class UserLoanHistory(
    @ManyToOne
    val user: User,

    val bookName: String,

    var isReturn: Boolean,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {

    fun doReturn() {
        isReturn = true
    }

}
```



### User.java

```kotlin
package com.group.libraryapp.domain.user

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import javax.persistence.*

@Entity
class User(
    var name: String,

    val age: Int?,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val userLoanHistories: MutableList<UserLoanHistory> = mutableListOf(),


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {

    init {
        if (name.isBlank()) {
            throw IllegalArgumentException("이름은 비어 있을 수 없습니다")
        }
    }

    fun updateName(name: String) {
        this.name = name
    }

    fun loanBook(book: Book) {
        this.userLoanHistories.add(UserLoanHistory(this, book.name, false))
    }

    fun returnBook(bookName: String) {
        this.userLoanHistories.first { history -> history.bookName == bookName }.doReturn()
    }
}
```

- `@OneToMany()`: 자바에서는 `CascadeType[] cascade()` 필드에 `cascade = Cascade.ALL` 을 바로 넣어줘도 됐지만, 코틀린에서는 배열 타입 애너테이션 필드에는 정확히 배열을 넣어 주어야 함



## Kotlin과 JPA 함께 사용할 때 이야깃거리 3가지

**setter**

User.kt

- setter 대신 좋은 이름의 함수를 사용하는 것이 훨씬 clean 하다!

- 하지만 name에 대한 setter는 public 이기 때문에 setter를 사용할 "수도" 있음.

- public getter 는 필요하기 때문에 setter만 private 하게 만드는 것이 최선!

  1. backing property

     ``` kotlin
     class Use(
       private var _name: String
     ) {
       val name: String
       	get() = this._name
     }
     ```

     - `_name` 이라는 프로퍼티를 만들고, 읽기 전용으로 추가 프로퍼티 `name`을 생성

  2. custom setter

     ``` kotlin
     class User(
       name: String	// 프로퍼티가 아닌 생성자 인자로만 name을 받는다
     ) {
       var name = name
       	private set
     }
     ```

  - 근데 두 방법 모두 프로퍼티가 많아지면 번거롭다~ 

    강사 개인적으로) setter 를 열어두지만 사용하지 않는 방법 선호



**생성자 안의 프로퍼티, 클래스 body 안의 프로퍼티**

- 꼭 primary constructor 안에 모든 프로퍼티를 넣어야 할까?
- 추천 방법
  1. 모든 프로퍼티를 생성자에 넣거나,
  2. 프로퍼티를 생성자 혹은 클래스 body 안에 구분해서 넣을 때 명확한 기준이 있거나



**JPA와 data class**

- Entity는 data class 로 만들지 말자!
- `equals()`, `hashCode()`, `toString()` 모두 JPA Entity와는 100% 어울리지 않기 때문!



**작은 팁**

- Entity 가 생성되는 로직을 찾고 싶다면 `constructor` 지시어를 명시적으로 작성하고 추적하자!



## Repository 변경

**BookRepository**

``` kotlin
package com.group.libraryapp.domain.book

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface BookRepository : JpaRepository<Book, Long> {

    fun findByName(bookName: String): Optional<Book>
}
```



**UserRepository**

``` kotlin
package com.group.libraryapp.domain.user

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, Long> {

    fun findByName(name: String): Optional<User>
}
```



**UserLoanHistoryRepository**

``` kotlin
package com.group.libraryapp.domain.user.loanhistory

import org.springframework.data.jpa.repository.JpaRepository

interface UserLoanHistoryRepository : JpaRepository<UserLoanHistory, Long> {

    fun findByBookNameAndIsReturn(bookName: String, isReturn: Boolean): UserLoanHistory?
}
```



## 서비스 계층 변경 - UserService.java

```kotlin
@Service
class UserService(
    private val userRepository: UserRepository
) {
    @Transactional
    fun saveUser(request: UserCreateRequest) {
        val newUser = User(request.name, request.age)

        userRepository.save(newUser)
    }

    @Transactional(readOnly = true)
    fun getUsers(): List<UserResponse> {
//        return userRepository.findAll().map { user ->
//            UserResponse(user)
//        }
//        return userRepository.findAll()
//            .map { UserResponse(it)
        return userRepository.findAll()
            .map(::UserResponse)
    }

    @Transactional
    fun updateUserName(request: UserUpdateRequest) {
        val user = userRepository.findById(request.id).orElseThrow(::IllegalArgumentException)
        user.updateName(request.name)
    }

    @Transactional
    fun deleteUser(name: String) {
        val user = userRepository.findByName(name).orElseThrow(::IllegalArgumentException)
        userRepository.delete(user)
    }
}
```

- `@Transactional` 애너테이션이 정상 동작하라면 해당 메서드를 상속받을 수 있어야 함

- 하지만 코틀린에서는 기본적으로 final class, final method.

- 아래 플러그인 사용하면 필요한 클래스에 대해 상속과 오버라이드를 자동 허용해줌.

  `id 'org.jetbrains.kotlin.plugin.spring' version '1.6.21'`

- 생성자 레퍼런스를 사용하려면 `UserResponse::new` == `::UserResponse`





## BookService.java 변경 + Optional 제거

```kotlin
@Service
class BookService(
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
) {

    @Transactional
    fun saveBook(request: BookRequest) {
        val book = Book(request.name)
        bookRepository.save(book)
    }

    @Transactional
    fun loanBook(request: BookLoanRequest) {
        val book = bookRepository.findByName(request.bookName) ?: throw IllegalArgumentException()
        if (userLoanHistoryRepository.findByBookNameAndIsReturn(request.bookName, false) != null) {
            throw IllegalArgumentException("진작 대출되어 있는 책입니다")
        }

        val user = userRepository.findByName(request.userName) ?: throw IllegalArgumentException()
        user.loanBook(book)
    }

    @Transactional
    fun returnBook(request: BookReturnRequest) {
        val user = userRepository.findByName(request.userName) ?: throw IllegalArgumentException()

        user.returnBook(request.bookName)
    }
}
```

- `Optional<Book>` 대신 `Book?` 사용
- `orElseThrow()` 대신 엘비스 연산자 `?:`사용



**`?: throw IllegalArgumentException()` 중복 제거**

ExceptionUtil.kt

``` kotlin
package com.group.libraryapp.util

fun fail(): Nothing {
    throw IllegalArgumentException()
}
```

- 사용: `val book = bookRepository.findByName(request.bookName) ?: fail()`



**CrudRepository가 반환하는 Optional**

- 코틀린의 확장 함수 사용. 스프링에선 `CrudRepositoryExtensionKt`을 만들어두었다.

- `val user = userRepository.findByIdOrNull(request.id) ?: fail()`



**확장함수 더 써보기**

```kotlin
package com.group.libraryapp.util

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull

fun fail(): Nothing {
    throw IllegalArgumentException()
}

fun <T, ID> CrudRepository<T, ID>.findByIdOrThrow(id: ID): T {
    return this.findByIdOrNull(id) ?: fail()
}
```



```kotlin
@Transactional
fun updateUserName(request: UserUpdateRequest) {
    val user = userRepository.findByIdOrThrow(request.id)
    user.updateName(request.name)
}
```



## DTO 변경

**UserResponse**

```kotlin
package com.group.libraryapp.dto.user.response

import com.group.libraryapp.domain.user.User

data class UserResponse(
    val id: Long,
    val name: String,
    val age: Int?,
) {

//    constructor(user: User) : this(
//        id = user.id!!,
//        name = user.name,
//        age = user.age,
//    )

    companion object {
        fun of(user: User): UserResponse {
            return UserResponse(
                id = user.id!!,
                name = user.name,
                age = user.age,
            )
        }
    }
}
```

- int -> Int 로 변경되는 경우 `Int?` 을 붙여 nullable 하게 바꿔주자.

- 부생성자보다 정적 팩토리 메서드를 사용하면 더 좋다.



## Controller 변경

**BookController**

```kotlin
@RestController
class BookController(
    private val bookService: BookService,
) {

    @PostMapping("/book")
    fun saveBook(@RequestBody request: BookRequest) {
        bookService.saveBook(request)
    }

    @PostMapping("/book/loan")
    fun loanBook(@RequestBody request: BookLoanRequest) {
        bookService.loanBook(request)
    }

    @PutMapping("/book/return")
    fun returnBook(@RequestBody request: BookReturnRequest) {
        bookService.returnBook(request)
    }
}
```



**UserController**

``` kotlin
@RestController
class UserController(val userService: UserService) {

    @PostMapping("/user")
    fun saveUser(@RequestBody request: UserCreateRequest) {
        userService.saveUser(request)
    }

//    @GetMapping("/user")
//    fun getUsers(): List<UserResponse> {
//        return userService.getUsers()
//    }

    fun getUsers(): List<UserResponse> = userService.getUsers()

    @PutMapping("/user")
    fun updateUserName(@RequestBody request: UserUpdateRequest) {
        userService.updateUserName(request)
    }

    @DeleteMapping("/user")
    fun deleteUser(@RequestParam name: String) {
        userService.deleteUser(name)
    }
}
```

- `@RequestParam`의 required 옵션은 true다. 근데 만약 `String?` 으로 되어있다면 스프링이 알아서 required 옵션을 false 로 바꿔준다.



**LibraryAppApplication**

```kotlin
package com.group.libraryapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LibraryAppApplication

fun main(args: Array<String>) {
    runApplication<LibraryAppApplication>(*args)
}
```