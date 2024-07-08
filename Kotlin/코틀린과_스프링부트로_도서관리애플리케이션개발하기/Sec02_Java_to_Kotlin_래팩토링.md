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



## Kotlin과 JPA 함께 사용할 때 이야깃거리 3가지





## Repository 변경





## 서비스 계층 변경 - UserService.java





## BookService.java 변경 + Optional 제거





## DTO 변경





## Controller 변경