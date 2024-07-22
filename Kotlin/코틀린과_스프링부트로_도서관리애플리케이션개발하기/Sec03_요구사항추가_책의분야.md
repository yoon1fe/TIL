## 책의 분야 추가하기

- Type, Status 등을 서버에서 관리하는 방법
- Test Fixture의 필요성.구성하는 방법
- Kotlin에서 Enum + JPA + Spring Boot 활용 방안



**요구사항**

- 책 등록할 때 '분야' 선택
  - 컴퓨터/경제/사회/언어/과학



```kotlin
package com.group.libraryapp.domain.book

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Book(
    val name: String,
    val type: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {

    init {
        if (name.isBlank()) {
            throw IllegalArgumentException("이름은 비어 있을 수 없습니다.")
        }
    }

    companion object {
        fun fixture(
            name: String = "책 이름",
            type: String = "COMPUTER",
            id: Long? = null,
        ): Book {
            return Book(name, type, id)
        }
    }
}
```

- `val type: String` 추가
- Book 생성자는 테스트 코드에서도 사용하고 있기 때문에 테스트 코드 역시 수정이 필요함. 근데 사용하는 부분을 보면 어떤 타입인지가 테스트의 성공/실패에 영향이 없음. 따라서 Book 도메인 클래스에 객체를 만드는 함수를 미리 만들어 둠으로써 이를 해결할 수 있음.
- companion object 는 코틀린 컨벤션 상 클래스의 가장 마지막 부분에 위치한다.
- 테스트에 이용할 객체를 만드는 함수를 Object Mother 이라고 부르고, 이렇게 생겨난 테스트용 객체를 Test Fixture 라고 부른다.

- 테스트 코드의 BookRequest 객체를 생성하는 부분때문에 fixture 함수를 마찬가지로 만들어도 되지만, DTO 의 경우 Entity 보다 적게 사용되기 때문에 필요한 경우에만 만드는 것이 좋을듯.



## Enum class 활용해 책의 분야 리팩토링

**위 구조의 문제점**

- 클라이언트에서 들어오는 type 필드에 무엇이 들어올 지 모른다.
- 코드만 보았을 때 Book 테이블의 type 필드에 어떤 값들이 있는지 알 수 없다.
- type과 관련한 새로운 로직을 작성해야 할 때 분기 로직이 들어가게 된다.



이러한 단점들을 Enum Class 로 해결할 수 있다.



```kotlin
package com.group.libraryapp.domain.book

enum class BookType {
    COMPUTER,
    ECONOMY,
    SOCIETY,
    LANGUAGE,
    SCIENCE,
    ;
}
```



```kotlin
package com.group.libraryapp.domain.book

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Book(
    val name: String,
    val type: BookType,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {

    init {
        if (name.isBlank()) {
            throw IllegalArgumentException("이름은 비어 있을 수 없습니다.")
        }
    }

    companion object {
        fun fixture(
            name: String = "책 이름",
            type: BookType = BookType.COMPUTER,
            id: Long? = null,
        ): Book {
            return Book(name, type, id)
        }
    }
}
```



**추가로 개선할 부분**

- Book 테이블의 type 필드에는 숫자가 저장된다. Enum의 순서에 따라 저장되는숫자이고, 0부터 시작한다.
  - JPA의 애너테이션인인 `@Enumerated(EnumType.STRING)`로 해결!



## Boolean에도 Enum 활용

**Boolean 이 별로인 이유~**

- 한 객체가 여러 상태를 표현할 수록 이해하기 어렵다.

​	예) 휴면 / 탈퇴 여부를 각각 boolean 으로 관리..

- Boolean 2개로 표현되는 4가지 상태가 모두 유의미하지 않다.
  - 비즈니스상 존재할 수 없는 조합이 있을 수 있음. 이렇게 '코드'에서만 가능한 케이스는 유지보수를 어렵게 만든다.

- Enum 을 도입하면 해결 가능!
  - 필드 1개로 여러 상태를 표현할 수 있기 때문에 코드 이해가 쉬워지고
  - 정확하게 유의미한 값만 갖고 있을 수 있기 때문에 유지보수가 용이해진다.



```kotlin
@Entity
class UserLoanHistory(
    @ManyToOne
    val user: User,

    val bookName: String,

    var status: UserLoanStatus,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {

    fun doReturn() {
        status = UserLoanStatus.RETURNED
    }

    companion object {
        fun fixture(
            user: User,
            bookName: String = "이상한 나라의 앨리스",
            status: UserLoanStatus = UserLoanStatus.LOADNED,
            id: Long? = null,
        ) : UserLoanHistory {
            return UserLoanHistory(
                user = user,
                bookName = bookName,
                status = status,
            )
        }
    }
}
```

- isReturn 프로퍼티를 status 로 변경했다. Repository 메서드 이름도 수정해주어야 하는데, 이는 런타임에서 확인 가능하다.
  - 이는 Spring Data JPA 혹은 JPQL을 사용하는 것의 단점인데, QueryDSL 을 적용하여 해결 가능!