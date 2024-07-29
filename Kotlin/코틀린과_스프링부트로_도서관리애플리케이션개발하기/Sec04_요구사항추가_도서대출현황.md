1. join 쿼리 종류와 차이점 이해
2. JPA N+1 문제
3. 새로운 API 만들 때 생길 수 있는 고민 포인트 이해 및 감 잡기!



## 유저 대출 현황 보여주기

### 프로덕션 코드

- 클라이언트는 개발되어 있다!
- 새로운 API 를 만들 때 코드의 위치를 어떻게 해야 할까?
  1. 새로운 컨트롤러를 만들어야 할까?
  2. 아니면 기존 어떤 컨트롤러에 추가해야 할까?



**컨트롤러를 구분하는 3가지 기준**

- 화면에서 사용되는 API끼리
  - 화면에서 어떤 API가 사용되는지 한 눈에 알기 쉽다.
  - 한 API가 여러 화면에서 사용되면 위치가 애매해진다.
  - 서버 코드가 화면(presentation)에 종속적이다.
- 동일한 도메인끼리
  - 화면 위치와 무관
  - 비슷한 API까지 모이게 되며 코드 위치 예측 가능
  - 이 API가 어디서 사용되는지 서버 코드만 보고 알기 어렵다.
- API 1:1 Controller
  - 화면 위치와 무관
  - 이 API가 어디서 사용되는지 서버 코드만 보고 알기 어렵다.

- 관건은 프로젝트가 낯선 사람 입장에서 컨트롤러가 어디에 있는지 쉽게 찾을 수 있도록 하는 것!



```kotlin
data class UserLoanHistoryResponse(
    val name: String,
    val books: List<BookHistoryResponse>,
)


data class BookHistoryResponse(
    val name: String,
    val isReturn: Boolean,
)
```



```kotlin
@RestController
class UserController(val userService: UserService) {

    @GetMapping("/user/loan")
    fun getUserLoanHistories(): List<UserLoanHistoryResponse> {
        return userService.getUserLoanHistories()
    }
}
```



```kotlin
@Service
class UserService(
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
    private val querydslActivator: EnableSpringDataWebSupport.QuerydslActivator
) {
    fun getUserLoanHistories(): List<UserLoanHistoryResponse> {
        return userRepository.findAll().map { user ->
            UserLoanHistoryResponse(
                name = user.name,
                books = user.userLoanHistories.map { userLoanHistory ->
                    BookHistoryResponse(
                        name = userLoanHistory.bookName,
                        isReturn = userLoanHistory.status == UserLoanStatus.RETURNED
                    )
                }
            )
        }
    }
}
```



### 테스트 코드

**검증해야 하는 부분**

1. 사용자가 한 번도 책을 빌리지 않은 경우 API 응답에 잘 포함되어 있어야 한다.
2. 사용자가 책을 빌리고 아직 반납하지 않은 경우 isReturn 값이 false로 잘 들어있어야 한다.
3. 사용자가 책을 빌리고 반납한 경우 isReturn 값이 true로 잘 들어 있어야 한다
4.  사용자가 책 여러권을 빌렸는데, 반납을 한 책도 있고 하지 않은 책도 있는 경우 중첩된 리스트에 여러권이 정상적으로 들어가 있어야 한다.
   - 2, 3번 검증 가능



**큰 테스트 2개보다 작은 테스트 1개가 더 좋은 이유**

- 복잡한 테스트 1개보다, 간단한 테스트 2개가 유지보수하기 용이하다.
- 위에서 테스트가 실패한다면 밑에 있는 단언문은 아예 실행조차 되지 않는다.



## N+1 문제

```kotlin
@Transactional(readOnly = true)
fun getUserLoanHistories(): List<UserLoanHistoryResponse> {
    return userRepository.findAll().map { user ->
        UserLoanHistoryResponse(
            name = user.name,
            books = user.userLoanHistories.map { history ->
                BookHistoryResponse(
                    name = history.bookName,
                    isReturn = history.status == UserLoanStatus.RETURNED
                )
            }
        )
    }
}
```

- 최초에 모든 유저를 가져오고(`userRepository.findAll()`, 쿼리 1회)
- Loop을 통해 유저별로 히스토리를 가져온다. (쿼리 N회)

**-> N+1문제!!**



**JPA 1:N 연관관계의 동작 원리**

- 최초 유저 로딩시 가짜 `List<UserLoanHistory>`가 들어감
  - 시작부터 모든 데이터를 들고 오는 것은 비효율적일 수 있음. 정말 필요할 때 들고오자: Lazy Fetching

- 이를 해결하려면 SQL join 에 대한 이해가 필요하다.



## SQL join

- 쿼리 하나로 두 개 이상의 테이블의 결과를 한 번에 보자!



## N+1 문제 해결 - fetch join

```kotlin
interface UserRepository : JpaRepository<User, Long> {

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userLoanHistories")
    fun findAllWithHistories(): List<User>
}
```



## 좀 더 깔끔한 코드로 변경

```kotlin
@Transactional(readOnly = true)
fun getUserLoanHistories(): List<UserLoanHistoryResponse> {
    return userRepository.findAllWithHistories()
        .map(UserLoanHistoryResponse::of)
}
```

```kotlin
data class UserLoanHistoryResponse(
    val name: String,
    val books: List<BookHistoryResponse>,
) {
    companion object {
        fun of(user: User): UserLoanHistoryResponse {
            return UserLoanHistoryResponse(
                name = user.name,
                books = user.userLoanHistories.map(BookHistoryResponse::of)
            )
        }
    }
}


data class BookHistoryResponse(
    val name: String,
    val isReturn: Boolean,
) {
    companion object {
        fun of(history: UserLoanHistory): BookHistoryResponse {
            return BookHistoryResponse(
                name = history.bookName,
                isReturn = history.isReturn
            )
        }
    }
}
```



```kotlin
data class UserLoanHistoryResponse(
    val name: String,
    val books: List<BookHistoryResponse>,
) {
    companion object {
        fun of(user: User): UserLoanHistoryResponse {
            return UserLoanHistoryResponse(
                name = user.name,
                books = user.userLoanHistories.map(BookHistoryResponse::of)
            )
        }
    }
}


data class BookHistoryResponse(
    val name: String,
    val isReturn: Boolean,
) {
    companion object {
        fun of(history: UserLoanHistory): BookHistoryResponse {
            return BookHistoryResponse(
                name = history.bookName,
                isReturn = history.isReturn
            )
        }
    }
}
```